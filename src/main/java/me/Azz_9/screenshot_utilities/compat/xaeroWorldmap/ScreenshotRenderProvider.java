package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Iterator;

import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import xaero.map.WorldMapSession;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.world.MapDimension;

public class ScreenshotRenderProvider extends ElementRenderProvider<Screenshot, ScreenshotRenderContext> {

	private Iterator<Screenshot> iterator;

	@Override
	public void begin(ElementRenderLocation location, ScreenshotRenderContext ctx) {
		MapDimension dim = WorldMapSession.getCurrentSession().getMapProcessor().getMapWorld().getCurrentDimension();
		ResourceKey<Level> currentDimId = dim.getDimId();
		String currentMultiworld = dim.getCurrentMultiworld();

		iterator = ScreenshotList.getScreenshots().stream()
				.filter(s -> s.metadata().dimension().equals(currentDimId.toString()) &&
						s.metadata().worldName().equals(currentMultiworld))
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