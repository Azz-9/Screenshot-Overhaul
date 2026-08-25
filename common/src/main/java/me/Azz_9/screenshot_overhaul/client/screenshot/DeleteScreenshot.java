package me.Azz_9.screenshot_overhaul.client.screenshot;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Files;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.metadata.Metadata;
import me.Azz_9.screenshot_overhaul.client.panorama.Panorama;

public class DeleteScreenshot {
	public static boolean delete(@NonNull Screenshot screenshot) {
		try {
			Files.delete(screenshot.file().toPath());
			ScreenshotManager.remove(screenshot.pathRelativeToScreenshotDir());
			ScreenshotLogger.info("Screenshot " + screenshot.pathRelativeToScreenshotDir() + " was deleted.");
			return true;
		} catch (Exception e) {
			ScreenshotLogger.error("Could not delete screenshot: " + screenshot.pathRelativeToScreenshotDir() + " " + e);
			return false;
		}
	}

	public static boolean delete(@NonNull File file) {
		return delete(new Screenshot(file, Metadata.empty()));
	}

	public static boolean deletePanorama(@NonNull Panorama panorama) {
		try {
			// delete each faces
			for (Screenshot face : panorama.presentFaces()) {
				delete(face);
			}
			// try deleting the folder if it's empty
			Files.delete(panorama.folder().toPath());
			ScreenshotManager.remove(panorama.pathRelativeToScreenshotDir());

			ScreenshotLogger.info("Panorama " + panorama.pathRelativeToScreenshotDir() + " was deleted.");
			return true;
		} catch (Exception e) {
			ScreenshotLogger.error("Could not delete panorama: " + panorama.pathRelativeToScreenshotDir() + " " + e);
			return false;
		}
	}
}
