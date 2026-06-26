package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.multiplayer.ClientAdvancements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotGrabber;

@Environment(EnvType.CLIENT)
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
			Screenshot_utilitiesClient.runLater(() ->
					ScreenshotGrabber.requestGrab(
							Config.getInstance().getAbsoluteScreenshotsDir().toFile(),
							null,
							MINECRAFT.gameRenderer.mainRenderTarget(),
							1,
							message -> MINECRAFT.execute(() -> {
								MINECRAFT.gui.hud.getChat().addClientSystemMessage(message);
								MINECRAFT.getNarrator().saySystemQueued(message);
							})
					), Config.getInstance().advancementScreenshotDelay.getValue());
		}
	}
}
