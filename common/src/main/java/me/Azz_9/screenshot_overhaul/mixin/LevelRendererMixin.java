package me.Azz_9.screenshot_overhaul.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	// Freeze tick delta in PhotoMode
	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, name = "deltaTracker")
	private static DeltaTracker freezeTickDelta(DeltaTracker deltaTracker) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			return DeltaTracker.ZERO;
		}

		return deltaTracker;
	}
}
