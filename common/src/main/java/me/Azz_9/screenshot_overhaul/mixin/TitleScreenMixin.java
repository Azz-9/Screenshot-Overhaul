package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.SCREENSHOT_SPRITE;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.gui.screen.PanoramaGalleryScreen;
import me.Azz_9.screenshot_overhaul.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_overhaul.compat.CompatManager;
import me.Azz_9.screenshot_overhaul.platform.Services;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Unique
	private static final int MARGIN = 4;
	@Unique
	private static final int SIZE = 20;
	@Unique
	private static final int BUTTONS_WIDTH = 200;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	// add buttons on the title screen
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo info) {
		int screenshotButtonX = (this.width + BUTTONS_WIDTH) / 2 + MARGIN;
		int screenshotButtonY = this.height / 4 + 96;
		if (!Services.PLATFORM.isNeoForge()) {
			if (CompatManager.modMenuPresent()) {
				screenshotButtonY -= SIZE + MARGIN;
			}
		} else {
			screenshotButtonY += 8;
		}

		SpriteIconButton screenshotViewerButton = this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_overhaul.options.screenshots"),
								(btn) -> MINECRAFT.setScreen(new ScreenshotGalleryScreen(this)),
								true
						)
						.withTootip()
						.size(SIZE, SIZE)
						.sprite(SCREENSHOT_SPRITE, 15, 15)
						.build()
		);

		screenshotViewerButton.setPosition(screenshotButtonX, screenshotButtonY);

		this.addRenderableWidget(Button.builder(
						Component.translatable("screenshot_overhaul.options.change_panorama"),
						(btn) -> MINECRAFT.setScreen(new PanoramaGalleryScreen(Component.empty(), this))
				)
				.bounds(width - 100 - 10, 10, 100, 20)
				.build());
	}
}
