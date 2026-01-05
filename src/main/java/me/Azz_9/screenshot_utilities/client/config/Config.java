package me.Azz_9.screenshot_utilities.client.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	public static ConfigObject<Path> screenshotsPath = new ConfigObject<>(null, "");
	public static ConfigObject<Boolean> showChatMessage = new ConfigObject<>(false, "");

	public static Path getScreenshotsDir() {
		if (screenshotsPath.getValue() == null) {
			screenshotsPath.setValue(FabricLoader.getInstance().getGameDir().resolve("screenshots"));
		}

		if (!Files.exists(screenshotsPath.getValue())) {
			try {
				Files.createDirectories(screenshotsPath.getValue());
			} catch (IOException e) {
				throw new RuntimeException("Creating screenshots directory", e);
			}
		}

		return screenshotsPath.getValue();
	}
}
