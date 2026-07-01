package me.Azz_9.screenshot_overhaul.client.screenshot.panorama;

import org.jspecify.annotations.NonNull;

public record PanoramaThumbnailRenderState(
		int x, int y, int width, int height,
		float pitch, float yaw,
		@NonNull PanoramaCubeMap cubeMap
) {
}