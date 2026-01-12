package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

@Environment(EnvType.CLIENT)
public final class ScreenshotTexture implements AutoCloseable {

	private final Identifier id;
	private final CompletableFuture<NativeImage> imageFuture;
	private final int maxSize; // 0 = full res
	private NativeImageBackedTexture texture;
	private NativeImage pendingImage;

	public ScreenshotTexture(Path file, int maxSize) {
		this.maxSize = maxSize;
		this.id = Identifier.of(MOD_ID, "screenshots/" + file.toAbsolutePath()
				.toString()
				.toLowerCase()
				.replace('\\', '/')
				.replaceAll("[^a-z0-9/._-]", "_"));

		this.imageFuture = CompletableFuture.supplyAsync(() -> {
			try (InputStream in = Files.newInputStream(file)) {
				return NativeImage.read(in);
			} catch (Exception e) {
				return null;
			}
		}, Util.getMainWorkerExecutor());
	}

	public static ScreenshotTexture loadThumbnail(Path file, int maxSize) {
		return new ScreenshotTexture(file, maxSize);
	}

	public boolean isReady() {
		return imageFuture.isDone() && texture != null;
	}

	public void uploadIfNeeded() {
		if (texture != null || !imageFuture.isDone()) return;

		NativeImage img = imageFuture.join();
		if (img == null) return;

		pendingImage = img;
		texture = new NativeImageBackedTexture(id::toString, img);

		CLIENT.getTextureManager().registerTexture(id, texture);

		pendingImage = null;
	}

	public Identifier id() {
		return id;
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
			CLIENT.execute(() -> CLIENT.getTextureManager().destroyTexture(this.id));

			texture = null;
		}

		if (pendingImage != null) {
			pendingImage.close();
			pendingImage = null;
		}
	}
}

