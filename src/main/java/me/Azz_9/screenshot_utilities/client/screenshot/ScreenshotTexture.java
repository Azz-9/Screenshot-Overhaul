package me.Azz_9.screenshot_utilities.client.screenshot;

import com.mojang.blaze3d.platform.NativeImage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class ScreenshotTexture implements AutoCloseable {

	private final @NonNull CompletableFuture<NativeImage> imageFuture;

	private @Nullable DynamicTexture texture;
	private final Path file;
	private volatile boolean closed = false;

	private ScreenshotTexture(Path file, int maxSize) {
		this(file, () -> {
			try (InputStream in = Files.newInputStream(file)) {
				NativeImage img = NativeImage.read(in);
				return ImageScaler.downscale(img, maxSize);
			} catch (Exception e) {
				return null;
			}
		});
	}

	private ScreenshotTexture(Path file, NativeImage image, int maxSize) {
		this(file, () -> ImageScaler.downscale(image, maxSize));
	}

	private ScreenshotTexture(Path file, Supplier<NativeImage> imageSupplier) {
		this.file = file;

		this.imageFuture = CompletableFuture.supplyAsync(
				imageSupplier,
				Util.backgroundExecutor()
		);
	}

	private ScreenshotTexture(Path file) {
		this(file, 0);
	}

	@NonNull
	public static ScreenshotTexture loadSmallThumbnail(Path file) {
		return new ScreenshotTexture(file, 128); // 128x72
	}

	@NonNull
	public static ScreenshotTexture loadThumbnail(Path file) {
		return new ScreenshotTexture(file, 512); // 512x288
	}

	@NonNull
	public static ScreenshotTexture loadThumbnail(Path file, NativeImage image) {
		return new ScreenshotTexture(file, image, 512); //512x288
	}

	@NonNull
	public static ScreenshotTexture loadScreenshot(Path file) {
		return new ScreenshotTexture(file, 1920); // 1920x1080
	}

	public Path getFile() {
		return file;
	}

	@Nullable
	public DynamicTexture getTexture() {
		if (closed) return null;

		if (texture != null) {
			return texture;
		}

		if (!imageFuture.isDone()) {
			return null;
		}

		NativeImage image;
		try {
			image = imageFuture.join();
		} catch (Exception e) {
			return null;
		}

		if (image == null) return null;

		texture = new DynamicTexture(() -> "screenshot_texture", image);

		return texture;
	}

	public boolean isReady() {
		return imageFuture.isDone();
	}

	public boolean isClosed() {
		return closed;
	}

	public void whenReady(BiConsumer<NativeImage, Throwable> consumer) {
		imageFuture.whenComplete(consumer);
	}

	public int width() {
		if (getTexture() == null) return 0;
		return getTexture().getPixels().getWidth();
	}

	public int height() {
		if (getTexture() == null) return 0;
		return getTexture().getPixels().getHeight();
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;

		if (texture != null) {
			texture.close();
			texture = null;
		}

		imageFuture.cancel(true);

		if (imageFuture.isDone()) {
			try {
				NativeImage image = imageFuture.join();
				if (image != null) {
					image.close();
				}
			} catch (Exception ignored) {
			}
		}
	}
}