package me.Azz_9.screenshot_overhaul.client.screenshot;

import org.jspecify.annotations.NonNull;

import java.io.File;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;

public class DeleteScreenshot {
	public static boolean delete(@NonNull Screenshot screenshot) {
		if (screenshot.file().delete()) {
			ScreenshotManager.remove(screenshot.pathRelativeToScreenshotDir());
			ScreenshotLogger.info(screenshot.pathRelativeToScreenshotDir() + " was deleted.");
			return true;
		}
		return false;
	}

	public static boolean delete(@NonNull File file) {
		return delete(new Screenshot(file, ScreenshotMetadata.empty()));
	}
}
