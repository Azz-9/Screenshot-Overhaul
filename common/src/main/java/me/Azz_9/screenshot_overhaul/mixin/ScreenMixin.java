package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.ScreenshotPreviewWidget;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotPreview;

@Mixin(Screen.class)
public abstract class ScreenMixin {

	// Add screenshot preview on init if 
	@Inject(method = "init(II)V", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		Screen self = (Screen) (Object) this;

		// Add preview if it's already visible
		if (ScreenshotPreview.isVisible()) {
			screenshot_overhaul$addPreviewWidget(self);
		}

		// set a listener to add the preview when a screenshot is taken
		ScreenshotPreview.setOnScreenshotSet(() -> MINECRAFT.execute(() -> screenshot_overhaul$addPreviewWidget(self)));
	}

	@Inject(method = "onClose", at = @At("HEAD"))
	private void onClose(CallbackInfo ci) {
		// clean listener when the screen is closed
		ScreenshotPreview.setOnScreenshotSet(null);

		ScreenshotPreview.resume();
		ScreenshotPreview.setHovered(false);
	}

	@Unique
	private static void screenshot_overhaul$addPreviewWidget(Screen screen) {
		int screenW = MINECRAFT.getWindow().getGuiScaledWidth();
		int screenH = MINECRAFT.getWindow().getGuiScaledHeight();
		((ScreenAccessor) screen).invokeAddRenderableWidget(new ScreenshotPreviewWidget(screenW, screenH));
	}
}
