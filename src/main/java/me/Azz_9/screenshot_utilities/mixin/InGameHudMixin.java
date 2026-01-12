package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoModeHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (!(CLIENT.currentScreen instanceof LevelLoadingScreen)) {
			if (!CLIENT.options.hudHidden) {
				PhotoModeHud.render(context, tickCounter);
			}
		}

		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	// Makes HUD correspond to the player rather than the PhotoCamera.
	@Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
	private void onGetCameraPlayer(CallbackInfoReturnable<PlayerEntity> cir) {
		if (PhotoMode.isEnabled()) {
			cir.setReturnValue(CLIENT.player);
		}
	}

	// Don't render equipped-item overlays while PhotoMode is active
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}
}
