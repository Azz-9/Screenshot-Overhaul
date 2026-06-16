package me.Azz_9.screenshot_utilities.client.screenshot.panorama;

import java.util.Map;

import me.Azz_9.screenshot_utilities.client.screenshot.AbstractLruTextureCache;

public final class PanoramaTextureCache extends AbstractLruTextureCache<Panorama, PanoramaTexture> {
	private static final int MAX_ENTRIES = 16;

	private final Map<Panorama, PanoramaTexture> THUMBNAIL_CACHE = createCache(MAX_ENTRIES);

	private static final PanoramaTextureCache INSTANCE = new PanoramaTextureCache();

	private PanoramaTextureCache() {
	}

	public static PanoramaTexture getThumbnail(Panorama panorama) {
		return INSTANCE.get(INSTANCE.THUMBNAIL_CACHE, panorama, PanoramaTexture::loadThumbnail);
	}

	public static void clearCache() {
		INSTANCE.clear();
	}
}
