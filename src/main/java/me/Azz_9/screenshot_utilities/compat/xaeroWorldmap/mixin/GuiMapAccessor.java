package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public interface GuiMapAccessor {
	@Accessor("cameraX")
	double getCameraX();

	@Accessor("cameraZ")
	double getCameraZ();

	@Accessor("scale")
	double getScale();

	@Accessor("screenScale")
	double getScreenScale();
}