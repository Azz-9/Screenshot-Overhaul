package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Mixin(Mouse.class)
public abstract class MouseMixin {

	@Unique
	private static final float SCROLL_SPEED_WITH_CTRL = 0.01f;
	@Unique
	private static final float SCROLL_SPEED_WITHOUT_CTRL = 0.05f;

	@Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null || window != CLIENT.getWindow().getHandle() || CLIENT.currentScreen != null) {
			return;
		}

		boolean discrete = CLIENT.options.getDiscreteMouseScroll().getValue();
		double sensitivity = CLIENT.options.getMouseWheelSensitivity().getValue();

		double scroll = (discrete ? Math.signum(vertical) : vertical) * sensitivity;

		if (scroll != 0.0) {
			float scrollSpeed = (CLIENT.isCtrlPressed() ? SCROLL_SPEED_WITH_CTRL : SCROLL_SPEED_WITHOUT_CTRL);
			PhotoMode.getCamera().setSpeed(MathHelper.clamp(PhotoMode.getCamera().getSpeed() + scroll * scrollSpeed, 0, 5.0));
		}

		ci.cancel();
	}
}
