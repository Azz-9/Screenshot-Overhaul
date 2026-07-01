package me.Azz_9.screenshot_overhaul.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_overhaul.client.config.ConfigLoader;
import me.Azz_9.screenshot_overhaul.client.gui.components.panoramaGallery.PanoramaGalleryWidget;
import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_overhaul.client.screenshot.panorama.PanoramaHolder;

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

		ScreenshotList.setOnChangeListener(_ -> gallery.refresh(false));
	}

	private @NonNull PanoramaGalleryWidget createGallery() {
		return new PanoramaGalleryWidget(
				GLOBAL_PADDING, GLOBAL_PADDING,
				width - GLOBAL_PADDING * 2, getBottomBarTop() - GLOBAL_PADDING,
				this::setTrackedItems, PanoramaHolder::usePanoramaAsync, PanoramaHolder::resetToDefaultAsync
		);
	}

	// input

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (event.key() == InputConstants.KEY_F5 && gallery != null) {
			gallery.refresh(true);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	@Override
	public void onClose() {
		super.onClose();
		ConfigLoader.trySave();
	}
}
