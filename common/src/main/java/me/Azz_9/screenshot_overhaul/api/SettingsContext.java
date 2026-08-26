package me.Azz_9.screenshot_overhaul.api;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOption;

public interface SettingsContext {

	@NonNull ConfigOption<Boolean> getEnableWholeMod();
}
