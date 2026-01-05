package me.Azz_9.screenshot_utilities.mixin;


import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyBinding.class, priority = 900)
public abstract class KeyBindingMixin {

	@Inject(method = "onKeyPressed", at = @At(value = "HEAD"))
	private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci) {

		if (Screenshot_utilitiesClient.getOpenPhotoModeKeybind().isPressed()) {
			if (PhotoMode.isEnabled()) {
				PhotoMode.disable();
			} else {
				PhotoMode.enable();
			}
		}
	}
}
