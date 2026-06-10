package me.Azz_9.screenshot_utilities.client.screenshot;

import org.jspecify.annotations.Nullable;

public class ScreenshotRenderState {
	public static volatile boolean suppressHud = false;
	public static volatile boolean suppressChat = false;
	public static volatile boolean suppressHand = false;
	public static volatile boolean captureRequested = false;

	public static volatile @Nullable Runnable pendingCapture = null;

	public static Runnable reset() {
		suppressHud = false;
		suppressChat = false;
		suppressHand = false;
		captureRequested = false;
		Runnable grabber = pendingCapture;
		pendingCapture = null;

		return grabber;
	}
}
