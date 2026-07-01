package me.Azz_9.screenshot_overhaul.compat.xaeroWorldmap.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_overhaul.compat.xaeroWorldmap.ScreenshotRenderer;
import xaero.map.element.MapElementRenderHandler;

@Mixin(value = MapElementRenderHandler.Builder.class, remap = false)
public abstract class MapElementRenderHandlerBuilderMixin {

	@Inject(method = "build", at = @At("RETURN"))
	private void onBuild(CallbackInfoReturnable<MapElementRenderHandler> cir) {
		cir.getReturnValue().add(
				new ScreenshotRenderer()
		);
	}
}
