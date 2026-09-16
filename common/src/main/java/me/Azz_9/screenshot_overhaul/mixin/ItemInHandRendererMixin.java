package me.Azz_9.screenshot_overhaul.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.screenshot.FutureScreenshotState;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandRendererMixin {

	// Hide hand on screenshot
	@Inject(
			method = "submitHandsWithItems",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onSubmitHandsWithItems(float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
										PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
		if (FutureScreenshotState.suppressHand) {
			ci.cancel();
		}
	}
}
