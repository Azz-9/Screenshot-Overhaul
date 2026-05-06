package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.compat.xaeroWorldmap.XaeroThumbnailRenderer;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin {
	@Inject(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V",
					ordinal = 0  // le premier endBatch après mapElementRenderHandler
			)
	)
	private void onAfterElementsRender(
			GuiGraphicsExtractor guiGraphics,
			int scaledMouseX, int scaledMouseY,
			float partialTicks,
			CallbackInfo ci
	) {
		if (Config.getInstance().showScreenshotsOnXaerosWorldMap.getValue()) {
			GuiMapAccessor self = (GuiMapAccessor) this;
			XaeroThumbnailRenderer.render(
					guiGraphics,
					(GuiMap) (Object) this,
					self.getCameraX(),
					self.getCameraZ(),
					self.getScale(),
					self.getScreenScale()
			);
		}
	}
}
