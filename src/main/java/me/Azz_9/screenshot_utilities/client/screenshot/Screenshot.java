package me.Azz_9.screenshot_utilities.client.screenshot;

import org.jspecify.annotations.NonNull;

import java.io.File;

import me.Azz_9.screenshot_utilities.client.config.Config;

public class Screenshot {
	private final @NonNull File file;
	private final @NonNull String pathRelativeToScreenshotDir;
	private @NonNull ScreenshotMetadata metadata;

	public Screenshot(@NonNull File file, @NonNull ScreenshotMetadata metadata, @NonNull String pathRelativeToScreenshotDir) {
		this.file = file;
		this.pathRelativeToScreenshotDir = pathRelativeToScreenshotDir;
		this.metadata = metadata;
	}

	public Screenshot(@NonNull File file, @NonNull ScreenshotMetadata metadata) {
		this(file, metadata, Config.getInstance().getAbsoluteScreenshotsDir().relativize(file.toPath()).toString());
	}

	public @NonNull File file() {
		return file;
	}

	public @NonNull ScreenshotMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(@NonNull ScreenshotMetadata metadata) {
		this.metadata = metadata;
	}

	public @NonNull String pathRelativeToScreenshotDir() {
		return pathRelativeToScreenshotDir;
	}
}
