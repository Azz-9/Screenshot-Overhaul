package me.Azz_9.screenshot_overhaul.client.guiFlexLib.screen;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import me.Azz_9.flex_lib.client.gui.components.FlexButton;
import me.Azz_9.flex_lib.client.gui.events.EventType;
import me.Azz_9.flex_lib.client.gui.style.Length;
import me.Azz_9.flex_lib.client.gui.style.Sides;
import me.Azz_9.flex_lib.client.gui.style.StyleProperties;
import me.Azz_9.flex_lib.client.gui.style.properties.Display;
import me.Azz_9.flex_lib.client.gui.style.properties.FlexDirection;
import me.Azz_9.screenshot_overhaul.client.gui.screen.PanoramaGalleryScreen;
import me.Azz_9.screenshot_overhaul.client.guiFlexLib.components.screenshotGallery.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotManager;

public class ScreenshotGalleryScreen extends AbstractSavableScreen {

	private static final int GLOBAL_PADDING = 10;
	private static final int PANORAMA_BUTTON_WIDTH = 160;
	private static final int PANORAMA_BUTTON_HEIGHT = 20;

	public ScreenshotGalleryScreen(@Nullable Screen parent) {
		super(Component.translatable("screenshot_overhaul.narrator.screenshot_gallery"), parent);
	}

	@Override
	protected void onInit() {
		root().declaredStyle()
				.set(StyleProperties.DISPLAY, Display.FLEX)
				.set(StyleProperties.FLEX_DIRECTION, FlexDirection.COLUMN);

		FlexButton panoramasButton = new FlexButton();
		panoramasButton.addText("screenshot_overhaul.options.panoramas");
		panoramasButton.events().addEventListener(EventType.MOUSE_DOWN, _ ->
				MINECRAFT.gui.setScreen(new PanoramaGalleryScreen(Component.empty(), this)));
		panoramasButton.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(PANORAMA_BUTTON_WIDTH))
				.set(StyleProperties.HEIGHT, Length.px(PANORAMA_BUTTON_HEIGHT))
				.set(StyleProperties.MARGIN, Sides.of(Length.px(5), Length.zero()))
				.set(StyleProperties.MARGIN_LEFT, Length.px(10));

		ScreenshotGalleryWidget gallery = new ScreenshotGalleryWidget();
		gallery.declaredStyle()
				.set(StyleProperties.WIDTH, Length.percent(100))
				.set(StyleProperties.FLEX_GROW, 1f);

		root().addAll(panoramasButton, gallery);
		super.onInit();
	}

	@Override
	public void onClose() {
		ScreenshotManager.save();
		super.onClose();
	}
}
