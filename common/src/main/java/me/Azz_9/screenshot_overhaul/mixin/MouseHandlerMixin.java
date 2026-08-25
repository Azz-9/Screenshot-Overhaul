package me.Azz_9.screenshot_overhaul.mixin;

import net.minecraft.client.MouseHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		if (PhotoMode.onMouseScroll(handle, yoffset)) {
			ci.cancel();
		}
	}
}
