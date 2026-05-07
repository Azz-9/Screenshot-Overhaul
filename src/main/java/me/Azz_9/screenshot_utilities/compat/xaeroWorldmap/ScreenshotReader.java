package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap;

import net.minecraft.client.Minecraft;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderLocation;

public class ScreenshotReader extends ElementReader<Screenshot, ScreenshotRenderContext, ScreenshotRenderer> {

	private static final int HALF_W = ScreenshotRenderer.THUMB_W / 2;
	private static final int HALF_H = ScreenshotRenderer.THUMB_H / 2;

	@Override
	public double getRenderX(Screenshot screenshot, ScreenshotRenderContext ctx, float pt) {
		return screenshot.metadata().x() + 0.5;
	}

	@Override
	public double getRenderZ(Screenshot screenshot, ScreenshotRenderContext ctx, float pt) {
		return screenshot.metadata().z() + 0.5;
	}

	@Override
	public boolean isHidden(Screenshot screenshot, ScreenshotRenderContext ctx) {
		return false;
	}

	@Override
	public int getRenderBoxLeft(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return -HALF_W;
	}

	@Override
	public int getRenderBoxRight(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return HALF_W;
	}

	@Override
	public int getRenderBoxTop(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return -HALF_H;
	}

	@Override
	public int getRenderBoxBottom(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return HALF_H;
	}

	@Override
	public int getInteractionBoxLeft(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return -HALF_W;
	}

	@Override
	public int getInteractionBoxRight(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return HALF_W;
	}

	@Override
	public int getInteractionBoxTop(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return -HALF_H;
	}

	@Override
	public int getInteractionBoxBottom(Screenshot screenshot, ScreenshotRenderContext c, float pt) {
		return HALF_H;
	}

	@Override
	public boolean shouldScaleBoxWithOptionalScale() {
		return true;
	}

	// Méthodes menu/tooltip — pas utilisées pour l'instant
	@Override
	public int getLeftSideLength(Screenshot screenshot, Minecraft mc) {
		return 0;
	}

	@Override
	public String getMenuName(Screenshot screenshot) {
		return screenshot.file().getName();
	}

	@Override
	public String getFilterName(Screenshot screenshot) {
		return screenshot.file().getName();
	}

	@Override
	public int getMenuTextFillLeftPadding(Screenshot screenshot) {
		return 0;
	}

	@Override
	public int getRightClickTitleBackgroundColor(Screenshot screenshot) {
		return Colors.DARK_GRAY;
	}

	@Override
	public boolean isInteractable(ElementRenderLocation location, Screenshot screenshot) {
		return true;
	}
}
