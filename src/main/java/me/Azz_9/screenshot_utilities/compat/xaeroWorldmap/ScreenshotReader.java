package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.screenshot.CopyScreenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotManager;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

public class ScreenshotReader extends ElementReader<Screenshot, ScreenshotRenderContext, ScreenshotRenderer> {

	private static final int HALF_W = ScreenshotRenderer.THUMB_W / 2;
	private static final int HALF_H = ScreenshotRenderer.THUMB_H / 2;

	@Override
	public double getRenderX(Screenshot screenshot, ScreenshotRenderContext ctx, float pt) {
		if (screenshot.metadata().getX() == null) return 0;
		return screenshot.metadata().getX() + 0.5;
	}

	@Override
	public double getRenderZ(Screenshot screenshot, ScreenshotRenderContext ctx, float pt) {
		if (screenshot.metadata().getZ() == null) return 0;
		return screenshot.metadata().getZ() + 0.5;
	}

	@Override
	public boolean isHidden(Screenshot screenshot, ScreenshotRenderContext ctx) {
		return ScreenshotManager.isHiddenFromMap(screenshot.pathRelativeToScreenshotDir());
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

	@Override
	public ArrayList<RightClickOption> getRightClickOptions(Screenshot screenshot, IRightClickableElement target) {
		ArrayList<RightClickOption> options = new ArrayList<>();

		options.add(new RightClickOption(Component.translatable("screenshot_utilities.copy").getString(), 0, target) {
			@Override
			public void onAction(Screen screen) {
				CopyScreenshot.copyToClipboard(screenshot.file());
			}
		});

		options.add(new RightClickOption(Component.translatable("screenshot_utilities.hide_from_worldmap").getString(), 1, target) {
			@Override
			public void onAction(Screen screen) {
				ScreenshotManager.setHiddenFromMap(screenshot.pathRelativeToScreenshotDir(), true);
			}
		});

		return options;
	}

	@Override
	public boolean isRightClickValid(Screenshot element) {
		return true;
	}
}
