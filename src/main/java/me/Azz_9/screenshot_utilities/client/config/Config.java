package me.Azz_9.screenshot_utilities.client.config;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class Config {
	private static Config INSTANCE;

	public final ConfigObject<Path> screenshotsDir = new ConfigObject<>(CLIENT.runDirectory.toPath().resolve("screenshots"), "");
	public final ConfigObject<Boolean> showChatMessage = new ConfigObject<>(true, "");

	public static Config getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Config();
		}
		return INSTANCE;
	}

	public Path getScreenshotsDir() {
		if (screenshotsDir.getValue() == null) {
			ScreenshotLogger.warn("screenshotsDir is null -> reset to default");
			screenshotsDir.resetToDefault();
		}

		if (!Files.exists(screenshotsDir.getValue())) {
			try {
				Files.createDirectories(screenshotsDir.getValue());
			} catch (IOException e) {
				ScreenshotLogger.error("Could not create screenshotsDir: {}, error: {}", screenshotsDir.getValue(), e);
			}
		}

		return screenshotsDir.getValue();
	}
}
