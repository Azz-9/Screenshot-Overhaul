package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;

import java.util.List;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotMetadata;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;
import xaero.map.gui.GuiMap;

public class XaeroThumbnailRenderer {

	private static final int THUMBNAIL_WIDTH = 64;
	private static final int THUMBNAIL_HEIGHT = 36;

	public static void render(@NonNull final GuiGraphicsExtractor graphics, @NonNull final GuiMap gui,
	                          double cameraX, double cameraZ, double scale, double screenScale) {
		int screenW = MINECRAFT.getWindow().getWidth();
		int screenH = MINECRAFT.getWindow().getHeight();

		ResourceKey<Level> currentDim = gui.getMapProcessor().getMapWorld().getCurrentDimension().getDimId();
		String currentWorld = gui.getMapProcessor().getMapWorld().getMapProcessor().getCurrentWorldId();


		List<Screenshot> screenshots = /* TODO get screenshots */;

		for (Screenshot screenshot : screenshots) {
			ScreenshotMetadata metadata = screenshot.metadata();

			// Conversion monde → pixel écran (espace physique)
			double screenX = screenW / 2.0 + (metadata.x() - cameraX) * scale;
			double screenY = screenH / 2.0 + (metadata.z() - cameraZ) * scale;

			// Conversion pixel physique → coordonnées GUI
			double guiX = screenX / screenScale;
			double guiY = screenY / screenScale;

			ScreenshotDrawHelper.drawFitCentered(
					graphics,
					ScreenshotTextureCache.getSmallThumbnail(screenshot.file().toPath()),
					(int) guiX, (int) guiY, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Colors.WHITE);
		}
	}
}
