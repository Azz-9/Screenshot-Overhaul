package me.Azz_9.screenshot_utilities.client.screenshot;

import com.mojang.blaze3d.platform.NativeImage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class ImageScaler {

	private ImageScaler() {
	}

	/**
	 * Downscales an image so that its largest side is less than or equal to {@code maxSize},
	 * using a box filter (average of source pixels) for each destination pixel.
	 * <p>
	 * Each output pixel is computed as the average ARGB value of all source pixels
	 * covered by its corresponding area in the original image.
	 * </p>
	 * <p>
	 * This method always returns a new {@link NativeImage}. The source image is
	 * closed by this method and must not be used afterward.
	 * </p>
	 *
	 * @param src     the source image to downscale (will be closed by this method)
	 * @param maxSize the maximum allowed size for the largest side of the image
	 * @return a new downscaled {@link NativeImage}
	 */
	public static @NonNull NativeImage downscale(@NonNull NativeImage src, int maxSize) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();

		if (srcW <= maxSize && srcH <= maxSize) {
			NativeImage copy = new NativeImage(srcW, srcH, true);
			copy.copyFrom(src);
			src.close();
			return copy;
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

				// Zone source couverte par ce pixel destination
				int srcX0 = (int) Math.floor(x / scale);
				int srcY0 = (int) Math.floor(y / scale);
				int srcX1 = (int) Math.ceil((x + 1) / scale);
				int srcY1 = (int) Math.ceil((y + 1) / scale);

				srcX0 = Math.max(0, srcX0);
				srcY0 = Math.max(0, srcY0);
				srcX1 = Math.min(srcW, srcX1);
				srcY1 = Math.min(srcH, srcY1);

				long a = 0, r = 0, g = 0, b = 0;
				int count = 0;

				for (int sy = srcY0; sy < srcY1; sy++) {
					for (int sx = srcX0; sx < srcX1; sx++) {
						int argb = src.getPixel(sx, sy);

						a += (argb >> 24) & 0xFF;
						r += (argb >> 16) & 0xFF;
						g += (argb >> 8) & 0xFF;
						b += argb & 0xFF;
						count++;
					}
				}

				if (count == 0) {
					dst.setPixel(x, y, 0);
				} else {
					int avgA = (int) (a / count);
					int avgR = (int) (r / count);
					int avgG = (int) (g / count);
					int avgB = (int) (b / count);

					dst.setPixel(x, y, (avgA << 24) | (avgR << 16) | (avgG << 8) | avgB);
				}
			}
		}

		src.close();
		return dst;
	}
}
