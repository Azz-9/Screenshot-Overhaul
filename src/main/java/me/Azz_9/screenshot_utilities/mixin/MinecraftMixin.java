package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
		if (PhotoMode.isEnabled()) {
			cir.cancel();
		}
	}

	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
	private void onPickBlockOrEntity(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void onHandleBlockBreaking(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "HEAD"))
	private void onDisconnect(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			PhotoMode.disable();
		}
	}

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void onHandleKeybinds(CallbackInfo ci) {
		while (Screenshot_utilitiesClient.getOpenPhotoModeKeybind().consumeClick()) {
			PhotoMode.toggle();
		}

		if (PhotoMode.isEnabled() && PhotoMode.getCamera() != null) {
			while (Screenshot_utilitiesClient.getRollLeftKeybind().consumeClick()) {
				PhotoMode.getCamera().rollLeft();
			}

			while (Screenshot_utilitiesClient.getRollRightKeybind().consumeClick()) {
				PhotoMode.getCamera().rollRight();
			}
		}
	}

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void onSetScreen(Screen screen, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && !(screen instanceof PauseScreen) && !(screen instanceof ChatScreen) && screen != null) {
			ci.cancel();
		}
	}
}
