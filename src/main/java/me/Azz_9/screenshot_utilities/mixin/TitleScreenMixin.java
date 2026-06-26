package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.gui.screen.PanoramaGalleryScreen;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Unique
	private static final int MARGIN = 4;
	@Unique
	private static final int SIZE = 20;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	// add buttons on the title screen
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo info) {
		int realmsY = this.height / 4 + 48 + 24 * 2;

		int realmsRightX = this.width / 2 + 100;

		SpriteIconButton screenshotViewerButton = this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_utilities.options.screenshots"),
								(btn) -> MINECRAFT.gui.setScreen(new ScreenshotGalleryScreen()),
								true
						)
						.withTootip()
						.size(SIZE, SIZE)
						.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/screenshot"), 15, 15)
						.build()
		);

		screenshotViewerButton.setPosition(realmsRightX + MARGIN, realmsY);

		this.addRenderableWidget(Button.builder(
						Component.translatable("screenshot_utilities.options.change_panorama"),
						(btn) -> MINECRAFT.gui.setScreen(new PanoramaGalleryScreen(Component.empty(), this))
				)
				.bounds(width - 100 - 10, 10, 100, 20)
				.build());
	}
}
