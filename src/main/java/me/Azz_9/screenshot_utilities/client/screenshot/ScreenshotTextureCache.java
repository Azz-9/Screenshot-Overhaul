package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.file.Path;
import java.util.Map;

/**
 * Cache LRU
 */
@Environment(EnvType.CLIENT)
public final class ScreenshotTextureCache extends AbstractLruTextureCache<Path, ScreenshotTexture> {

	private static final int MAX_SMALL_THUMBNAIL_ENTRIES = 128;
	private static final int MAX_THUMBNAIL_ENTRIES = 64;
	private static final int MAX_FULL_VIEW_ENTRIES = 6;

	private final Map<Path, ScreenshotTexture> SMALL_THUMBNAIL_CACHE = createCache(MAX_SMALL_THUMBNAIL_ENTRIES);
	private final Map<Path, ScreenshotTexture> THUMBNAIL_CACHE = createCache(MAX_THUMBNAIL_ENTRIES);
	private final Map<Path, ScreenshotTexture> FULL_VIEW_CACHE = createCache(MAX_FULL_VIEW_ENTRIES);

	private static final ScreenshotTextureCache INSTANCE = new ScreenshotTextureCache();

	private ScreenshotTextureCache() {
	}

	public static ScreenshotTexture getSmallThumbnail(Path path) {
		return INSTANCE.get(INSTANCE.SMALL_THUMBNAIL_CACHE, path, ScreenshotTexture::loadSmallThumbnail);
	}

	public static ScreenshotTexture getThumbnail(Path path) {
		return INSTANCE.get(INSTANCE.THUMBNAIL_CACHE, path, ScreenshotTexture::loadThumbnail);
	}

	public static ScreenshotTexture getFullView(Path path) {
		return INSTANCE.get(INSTANCE.FULL_VIEW_CACHE, path, ScreenshotTexture::loadScreenshot);
	}

	public static void clearCache() {
		INSTANCE.clear();
	}
}

