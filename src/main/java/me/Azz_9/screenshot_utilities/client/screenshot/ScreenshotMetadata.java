package me.Azz_9.screenshot_utilities.client.screenshot;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record ScreenshotMetadata(Integer x, Integer y, Integer z, String dimension, String biome, String worldName,
                                 String server, Long timestamp, @NonNull List<String> tags) {

	public static ScreenshotMetadata empty() {
		return new ScreenshotMetadata(null, null, null,
				null, null, null, null, null, new ArrayList<>());
	}
}