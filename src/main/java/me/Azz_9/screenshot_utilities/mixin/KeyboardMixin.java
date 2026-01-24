package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	@Inject(method = "onKey", at = @At("HEAD"))
	private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
		if (window == CLIENT.getWindow().getHandle() && CLIENT.player != null && CLIENT.world != null &&
				action == InputUtil.GLFW_PRESS &&
				(!(CLIENT.currentScreen instanceof KeybindsScreen keybindsScreen) ||
						(keybindsScreen.lastKeyCodeUpdateTime <= Util.getMeasuringTimeMs() - 20L)) &&
				Screenshot_utilitiesClient.getPanoramaScreenshotKeybind().matchesKey(input)) {

			CLIENT.player.sendMessage(CLIENT.takePanorama(CLIENT.runDirectory), false);

		}
	}
}
