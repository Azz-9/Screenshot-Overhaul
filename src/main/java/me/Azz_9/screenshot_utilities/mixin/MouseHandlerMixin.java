package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MouseHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		if (PhotoMode.onMouseScroll(handle, xoffset, yoffset)) {
			ci.cancel();
		}
	}
}
