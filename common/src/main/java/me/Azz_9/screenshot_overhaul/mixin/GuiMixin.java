package me.Azz_9.screenshot_overhaul.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.CommonClass;
import me.Azz_9.screenshot_overhaul.client.screenshot.FutureScreenshotState;

@Mixin(Gui.class)
public abstract class GuiMixin {

	// On hud render
	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (FutureScreenshotState.suppressHud || CommonClass.hudRenderHook(graphics, deltaTracker)) {
			ci.cancel();
		}
	}

	@Inject(method = "extractChat", at = @At("HEAD"), cancellable = true)
	private void onExtractChat(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (FutureScreenshotState.suppressChat) {
			ci.cancel();
		}
	}
}
