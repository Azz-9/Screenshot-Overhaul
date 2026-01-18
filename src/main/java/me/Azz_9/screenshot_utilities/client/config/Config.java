package me.Azz_9.screenshot_utilities.client.config;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class Config {
	@NonNull
	private static final Config INSTANCE = new Config();

	@NonNull
	public final ConfigObject<Path> screenshotsDir = new ConfigObject<>(CLIENT.runDirectory.toPath().resolve("screenshots"), "");
	@NonNull
	public final ConfigObject<Boolean> showChatMessage = new ConfigObject<>(true, "");

	public static @NonNull Config getInstance() {
		return INSTANCE;
	}

	public @NonNull Path getScreenshotsDir() {
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
