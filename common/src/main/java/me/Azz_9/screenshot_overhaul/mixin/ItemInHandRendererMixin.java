package me.Azz_9.screenshot_overhaul.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_overhaul.client.screenshot.FutureScreenshotState;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	// Hide hand in PhotoMode and on screenshot
	@Inject(
			method = "renderArmWithItem",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onSubmitHandsWithItems(AbstractClientPlayer player, float frameInterp, float xRot,
										InteractionHand hand, float attack, ItemStack itemStack,
										float inverseArmHeight, PoseStack poseStack,
										SubmitNodeCollector submitNodeCollector, int lightCoords,
										CallbackInfo ci) {
		if (PhotoMode.isEnabled() || FutureScreenshotState.suppressHand) {
			ci.cancel();
		}
	}
}
