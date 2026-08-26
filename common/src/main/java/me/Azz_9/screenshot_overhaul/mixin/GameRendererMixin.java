package me.Azz_9.screenshot_overhaul.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.screenshot.FutureScreenshotState;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = "render", at = @At("TAIL"))
	private void afterRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
		if (!FutureScreenshotState.captureRequested) return;

		// Reset immédiatement pour ne pas affecter les frames suivantes
		Runnable pendingCapture = FutureScreenshotState.pendingCapture;
		if (pendingCapture != null) {
			pendingCapture.run();
		}

		FutureScreenshotState.reset();
	}
}
