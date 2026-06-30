package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

	@Shadow
	private @Nullable Button disconnectButton;
	@Unique
	private static final int MARGIN = 4;
	@Unique
	private static final int SIZE = 20;

	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void init(CallbackInfo ci) {
		SpriteIconButton screenshotViewerButton = this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_utilities.options.screenshots"),
								(btn) -> MINECRAFT.setScreen(new ScreenshotGalleryScreen()),
								true
						)
						.withTootip()
						.size(SIZE, SIZE)
						.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/screenshot"), 15, 15)
						.build()
		);

		if (disconnectButton != null) {
			screenshotViewerButton.setPosition(
					disconnectButton.getRight() + MARGIN,
					disconnectButton.getY()
			);
		} else {
			screenshotViewerButton.setPosition(
					width - SIZE - MARGIN,
					height - SIZE - MARGIN
			);
		}
	}
}
