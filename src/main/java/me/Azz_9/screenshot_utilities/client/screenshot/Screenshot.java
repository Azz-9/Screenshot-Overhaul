package me.Azz_9.screenshot_utilities.client.screenshot;

import org.jspecify.annotations.NonNull;

import java.io.File;

import me.Azz_9.screenshot_utilities.client.config.Config;

public record Screenshot(@NonNull File file, @NonNull ScreenshotMetadata metadata,
                         @NonNull String pathRelativeToScreenshotDir) {

	public Screenshot(@NonNull File file, @NonNull ScreenshotMetadata metadata) {
		this(file, metadata, Config.getInstance().getScreenshotsDir().relativize(file.toPath()).toString());
	}
}
