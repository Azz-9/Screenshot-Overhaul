package me.Azz_9.screenshot_overhaul.client.panorama;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public record PanoramaCaptureContext(@NonNull String panoramaId, @NonNull AtomicInteger pendingFaces,
									 @NonNull File panoramaFolder) {
	public PanoramaCaptureContext(File panoramaFolder) {
		this(UUID.randomUUID().toString(), new AtomicInteger(6), panoramaFolder);
	}

}
