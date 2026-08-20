package me.Azz_9.screenshot_overhaul.client.photoMode.preset;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoSettings;

public class PhotoPreset {

	private final @NonNull String id;
	private @NonNull Component name;
	private @NonNull PhotoSettings settings;
	private final boolean isBuiltIn;

	private PhotoPreset(@NonNull String id, @NonNull PhotoSettings settings) {
		this.id = id;
		this.name = Component.translatable("screenshot_overhaul.photo_mode.preset." + id);
		this.settings = settings;
		this.isBuiltIn = true;
	}

	public PhotoPreset(@NonNull String id, @NonNull String name, @NonNull PhotoSettings settings, boolean isBuiltIn) {
		this.id = id;
		this.name = Component.literal(name);
		this.settings = settings;
		this.isBuiltIn = isBuiltIn;
	}

	public static PhotoPreset createBuiltIn(@NonNull String id, @NonNull PhotoSettings settings) {
		return new PhotoPreset(id, settings);
	}

	public @NonNull String getId() {
		return id;
	}

	public @NonNull Component getName() {
		return name;
	}

	public void setName(@NonNull Component name) {
		this.name = name;
	}

	public void setSettings(@NonNull PhotoSettings settings) {
		this.settings = settings;
	}

	public @NonNull PhotoSettings getSettings() {
		return settings;
	}

	public boolean isBuiltIn() {
		return isBuiltIn;
	}
}
