package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoCamera;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	private Entity entity;
	@Shadow
	private float eyeHeightOld;
	@Shadow
	private float eyeHeight;

	// When toggling photomode, update the camera's eye height instantly without any transition.
	@Inject(method = "setEntity", at = @At("HEAD"))
	private void onSetEntity(Entity entity, CallbackInfo ci) {
		if (entity instanceof PhotoCamera || this.entity instanceof PhotoCamera) {
			this.eyeHeightOld = this.eyeHeight = entity.getEyeHeight();
		}
	}

	// Removes the submersion overlay when underwater, in lava, or powdered snow.
	@Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
	public void onGetSubmersionType(CallbackInfoReturnable<FogType> cir) {
		if (PhotoMode.isEnabled()) {
			cir.setReturnValue(FogType.NONE);
		}
	}
}