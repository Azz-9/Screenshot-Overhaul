package me.Azz_9.screenshot_overhaul.client.screenshot;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.mixin.GuiGraphicsExtractorAccessor;
import me.Azz_9.screenshot_overhaul.platform.Services;

public final class ScreenshotDrawHelper {

	public static void drawCover(@NonNull final GuiGraphicsExtractor graphics, @NonNull final ScreenshotTexture texture, int x, int y, int boxW, int boxH) {
		drawCover(graphics, texture, x, y, boxW, boxH, Colors.WHITE);
	}

	public static void drawCover(@NonNull final GuiGraphicsExtractor graphics, @NonNull final ScreenshotTexture texture, int x, int y, int boxW, int boxH, int color) {
		float imgRatio = texture.width() / (float) texture.height();
		float boxRatio = boxW / (float) boxH;

		int u = 0, v = 0;
		int regionW = texture.width();
		int regionH = texture.height();

		if (imgRatio > boxRatio) {
			regionW = (int) (texture.height() * boxRatio);
			u = (texture.width() - regionW) / 2;
		} else {
			regionH = (int) (texture.width() / boxRatio);
			v = (texture.height() - regionH) / 2;
		}

		DynamicTexture backedTexture = texture.getTexture();

		if (backedTexture == null) return;

		((GuiGraphicsExtractorAccessor) graphics).guiRenderState().addGuiElement(
				new BlitRenderState(
						RenderPipelines.GUI_TEXTURED,
						TextureSetup.singleTexture(backedTexture.getTextureView(), backedTexture.getSampler()),
						new Matrix3x2f(graphics.pose()),
						x, y, x + boxW, y + boxH,
						(float) u / texture.width(), (float) (u + regionW) / texture.width(),
						(float) v / texture.height(), (float) (v + regionH) / texture.height(),
						color,
						Services.PLATFORM.scissorStackPeek(graphics)
				)
		);
	}

	public static void drawContain(@NonNull final GuiGraphicsExtractor graphics, @NonNull final ScreenshotTexture texture, int x, int y, int boxW, int boxH) {
		drawContain(graphics, texture, x, y, boxW, boxH, Colors.WHITE);
	}

	public static void drawContain(@NonNull final GuiGraphicsExtractor graphics, @NonNull final ScreenshotTexture texture, int x, int y, int boxW, int boxH, int color) {
		float imgRatio = texture.width() / (float) texture.height();
		float boxRatio = boxW / (float) boxH;

		int drawW, drawH;

		if (imgRatio > boxRatio) {
			// Image plus large que la box → on limite par la largeur
			drawW = boxW;
			drawH = Math.round(boxW / imgRatio);
		} else {
			// Image plus haute que la box → on limite par la hauteur
			drawH = boxH;
			drawW = Math.round(boxH * imgRatio);
		}

		// Centrage dans la box
		int drawX = x + (boxW - drawW) / 2;
		int drawY = y + (boxH - drawH) / 2;

		DynamicTexture backedTexture = texture.getTexture();
		if (backedTexture == null) return;

		((GuiGraphicsExtractorAccessor) graphics).guiRenderState().addGuiElement(
				new BlitRenderState(
						RenderPipelines.GUI_TEXTURED,
						TextureSetup.singleTexture(backedTexture.getTextureView(), backedTexture.getSampler()),
						new Matrix3x2f(graphics.pose()),
						drawX, drawY, drawX + drawW, drawY + drawH,
						0.0f, 1.0f,   // U
						0.0f, 1.0f,   // V
						color,
						Services.PLATFORM.scissorStackPeek(graphics)
				)
		);
	}

	public static void drawContainCenter(@NonNull final GuiGraphicsExtractor graphics, @NonNull final ScreenshotTexture texture, int x, int y, int boxW, int boxH, int color) {
		drawContain(graphics, texture, x - boxW / 2, y - boxH / 2, boxW, boxH, color);
	}

	// Draw en conservant le ratio exact de l'image
	public static void drawFit(@NonNull final GuiGraphicsExtractor graphics, @NonNull final ScreenshotTexture texture, int x, int y, int w, int h) {
		DynamicTexture backedTexture = texture.getTexture();
		if (backedTexture == null) return;

		((GuiGraphicsExtractorAccessor) graphics).guiRenderState().addGuiElement(
				new BlitRenderState(
						RenderPipelines.GUI_TEXTURED,
						TextureSetup.singleTexture(backedTexture.getTextureView(), backedTexture.getSampler()),
						new Matrix3x2f(graphics.pose()),
						x, y, x + w, y + h,
						0.0f, 1.0f,
						0.0f, 1.0f,
						Colors.WHITE,
						Services.PLATFORM.scissorStackPeek(graphics)
				)
		);
	}
}