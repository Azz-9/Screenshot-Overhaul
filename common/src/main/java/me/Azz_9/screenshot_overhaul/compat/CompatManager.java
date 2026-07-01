package me.Azz_9.screenshot_overhaul.compat;

import me.Azz_9.screenshot_overhaul.platform.Services;

public class CompatManager {
	public static boolean irisPresent() {
		return Services.PLATFORM.isModLoaded("iris");
	}

	public static boolean xaerosWorldMapPresent() {
		return Services.PLATFORM.isModLoaded("xaeroworldmap");
	}

	public static boolean journeyMapPresent() {
		return Services.PLATFORM.isModLoaded("journeymap");
	}
}
