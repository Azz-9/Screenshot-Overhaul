package me.Azz_9.screenshot_overhaul.client.cache;

import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.Map;

import me.Azz_9.screenshot_overhaul.client.texture.ScreenshotTexture;

/**
 * Cache LRU
 */
public final class ScreenshotTextureCache extends AbstractLruTextureCache<Path, ScreenshotTexture> {

	private static final int MAX_SMALL_THUMBNAIL_ENTRIES = 128;
	private static final int MAX_THUMBNAIL_ENTRIES = 64;
	private static final int MAX_FULL_VIEW_ENTRIES = 6;

	private final @NonNull Map<Path, ScreenshotTexture> SMALL_THUMBNAIL_CACHE = createCache(MAX_SMALL_THUMBNAIL_ENTRIES);
	private final @NonNull Map<Path, ScreenshotTexture> THUMBNAIL_CACHE = createCache(MAX_THUMBNAIL_ENTRIES);
	private final @NonNull Map<Path, ScreenshotTexture> FULL_VIEW_CACHE = createCache(MAX_FULL_VIEW_ENTRIES);

	private static final @NonNull ScreenshotTextureCache INSTANCE = new ScreenshotTextureCache();

	private ScreenshotTextureCache() {
	}

	public static @NonNull ScreenshotTexture getSmallThumbnail(@NonNull Path path) {
		return INSTANCE.get(INSTANCE.SMALL_THUMBNAIL_CACHE, path, ScreenshotTexture::loadSmallThumbnail);
	}

	public static @NonNull ScreenshotTexture getThumbnail(@NonNull Path path) {
		return INSTANCE.get(INSTANCE.THUMBNAIL_CACHE, path, ScreenshotTexture::loadThumbnail);
	}

	public static @NonNull ScreenshotTexture getFullView(@NonNull Path path) {
		return INSTANCE.get(INSTANCE.FULL_VIEW_CACHE, path, ScreenshotTexture::loadScreenshot);
	}

	public static void clearCache() {
		INSTANCE.clear();
	}
}

