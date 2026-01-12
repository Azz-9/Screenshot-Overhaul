package me.Azz_9.screenshot_utilities.client.config;

public class ConfigObject<T> {
	private final T defaultValue;
	String translationKey;
	private T value;

	public ConfigObject(T defaultValue, String translationKey) {
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.translationKey = translationKey;
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

	public String getTranslationKey() {
		return translationKey;
	}

	public void resetToDefault() {
		this.setValue(getDefaultValue());
	}
}
