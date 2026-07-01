package me.Azz_9.screenshot_overhaul.utils;

import static org.apache.commons.lang3.StringUtils.capitalize;

import org.jspecify.annotations.NonNull;

public class StringUtils {

	public static @NonNull String pretty(@NonNull String s) {
		return capitalize(s.trim().replace('_', ' '));
	}
}
