package me.Azz_9.screenshot_overhaul.compat.xaeroWorldmap.mixin;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import me.Azz_9.screenshot_overhaul.platform.Services;

public class XaeroWorldmapMixinPlugin implements IMixinConfigPlugin {

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return Services.PLATFORM.isModLoaded("xaeroworldmap");
	}
}
