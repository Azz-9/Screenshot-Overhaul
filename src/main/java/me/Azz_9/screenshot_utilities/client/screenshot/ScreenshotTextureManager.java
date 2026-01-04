package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

@Environment(EnvType.CLIENT)
public class ScreenshotTextureManager {

	private static final Map<Path, ScreenshotTexture> CACHE = new HashMap<>();

	public static ScreenshotTexture get(Path file, int width, int height) {
		return CACHE.computeIfAbsent(file, (f) -> ScreenshotTextureManager.load(f, width, height));
	}

	private static ScreenshotTexture load(Path file, int width, int height) {
		try {
			NativeImage image = ScreenshotLoader.loadThumbnail(file, width, height);

			Identifier id = Identifier.of(MOD_ID, "screenshots/" + Integer.toHexString(file.hashCode()));

			NativeImageBackedTexture texture = new NativeImageBackedTexture(id::toString, image);
			MinecraftClient.getInstance()
					.getTextureManager()
					.registerTexture(id, texture);

			return new ScreenshotTexture(id, texture);
		} catch (IOException e) {
			return null;
		}
	}

	public static void clear() {
		TextureManager tm = MinecraftClient.getInstance().getTextureManager();

		for (ScreenshotTexture texture : CACHE.values()) {
			tm.destroyTexture(texture.getId());
			texture.close();
		}

		CACHE.clear();
	}
}
