package me.Azz_9.screenshot_utilities.client.config;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

public class ConfigObject<T> extends SavableObject<T> {

	private final @NonNull String translationKey;

	public ConfigObject(@NonNull final T defaultValue, @NonNull final String translationKey, Class<T> valueType) {
		super(defaultValue, valueType);
		this.translationKey = translationKey;
	}

	public @NonNull String getTranslationKey() {
		return translationKey;
	}

	public @NonNull Component getTranslationText() {
		return Component.translatable(translationKey);
	}
}
