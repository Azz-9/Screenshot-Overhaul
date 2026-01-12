package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.api.network.ClientPlayNetworkHandlerAccessor;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayNetworkHandlerAccessor {

	@Shadow
	protected abstract void setLoaded(boolean loaded);

	@Inject(method = "onPlayerRespawn", at = @At("HEAD"))
	private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			PhotoMode.disable();
		}
	}

	@Override
	public void screenshotUtilities$setLoaded(boolean loaded) {
		this.setLoaded(loaded);
	}
}
