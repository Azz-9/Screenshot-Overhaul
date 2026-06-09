package me.Azz_9.screenshot_utilities.client.config;

import org.jspecify.annotations.Nullable;

public class NullableSavableObject<T> {
	private final @Nullable T defaultValue;
	private @Nullable T value;

	public NullableSavableObject(@Nullable final T defaultValue) {
		this.value = defaultValue;
		this.defaultValue = defaultValue;
	}

	public @Nullable T getValue() {
		return value;
	}

	public void setValue(@Nullable T value) {
		this.value = value;
	}

	public @Nullable T getDefaultValue() {
		return defaultValue;
	}

	public void resetToDefault() {
		this.setValue(getDefaultValue());
	}
}
