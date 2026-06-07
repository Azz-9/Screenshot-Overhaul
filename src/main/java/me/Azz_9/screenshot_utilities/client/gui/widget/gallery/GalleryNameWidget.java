package me.Azz_9.screenshot_utilities.client.gui.widget.gallery;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.gui.widget.screenshotGallery.content.ScreenshotNameWidget;

@Environment(EnvType.CLIENT)
public class GalleryNameWidget extends ScreenshotNameWidget {
	public GalleryNameWidget(int width, int height, @NonNull String initialText) {
		this(0, 0, width, height, initialText);
	}

	public GalleryNameWidget(int x, int y, int width, int height, @NonNull String initialText) {
		super(x, y, width, height, initialText, false);
	}
}
