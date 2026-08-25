package me.Azz_9.screenshot_overhaul.client.config;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class ConfigObject<T> extends SavableObject<T> {

	private final @NonNull String translationKey;

	protected ConfigObject(@NonNull final T defaultValue, @NonNull final String translationKey, @NonNull Class<T> valueType, @NonNull Function<T, T> applier) {
		super(defaultValue, valueType, applier);
		this.translationKey = translationKey;
	}

	public static <T> ConfigObject<T> nonNull(@NonNull T defaultValue, @NonNull final String translationKey, @NonNull Class<T> valueType) {
		return new ConfigObject<>(defaultValue, translationKey, valueType, t -> t == null ? defaultValue : t);
	}

	public static <T> ConfigObject<T> withApplier(@NonNull T defaultValue, @NonNull final String translationKey, @NonNull Class<T> valueType, @NonNull Function<@Nullable T, T> applier) {
		return new ConfigObject<>(defaultValue, translationKey, valueType, applier);
	}

	public @NonNull Component getTranslationText() {
		return Component.translatable(translationKey);
	}
}
