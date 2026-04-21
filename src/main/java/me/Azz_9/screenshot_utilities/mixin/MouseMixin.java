package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public abstract class MouseMixin {

	@Unique
	private static final float SCROLL_SPEED_WITH_CTRL = 0.01f;
	@Unique
	private static final float SCROLL_SPEED_WITHOUT_CTRL = 0.05f;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null || handle != MINECRAFT.getWindow().handle() || MINECRAFT.screen != null) {
			return;
		}

		boolean discrete = MINECRAFT.options.discreteMouseScroll().get();
		double sensitivity = MINECRAFT.options.mouseWheelSensitivity().get();

		double scroll = (discrete ? Math.signum(yoffset) : yoffset) * sensitivity;

		if (scroll != 0.0) {
			float scrollSpeed = (MINECRAFT.hasControlDown() ? SCROLL_SPEED_WITH_CTRL : SCROLL_SPEED_WITHOUT_CTRL);
			PhotoMode.getCamera().setVelocity(Mth.clamp(PhotoMode.getCamera().getVelocity() + scroll * scrollSpeed, 0, 5.0));
		}

		ci.cancel();
	}
}
