package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotPreview;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotPreviewWidget;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public abstract class ScreenMixin {

	@Inject(method = "init(II)V", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		Screen self = (Screen) (Object) this;

		// Si déjà visible au moment d'ouvrir le chat
		if (ScreenshotPreview.isVisible()) {
			addPreviewWidget(self);
		}

		// Sinon on attend l'apparition
		ScreenshotPreview.setOnScreenshotSet(() -> MINECRAFT.execute(() -> addPreviewWidget(self)));
	}

	@Inject(method = "onClose", at = @At("HEAD"))
	private void onClose(CallbackInfo ci) {
		// Nettoyage du listener quand le screen se ferme
		ScreenshotPreview.setOnScreenshotSet(null);

		ScreenshotPreview.resume();
		ScreenshotPreview.setHovered(false);
	}

	@Unique
	private static void addPreviewWidget(Screen screen) {
		int screenW = MINECRAFT.getWindow().getGuiScaledWidth();
		int screenH = MINECRAFT.getWindow().getGuiScaledHeight();
		((ScreenAccessor) screen).invokeAddRenderableWidget(new ScreenshotPreviewWidget(screenW, screenH));
	}
}
