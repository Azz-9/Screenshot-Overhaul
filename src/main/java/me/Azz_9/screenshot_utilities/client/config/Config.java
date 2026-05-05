package me.Azz_9.screenshot_utilities.client.config;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.ScreenshotGalleryWidget;

public class Config {
	private static final @NonNull Config INSTANCE = new Config();
	
	public final @NonNull ConfigObject<Boolean> freezeInPhotoMode = new ConfigObject<>(true, "");
	public final @NonNull ConfigObject<Boolean> showPlayer = new ConfigObject<>(true, "");
	public final @NonNull ConfigObject<Boolean> showNametags = new ConfigObject<>(true, "");

	public final @NonNull ConfigObject<Path> screenshotsDir = new ConfigObject<>(MINECRAFT.gameDirectory.toPath().resolve("screenshots"), "");
	public final @NonNull ConfigObject<Boolean> showChatMessage = new ConfigObject<>(true, "");
	public final @NonNull SavableObject<ScreenshotGalleryWidget.SortMode> sortOrder = new SavableObject<>(ScreenshotGalleryWidget.SortMode.DATE_DESC);

	public static @NonNull Config getInstance() {
		return INSTANCE;
	}

	public @NonNull Path getScreenshotsDir() {
		if (!Files.exists(screenshotsDir.getValue())) {
			try {
				Files.createDirectories(screenshotsDir.getValue());
			} catch (IOException e) {
				ScreenshotLogger.error("Could not create screenshotsDir: {}, error: {}", MINECRAFT.gameDirectory.toPath().relativize(screenshotsDir.getValue()), e);
			}
		}

		return screenshotsDir.getValue();
	}
}
