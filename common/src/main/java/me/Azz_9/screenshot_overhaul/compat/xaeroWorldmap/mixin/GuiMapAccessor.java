package me.Azz_9.screenshot_overhaul.compat.xaeroWorldmap.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import xaero.map.element.HoveredMapElementHolder;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public interface GuiMapAccessor {

	@Accessor("viewed")
	HoveredMapElementHolder<?, ?> getViewed();
	// ... cameraX, cameraZ, scale, screenScale
}