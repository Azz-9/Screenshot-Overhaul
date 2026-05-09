package me.Azz_9.screenshot_utilities.client.screenshot;

import org.jspecify.annotations.NonNull;

import java.io.File;

import me.Azz_9.screenshot_utilities.client.config.Config;

public class Screenshot {
	private final @NonNull File file;
	private final @NonNull ScreenshotMetadata metadata;
	private final @NonNull String pathRelativeToScreenshotDir;
	private boolean dirty;

	public Screenshot(@NonNull File file, @NonNull ScreenshotMetadata metadata, @NonNull String pathRelativeToScreenshotDir) {
		this.file = file;
		this.metadata = metadata;
		this.pathRelativeToScreenshotDir = pathRelativeToScreenshotDir;
		this.metadata.setOnUpdate(() -> dirty = true);
	}

	public Screenshot(@NonNull File file, @NonNull ScreenshotMetadata metadata) {
		this(file, metadata, Config.getInstance().getScreenshotsDir().relativize(file.toPath()).toString());
	}

	public @NonNull File file() {
		return file;
	}

	public @NonNull ScreenshotMetadata metadata() {
		return metadata;
	}

	public @NonNull String pathRelativeToScreenshotDir() {
		return pathRelativeToScreenshotDir;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void markSaved() {
		dirty = false;
	}
}
