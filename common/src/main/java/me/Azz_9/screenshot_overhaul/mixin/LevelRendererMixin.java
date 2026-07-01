package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Shadow protected abstract boolean shouldShowEntityOutlines();

	@Shadow protected abstract EntityRenderState extractEntity(Entity entity, float partialTickTime);

	// Freeze tick delta in PhotoMode
	@ModifyVariable(method = "extractLevel", at = @At("HEAD"), argsOnly = true, name = "deltaTracker")
	private static DeltaTracker freezeTickDelta(DeltaTracker deltaTracker) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			return DeltaTracker.ZERO;
		}

		return deltaTracker;
	}

	// Freeze tick delta in PhotoMode
	@ModifyVariable(method = "extractLevel", at = @At("HEAD"), argsOnly = true, name = "deltaPartialTick")
	private static float freezeTickDelta(float deltaPartialTick) {
		if (PhotoMode.isEnabled() && Config.getInstance().freezeInPhotoMode.getValue()) {
			return 0;
		}

		return deltaPartialTick;
	}

	// Makes the player render if showPlayer is enabled.
	@Inject(method = "extractVisibleEntities", at = @At(value = "RETURN"))
	private void onExtractVisibleEntities(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState output, CallbackInfo ci) {
		if (Config.getInstance().showPlayer.getValue() && PhotoMode.isEnabled() && MINECRAFT.level != null && MINECRAFT.player != null) {
			Entity player = MINECRAFT.player;
			TickRateManager tickRateManager = MINECRAFT.level.tickRateManager();
			boolean shouldShowEntityOutlines = this.shouldShowEntityOutlines();
			float partialEntity = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(player));
			EntityRenderState entityRenderState = this.extractEntity(player, partialEntity);
			output.entityRenderStates.add(entityRenderState);
			if (entityRenderState.appearsGlowing() && shouldShowEntityOutlines) {
				output.haveGlowingEntities = true;
			}
		}
	}
}
