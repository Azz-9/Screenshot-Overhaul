package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.accessors.network.ClientPacketListenerAccessor;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotGrabber;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements ClientPacketListenerAccessor {

	@Shadow
	protected abstract void setClientLoaded(boolean loaded);

	@Inject(method = "handleRespawn", at = @At("HEAD"))
	private void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			PhotoMode.disable();
		}
	}

	@Inject(method = "handleUpdateAdvancementsPacket", at = @At("HEAD"))
	private void onHandleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
		if (Config.getInstance().grabScreenshotOnAdvancement.getValue() && !packet.getAdded().isEmpty()) {
			ScreenshotGrabber.requestGrab(
					Config.getInstance().getAbsoluteScreenshotsDir().toFile(),
					null,
					MINECRAFT.getMainRenderTarget(),
					1,
					message -> MINECRAFT.execute(() -> {
						MINECRAFT.gui.getChat().addClientSystemMessage(message);
						MINECRAFT.getNarrator().saySystemQueued(message);
					})
			);
		}
	}

	@Override
	public void screenshotUtilities$setClientLoaded(boolean loaded) {
		this.setClientLoaded(loaded);
	}
}
