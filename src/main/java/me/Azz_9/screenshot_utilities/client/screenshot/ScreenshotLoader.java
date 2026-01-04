package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class ScreenshotLoader {

	public static NativeImage loadThumbnail(Path file, int width, int height) throws IOException {
		try (InputStream in = Files.newInputStream(file)) {
			NativeImage image = NativeImage.read(in);
			return resizeCover(image, width, height);
		}
	}

	private static NativeImage resizeCover(NativeImage src, int targetW, int targetH) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();

		float scale = Math.max(
				(float) targetW / srcW,
				(float) targetH / srcH
		);

		int scaledW = Math.round(srcW * scale);
		int scaledH = Math.round(srcH * scale);

		NativeImage scaled = new NativeImage(scaledW, scaledH, true);

		// Resize proportionnel
		for (int y = 0; y < scaledH; y++) {
			for (int x = 0; x < scaledW; x++) {
				int srcX = x * srcW / scaledW;
				int srcY = y * srcH / scaledH;
				scaled.setColor(x, y, src.getColorArgb(srcX, srcY));
			}
		}

		src.close();

		// Crop centré
		int cropX = (scaledW - targetW) / 2;
		int cropY = (scaledH - targetH) / 2;

		NativeImage result = new NativeImage(targetW, targetH, true);

		for (int y = 0; y < targetH; y++) {
			for (int x = 0; x < targetW; x++) {
				result.setColor(
						x,
						y,
						scaled.getColorArgb(x + cropX, y + cropY)
				);
			}
		}

		scaled.close();
		return result;
	}

}