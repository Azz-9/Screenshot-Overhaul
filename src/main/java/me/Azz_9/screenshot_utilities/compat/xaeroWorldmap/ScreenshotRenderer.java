package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;

import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;
import xaero.map.WorldMap;
import xaero.map.element.MapElementGraphics;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.CustomRenderTypes;
import xaero.map.graphics.MapRenderHelper;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.gui.GuiMap;

public class ScreenshotRenderer extends ElementRenderer<Screenshot, ScreenshotRenderContext, ScreenshotRenderer> {

	private static final double THUMBNAIL_ZOOM_THRESHOLD = 1.5;
	public static final int THUMB_W = 48;
	public static final int THUMB_H = 27;

	public ScreenshotRenderer() {
		super(new ScreenshotRenderContext(), new ScreenshotRenderProvider(), new ScreenshotReader());
	}

	@Override
	public void preRender(ElementRenderInfo info, MultiBufferSource.BufferSource vanillaBuffers,
	                      MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {
		context.textureRenderer = rendererProvider.getRenderer(CustomRenderTypes.GUI_BILINEAR_PRE);
		context.userScale = MINECRAFT.screen instanceof GuiMap guiMap ? guiMap.getUserScale() : 1.0;
	}

	@Override
	public void postRender(ElementRenderInfo info, MultiBufferSource.BufferSource vanillaBuffers,
	                       MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {
		rendererProvider.draw(context.textureRenderer);
	}

	@Override
	public void renderElementShadow(Screenshot screenshot, boolean hovered, float optionalScale,
	                                double partialX, double partialY, ElementRenderInfo info,
	                                MapElementGraphics guiGraphics,
	                                MultiBufferSource.BufferSource vanillaBuffers,
	                                MultiTextureRenderTypeRendererProvider rendererProvider) {
		// pas d'ombre
	}

	@Override
	public boolean renderElement(Screenshot screenshot, boolean hovered, double optionalDepth,
	                             float optionalScale, double partialX, double partialY,
	                             ElementRenderInfo info, MapElementGraphics guiGraphics,
	                             MultiBufferSource.BufferSource vanillaBuffers,
	                             MultiTextureRenderTypeRendererProvider rendererProvider) {
		PoseStack matrixStack = guiGraphics.pose();

		matrixStack.translate(partialX, partialY, 0.0);
		matrixStack.scale(optionalScale, optionalScale, 1.0f);

		if (info.scale >= THUMBNAIL_ZOOM_THRESHOLD) {
			DynamicTexture dynamicTexture = ScreenshotTextureCache.getSmallThumbnail(screenshot.file().toPath()).getTexture();
			if (dynamicTexture != null) {
				GpuTextureView textureView = dynamicTexture.getTextureView();
				// signature : (matrix, renderer, x, y, u, v, w, h, r, g, b, a, texW, texH, texture)
				MapRenderHelper.blitIntoMultiTextureRenderer(
						matrixStack.last().pose(),
						context.textureRenderer,
						-THUMB_W / 2.0f, -THUMB_H / 2.0f,  // position centrée
						0, 0,                          // u, v (coin haut-gauche de la texture)
						THUMB_W, THUMB_H,              // taille affichée = taille texture
						1f, 1f, 1f, hovered ? 1f : 0.85f,
						THUMB_W, THUMB_H,              // taille réelle de la texture thumbnail
						textureView
				);
			}
		} else {
			// Petit point doré — réutilise le dot de GuiMap (UV 0,69 taille 5x5 dans guiTextures)
			GpuTextureView guiTex = MINECRAFT.getTextureManager().getTexture(WorldMap.guiTextures).getTextureView();
			MapRenderHelper.blitIntoMultiTextureRenderer(
					matrixStack.last().pose(),
					context.textureRenderer,
					-3, -3,
					0, 69,
					5, 5,
					1f, 0.85f, 0f, hovered ? 1f : 0.75f, // teinte dorée
					256, 256,
					guiTex
			);
		}

		return true;
	}

	@Override
	public boolean shouldRender(ElementRenderLocation location, boolean shadow) {
		return !shadow;
	}

	@Override
	public boolean shouldBeDimScaled() {
		return false;
	}

	@Override
	public int getOrder() {
		return 150; // before waypoints (200)
	}
}