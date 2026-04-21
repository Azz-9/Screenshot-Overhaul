package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo info) {
		int realmsY = this.height / 4 + 48 + 24 * 2;

		int realmsRightX = this.width / 2 + 100;
		int margin = 4;

		SpriteIconButton screenshotViewerButton = this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_utilities.options.screenshots"),
								(btn) -> MINECRAFT.setScreen(new ScreenshotGalleryScreen()),
								true
						)
						.withTootip()
						.width(20)
						.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/screenshot"), 15, 15)
						.build()
		);

		screenshotViewerButton.setPosition(realmsRightX + margin, realmsY);
	}
}
