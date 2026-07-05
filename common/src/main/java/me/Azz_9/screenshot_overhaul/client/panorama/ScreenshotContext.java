package me.Azz_9.screenshot_overhaul.client.panorama;

public class ScreenshotContext {
	public static boolean capturingPanorama = false;

	public static final ThreadLocal<PanoramaCaptureContext> PANORAMA = new ThreadLocal<>();
	public static final ThreadLocal<PanoramaFaceContext> PANORAMA_FACE = new ThreadLocal<>();
}
