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

	private static final int MAX_THUMBNAIL_ENTRIES = 128;
	private static final int MAX_FULLVIEW_ENTRIES = 6;

	private static final Map<Path, ScreenshotTexture> THUMBNAIL_CACHE =
			makeCache(MAX_THUMBNAIL_ENTRIES);

	private static final Map<Path, ScreenshotTexture> FULLVIEW_CACHE =
			makeCache(MAX_FULLVIEW_ENTRIES);

	private ScreenshotTextureCache() {
	}

	private static Map<Path, ScreenshotTexture> makeCache(int maxEntries) {
		return new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Path, ScreenshotTexture> eldest) {
				if (size() > maxEntries) {
					eldest.getValue().close();
					return true;
				}
				return false;
			}
		};
	}

	public static synchronized ScreenshotTexture getThumbnail(Path path) {
		return THUMBNAIL_CACHE.computeIfAbsent(path, ScreenshotTexture::loadThumbnail);
	}

	public static synchronized ScreenshotTexture getFullView(Path path) {
		return FULLVIEW_CACHE.computeIfAbsent(path, ScreenshotTexture::loadScreenshot);
	}

	public static synchronized void clear() {
		clearMap(THUMBNAIL_CACHE);
		clearMap(FULLVIEW_CACHE);
	}

	private static void clearMap(Map<Path, ScreenshotTexture> map) {
		map.values().forEach(ScreenshotTexture::close);
		map.clear();
	}
}

