package me.Azz_9.screenshot_utilities.client.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	public static Path screenshotsPath;

	public static Path getScreenshotsDir() {
		if (screenshotsPath == null) {
			screenshotsPath = FabricLoader.getInstance().getGameDir().resolve("screenshots");
		}

		if (!Files.exists(screenshotsPath)) {
			try {
				Files.createDirectories(screenshotsPath);
			} catch (IOException e) {
				throw new RuntimeException("Creating screenshots directory", e);
			}
		}

		return screenshotsPath;
	}
}
