package me.Azz_9.screenshot_overhaul.compat;

import net.irisshaders.iris.Iris;

import org.jspecify.annotations.NonNull;

public class IrisCompat {

	public static @NonNull String getShaderName() {
		if (CompatManager.irisPresent() && !Iris.getCurrentPackName().equals("(off)")) {
			int lastDotIndex = Iris.getCurrentPackName().lastIndexOf('.');
			if (lastDotIndex == -1) return Iris.getCurrentPackName();
			return Iris.getCurrentPackName().substring(0, lastDotIndex);
		}
		return "";
	}
}
