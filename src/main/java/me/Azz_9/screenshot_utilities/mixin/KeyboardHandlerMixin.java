package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotGrabber;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Inject(method = "keyPress", at = @At("HEAD"))
	private void onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
		if (handle == MINECRAFT.getWindow().handle() && MINECRAFT.player != null && MINECRAFT.level != null &&
				action == InputConstants.PRESS &&
				(!(MINECRAFT.gui.screen() instanceof KeyBindsScreen keybindsScreen) ||
						(keybindsScreen.lastKeySelection <= Util.getMillis() - 20L)) &&
				Screenshot_utilitiesClient.getPanoramaScreenshotKeybind().matches(event)) {

			Component text = ScreenshotGrabber.grabPanoramixScreenshot(Config.getInstance().getAbsoluteScreenshotsDir().toFile());
			if (Config.getInstance().showChatMessage.getValue()) {
				MINECRAFT.player.sendSystemMessage(text);
			}
		}
	}
}
