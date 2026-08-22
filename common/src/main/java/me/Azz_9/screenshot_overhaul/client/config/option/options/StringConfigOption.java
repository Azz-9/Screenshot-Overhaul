package me.Azz_9.screenshot_overhaul.client.config.option.options;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.config.ConfigObject;
import me.Azz_9.screenshot_overhaul.client.config.option.AbstractConfigOption;
import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOption;

/**
 * A {@link ConfigOption} backed by a {@link String} value, rendered as a text field.
 */
public final class StringConfigOption extends AbstractConfigOption<String> {

	private final int maxLength;

	private StringConfigOption(@NonNull Builder builder) {
		super(
				builder.configObject,
				builder.label,
				builder.tooltipSupplier,
				builder.validator,
				builder.validationErrorMessage,
				builder.dependencies
		);
		this.maxLength = builder.maxLength;
	}

	/**
	 * Maximum number of characters allowed. {@code -1} means unlimited.
	 */
	public int getMaxLength() {
		return maxLength;
	}

	public static @NonNull Builder builder(@NonNull ConfigObject<String> configObject) {
		return new Builder(configObject);
	}

	public static final class Builder extends AbstractConfigOption.Builder<String, StringConfigOption, Builder> {

		private int maxLength = -1;

		private Builder(@NonNull ConfigObject<String> configObject) {
			super(configObject);
		}

		public @NonNull Builder maxLength(int maxLength) {
			this.maxLength = maxLength;
			return this;
		}

		@Override
		public @NonNull StringConfigOption build() {
			return new StringConfigOption(this);
		}
	}
}
