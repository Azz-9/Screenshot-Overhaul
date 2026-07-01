package me.Azz_9.screenshot_overhaul.mixin;

import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;
import com.mojang.blaze3d.vulkan.VulkanDevice;

import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotGrabber;
import me.Azz_9.screenshot_overhaul.platform.Services;

@Mixin(VulkanCommandEncoder.class)
public abstract class VulkanCommandEncoderMixin {

	@Shadow @Final private VulkanDevice device;

	@Shadow private long completedSubmitIndex;

	@Shadow private long currentSubmitIndex;

	@Inject(method = "awaitSubmitCompletion", at = @At("HEAD"), cancellable = true)
	private void awaitSubmitCompletion(long submitIndex, long timeoutNS, CallbackInfoReturnable<Boolean> cir) {
		if (ScreenshotGrabber.capturingPanorama && Services.PLATFORM.isModLoaded("sodium")) {
			this.device.graphicsQueue().waitIdle();
			this.completedSubmitIndex = this.currentSubmitIndex;
			cir.setReturnValue(true);
		}
	}
}
