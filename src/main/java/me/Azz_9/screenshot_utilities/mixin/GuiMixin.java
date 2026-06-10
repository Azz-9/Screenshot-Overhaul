package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotRenderState;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class GuiMixin {

	// On hud render
	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ScreenshotRenderState.suppressHud) {
			ci.cancel();
			return;
		}
		if (Screenshot_utilitiesClient.hudRenderHook(graphics, deltaTracker)) {
			ci.cancel();
		}
	}

	@Inject(method = "extractChat", at = @At("HEAD"), cancellable = true)
	private void onRenderChat(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ScreenshotRenderState.suppressChat) {
			System.out.println("canceled");
			ci.cancel();
		}
	}

	// Makes HUD correspond to the player rather than the PhotoCamera.
	@Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
	private void onGetCameraPlayer(CallbackInfoReturnable<Player> cir) {
		if (PhotoMode.isEnabled()) {
			cir.setReturnValue(MINECRAFT.player);
		}
	}

	// Don't render equipped-item overlays while PhotoMode is active
	@Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
	private void onExtractTextureOverlay(GuiGraphicsExtractor graphics, Identifier texture, float opacity, CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}
}
