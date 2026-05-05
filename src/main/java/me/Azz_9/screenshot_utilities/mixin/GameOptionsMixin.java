package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(Options.class)
public abstract class GameOptionsMixin {

	// Disable F5 in PhotoMode
	@Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
	private void onSetCameraType(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}
}
