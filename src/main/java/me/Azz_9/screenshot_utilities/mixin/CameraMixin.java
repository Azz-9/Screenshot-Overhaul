package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoCamera;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	private Entity focusedEntity;
	@Shadow
	private float lastCameraY;
	@Shadow
	private float cameraY;

	// When toggling photomode, update the camera's eye height instantly without any transition.
	@Inject(method = "update", at = @At("HEAD"))
	public void onUpdate(World area, Entity newFocusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		if (newFocusedEntity == null || this.focusedEntity == null || newFocusedEntity.equals(this.focusedEntity)) {
			return;
		}

		if (newFocusedEntity instanceof PhotoCamera || this.focusedEntity instanceof PhotoCamera) {
			this.lastCameraY = this.cameraY = newFocusedEntity.getStandingEyeHeight();
		}
	}

	// Removes the submersion overlay when underwater, in lava, or powdered snow.
	@Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
	public void onGetSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
		if (PhotoMode.isEnabled()) {
			cir.setReturnValue(CameraSubmersionType.NONE);
		}
	}
}