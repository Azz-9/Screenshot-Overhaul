package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {

	@Shadow protected abstract boolean shouldShowEntityOutlines(Camera camera);

	@Shadow protected abstract EntityRenderState extractEntity(Entity entity, float partialTickTime);

	// Makes the player render if showPlayer is enabled.
	@Inject(method = "extractVisibleEntities", at = @At(value = "RETURN"))
	private void onExtractVisibleEntities(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState output, CallbackInfo ci) {
		if (Config.getInstance().showPlayer.getValue() && PhotoMode.isEnabled() && MINECRAFT.level != null && MINECRAFT.player != null) {
			Entity player = MINECRAFT.player;
			TickRateManager tickRateManager = MINECRAFT.level.tickRateManager();
			boolean shouldShowEntityOutlines = this.shouldShowEntityOutlines(camera);
			float partialEntity = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(player));
			EntityRenderState entityRenderState = this.extractEntity(player, partialEntity);
			output.entityRenderStates.add(entityRenderState);
			if (entityRenderState.appearsGlowing() && shouldShowEntityOutlines) {
				output.shouldShowEntityOutlines = true;
			}
		}
	}
}
