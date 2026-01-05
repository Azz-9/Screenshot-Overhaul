package me.Azz_9.screenshot_utilities.client.config;

public class ConfigObject<T> {
	private T value;
	private T defaultValue;
	String translationKey;

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
}
