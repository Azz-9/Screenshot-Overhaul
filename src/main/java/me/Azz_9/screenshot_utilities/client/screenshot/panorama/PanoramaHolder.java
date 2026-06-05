package me.Azz_9.screenshot_utilities.client.screenshot.panorama;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.nio.file.Files;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;

public class PanoramaHolder {

	private static final int[] order = new int[]{1, 3, 5, 4, 0, 2};

	public static void usePanorama(Panorama panorama) {
		NativeImage[] images = new NativeImage[order.length];
		for (int i = 0; i < order.length; i++) {

			try (InputStream in = Files.newInputStream(panorama.faces()[order[i]].file().toPath())) {
				images[i] = flipVertical(NativeImage.read(in));
			} catch (Exception _) {
				ScreenshotLogger.error("Failed to change panorama");
				return;
			}
		}

		DynamicCubeMapTexture texture = new DynamicCubeMapTexture();
		texture.setImages(images);
		MINECRAFT.getTextureManager().register(Identifier.withDefaultNamespace("textures/gui/title/background/panorama"), texture);
	}

	private static NativeImage flipVertical(NativeImage src) {
		int width = src.getWidth();
		int height = src.getHeight();

		NativeImage flipped = new NativeImage(width, height, false);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				flipped.setPixel(x, height - 1 - y, src.getPixel(x, y));
			}
		}

		return flipped;
	}
}
