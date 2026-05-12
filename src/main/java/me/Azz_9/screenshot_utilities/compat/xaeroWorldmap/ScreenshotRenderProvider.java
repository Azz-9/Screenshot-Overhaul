package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.Objects;

import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import xaero.map.WorldMapSession;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.world.MapWorld;

public class ScreenshotRenderProvider extends ElementRenderProvider<Screenshot, ScreenshotRenderContext> {

	private Iterator<Screenshot> iterator;

	@Override
	public void begin(ElementRenderLocation location, ScreenshotRenderContext ctx) {
		MapWorld mapWorld = WorldMapSession.getCurrentSession().getMapProcessor().getMapWorld();
		ResourceKey<Level> currentDim = mapWorld.getCurrentDimension().getDimId();
		String currentWorld = mapWorld.getMapProcessor().getCurrentWorldId();

		iterator = ScreenshotList.getScreenshots().stream()
				.filter(s -> Objects.equals(s.getMetadata().getWorldName(), currentWorld) &&
						Objects.equals(s.getMetadata().getDimension(), currentDim.identifier()))
				.iterator();
	}

	@Override
	public boolean hasNext(ElementRenderLocation location, ScreenshotRenderContext ctx) {
		return iterator != null && iterator.hasNext();
	}

	@Override
	public Screenshot getNext(ElementRenderLocation location, ScreenshotRenderContext ctx) {
		return iterator.next();
	}

	@Override
	public void end(ElementRenderLocation location, ScreenshotRenderContext ctx) {
		iterator = null;
	}
}