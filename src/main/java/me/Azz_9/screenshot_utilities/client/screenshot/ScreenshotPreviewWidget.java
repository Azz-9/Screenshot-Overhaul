package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.Colors;

public class ScreenshotPreviewWidget extends AbstractWidget {

	private static final Identifier ICON_COPY = Identifier.fromNamespaceAndPath(MOD_ID, "icon/copy");
	private static final Identifier ICON_DELETE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/delete");

	public ScreenshotPreviewWidget(int screenW, int screenH) {
		super(
				ScreenshotPreview.getX(screenW),
				ScreenshotPreview.getY(screenH),
				ScreenshotPreview.PREVIEW_WIDTH,
				ScreenshotPreview.getPreviewHeight(),
				Component.empty()
		);
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (!ScreenshotPreview.isVisible()) {
			this.visible = false;
			return;
		}

		if (ScreenshotPreview.isSlidingOut()) {
			this.active = false;
		}

		int screenW = MINECRAFT.getWindow().getGuiScaledWidth();
		int screenH = MINECRAFT.getWindow().getGuiScaledHeight();

		// Sync position avec le HUD (le slide continue pendant que le chat est ouvert)
		this.setX(ScreenshotPreview.getX(screenW));
		this.setY(ScreenshotPreview.getY(screenH));

		// Pause/resume selon hover
		if (isHovered() && active) {
			ScreenshotPreview.pause();
			ScreenshotPreview.setHovered(true);
		} else {
			ScreenshotPreview.resume();
			ScreenshotPreview.setHovered(false);
		}

		// Délègue le rendu au HUD
		ScreenshotPreview.render(graphics, mouseX, mouseY, delta);

		float hoverProgress = ScreenshotPreview.getHoverProgress();
		if (hoverProgress > 0f) {
			renderButtons(graphics, mouseX, mouseY, hoverProgress);
		}
	}

	private void renderButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float hoverProgress) {
		int halfW = this.width / 2;

		// Fond semi-transparent fadein
		int overlayAlpha = (int) (0x88 * hoverProgress);
		graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height,
				(overlayAlpha << 24));

		// Bouton Copier
		int copyX = this.getX();
		boolean hoverCopy = isHovered() && mouseX >= copyX && mouseX < copyX + halfW
				&& mouseY >= this.getY() && mouseY < this.getY() + this.height;
		if (hoverCopy) {
			graphics.fill(copyX, this.getY(), copyX + halfW, this.getY() + this.height,
					(int) (0x44 * hoverProgress) << 24 | 0x00FFFFFF);
		}
		renderIcon(graphics, ICON_COPY, copyX, this.getY(), halfW, this.height, hoverProgress);
		renderLabel(graphics, copyX, this.getY(), halfW, this.height, Component.translatable("screenshot_utilities.preview.copy"), hoverProgress);

		// Bouton Supprimer
		int deleteX = this.getX() + halfW;
		boolean hoverDelete = isHovered() && mouseX >= deleteX && mouseX < deleteX + halfW
				&& mouseY >= this.getY() && mouseY < this.getY() + this.height;
		if (hoverDelete) {
			graphics.fill(deleteX, this.getY(), deleteX + halfW, this.getY() + this.height,
					(int) (0x44 * hoverProgress) << 24 | 0x00FF4444);
		}
		renderIcon(graphics, ICON_DELETE, deleteX, this.getY(), halfW, this.height, hoverProgress);
		renderLabel(graphics, deleteX, this.getY(), halfW, this.height, Component.translatable("screenshot_utilities.preview.delete"), hoverProgress);

		if (hoverCopy || hoverDelete) {
			graphics.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	private void renderIcon(GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int areaW, int areaH, float alpha) {
		int iconSize = Math.min(areaW, areaH) / 3;
		int iconX = x + (areaW - iconSize) / 2;
		int iconY = y + (areaH - iconSize) / 3;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, iconSize, iconSize, ARGB.color(alpha, Colors.WHITE));
	}

	private void renderLabel(GuiGraphicsExtractor graphics, int x, int y, int areaW, int areaH, Component text, float alpha) {
		int iconSize = Math.min(areaW, areaH) / 3;
		graphics.centeredText(MINECRAFT.font, text, x + areaW / 2, y + (areaH + iconSize) / 2, ARGB.color(alpha, Colors.WHITE));
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubleClick) {
		int halfW = this.width / 2;
		int previewH = this.height - ScreenshotPreview.BAR_HEIGHT - 2;

		if (click.y() < this.getY() || click.y() >= this.getY() + previewH) return;

		if (click.x() >= this.getX() && click.x() < this.getX() + halfW) {
			ScreenshotPreview.copyCurrentScreenshot();
		} else if (click.x() >= this.getX() + halfW && click.x() < this.getX() + this.width) {
			ScreenshotPreview.deleteCurrentScreenshot();
		}
		ScreenshotPreview.dismiss();

	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}
}
