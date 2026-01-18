package me.Azz_9.screenshot_utilities.client.config;

import org.jspecify.annotations.NonNull;

public class ConfigObject<T> {
	@NonNull
	private final T defaultValue;
	@NonNull
	String translationKey;
	@NonNull
	private T value;

	public ConfigObject(@NonNull T defaultValue, @NonNull String translationKey) {
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.translationKey = translationKey;
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

	public @NonNull String getTranslationKey() {
		return translationKey;
	}

	public void resetToDefault() {
		this.setValue(getDefaultValue());
	}
}
