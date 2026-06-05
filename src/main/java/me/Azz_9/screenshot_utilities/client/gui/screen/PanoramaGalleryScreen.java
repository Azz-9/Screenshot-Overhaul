package me.Azz_9.screenshot_utilities.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent.ScreenshotThumbnailWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.Panorama;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaHolder;

public class PanoramaGalleryScreen extends AbstractBackNavigableScreen implements FocusableScreen {

	// focus manager
	private final @NonNull FocusManager focusManager = new FocusManager();

	private List<Panorama> panoramas = new ArrayList<>();

	public PanoramaGalleryScreen(@NonNull Component title, @Nullable Screen parent) {
		super(title, parent);
		ScreenshotList.whenPanoramasLoaded(this::setPanoramas);
	}

	public void setPanoramas(List<Panorama> panoramas) {
		this.panoramas = panoramas;

		clearWidgets();

		int x = 10;
		for (Panorama panorama : panoramas) {
			addRenderableWidget(new ScreenshotThumbnailWidget(x, 10, 150, 150, panorama.faces()[0], screenshot -> {
				PanoramaHolder.usePanorama(panorama);
			}));

			x += 160;
		}
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}
}
