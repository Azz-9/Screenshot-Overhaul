package me.Azz_9.screenshot_utilities.client.screenshot;

import java.io.File;

public class DeleteScreenshot {
	public static boolean delete(Screenshot screenshot) {
		if (screenshot.file().delete()) {
			ScreenshotManager.remove(screenshot.pathRelativeToScreenshotDir());
			return true;
		}
		return false;
	}

	public static boolean delete(File file) {
		return delete(new Screenshot(file, ScreenshotMetadata.empty()));
	}
}
