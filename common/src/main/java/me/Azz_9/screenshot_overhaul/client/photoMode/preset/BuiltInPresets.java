package me.Azz_9.screenshot_overhaul.client.photoMode.preset;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoSettings;

public class BuiltInPresets {

	public static @NonNull List<PhotoPreset> create() {
		return new ArrayList<>(List.of(
				cinematic(),
				blackAndWhite(),
				warm()
		));
	}

	public static @NonNull PhotoPreset cinematic() {
		return PhotoPreset.createBuiltIn("cinematic", new PhotoSettings(
				50, 50, 50, 50, 50, 50
		));
	}

	public static @NonNull PhotoPreset blackAndWhite() {
		return PhotoPreset.createBuiltIn("black_and_white", new PhotoSettings(
				50, 50, 0, 50, 50, 50
		));
	}

	public static @NonNull PhotoPreset warm() {
		return PhotoPreset.createBuiltIn("warm", new PhotoSettings(
				50, 50, 50, 100, 50, 50
		));
	}
}
