package me.Azz_9.screenshot_overhaul.client.panorama;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public record PanoramaFaceContext(
		@NonNull String panoramaId,
		@NonNull AtomicInteger pendingFaces,
		@NonNull File panoramaFolder,
		int faceIndex
) {
	public PanoramaFaceContext(PanoramaCaptureContext ctx, int faceIndex) {
		this(ctx.panoramaId, ctx.pendingFaces, ctx.panoramaFolder, faceIndex);
	}
}