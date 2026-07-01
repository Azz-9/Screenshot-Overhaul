package me.Azz_9.screenshot_overhaul.mixin;

import net.minecraft.client.Options;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;

@Mixin(Options.class)
public abstract class OptionsMixin {

	// Disable F5 in PhotoMode
	@Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
	private void onSetCameraType(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}
}
