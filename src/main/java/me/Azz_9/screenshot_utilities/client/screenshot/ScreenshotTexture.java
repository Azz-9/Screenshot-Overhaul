package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public final class ScreenshotTexture implements AutoCloseable {

	private final @NonNull CompletableFuture<NativeImage> imageFuture;
	private final int maxSize;
	private @Nullable NativeImageBackedTexture texture;

	private ScreenshotTexture(Path file, int maxSize) {
		this.maxSize = maxSize;

		this.imageFuture = CompletableFuture.supplyAsync(() -> {
			try (InputStream in = Files.newInputStream(file)) {
				NativeImage img = NativeImage.read(in);
				return maxSize > 0 ? ImageScaler.downscale(img, maxSize) : img;
			} catch (Exception e) {
				return null;
			}
		}, Util.getMainWorkerExecutor());
	}

	private ScreenshotTexture(Path file) {
		this(file, 0);
	}

	@NonNull
	public static ScreenshotTexture loadThumbnail(Path file) {
		return new ScreenshotTexture(file, 512);
	}

	@NonNull
	public static ScreenshotTexture loadScreenshot(Path file) {
		return new ScreenshotTexture(file, 1920);
	}

	@Nullable
	public NativeImageBackedTexture getTexture() {
		if (texture != null) {
			return texture;
		}

		NativeImage nativeImage = imageFuture.join();
		if (imageFuture.isDone() && nativeImage != null) {
			texture = new NativeImageBackedTexture(() -> "screenshot_texture", nativeImage);
			return texture;
		}

		return null;
	}

	public boolean isReady() {
		return imageFuture.isDone();
	}

	public int width() {
		if (texture == null || texture.getImage() == null) return 0;
		return texture.getImage().getWidth();
	}

	public int height() {
		if (texture == null || texture.getImage() == null) return 0;
		return texture.getImage().getHeight();
	}

	@Override
	public void close() {
		if (texture != null) {
			texture.close();
		} else if (!imageFuture.isDone()) {
			imageFuture.cancel(true);
			imageFuture.thenAccept(image -> {
				if (image != null) image.close();
			});
		} else {
			imageFuture.join();
		}
		texture = null;
	}
}

