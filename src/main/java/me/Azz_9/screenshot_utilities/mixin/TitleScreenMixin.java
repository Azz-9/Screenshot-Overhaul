package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.gui.ScreenshotGalleryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo info) {
		int realmsY = this.height / 4 + 48 + 24 * 2;

		int realmsRightX = this.width / 2 + 100;
		int margin = 4;

		TextIconButtonWidget screenshotViewerButton = this.addDrawableChild(
				TextIconButtonWidget.builder(
								Text.translatable("screenshot_utilities.options.screenshots"),
								(btn) -> CLIENT.setScreen(new ScreenshotGalleryScreen()),
								true
						)
						.useTextAsTooltip()
						.width(20)
						.texture(Identifier.of(MOD_ID, "icon/screenshot"), 15, 15)
						.build()
		);

		screenshotViewerButton.setPosition(realmsRightX + margin, realmsY);
	}
}
