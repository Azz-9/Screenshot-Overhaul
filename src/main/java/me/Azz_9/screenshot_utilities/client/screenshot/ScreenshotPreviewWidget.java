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
import net.minecraft.util.Ease;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.Colors;

public class ScreenshotPreviewWidget extends AbstractWidget {

	private static final Identifier ICON_COPY = Identifier.fromNamespaceAndPath(MOD_ID, "icon/copy");
	private static final Identifier ICON_DELETE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/delete");

	private static final int BUTTON_HOVER_DURATION = 150;
	private float copyHoverProgress = 0f;
	private float deleteHoverProgress = 0f;
	private long copyHoverStart = -1;
	private long deleteHoverStart = -1;
	private boolean lastHoverCopy = false;
	private boolean lastHoverDelete = false;

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
			updateButtonHovers(mouseX, mouseY);
			renderButtons(graphics, mouseX, mouseY, hoverProgress);
		} else {
			// Reset quand l'overlay disparait
			copyHoverProgress = 0f;
			deleteHoverProgress = 0f;
			copyHoverStart = -1;
			deleteHoverStart = -1;
			lastHoverCopy = false;
			lastHoverDelete = false;
		}
	}

	private void updateButtonHovers(int mouseX, int mouseY) {
		int halfW = this.width / 2;
		int previewH = this.height - ScreenshotPreview.BAR_HEIGHT - 2;
		long now = System.currentTimeMillis();

		boolean hoverCopy = mouseX >= this.getX() && mouseX < this.getX() + halfW
				&& mouseY >= this.getY() && mouseY < this.getY() + previewH;
		boolean hoverDelete = mouseX >= this.getX() + halfW && mouseX < this.getX() + this.width
				&& mouseY >= this.getY() && mouseY < this.getY() + previewH;

		// Copy
		if (hoverCopy != lastHoverCopy) {
			copyHoverStart = now;
			lastHoverCopy = hoverCopy;
		}
		if (copyHoverStart != -1) {
			float t = Math.min(1f, (now - copyHoverStart) / (float) BUTTON_HOVER_DURATION);
			copyHoverProgress = hoverCopy ? Ease.outQuad(t) : 1f - Ease.outQuad(t);
			if (t >= 1f) copyHoverStart = -1;
		}

		// Delete
		if (hoverDelete != lastHoverDelete) {
			deleteHoverStart = now;
			lastHoverDelete = hoverDelete;
		}
		if (deleteHoverStart != -1) {
			float t = Math.min(1f, (now - deleteHoverStart) / (float) BUTTON_HOVER_DURATION);
			deleteHoverProgress = hoverDelete ? Ease.outQuad(t) : 1f - Ease.outQuad(t);
			if (t >= 1f) deleteHoverStart = -1;
		}
	}

	private void renderButtons(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float hoverProgress) {
		int halfW = width / 2;

		// Fond semi-transparent fadein
		int overlayAlpha = (int) (0x88 * hoverProgress);
		graphics.fill(getX(), getY(), getX() + width, getY() + height, (overlayAlpha << 24));

		// Bouton Copier
		if (copyHoverProgress > 0) {
			int copyAlpha = (int) (0x44 * copyHoverProgress * hoverProgress);
			graphics.fill(getX(), getY(), getX() + halfW, getY() + height, ARGB.color(copyAlpha, Colors.WHITE));
		}
		renderIcon(graphics, ICON_COPY, getX(), getY(), halfW, height, hoverProgress);
		renderLabel(graphics, getX(), getY(), halfW, height, Component.translatable("screenshot_utilities.copy"), hoverProgress);

		// Bouton Supprimer
		int deleteX = getX() + halfW;
		if (deleteHoverProgress > 0) {
			int deleteAlpha = (int) (0x44 * deleteHoverProgress * hoverProgress);
			graphics.fill(deleteX, getY(), deleteX + halfW, getY() + height, ARGB.color(deleteAlpha, Colors.RED));
		}
		renderIcon(graphics, ICON_DELETE, deleteX, getY(), halfW, height, hoverProgress);
		renderLabel(graphics, deleteX, getY(), halfW, height, Component.translatable("screenshot_utilities.delete"), hoverProgress);

		if (copyHoverProgress > 0 || deleteHoverProgress > 0) {
			graphics.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	private void renderIcon(@NonNull GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int areaW, int areaH, float alpha) {
		int iconSize = Math.min(areaW, areaH) / 3;
		int iconX = x + (areaW - iconSize) / 2;
		int iconY = y + (areaH - iconSize) / 3;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, iconSize, iconSize, ARGB.color(alpha, Colors.WHITE));
	}

	private void renderLabel(@NonNull GuiGraphicsExtractor graphics, int x, int y, int areaW, int areaH, Component text, float alpha) {
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
