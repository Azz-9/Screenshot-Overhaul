package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.util.Mth;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record ScreenshotMetadata(Integer x, Integer y, Integer z, String dimension, String worldName,
                                 String server, Long timestamp, @NonNull List<String> tags) {

	public static ScreenshotMetadata collectMetadata() {
		if (MINECRAFT.player == null || MINECRAFT.level == null) return null;

		String server = null;
		String worldName = null;

		if (MINECRAFT.getCurrentServer() != null) {
			server = MINECRAFT.getCurrentServer().ip;
		} else if (MINECRAFT.getSingleplayerServer() != null) {
			worldName = MINECRAFT.getSingleplayerServer().getWorldData().getLevelName();
		}

		return new ScreenshotMetadata(
				Mth.floor(MINECRAFT.player.getX()),
				Mth.floor(MINECRAFT.player.getY()),
				Mth.floor(MINECRAFT.player.getZ()),
				MINECRAFT.level.dimension().identifier().toString(),
				worldName,
				server,
				System.currentTimeMillis(),
				new ArrayList<>()
		);
	}
}