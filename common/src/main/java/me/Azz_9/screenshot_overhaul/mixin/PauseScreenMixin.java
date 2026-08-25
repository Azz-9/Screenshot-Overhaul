package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.SCREENSHOT_SPRITE;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.gui.screen.ScreenshotGalleryScreen;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

	@Shadow
	private @Nullable Button disconnectButton;
	@Unique
	private static final int screenshot_overhaul$MARGIN = 4;
	@Unique
	private static final int screenshot_overhaul$SIZE = 20;

	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		SpriteIconButton screenshotViewerButton = this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_overhaul.options.screenshots"),
								(btn) -> MINECRAFT.setScreen(new ScreenshotGalleryScreen(this)),
								true
						)
						.withTootip()
						.size(screenshot_overhaul$SIZE, screenshot_overhaul$SIZE)
						.sprite(SCREENSHOT_SPRITE, 15, 15)
						.build()
		);

		if (disconnectButton != null) {
			screenshotViewerButton.setPosition(
					disconnectButton.getRight() + screenshot_overhaul$MARGIN,
					disconnectButton.getY()
			);
		} else {
			screenshotViewerButton.setPosition(
					width - screenshot_overhaul$SIZE - screenshot_overhaul$MARGIN,
					height - screenshot_overhaul$SIZE - screenshot_overhaul$MARGIN
			);
		}
	}
}
