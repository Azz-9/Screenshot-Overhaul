package me.Azz_9.screenshot_utilities.client.screenshot.panorama;

public record PanoramaThumbnailRenderState(
		int x, int y, int width, int height,
		float pitch, float yaw,
		PanoramaCubeMap cubeMap
) {
}