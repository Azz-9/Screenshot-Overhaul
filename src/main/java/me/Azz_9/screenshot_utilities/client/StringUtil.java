package me.Azz_9.screenshot_utilities.client;

import static org.apache.commons.lang3.StringUtils.capitalize;

import org.jspecify.annotations.NonNull;

public class StringUtil {

	public static @NonNull String pretty(@NonNull String s) {
		return capitalize(s.trim().replace('_', ' '));
	}
}
