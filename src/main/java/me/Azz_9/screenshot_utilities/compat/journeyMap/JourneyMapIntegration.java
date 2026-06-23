package me.Azz_9.screenshot_utilities.compat.journeyMap;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.awt.geom.Point2D;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.Context;
import journeymap.api.v2.client.display.IOverlayListener;
import journeymap.api.v2.client.display.MarkerOverlay;
import journeymap.api.v2.client.display.Overlay;
import journeymap.api.v2.client.fullscreen.ModPopupMenu;
import journeymap.api.v2.client.model.MapImage;
import journeymap.api.v2.client.util.UIState;
import journeymap.api.v2.common.JourneyMapPlugin;
import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.screenshot.*;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneyMapIntegration implements IClientPlugin {

	private static IClientAPI api;
	private static final String GROUP_NAME = "screenshots";
	private static final int SCREENSHOTS_WIDTH = 128;

	@Override
	public String getModId() {
		return MOD_ID;
	}

	@Override
	public void initialize(IClientAPI clientAPI) {
		api = clientAPI;
	}

	public static void init() {
		if (!Config.getInstance().showScreenshotsOnJourneyMap.getValue()) return;

		ScreenshotList.whenScreenshotsLoaded(screenshots -> {
			for (Screenshot screenshot : screenshots) {
				ScreenshotMetadata metadata = screenshot.getMetadata();
				if (ScreenshotManager.isHiddenFromMap(screenshot.pathRelativeToScreenshotDir()) || metadata.getX() == null || metadata.getY() == null || metadata.getZ() == null || metadata.getDimension() == null)
					continue;

				ScreenshotTextureCache.getSmallThumbnail(screenshot.file().toPath()).whenReady((nativeImage, throwable) -> {
					if (throwable != null || nativeImage == null) return;

					BlockPos pos = new BlockPos(metadata.getX(), metadata.getY(), metadata.getZ());

					MapImage icon = new MapImage(nativeImage)
							.centerAnchors()
							.setDisplayWidth(SCREENSHOTS_WIDTH)
							.setDisplayHeight((double) nativeImage.getHeight() * SCREENSHOTS_WIDTH / nativeImage.getWidth());

					Overlay marker = new MarkerOverlay(MOD_ID, pos, icon)
							.setDimension(ResourceKey.create(Registries.DIMENSION, metadata.getDimension()))
							.setTitle(screenshot.file().getName())
							.setOverlayGroupName(GROUP_NAME)
							.setActiveUIs(Context.UI.Fullscreen);
					marker.setOverlayListener(new IOverlayListener() {
								@Override
								public void onOverlayMenuPopup(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition, ModPopupMenu modPopupMenu) {
									modPopupMenu.addMenuItem(
											"screenshot_utilities.copy",
											_ -> CopyScreenshot.copyToClipboard(screenshot.file())
									);

									modPopupMenu.addMenuItem(
											"screenshot_utilities.hide_from_worldmap",
											_ -> {
												ScreenshotManager.setHiddenFromMap(screenshot.pathRelativeToScreenshotDir(), true);
												try {
													api.remove(marker);
												} catch (Exception e) {
													ScreenshotLogger.error("Couldn't remove marker: {}", e.getMessage());
												}
											}
									);
								}
							});

					MINECRAFT.execute(() -> {
						try {
							api.show(marker);
						} catch (Exception e) {
							ScreenshotLogger.error("Couldn't add screenshot marker on JourneyMap: {}", e.getMessage());
						}
					});
				});
			}
		});
	}

	public static IClientAPI getAPI() {
		return api;
	}
}