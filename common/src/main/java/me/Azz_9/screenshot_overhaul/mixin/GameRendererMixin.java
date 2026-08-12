package me.Azz_9.screenshot_overhaul.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_overhaul.client.screenshot.FutureScreenshotState;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {


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
		if (!FutureScreenshotState.captureRequested) return;

		// Reset immédiatement pour ne pas affecter les frames suivantes
		Runnable grabber = FutureScreenshotState.reset();

		if (grabber != null) grabber.run();
	}

	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, name = "deltaTracker")
	private static DeltaTracker freezeRenderTickDelta(DeltaTracker deltaTracker) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			return DeltaTracker.ZERO;
		}

		return deltaTracker;
	}

	@ModifyVariable(method = "extract", at = @At("HEAD"), argsOnly = true, name = "deltaTracker")
	private static DeltaTracker freezeExtractTickDelta(DeltaTracker deltaTracker) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			return DeltaTracker.ZERO;
		}

		return deltaTracker;
	}
}
