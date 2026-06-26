package me.Azz_9.screenshot_utilities.utils;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility methods for converting between stored (relative-when-possible) paths
 * and absolute paths, using {@code MINECRAFT.gameDirectory} as the base.
 *
 * <h3>Storage strategy</h3>
 * <ul>
 *   <li>If the selected path shares the same root as {@code gameDirectory}
 *       (same drive on Windows, always true on Unix), it is stored as a
 *       relative path — which may contain {@code ../} segments if the user
 *       picked something outside {@code gameDirectory}.</li>
 *   <li>If the roots differ (e.g. {@code C:\} vs {@code D:\} on Windows),
 *       the path is stored as-is (absolute), since relativizing across roots
 *       is not possible. This case is uncommon and the path is less likely to
 *       contain personally identifiable information.</li>
 * </ul>
 */
public final class PathUtils {

	public static final int MAX_FILE_NAME_LENGTH = 255;

	public static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\\\:*?\"<>|\u0000-\u001F]");

	private PathUtils() {
	}

	/**
	 * Converts an absolute path to the form that should be persisted in config.
	 *
	 * <ul>
	 *   <li>Same root as {@code gameDirectory} → relative path
	 *       (may include {@code ../} segments)</li>
	 *   <li>Different root → absolute path kept as-is</li>
	 * </ul>
	 *
	 * @param absolute the absolute path to convert
	 * @return a relative or absolute path suitable for storage
	 */
	public static @NonNull Path toStoredPath(@NonNull Path absolute) {
		Path gameDir = MINECRAFT.gameDirectory.toPath().toAbsolutePath().normalize();
		Path normalized = absolute.toAbsolutePath().normalize();

		try {
			if (sameRoot(normalized, gameDir)) {
				return gameDir.relativize(normalized);
			}
		} catch (IllegalArgumentException ignored) {
			// Defensive: fall through to returning the absolute path
		}
		return normalized;
	}

	/**
	 * Resolves a stored path to an absolute path.
	 * <ul>
	 *   <li>Relative paths are resolved against {@code gameDirectory}.</li>
	 *   <li>Absolute paths are returned normalized.</li>
	 * </ul>
	 *
	 * @param stored the path as read from config (relative or absolute)
	 * @return the corresponding absolute path
	 */
	public static @NonNull Path toAbsolutePath(@NonNull Path stored) {
		if (stored.isAbsolute()) return stored.normalize();
		return MINECRAFT.gameDirectory.toPath()
				.toAbsolutePath()
				.resolve(stored)
				.normalize();
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	private static boolean sameRoot(@NonNull Path a, @NonNull Path b) {
		Path rootA = a.getRoot();
		Path rootB = b.getRoot();
		if (rootA == null || rootB == null) return rootA == rootB;
		return rootA.equals(rootB);
	}

	/**
	 * Noms réservés sur Windows (CON, PRN, AUX, NUL, COM1-9, LPT1-9).
	 */
	public static boolean isReservedName(@NonNull String fileName) {
		String upper = fileName.toUpperCase(Locale.ROOT);
		return upper.matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]");
	}

	public static boolean isValidString(@NonNull String string) {
		return !ILLEGAL_CHARS.matcher(string).find();
	}

	public static boolean isValidFileName(@NonNull String fileName) {
		fileName = fileName.trim();
		return !fileName.isEmpty() && !isReservedName(fileName) && !fileName.equals("..") && !fileName.equals(".") && !ILLEGAL_CHARS.matcher(fileName).find();
	}
}