package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import me.Azz_9.screenshot_utilities.compat.CompatManager;
import me.Azz_9.screenshot_utilities.compat.IrisCompat;

public final class ScreenshotFileNameParser {

	// Tous les tokens supportés
	private static final Map<String, Supplier<String>> TOKENS;

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
				return IrisCompat.getShaderName();
			else return "";
		});
		TOKENS = Collections.unmodifiableMap(map);
	}

	private static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\\\:*?\"<>|\u0000-\u001F]");

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
	public static Path resolve(Path screenshotsFolder, String pattern) {

		String resolved = resolveTokens(pattern);

		// normalisation slash
		String normalized = resolved.replace('\\', '/');

		// suppression des segments vides
		String[] rawSegments = normalized.split("/");

		List<String> segments = new ArrayList<>();
		for (String s : rawSegments) {
			String cleaned = sanitize(s);
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

	private static String resolveTokens(String pattern) {
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

	private static String sanitize(String segment) {
		String s = ILLEGAL_CHARS.matcher(segment.trim()).replaceAll("_");

		if (s.equals(".") || s.equals("..")) return "_";

		if (isReservedName(s)) {
			return "_" + s;
		}

		return s;
	}

	private static Path findUniqueFile(Path folder, String stem) {
		Path candidate = folder.resolve(stem + ".png");
		if (!Files.exists(candidate)) return candidate;

		int count = 2;
		while (true) {
			candidate = folder.resolve(stem + "_" + count + ".png");
			if (!Files.exists(candidate)) return candidate;
			count++;
		}
	}

	private static Path findNumericFile(Path folder) {
		int count = 1;

		while (true) {
			Path candidate = folder.resolve(count + ".png");
			if (!Files.exists(candidate)) return candidate;
			count++;
		}
	}

	public static boolean validate(String pattern) {
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
			if (segment.equals(".") || segment.equals("..") || isReservedName(segment) || segment.isEmpty()) {
				return false;
			}
		}

		// profondeur
		return pattern.split("/").length <= 8;
	}

	/**
	 * Retire les <token> du string pour analyser la partie statique uniquement.
	 */
	private static String stripTokens(String s) {
		return s.replaceAll("<[^>]*>", "");
	}

	/**
	 * Noms réservés sur Windows (CON, PRN, AUX, NUL, COM1-9, LPT1-9).
	 */
	private static boolean isReservedName(String seg) {
		String upper = seg.toUpperCase(Locale.ROOT);
		return upper.matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]");
	}
}