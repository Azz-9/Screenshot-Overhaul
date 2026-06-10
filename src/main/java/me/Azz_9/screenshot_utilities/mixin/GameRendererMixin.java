package me.Azz_9.screenshot_utilities.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotRenderState;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	// Hide hand in PhotoMode
	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void onRenderHand(CallbackInfo ci) {
		if (PhotoMode.isEnabled() || ScreenshotRenderState.suppressHand) {
			ci.cancel();
		}
	}

	// Apply roll
	@Inject(
			method = "renderLevel",
			at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;mul(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;")
	)
	private void applyRoll(DeltaTracker deltaTracker, CallbackInfo ci, @Local(name = "projectionMatrix") Matrix4f projectionMatrix) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null) return;

		float roll = PhotoMode.getCamera().getRoll(deltaTracker.getGameTimeDeltaPartialTick(true));
		if (roll != 0.0f) {
			projectionMatrix.rotateZ(roll * Mth.DEG_TO_RAD);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void afterRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		if (!ScreenshotRenderState.captureRequested) return;

		// Reset immédiatement pour ne pas affecter les frames suivantes
		Runnable grabber = ScreenshotRenderState.reset();

		if (grabber == null) return;

		grabber.run();
	}
}
