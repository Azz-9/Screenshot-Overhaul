package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

@Environment(EnvType.CLIENT)
public final class ScreenshotTexture implements AutoCloseable {

	private final Identifier id;
	private NativeImageBackedTexture texture;
	private final CompletableFuture<NativeImage> imageFuture;

	public ScreenshotTexture(Path file) {
		this.id = Identifier.of(MOD_ID, "screenshots/" + file.getFileName().toString().hashCode());

		this.imageFuture = CompletableFuture.supplyAsync(() -> {
			try (InputStream in = Files.newInputStream(file)) {
				return NativeImage.read(in);
			} catch (Exception e) {
				return null;
			}
		}, Util.getMainWorkerExecutor());
	}

	public boolean isReady() {
		return imageFuture.isDone() && texture != null;
	}

	public void uploadIfNeeded() {
		if (texture != null || !imageFuture.isDone()) return;

		NativeImage img = imageFuture.join();
		if (img == null) return;

		texture = new NativeImageBackedTexture(id::toString, img);
		MinecraftClient.getInstance()
				.getTextureManager()
				.registerTexture(id, texture);
	}

	public Identifier id() {
		return id;
	}

	public int width() {
		return texture != null ? texture.getImage().getWidth() : 0;
	}

	public int height() {
		return texture != null ? texture.getImage().getHeight() : 0;
	}

	@Override
	public void close() {
		if (texture != null) {
			texture.close();
			texture = null;
		}
	}
}

