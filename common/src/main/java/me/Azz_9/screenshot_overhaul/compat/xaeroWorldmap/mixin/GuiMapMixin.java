package me.Azz_9.screenshot_overhaul.compat.xaeroWorldmap.mixin;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.cache.ScreenshotTextureCache;
import me.Azz_9.screenshot_overhaul.client.gui.Loading;
import me.Azz_9.screenshot_overhaul.client.gui.ScreenshotDrawHelper;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;
import me.Azz_9.screenshot_overhaul.client.texture.ScreenshotTexture;
import xaero.lib.client.gui.ScreenBase;
import xaero.map.element.HoveredMapElementHolder;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin extends ScreenBase {
	@Unique
	private static final int SCREENSHOT_PADDING = 40;
	@Unique
	private @Nullable Screenshot screenshot_overhaul$selectedScreenshot = null;
	@Unique
	private static final int DONE_BUTTON_WIDTH = 200;
	@Unique
	private static final int DONE_BUTTON_HEIGHT = 20;
	@Unique
	private static final int DONE_BUTTON_BOTTOM_MARGIN = (SCREENSHOT_PADDING - DONE_BUTTON_HEIGHT) / 2;
	@Unique
	private Button screenshot_overhaul$doneButton;
	@Unique
	private int screenshot_overhaul$mouseX, screenshot_overhaul$mouseY;

	protected GuiMapMixin(Screen parent, Screen escape, Component titleIn) {
		super(parent, escape, titleIn);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		screenshot_overhaul$doneButton = Button.builder(Component.translatable("gui.done"), button -> screenshot_overhaul$deselectScreenshot())
				.bounds((this.width - DONE_BUTTON_WIDTH) / 2,
						this.height - DONE_BUTTON_BOTTOM_MARGIN - DONE_BUTTON_HEIGHT,
						DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT)
				.build();
	}

	@Inject(method = "mapClicked", at = @At("HEAD"))
	private void onMapClicked(int button, int x, int y, CallbackInfo ci) {
		if (button != 0) return;

		GuiMap self = (GuiMap) (Object) this;
		HoveredMapElementHolder<?, ?> viewed = ((GuiMapAccessor) self).getViewed();
		if (viewed == null) return;

		Object element = viewed.getElement();
		if (!(element instanceof Screenshot screenshot)) return;

		screenshot_overhaul$selectScreenshot(screenshot);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void onExtractRenderState(GuiGraphicsExtractor guiGraphics, int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
		if (screenshot_overhaul$selectedScreenshot == null) {
			// hover cursor
			HoveredMapElementHolder<?, ?> viewed = ((GuiMapAccessor) this).getViewed();
			if (viewed != null && viewed.getElement() instanceof Screenshot)
				guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
			return;
		}

		guiGraphics.requestCursor(CursorTypes.ARROW);

		// overlay
		guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), Colors.BLACK_TRANSPARENT);

		ScreenshotTexture texture = ScreenshotTextureCache.getFullView(screenshot_overhaul$selectedScreenshot.file().toPath());
		int boxW = guiGraphics.guiWidth() - SCREENSHOT_PADDING * 2;
		int boxH = guiGraphics.guiHeight() - SCREENSHOT_PADDING * 2;
		int centerX = guiGraphics.guiWidth() / 2;
		int centerY = guiGraphics.guiHeight() / 2;

		if (texture == null || !texture.isReady()) {
			guiGraphics.fill(
					SCREENSHOT_PADDING, SCREENSHOT_PADDING,
					guiGraphics.guiWidth() - SCREENSHOT_PADDING,
					guiGraphics.guiHeight() - SCREENSHOT_PADDING,
					Colors.BLACK_TRANSPARENT
			);
			Loading.drawLoadingSpinner(
					guiGraphics,
					centerX, centerY,
					boxH / 20, boxH / 10);
		} else {
			ScreenshotDrawHelper.drawContainCenter(
					guiGraphics,
					ScreenshotTextureCache.getFullView(screenshot_overhaul$selectedScreenshot.file().toPath()),
					centerX, centerY,
					boxW, boxH,
					Colors.WHITE
			);
		}

		screenshot_overhaul$doneButton.extractRenderState(guiGraphics, screenshot_overhaul$mouseX, screenshot_overhaul$mouseY, partialTicks);
	}

	@Unique
	private void screenshot_overhaul$selectScreenshot(Screenshot screenshot) {
		screenshot_overhaul$selectedScreenshot = screenshot;
		screenshot_overhaul$doneButton.active = true;
	}

	@Unique
	private void screenshot_overhaul$deselectScreenshot() {
		screenshot_overhaul$selectedScreenshot = null;
		screenshot_overhaul$doneButton.active = false;
	}

	@ModifyVariable(method = "extractRenderState", at = @At("HEAD"), argsOnly = true, name = "scaledMouseX")
	private int modifyScaledMouseX(int scaledMouseX) {
		screenshot_overhaul$mouseX = scaledMouseX;
		if (screenshot_overhaul$selectedScreenshot != null) return -1;
		return scaledMouseX;
	}

	@ModifyVariable(method = "extractRenderState", at = @At("HEAD"), argsOnly = true, name = "scaledMouseY")
	private int modifyScaledMouseY(int scaledMouseY) {
		screenshot_overhaul$mouseY = scaledMouseY;
		if (screenshot_overhaul$selectedScreenshot != null) return -1;
		return scaledMouseY;
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (screenshot_overhaul$selectedScreenshot != null) {
			cir.setReturnValue(screenshot_overhaul$doneButton.mouseClicked(event, doubleClick));
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (screenshot_overhaul$selectedScreenshot != null)
			cir.setReturnValue(false);
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void mouseScrolled(double par1, double par2, double g, double wheel, CallbackInfoReturnable<Boolean> cir) {
		if (screenshot_overhaul$selectedScreenshot != null)
			cir.setReturnValue(false);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (screenshot_overhaul$selectedScreenshot != null) {
			if (event.isEscape()) {
				screenshot_overhaul$deselectScreenshot();
				cir.setReturnValue(true);
			} else {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
	private void keyReleased(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (screenshot_overhaul$selectedScreenshot != null)
			cir.setReturnValue(false);
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void charTyped(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (screenshot_overhaul$selectedScreenshot != null)
			cir.setReturnValue(false);
	}
}