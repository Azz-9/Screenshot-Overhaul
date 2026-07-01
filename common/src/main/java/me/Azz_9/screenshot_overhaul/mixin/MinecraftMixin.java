package me.Azz_9.screenshot_overhaul.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.CommonClass;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotGrabber;

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

	// Disable opening container screens in PhotoMode
	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void onSetScreen(Screen screen, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && screen instanceof AbstractContainerScreen<?>) {
			ci.cancel();
		}
	}

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void onHandleKeybinds(CallbackInfo ci) {
		CommonClass.handleKeybindsHook();
	}
}
