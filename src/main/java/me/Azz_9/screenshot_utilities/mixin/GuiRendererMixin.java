package me.Azz_9.screenshot_utilities.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.render.GuiRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content.PanoramaThumbnailRenderQueue;
import me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content.PanoramaThumbnailRenderState;

@Environment(EnvType.CLIENT)
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

	@Inject(method = "render", at = @At("HEAD"))
	private void onRenderHead(GpuBufferSlice fogBuffer, CallbackInfo ci) {
		for (PanoramaThumbnailRenderState state : PanoramaThumbnailRenderQueue.drain()) {
			state.cubeMap().renderToArea(
					state.x(), state.y(),
					state.width(), state.height(),
					state.pitch(), state.yaw()
			);
		}
	}
}
