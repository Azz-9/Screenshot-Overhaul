package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.config.Config.RotationDirection.TO_RIGHT;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.CubeMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaThumbnailRenderQueue;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaThumbnailRenderState;

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

	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/CubeMap;render(FF)V"
			)
	)
	private void wrapCubeMapRender(CubeMap instance, float rotXInDegrees, float rotYInDegrees, Operation<Void> original) {
		original.call(
				instance,
				(float) Config.getInstance().verticalAngle.getValue(),
				Config.getInstance().startingHorizontalAngle.getValue() + rotYInDegrees *
						Config.getInstance().rotationSpeed.getValue() *
						(Config.getInstance().rotationDirection.getValue() == TO_RIGHT ? -0.1f : 0.1f)
		);
	}
}
