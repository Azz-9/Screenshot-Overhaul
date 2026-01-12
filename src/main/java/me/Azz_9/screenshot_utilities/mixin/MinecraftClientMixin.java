package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
		if (PhotoMode.isEnabled()) {
			cir.cancel();
		}
	}

	@Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
	private void onDoItemPick(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void onHandleBlockBreaking(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;ZZ)V", at = @At(value = "HEAD"))
	private void onDisconnect(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			PhotoMode.disable();
		}
	}

	@Inject(method = "handleInputEvents", at = @At("HEAD"))
	private void onHandleInputEvents(CallbackInfo ci) {
		while (Screenshot_utilitiesClient.getOpenPhotoModeKeybind().wasPressed()) {
			PhotoMode.toggle();
		}

		if (PhotoMode.isEnabled() && PhotoMode.getCamera() != null) {
			while (Screenshot_utilitiesClient.getRollLeftKeybind().wasPressed()) {
				PhotoMode.getCamera().rollLeft();
			}

			while (Screenshot_utilitiesClient.getRollRightKeybind().wasPressed()) {
				PhotoMode.getCamera().rollRight();
			}
		}
	}

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void onSetScreen(Screen screen, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && !(screen instanceof GameMenuScreen) && screen != null) {
			ci.cancel();
		}
	}
}
