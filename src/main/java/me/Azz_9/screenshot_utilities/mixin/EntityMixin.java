package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Mixin(Entity.class)
public abstract class EntityMixin {

	// Makes mouse input rotate the PhotoCamera.
	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && this.equals(CLIENT.player) && PhotoMode.getCamera() != null) {
			PhotoMode.getCamera().changeLookDirection(cursorDeltaX, cursorDeltaY);
			ci.cancel();
		}
	}

	// Prevents PhotoCamera from pushing/getting pushed by entities.
	@Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
	private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && (entity.equals(PhotoMode.getCamera()) || this.equals(PhotoMode.getCamera()))) {
			ci.cancel();
		}
	}
}
