package me.Azz_9.screenshot_utilities.compat;

import net.irisshaders.iris.Iris;

import org.jspecify.annotations.NonNull;

public class IrisCompat {

	public static @NonNull String getShaderName() {
		if (CompatManager.irisPresent()) {
			int lastDotIndex = Iris.getCurrentPackName().lastIndexOf('.');
			return Iris.getCurrentPackName().substring(lastDotIndex + 1);
		}
		return "";
	}
}
