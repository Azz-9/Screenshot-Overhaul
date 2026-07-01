package me.Azz_9.screenshot_overhaul.client.screenshot;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.utils.PathUtils.ILLEGAL_CHARS;
import static me.Azz_9.screenshot_overhaul.utils.PathUtils.MAX_FILE_NAME_LENGTH;

import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import me.Azz_9.screenshot_overhaul.platform.Services;
import me.Azz_9.screenshot_overhaul.utils.PathUtils;
import me.Azz_9.screenshot_overhaul.compat.CompatManager;

public final class ScreenshotFileNameParser {

	// Tous les tokens supportés
	private static final @NonNull Map<String, Supplier<String>> TOKENS;

	private static final int MAX_FILE_STEM_LENGTH = MAX_FILE_NAME_LENGTH - 4;

	static {
		Map<String, Supplier<String>> map = new LinkedHashMap<>();
		map.put("datetime", Util::getFilenameFormattedDateTime);

		Supplier<LocalDateTime> now = LocalDateTime::now;
		map.put("year", () -> String.format("%04d", now.get().getYear()));
		map.put("month", () -> String.format("%02d", now.get().getMonthValue()));
		map.put("day", () -> String.format("%02d", now.get().getDayOfMonth()));
		map.put("hour", () -> String.format("%02d", now.get().getHour()));
		map.put("minute", () -> String.format("%02d", now.get().getMinute()));
		map.put("second", () -> String.format("%02d", now.get().getSecond()));

		map.put("worldname", () -> {
			if (MINECRAFT.getSingleplayerServer() != null)
				return MINECRAFT.getSingleplayerServer().getWorldData().getLevelName();
			else return "";
		});
		map.put("serverip", () -> {
			if (MINECRAFT.getCurrentServer() != null)
				return MINECRAFT.getCurrentServer().ip;
			else return "";
		});
		map.put("version", () -> SharedConstants.getCurrentVersion().id());
		map.put("shader", () -> {
			if (CompatManager.irisPresent())
				return Services.PLATFORM.getShaderName();
			else return "";
		});
		TOKENS = Collections.unmodifiableMap(map);
	}

	private ScreenshotFileNameParser() {
	}

	/**
	 * Résout le pattern de config et retourne un Path unique sous {@code screenshotsFolder}.
	 * Le pattern peut contenir des "/" pour des sous-dossiers, et des tokens <...>.
	 *
	 * @param screenshotsFolder dossier racine des screenshots (ex. .minecraft/screenshots)
	 * @param pattern           valeur de la ConfigOption (ex. "<year>/<month>/<datetime>")
	 * @return Path absolu vers le fichier .png, garanti inexistant
	 */
	public static @NonNull Path resolve(@NonNull Path screenshotsFolder, @NonNull String pattern) {

		String resolved = resolveTokens(pattern);

		// normalisation slash
		String normalized = resolved.replace('\\', '/');

		// suppression des segments vides
		String[] rawSegments = normalized.split("/", -1);

		List<String> segments = new ArrayList<>();
		for (int i = 0; i < rawSegments.length; i++) {
			String cleaned = i == rawSegments.length - 1
					? sanitizeFileStem(rawSegments[i])
					: sanitizeDirectoryName(rawSegments[i]);
			if (!cleaned.isEmpty()) {
				segments.add(cleaned);
			}
		}

		Path folder = screenshotsFolder;
		if (segments.isEmpty()) {
			return findNumericFile(folder);
		} else {
			for (int i = 0; i < segments.size() - 1; i++) {
				folder = folder.resolve(segments.get(i));
			}
			return findUniqueFile(folder, segments.getLast());
		}
	}

	// -------------------------------------------------------------------------

	private static @NonNull String resolveTokens(@NonNull String pattern) {
		StringBuilder sb = new StringBuilder(pattern.length());

		int i = 0;
		while (i < pattern.length()) {
			int open = pattern.indexOf('<', i);

			if (open == -1) {
				sb.append(pattern, i, pattern.length());
				break;
			}

			sb.append(pattern, i, open);

			int close = pattern.indexOf('>', open + 1);

			if (close == -1) {
				sb.append(pattern, open, pattern.length());
				break;
			}

			String token = pattern.substring(open + 1, close);
			Supplier<String> supplier = TOKENS.get(token);

			if (supplier != null) {
				sb.append(supplier.get());
			} else {
				sb.append('<').append(token).append('>');
			}

			i = close + 1;
		}
		return sb.toString();
	}

	private static @NonNull String sanitizeDirectoryName(@NonNull String segment) {
		String s = sanitize(segment);

		if (s.length() > MAX_FILE_NAME_LENGTH) {
			s = s.substring(0, MAX_FILE_NAME_LENGTH);
		}

		return s;
	}

	private static @NonNull String sanitizeFileStem(@NonNull String stem) {
		String s = sanitize(stem);

		if (s.length() > MAX_FILE_STEM_LENGTH) {
			s = s.substring(0, MAX_FILE_STEM_LENGTH);
		}

		return s;
	}

	private static @NonNull String sanitize(@NonNull String segment) {
		String s = ILLEGAL_CHARS.matcher(segment.trim()).replaceAll("_");

		if (s.equals(".") || s.equals("..")) return "_";

		if (PathUtils.isReservedName(s)) {
			return "_" + s;
		}

		return s;
	}

	private static @NonNull Path findUniqueFile(@NonNull Path folder, @NonNull String stem) {
		Path candidate = folder.resolve(stem + ".png");
		if (!Files.exists(candidate)) return candidate;

		int count = 2;
		while (true) {
			String strCount = String.valueOf(count);

			int maxLength = MAX_FILE_STEM_LENGTH - (strCount.length() + 1);

			String truncatedStem = stem.length() > maxLength
					? stem.substring(0, maxLength)
					: stem;

			candidate = folder.resolve(truncatedStem + "_" + strCount + ".png");

			if (!Files.exists(candidate)) return candidate;
			count++;
		}
	}

	private static @NonNull Path findNumericFile(@NonNull Path folder) {
		int count = 1;

		while (true) {
			Path candidate = folder.resolve(count + ".png");
			if (!Files.exists(candidate)) return candidate;
			count++;
		}
	}

	public static boolean validate(@Nullable String pattern) {
		if (pattern == null || pattern.isBlank()) {
			return false;
		}

		// check tokens
		int i = 0;
		while (i < pattern.length()) {
			int open = pattern.indexOf('<', i);
			if (open == -1) break;

			int close = pattern.indexOf('>', open + 1);
			if (close == -1) {
				return false;
			}
			if (close == open + 1) {
				return false;
			}

			String token = pattern.substring(open + 1, close);
			if (!TOKENS.containsKey(token)) {
				return false;
			}

			i = close + 1;
		}

		// orphan >
		if (stripTokens(pattern).contains(">")) {
			return false;
		}

		// illegal chars uniquement sur pattern brut hors tokens
		String staticPart = stripTokens(pattern);
		if (ILLEGAL_CHARS.matcher(staticPart).find()) {
			return false;
		}

		String[] segments = pattern.split("/", -1);

		for (String segment : segments) {
			if (segment.equals(".") || segment.equals("..") || PathUtils.isReservedName(segment) || segment.isEmpty()) {
				return false;
			}
		}

		// profondeur
		return pattern.split("/").length <= 8;
	}

	/**
	 * Retire les <token> du string pour analyser la partie statique uniquement.
	 */
	private static @NonNull String stripTokens(@NonNull String s) {
		return s.replaceAll("<[^>]*>", "");
	}
}