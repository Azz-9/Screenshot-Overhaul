package me.Azz_9.screenshot_overhaul.client.panorama;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PanoramaCaptureContext {
	public final @NonNull String panoramaId;
	public final @NonNull AtomicInteger pendingFaces;
	public final @NonNull File panoramaFolder;

	public PanoramaCaptureContext(File panoramaFolder) {
		this(UUID.randomUUID().toString(), new AtomicInteger(6), panoramaFolder);
	}

	public PanoramaCaptureContext(@NonNull String panoramaId, @NonNull AtomicInteger pendingFaces, @NonNull File panoramaFolder) {
		this.panoramaId = panoramaId;
		this.pendingFaces = pendingFaces;
		this.panoramaFolder = panoramaFolder;
	}

	public PanoramaCaptureContext copy() {
		return new PanoramaCaptureContext(this.panoramaId, this.pendingFaces, this.panoramaFolder);
	}
}
