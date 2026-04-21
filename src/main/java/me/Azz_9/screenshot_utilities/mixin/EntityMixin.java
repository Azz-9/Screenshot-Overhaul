package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixin {

	// Makes mouse input rotate the PhotoCamera.
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	private void onTurn(double xo, double yo, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && this.equals(MINECRAFT.player) && PhotoMode.getCamera() != null) {
			PhotoMode.getCamera().turn(xo, yo);
			ci.cancel();
		}
	}

	// Prevents PhotoCamera from pushing/getting pushed by entities.
	@Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	private void onPush(Entity entity, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && (entity.equals(PhotoMode.getCamera()) || this.equals(PhotoMode.getCamera()))) {
			ci.cancel();
		}
	}
}
