package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotGrabber;

@Environment(EnvType.CLIENT)
@Mixin(ClientAdvancements.class)
public abstract class ClientAdvancementsMixin {

	@Inject(method = "update", at = @At("HEAD"))
	private void update(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
		if (Config.getInstance().grabScreenshotOnAdvancement.getValue() && !packet.getAdded().isEmpty()) {
			Screenshot_utilitiesClient.runLater(() ->
					ScreenshotGrabber.requestGrab(
							Config.getInstance().getAbsoluteScreenshotsDir().toFile(),
							null,
							MINECRAFT.getMainRenderTarget(),
							1,
							message -> MINECRAFT.execute(() -> {
								MINECRAFT.gui.getChat().addClientSystemMessage(message);
								MINECRAFT.getNarrator().saySystemQueued(message);
							})
					), Config.getInstance().advancementScreenshotDelay.getValue());
		}
	}
}
