package me.Azz_9.screenshot_overhaul.mixin;

import net.minecraft.client.ClientClockManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;

@Mixin(ClientClockManager.class)
public abstract class ClientClockManagerMixin {

	@Inject(method = "getTotalTicks", at = @At("RETURN"), cancellable = true)
	private void getTotalTicks(CallbackInfoReturnable<Long> cir) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			cir.setReturnValue(PhotoMode.getFrozenTime());
		} else {
			cir.cancel();
		}
	}
}
