package me.Azz_9.screenshot_overhaul.client.guiFlexLib.components.screenshotGallery;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

import me.Azz_9.screenshot_overhaul.client.guiFlexLib.components.gallery.ScrollableGallery;

public class ScreenshotGalleryWidget extends ScrollableGallery<> {

	private final @NonNull AtomicBoolean refreshing = new AtomicBoolean(false);

	public ScreenshotGalleryWidget() {

	}
}
