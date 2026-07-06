package me.Azz_9.screenshot_overhaul.mixin;


import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.multiplayer.ClientAdvancements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import me.Azz_9.screenshot_overhaul.CommonClass;
import me.Azz_9.screenshot_overhaul.client.config.Config;

@Mixin(ClientAdvancements.class)
public abstract class ClientAdvancementsMixin {

	@WrapOperation(
			method = "update",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/toasts/ToastManager;addToast(Lnet/minecraft/client/gui/components/toasts/Toast;)V"
			)
	)
	private void update(ToastManager instance, Toast toast, Operation<Void> original) {
		if (Config.getInstance().grabScreenshotOnAdvancement.getValue()) {
			CommonClass.runLater(() -> Screenshot.grab(
					MINECRAFT.gameDirectory,
					MINECRAFT.gameRenderer.mainRenderTarget(),
					message -> MINECRAFT.showDebugChat(message)
			), Config.getInstance().advancementScreenshotDelay.getValue());
		}
	}
}
