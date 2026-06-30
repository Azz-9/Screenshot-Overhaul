package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

	// Freeze tick delta in PhotoMode
	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, name = "deltaTracker")
	private static DeltaTracker freezeTickDelta(DeltaTracker deltaTracker) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			return DeltaTracker.ZERO;
		}

		return deltaTracker;
	}

	// Freeze tick delta in PhotoMode
	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, name = "deltaPartialTick")
	private static float freezeTickDelta(float deltaPartialTick) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			return 0;
		}

		return deltaPartialTick;
	}
}
