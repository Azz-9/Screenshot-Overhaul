package me.Azz_9.screenshot_utilities;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

public class ScreenshotLogger {
	private static final @NonNull Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void info(String message, Object... args) {
		LOGGER.info("[Screenshot utilities] " + message, args);
	}

	public static void warn(String message, Object... args) {
		LOGGER.warn("[Screenshot utilities] " + message, args);
	}

	public static void error(String message, Object... args) {
		LOGGER.error("[Screenshot utilities] " + message, args);
	}

	public static void debug(String message, Object... args) {
		LOGGER.debug("[Screenshot utilities] " + message, args);
	}
}
