package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ScreenshotDrawHelper {

	public static void drawCover(
			DrawContext ctx,
			Identifier textureId,
			int imgW, int imgH,
			int x, int y,
			int boxW, int boxH
	) {
		float imgRatio = imgW / (float) imgH;
		float boxRatio = boxW / (float) boxH;

		int u = 0, v = 0;
		int regionW = imgW;
		int regionH = imgH;

		if (imgRatio > boxRatio) {
			regionW = (int) (imgH * boxRatio);
			u = (imgW - regionW) / 2;
		} else {
			regionH = (int) (imgW / boxRatio);
			v = (imgH - regionH) / 2;
		}

		ctx.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				textureId,
				x, y,
				u, v,
				boxW, boxH,
				regionW, regionH,
				imgW, imgH
		);
	}
}