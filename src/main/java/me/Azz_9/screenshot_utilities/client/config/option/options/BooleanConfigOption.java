package me.Azz_9.screenshot_utilities.client.config.option.options;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.config.ConfigObject;
import me.Azz_9.screenshot_utilities.client.config.option.AbstractConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;

/**
 * A {@link ConfigOption} backed by a {@link Boolean} value.
 * Rendered as a toggle button (ON / OFF).
 */
public final class BooleanConfigOption extends AbstractConfigOption<Boolean> {

	private BooleanConfigOption(Builder builder) {
		super(
				builder.configObject,
				builder.label,
				builder.tooltipSupplier,
				builder.validator,
				builder.validationErrorMessage,
				builder.dependencies
		);
	}

	// -------------------------------------------------------------------------
	// Builder
	// -------------------------------------------------------------------------

	public static @NonNull Builder builder(@NonNull ConfigObject<Boolean> configObject) {
		return new Builder(configObject);
	}

	public static final class Builder extends AbstractConfigOption.Builder<Boolean, BooleanConfigOption, Builder> {

		private Builder(@NonNull ConfigObject<Boolean> configObject) {
			super(configObject);
		}

		@Override
		public @NonNull BooleanConfigOption build() {
			return new BooleanConfigOption(this);
		}
	}
}
