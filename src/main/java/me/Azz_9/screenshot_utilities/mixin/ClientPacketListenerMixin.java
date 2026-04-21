package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.accessors.network.ClientPacketListenerAccessor;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

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

	@Override
	public void screenshotUtilities$setClientLoaded(boolean loaded) {
		this.setClientLoaded(loaded);
	}
}
