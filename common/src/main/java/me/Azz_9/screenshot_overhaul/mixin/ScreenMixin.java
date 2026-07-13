package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.ScreenshotPreviewWidget;
import me.Azz_9.screenshot_overhaul.client.preview.ScreenshotPreview;

@Mixin(Screen.class)
public abstract class ScreenMixin {

	@Shadow
	@Final
	private List<GuiEventListener> children;
	@Shadow
	@Final
	private List<NarratableEntry> narratables;
	@Unique
	private @Nullable ScreenshotPreviewWidget screenshot_overhaul$previewWidget;

	// Add screenshot preview on init if
	@Inject(method = "init(II)V", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		// Add preview if it's already visible
		if (ScreenshotPreview.isVisible()) {
			screenshot_overhaul$addPreviewWidget();
		}

		// set a listener to add the preview when a screenshot is taken
		ScreenshotPreview.setOnScreenshotSet(() -> MINECRAFT.execute(this::screenshot_overhaul$addPreviewWidget));
	}

	@Inject(method = "onClose", at = @At("HEAD"))
	private void onClose(CallbackInfo ci) {
		// clean listener when the screen is closed
		ScreenshotPreview.setOnScreenshotSet(null);

		ScreenshotPreview.resume();
		ScreenshotPreview.setHovered(false);
	}

	@Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
	private void extractPreviewRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (screenshot_overhaul$previewWidget != null) {
			screenshot_overhaul$previewWidget.extractRenderState(graphics, mouseX, mouseY, a);
		}
	}

	@Unique
	private void screenshot_overhaul$addPreviewWidget() {
		int screenW = MINECRAFT.getWindow().getGuiScaledWidth();
		int screenH = MINECRAFT.getWindow().getGuiScaledHeight();
		this.screenshot_overhaul$previewWidget = new ScreenshotPreviewWidget(screenW, screenH);
		// the preview is added at the start of the list so the click events are sent to this widget first
		this.children.addFirst(this.screenshot_overhaul$previewWidget);
		this.narratables.addFirst(this.screenshot_overhaul$previewWidget);
	}
}
