package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

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

	// Disable attack in PhotoMode
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
		if (PhotoMode.isEnabled()) {
			cir.cancel();
		}
	}

	// Disable pick block or entity in PhotoMode
	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
	private void onPickBlockOrEntity(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	// Disable block breaking in PhotoMode
	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void onContinueAttack(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	// Disable PhotoMode on disconnect
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "HEAD"))
	private void onDisconnect(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			PhotoMode.disable();
		}
	}

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void onHandleKeybinds(CallbackInfo ci) {
		Screenshot_utilitiesClient.handleKeybindsHook();
	}
}
