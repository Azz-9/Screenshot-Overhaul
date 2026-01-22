package me.Azz_9.screenshot_utilities.client.config;

import org.jspecify.annotations.NonNull;

public class SavableObject<T> {
	private final @NonNull T defaultValue;
	private @NonNull T value;

	public SavableObject(@NonNull T defaultValue) {
		this.value = defaultValue;
		this.defaultValue = defaultValue;
	}

	public @NonNull T getValue() {
		return value;
	}

	public void setValue(@NonNull T value) {
		this.value = value;
	}

	public @NonNull T getDefaultValue() {
		return defaultValue;
	}

	public void resetToDefault() {
		this.setValue(getDefaultValue());
	}
}
