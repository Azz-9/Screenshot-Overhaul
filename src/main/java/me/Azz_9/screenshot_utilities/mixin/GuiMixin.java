package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class GuiMixin {

	// Disable opening container screens in PhotoMode
	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void onSetScreen(Screen screen, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && screen instanceof AbstractContainerScreen<?>) {
			ci.cancel();
		}
	}
}
