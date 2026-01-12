package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@ModifyVariable(
			method = "render",
			at = @At("HEAD"),
			argsOnly = true,
			index = 2
	)
	private static RenderTickCounter freezeTickDelta(RenderTickCounter tickCounter) {
		if (PhotoMode.isEnabled()) {
			return RenderTickCounter.ZERO;
		}

		return tickCounter;
	}

	@Shadow
	protected abstract EntityRenderState getAndUpdateRenderState(Entity entity, float tickProgress);

	@Shadow
	protected abstract boolean canDrawEntityOutlines();

	// Makes the player render if showPlayer is enabled.
	@Inject(method = "fillEntityRenderStates", at = @At(value = "RETURN"))
	private void onFillEntityRenderStates(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates, CallbackInfo ci) {
		if (CLIENT.world != null && PhotoMode.isEnabled()) {
			Entity player = CLIENT.player;
			TickManager tickManager = CLIENT.world.getTickManager();
			boolean bl = this.canDrawEntityOutlines();
			float g = tickCounter.getTickProgress(!tickManager.shouldSkipTick(player));
			EntityRenderState entityRenderState = this.getAndUpdateRenderState(player, g);
			renderStates.entityRenderStates.add(entityRenderState);
			if (entityRenderState.hasOutline() && bl) {
				renderStates.hasOutline = true;
			}
		}
	}
}
