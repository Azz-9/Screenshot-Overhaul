package me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PanoramaThumbnailRenderQueue {
	private static final List<PanoramaThumbnailRenderState> QUEUE = new ArrayList<>();

	public static void enqueue(PanoramaThumbnailRenderState state) {
		QUEUE.add(state);
	}

	public static List<PanoramaThumbnailRenderState> drain() {
		List<PanoramaThumbnailRenderState> copy = List.copyOf(QUEUE);
		QUEUE.clear();
		return copy;
	}
}
