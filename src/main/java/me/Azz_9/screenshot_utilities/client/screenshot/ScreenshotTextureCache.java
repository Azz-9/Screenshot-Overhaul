package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache LRU
 */
@Environment(EnvType.CLIENT)
public final class ScreenshotTextureCache {

	private static final int MAX_ENTRIES = 128;

	private static final Map<CacheKey, ScreenshotTexture> CACHE =
			new LinkedHashMap<>(16, 0.75f, true) {

				@Override
				protected boolean removeEldestEntry(Map.Entry<CacheKey, ScreenshotTexture> eldest) {
					if (size() > MAX_ENTRIES) {
						eldest.getValue().close();
						return true;
					}
					return false;
				}
			};

	private ScreenshotTextureCache() {
	}

	public static synchronized ScreenshotTexture getThumbnail(Path path) {
		CacheKey key = new CacheKey(path, 512);
		return CACHE.computeIfAbsent(key, k -> ScreenshotTexture.loadThumbnail(path));
	}

	public static synchronized ScreenshotTexture getScreenshot(Path path) {
		CacheKey key = new CacheKey(path, 1920);
		return CACHE.computeIfAbsent(key, k -> ScreenshotTexture.loadScreenshot(path));
	}

	public static synchronized void clear() {
		for (ScreenshotTexture texture : CACHE.values()) {
			texture.close();
		}
		CACHE.clear();
	}

	public record CacheKey(Path path, int maxSize) {
	}
}

