package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class ImageScaler {

	private ImageScaler() {
	}

	/**
	 * Resize an image to make it's bigger side <= maxSize
	 */
	public static @NonNull NativeImage downscale(@NonNull NativeImage src, int maxSize) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();

		if (srcW <= maxSize && srcH <= maxSize) {
			return src;
		}

		float scale = Math.min(
				(float) maxSize / srcW,
				(float) maxSize / srcH
		);

		int dstW = Math.max(1, Math.round(srcW * scale));
		int dstH = Math.max(1, Math.round(srcH * scale));

		NativeImage dst = new NativeImage(dstW, dstH, true);

		for (int y = 0; y < dstH; y++) {
			for (int x = 0; x < dstW; x++) {

				int srcX = Math.min(srcW - 1, (int) (x / scale));
				int srcY = Math.min(srcH - 1, (int) (y / scale));

				int abgr = src.getColorArgb(srcX, srcY);
				dst.setColorArgb(x, y, abgr);
			}
		}

		src.close();
		return dst;
	}
}
