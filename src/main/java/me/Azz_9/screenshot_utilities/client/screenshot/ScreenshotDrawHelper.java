package me.Azz_9.screenshot_utilities.client.screenshot;

import me.Azz_9.screenshot_utilities.client.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class ScreenshotDrawHelper {

	public static void drawCover(@NonNull DrawContext context, @NonNull ScreenshotTexture texture, int x, int y, int boxW, int boxH) {
		drawCover(context, texture, x, y, boxW, boxH, Colors.WHITE);
	}

	public static void drawCover(@NonNull DrawContext context, @NonNull ScreenshotTexture texture, int x, int y, int boxW, int boxH, int color) {
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

		NativeImageBackedTexture backedTexture = texture.getTexture();

		if (backedTexture == null) {
			return;
		}

		context.state.addSimpleElement(
				new TexturedQuadGuiElementRenderState(
						RenderPipelines.GUI_TEXTURED,
						TextureSetup.of(backedTexture.getGlTextureView(), backedTexture.getSampler()),
						new Matrix3x2f(context.getMatrices()),
						x, y, x + boxW, y + boxH,
						(float) u / texture.width(), (float) (u + regionW) / texture.width(),
						(float) v / texture.height(), (float) (v + regionH) / texture.height(),
						color,
						context.scissorStack.peekLast()
				)
		);
	}
}