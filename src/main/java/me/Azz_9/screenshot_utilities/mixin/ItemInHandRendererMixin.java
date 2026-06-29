package me.Azz_9.screenshot_utilities.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotRenderState;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	// Hide hand in PhotoMode and on screenshot
	@Inject(
			method = "submitHandsWithItems",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onSubmitHandsWithItems(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
										LocalPlayer player, int lightCoords, CallbackInfo ci) {
		if (PhotoMode.isEnabled() || ScreenshotRenderState.suppressHand) {
			ci.cancel();
		}
	}
}
