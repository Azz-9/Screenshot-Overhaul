package me.Azz_9.screenshot_utilities.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.PanoramaGalleryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaHolder;

public class PanoramaGalleryScreen extends AbstractSavableScreen implements FocusableScreen {

	private static final int GLOBAL_PADDING = 10;

	// focus manager
	private final @NonNull FocusManager focusManager = new FocusManager();

	private @Nullable PanoramaGalleryWidget gallery;

	public PanoramaGalleryScreen(@NonNull Component title, @Nullable Screen parent) {
		super(title, parent);
	}

	@Override
	protected void initContent() {
		gallery = createGallery();

		addRenderableWidget(gallery);
	}

	private PanoramaGalleryWidget createGallery() {
		return new PanoramaGalleryWidget(
				GLOBAL_PADDING, GLOBAL_PADDING,
				width - GLOBAL_PADDING * 2, getBottomBarTop() - GLOBAL_PADDING,
				PanoramaHolder::usePanoramaAsync, PanoramaHolder::resetToDefaultAsync
		);
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}
}
