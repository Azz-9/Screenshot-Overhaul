package me.Azz_9.screenshot_utilities.compat.journeyMap;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.minecraft.core.BlockPos;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.ImageOverlay;
import journeymap.api.v2.client.model.MapImage;
import journeymap.api.v2.common.JourneyMapPlugin;
import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.screenshot.*;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneyMapIntegration implements IClientPlugin {

	private static IClientAPI api;

	@Override
	public String getModId() {
		return MOD_ID;
	}

	@Override
	public void initialize(IClientAPI clientAPI) {
		api = clientAPI;

		if (!Config.getInstance().showScreenshotsOnJourneyMap.getValue()) return;

		ScreenshotList.whenScreenshotsLoaded(screenshots -> {
			for (Screenshot screenshot : screenshots) {
				ScreenshotMetadata metadata = screenshot.getMetadata();
				if (ScreenshotManager.isHiddenFromMap(screenshot.pathRelativeToScreenshotDir()) || metadata.getX() == null || metadata.getY() == null || metadata.getZ() == null)
					continue;

				ScreenshotTextureCache.getSmallThumbnail(screenshot.file().toPath()).whenReady((nativeImage, throwable) -> {
					try {
						api.show(
								new ImageOverlay(
										getModId(),
										new BlockPos(metadata.getX(), metadata.getY(), metadata.getZ()),
										new BlockPos(metadata.getX(), metadata.getY(), metadata.getZ()),
										new MapImage(nativeImage)
								)
						);
					} catch (Exception e) {
						ScreenshotLogger.error("Couldn't add screenshots on journey map : {}", e.getMessage());
					}
				});
			}
		});
	}

	public static IClientAPI getAPI() {
		return api;
	}
}