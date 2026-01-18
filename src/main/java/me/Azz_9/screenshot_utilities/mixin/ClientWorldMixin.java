package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

	@Shadow
	public abstract void tickEntity(Entity entity);

	// visual freeze of the world
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void tick(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "tickEntities", at = @At("HEAD"), cancellable = true)
	private void tickEntities(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			if (PhotoMode.getCamera() != null) {
				this.tickEntity(PhotoMode.getCamera());
			}
			ci.cancel();
		}
	}
}