package me.Azz_9.screenshot_overhaul.client.renderState;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.texture.PanoramaCubeMap;

public record PanoramaThumbnailRenderState(
		int x, int y, int width, int height,
		float pitch, float yaw,
		@NonNull PanoramaCubeMap cubeMap
) {
}