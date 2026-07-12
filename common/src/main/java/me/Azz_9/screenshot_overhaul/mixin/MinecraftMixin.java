package me.Azz_9.screenshot_overhaul.mixin;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

import me.Azz_9.screenshot_overhaul.CommonClass;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.panorama.PanoramaCaptureContext;
import me.Azz_9.screenshot_overhaul.client.panorama.PanoramaFaceContext;
import me.Azz_9.screenshot_overhaul.client.panorama.ScreenshotContext;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotFileNameParser;

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

	// panorama

	@Expression("4096")
	@ModifyExpressionValue(method = "grabPanoramixScreenshot", at = @At("MIXINEXTRAS:EXPRESSION"))
	private int changeResolution(int height, @Local(name = "downscaleFactor") int downscaleFactor) {
		return Config.getInstance().panoramaResolution.getValue() * downscaleFactor;
	}

	@Inject(method = "grabPanoramixScreenshot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;enablePanoramicMode()V"))
	private void setPanoramaContext(File folder, CallbackInfoReturnable<Component> cir) {
		ScreenshotContext.PANORAMA.set(new PanoramaCaptureContext(screenshot_overhaul$getPanoramaFolder(folder)));
	}

	@Inject(
			method = "grabPanoramixScreenshot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"
			)
	)
	private void setCurrentFace(File folder, CallbackInfoReturnable<Component> cir, @Local(name = "i", type = int.class) int i) {
		PanoramaCaptureContext ctx = ScreenshotContext.PANORAMA.get();

		if (ctx != null) {
			ScreenshotContext.PANORAMA_FACE.set(new PanoramaFaceContext(ctx, i));
		}
	}

	@Unique
	private static @NonNull File screenshot_overhaul$getPanoramaFolder(final @NonNull File folder) {
		String pattern = Config.getInstance().panoramaFolderName.getValue();
		return ScreenshotFileNameParser.resolveDirectory(folder.toPath(), pattern).toFile();
	}
}
