package me.Azz_9.screenshot_utilities.client.config;

import org.jspecify.annotations.NonNull;

public class ConfigObject<T> extends SavableObject<T> {

	private final @NonNull String translationKey;

	public ConfigObject(@NonNull T defaultValue, @NonNull String translationKey) {
		super(defaultValue);
		this.translationKey = translationKey;
	}

	public @NonNull String getTranslationKey() {
		return translationKey;
	}
}
