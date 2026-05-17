package me.Azz_9.screenshot_utilities.client.screenshot;

import java.io.File;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;

public class DeleteScreenshot {
	public static boolean delete(Screenshot screenshot) {
		if (screenshot.file().delete()) {
			ScreenshotManager.remove(screenshot.pathRelativeToScreenshotDir());
			ScreenshotLogger.info(screenshot.pathRelativeToScreenshotDir() + " was deleted.");
			return true;
		}
		return false;
	}

	public static boolean delete(File file) {
		return delete(new Screenshot(file, ScreenshotMetadata.empty()));
	}
}
