package me.Azz_9.screenshot_overhaul.client.screenshot.panorama;

import org.jspecify.annotations.NonNull;

import java.util.Map;

import me.Azz_9.screenshot_overhaul.client.screenshot.AbstractLruTextureCache;

public final class PanoramaTextureCache extends AbstractLruTextureCache<Panorama, PanoramaTexture> {
	private static final int MAX_ENTRIES = 16;

	private final @NonNull Map<Panorama, PanoramaTexture> THUMBNAIL_CACHE = createCache(MAX_ENTRIES);

	private static final @NonNull PanoramaTextureCache INSTANCE = new PanoramaTextureCache();

	private PanoramaTextureCache() {
	}

	public static @NonNull PanoramaTexture getThumbnail(@NonNull Panorama panorama) {
		return INSTANCE.get(INSTANCE.THUMBNAIL_CACHE, panorama, PanoramaTexture::loadThumbnail);
	}

	public static void clearCache() {
		INSTANCE.clear();
	}
}
