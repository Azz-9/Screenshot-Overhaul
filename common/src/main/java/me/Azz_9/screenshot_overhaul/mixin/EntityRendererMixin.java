package me.Azz_9.screenshot_overhaul.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> {
	
	// Hide name tags if showNametags is false
	@Inject(
			method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", 
			at = @At("HEAD"),
			cancellable = true
	)
	private void submitNameDisplay(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, int offset, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && Config.getInstance().showNametags.getValue()) {
			ci.cancel();
		}
	}
}
