package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.screenshot.*;
import xaero.map.gui.GuiMap;

public class XaeroThumbnailRenderer {

	private static final int THUMBNAIL_WIDTH = 64;
	private static final int THUMBNAIL_HEIGHT = 36;

	public static void render(@NonNull final GuiGraphicsExtractor graphics, @NonNull final GuiMap gui,
	                          double cameraX, double cameraZ, double scale, double screenScale) {
		if (!ScreenshotList.isLoaded()) return;

		int screenW = MINECRAFT.getWindow().getWidth();
		int screenH = MINECRAFT.getWindow().getHeight();

		String currentDim = gui.getMapProcessor().getMapWorld().getCurrentDimension().getDimId().identifier().getPath();
		String currentWorld = gui.getMapProcessor().getMapWorld().getMapProcessor().getCurrentWorldId();

		List<Screenshot> screenshots;
		screenshots = ScreenshotList.getScreenshots().stream()
				.filter(s -> Objects.equals(s.metadata().worldName(), currentWorld) && Objects.equals(s.metadata().dimension(), currentDim))
				.toList();

		for (Screenshot screenshot : screenshots) {
			ScreenshotMetadata metadata = screenshot.metadata();

			// Conversion monde → pixel écran (espace physique)
			double screenX = screenW / 2.0 + (metadata.x() - cameraX) * scale;
			double screenY = screenH / 2.0 + (metadata.z() - cameraZ) * scale;

			// Conversion pixel physique → coordonnées GUI
			double guiX = screenX / screenScale;
			double guiY = screenY / screenScale;

			graphics.pose().pushMatrix();
			graphics.pose().translate((float) guiX, (float) guiY);

			ScreenshotDrawHelper.drawContainCenter(
					graphics,
					ScreenshotTextureCache.getSmallThumbnail(screenshot.file().toPath()),
					0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Colors.WHITE);

			graphics.pose().popMatrix();
		}
	}
}
