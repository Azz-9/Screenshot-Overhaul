package me.Azz_9.screenshot_overhaul.client.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class SavableObject<T> {
	private final T defaultValue;
	private T value;
	private final @NonNull Class<T> valueType;
	private final @NonNull Function<@Nullable T, T> applier;
	private final @NonNull List<Consumer<T>> listeners = new ArrayList<>();

	protected SavableObject(final T defaultValue, @NonNull Class<T> valueType, @NonNull Function<@Nullable T, T> applier) {
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.valueType = valueType;
		this.applier = applier;
	}

	/**
	 * Use when T is guaranteed non-null
	 */
	public static <T> SavableObject<@NonNull T> nonNull(@NonNull T defaultValue, @NonNull Class<T> valueType) {
		return new SavableObject<>(defaultValue, valueType, t -> t == null ? defaultValue : t);
	}

	/**
	 * Use when T may be null
	 */
	public static <T> SavableObject<@Nullable T> nullable(@Nullable T defaultValue, @NonNull Class<T> valueType) {
		return new SavableObject<>(defaultValue, valueType, t -> t);
	}

	public static <T> SavableObject<T> withApplier(T defaultValue, @NonNull Class<T> valueType, @NonNull Function<@Nullable T, T> applier) {
		return new SavableObject<>(defaultValue, valueType, applier);
	}

	public @NonNull Class<T> getValueType() {
		return valueType;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		if (!Objects.equals(this.value, value)) {
			listeners.forEach(listener -> listener.accept(value));
		}
		this.value = applier.apply(value);
	}

	public void addOnChangeListener(Consumer<T> listener) {
		listeners.add(listener);
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void resetToDefault() {
		this.setValue(getDefaultValue());
	}
}
