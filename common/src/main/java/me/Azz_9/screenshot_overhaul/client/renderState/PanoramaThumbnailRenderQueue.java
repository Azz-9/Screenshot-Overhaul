package me.Azz_9.screenshot_overhaul.client.renderState;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PanoramaThumbnailRenderQueue {
	private static final @NonNull List<PanoramaThumbnailRenderState> QUEUE = new ArrayList<>();

	public static void enqueue(@NonNull PanoramaThumbnailRenderState state) {
		QUEUE.add(state);
	}

	public static @NonNull List<PanoramaThumbnailRenderState> drain() {
		List<PanoramaThumbnailRenderState> copy = List.copyOf(QUEUE);
		QUEUE.clear();
		return copy;
	}
}
