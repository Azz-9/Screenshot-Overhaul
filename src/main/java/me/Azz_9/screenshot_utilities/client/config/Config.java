package me.Azz_9.screenshot_utilities.client.config;

import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	private static Config INSTANCE;

	public final ConfigObject<Path> screenshotsPath = new ConfigObject<>(null, "");
	public final ConfigObject<Boolean> showChatMessage = new ConfigObject<>(false, "");

	public static Config getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Config();
		}
		return INSTANCE;
	}

	public Path getScreenshotsDir() {
		if (screenshotsPath.getValue() == null) {
			screenshotsPath.setValue(MinecraftClient.getInstance().runDirectory.toPath().resolve("screenshots"));
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
