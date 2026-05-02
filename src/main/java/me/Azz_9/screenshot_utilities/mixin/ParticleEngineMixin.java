package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

	// freeze particle when PhotoMode is enabled
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void tick(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "add", at = @At("HEAD"), cancellable = true)
	private void add(Particle p, CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}
}
