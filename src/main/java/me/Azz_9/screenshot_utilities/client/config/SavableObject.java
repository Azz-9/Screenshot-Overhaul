package me.Azz_9.screenshot_utilities.client.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SavableObject<T> {
	private final T defaultValue;
	private T value;
	private final Class<T> valueType;

	protected SavableObject(final T defaultValue, Class<T> valueType) {
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.valueType = valueType;
	}

	/**
	 * Use when T is guaranteed non-null
	 */
	public static <T> SavableObject<@NonNull T> nonNull(@NonNull T defaultValue, Class<T> valueType) {
		return new SavableObject<>(defaultValue, valueType);
	}

	/**
	 * Use when T may be null
	 */
	public static <T> SavableObject<@Nullable T> nullable(@Nullable T defaultValue, Class<T> valueType) {
		return new SavableObject<>(defaultValue, valueType);
	}

	public Class<T> getValueType() {
		return valueType;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void resetToDefault() {
		this.setValue(getDefaultValue());
	}
}
