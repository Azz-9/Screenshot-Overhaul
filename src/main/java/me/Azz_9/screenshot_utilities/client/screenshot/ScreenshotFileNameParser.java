package me.Azz_9.screenshot_utilities.client.screenshot;

import net.minecraft.util.Util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
		// 1. Snapshot du temps — appelé une seule fois pour tout le pattern
		LocalDateTime capturedNow = LocalDateTime.now();
		String resolved = resolveTokens(pattern, capturedNow);

		// 2. Split sur "/" pour extraire sous-dossiers + stem du fichier
		//    On normalise d'abord les séparateurs Windows potentiels
		String normalized = resolved.replace('\\', '/');
		String[] segments = normalized.split("/", -1);

		// 3. Sanitize chaque segment, construire le sous-dossier et le stem
		Path subFolder = screenshotsFolder;
		for (int i = 0; i < segments.length - 1; i++) {
			String seg = sanitize(segments[i]);
			if (!seg.isEmpty()) {
				subFolder = subFolder.resolve(seg);
			}
		}
		String stem = sanitize(segments[segments.length - 1]);
		if (stem.isEmpty()) {
			stem = Util.getFilenameFormattedDateTime(); // fallback si le pattern résout vide
		}

		// 4. Collision avoidance
		return findUniqueFile(subFolder, stem);
	}

	// -------------------------------------------------------------------------

	private static String resolveTokens(String pattern, LocalDateTime now) {
		// On remplace tous les <token> d'un coup avec un seul passage
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
				// '<' non fermé → on l'écrit tel quel
				sb.append(pattern, open, pattern.length());
				break;
			}
			String token = pattern.substring(open + 1, close);
			Supplier<String> supplier = TOKENS.get(token);
			if (supplier != null) {
				sb.append(supplier.get()); // note: now snapshottée pour <year>/<month>/etc.
			} else {
				// Token inconnu → on le laisse (ou on peut logger un warning)
				sb.append('<').append(token).append('>');
			}
			i = close + 1;
		}
		return sb.toString();
	}

	private static String sanitize(String segment) {
		// Trim whitespace + supprime les chars interdits
		String s = ILLEGAL_CHARS.matcher(segment.trim()).replaceAll("_");
		// Évite les segments qui sont juste "." ou ".."
		if (s.equals(".") || s.equals("..")) return "_";
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

	public static boolean validate(String pattern) {
		if (pattern == null || pattern.isBlank()) {
			return false;
		}

		// 1. Vérifier que tous les < sont fermés et que les tokens sont connus
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

		// 2. Vérifier qu'il n'y a pas de > orphelin
		if (pattern.indexOf('>') != -1) {
			String stripped = stripTokens(pattern);
			if (stripped.contains(">")) {
				return false;
			}
		}

		// 3. Vérifier les segments (split sur "/")
		String[] segments = pattern.split("/", -1);
		for (int s = 0; s < segments.length; s++) {
			String seg = stripTokens(segments[s]).trim();
			boolean isLast = s == segments.length - 1;

			// Segment vide uniquement toléré pour le trailing slash (erreur quand même)
			if (seg.isBlank()) {
				if (isLast && segments.length > 1) {
					return false;
				}
				if (!isLast) {
					return false;
				}
			}

			// Chars illégaux dans la partie statique (hors tokens)
			if (ILLEGAL_CHARS.matcher(seg).find()) {
				return false;
			}

			// Segments réservés Windows
			if (isReservedName(seg)) {
				return false;
			}
		}

		// 4. Profondeur raisonnable
		return segments.length <= 8;
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