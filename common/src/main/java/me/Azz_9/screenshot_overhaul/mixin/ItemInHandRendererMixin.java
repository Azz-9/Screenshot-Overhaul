package me.Azz_9.screenshot_overhaul.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.screenshot.FutureScreenshotState;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	// Hide hand on screenshot
	@Inject(
			method = "submitHandsWithItems",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onSubmitHandsWithItems(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
										LocalPlayer player, int lightCoords, CallbackInfo ci) {
		if (FutureScreenshotState.suppressHand) {
			ci.cancel();
		}
	}
}
