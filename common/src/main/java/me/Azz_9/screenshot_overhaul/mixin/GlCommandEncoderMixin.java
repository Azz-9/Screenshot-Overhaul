package me.Azz_9.screenshot_overhaul.mixin;

import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_overhaul.client.panorama.ScreenshotContext;
import me.Azz_9.screenshot_overhaul.platform.Services;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder", remap = false)
public abstract class GlCommandEncoderMixin {

	@Inject(method = "awaitSubmit", at = @At("HEAD"), cancellable = true, remap = false)
	private void awaitSubmit(long p1, long p2, CallbackInfoReturnable<Boolean> cir) {
		if (ScreenshotContext.capturingPanorama && Services.PLATFORM.isModLoaded("sodium")) {
			GL11C.glFinish();
			cir.setReturnValue(true);
		}
	}
}
