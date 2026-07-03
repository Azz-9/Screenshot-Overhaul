package me.Azz_9.screenshot_overhaul.client.screenshot;

import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_overhaul.client.config.Config;

public class FutureScreenshotState {
	public static volatile boolean suppressHud = false;
	public static volatile boolean suppressChat = false;
	public static volatile boolean suppressHand = false;
	public static volatile boolean captureRequested = false;

	public static volatile @Nullable Runnable pendingCapture = null;

	public static @Nullable Runnable reset() {
		suppressHud = false;
		suppressChat = false;
		suppressHand = false;
		captureRequested = false;
		Runnable grabber = pendingCapture;
		pendingCapture = null;

		return grabber;
	}

	public static boolean shouldRequestScreenshot() {
		return Config.getInstance().hideHudOnScreenshot.getValue()
				|| Config.getInstance().hideChatOnScreenshot.getValue()
				|| Config.getInstance().hideHandOnScreenshot.getValue();
	}
}
