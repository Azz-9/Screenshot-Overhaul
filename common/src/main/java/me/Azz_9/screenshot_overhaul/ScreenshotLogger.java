package me.Azz_9.screenshot_overhaul;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenshotLogger {
	private static final @NonNull Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void info(@NonNull String message, Object... args) {
		LOGGER.info("[Screenshot overhaul] " + message, args);
	}

	public static void warn(@NonNull String message, Object... args) {
		LOGGER.warn("[Screenshot overhaul] " + message, args);
	}

	public static void error(@NonNull String message, Object... args) {
		LOGGER.error("[Screenshot overhaul] " + message, args);
	}

	public static void debug(@NonNull String message, Object... args) {
		LOGGER.debug("[Screenshot overhaul] " + message, args);
	}
}