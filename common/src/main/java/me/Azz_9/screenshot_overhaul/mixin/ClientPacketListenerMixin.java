package me.Azz_9.screenshot_overhaul.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.photoMode.PacketBuffer;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

	@Inject(method = "handleRespawn", at = @At("HEAD"))
	private void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			PhotoMode.disable();
		}
	}

	@Inject(method = "handleBlockUpdate", at = @At("HEAD"), cancellable = true)
	private void disableBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			PacketBuffer.addPacket(packet);
			ci.cancel();
		}
	}

	// disable block event
	@Inject(method = "handleBlockEvent", at = @At("HEAD"), cancellable = true)
	private void disableBlockEvent(ClientboundBlockEventPacket packet, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			// this is event for block animation like chest opening and closing so we don't need to buffer this
			ci.cancel();
		}
	}

	@Inject(method = "handleBlockDestruction", at = @At("HEAD"), cancellable = true)
	private void disableBlockDestruction(ClientboundBlockDestructionPacket packet, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			PacketBuffer.addPacket(packet);
			ci.cancel();
		}
	}

	@Inject(method = "handleChunkBlocksUpdate", at = @At("HEAD"), cancellable = true)
	private void disableChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			PacketBuffer.addPacket(packet);
			ci.cancel();
		}
	}
}
