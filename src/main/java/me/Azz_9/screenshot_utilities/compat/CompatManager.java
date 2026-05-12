package me.Azz_9.screenshot_utilities.compat;

import net.fabricmc.loader.api.FabricLoader;

public class CompatManager {
	public static boolean irisPresent() {
		return FabricLoader.getInstance().isModLoaded("iris");
	}

	public static boolean xaerosWorldMapPresent() {
		return FabricLoader.getInstance().isModLoaded("xaeroworldmap");
	}
}
