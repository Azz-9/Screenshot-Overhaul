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

	private static final int MAX_SMALL_THUMBNAIL_ENTRIES = 128;
	private static final int MAX_THUMBNAIL_ENTRIES = 64;
	private static final int MAX_FULL_VIEW_ENTRIES = 6;

	private static final Map<Path, ScreenshotTexture> SMALL_THUMBNAIL_CACHE = makeCache(MAX_SMALL_THUMBNAIL_ENTRIES);
	private static final Map<Path, ScreenshotTexture> THUMBNAIL_CACHE = makeCache(MAX_THUMBNAIL_ENTRIES);
	private static final Map<Path, ScreenshotTexture> FULL_VIEW_CACHE = makeCache(MAX_FULL_VIEW_ENTRIES);

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

	public static synchronized ScreenshotTexture getSmallThumbnail(Path path) {
		return SMALL_THUMBNAIL_CACHE.computeIfAbsent(path, ScreenshotTexture::loadSmallThumbnail);
	}

	public static synchronized ScreenshotTexture getThumbnail(Path path) {
		return THUMBNAIL_CACHE.computeIfAbsent(path, ScreenshotTexture::loadThumbnail);
	}

	public static synchronized ScreenshotTexture getFullView(Path path) {
		return FULL_VIEW_CACHE.computeIfAbsent(path, ScreenshotTexture::loadScreenshot);
	}

	public static synchronized void clear() {
		clearMap(THUMBNAIL_CACHE);
		clearMap(FULL_VIEW_CACHE);
	}

	private static void clearMap(Map<Path, ScreenshotTexture> map) {
		map.values().forEach(ScreenshotTexture::close);
		map.clear();
	}
}

