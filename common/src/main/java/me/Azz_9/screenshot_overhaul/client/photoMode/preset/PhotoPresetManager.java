package me.Azz_9.screenshot_overhaul.client.photoMode.preset;

import java.util.Collections;
import java.util.List;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoSettings;

public class PhotoPresetManager {

	private static final List<PhotoPreset> presets = BuiltInPresets.create();

	public static void addPreset(PhotoPreset preset) {
		presets.add(preset);
	}

	public static void removePreset(int index) {
		if (index >= 0 && index < presets.size()) {
			if (!presets.get(index).isBuiltIn()) {
				presets.remove(index);
			} else {
				ScreenshotLogger.error("Cannot remove a built-in preset");
			}
		} else {
			ScreenshotLogger.error("Invalid preset index");
		}
	}

	public static void updatePreset(PhotoPreset preset, PhotoSettings settings) {
		preset.setSettings(settings.copy());
	}

	public static void applyPreset(PhotoPreset preset, PhotoSettings target) {
		target.copyFrom(preset.getSettings());
	}

	public static List<PhotoPreset> getPresets() {
		return Collections.unmodifiableList(presets);
	}

	public static int getNextIndex(int index) {
		return getIndexWithOffset(index, 1);
	}

	public static int getPreviousIndex(int index) {
		return getIndexWithOffset(index, -1);
	}

	private static int getIndexWithOffset(int index, int offset) {
		return (index + presets.size() + offset) % presets.size();
	}
}
