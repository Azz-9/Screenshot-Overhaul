package me.Azz_9.screenshot_utilities.client.config.option.options;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.config.ConfigObject;
import me.Azz_9.screenshot_utilities.client.config.option.AbstractConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;

/**
 * A {@link ConfigOption} backed by an {@link Integer} value, rendered as a slider.
 */
public final class IntSliderConfigOption extends AbstractConfigOption<Integer> {

	private final int min;
	private final int max;

	private IntSliderConfigOption(Builder builder) {
		super(
				builder.configObject,
				builder.label,
				builder.tooltipSupplier,
				builder.validator,
				builder.validationErrorMessage,
				builder.dependencies
		);
		this.min = builder.min;
		this.max = builder.max;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	// -------------------------------------------------------------------------
	// Builder
	// -------------------------------------------------------------------------

	public static @NonNull Builder builder(@NonNull ConfigObject<Integer> configObject, int min, int max) {
		return new Builder(configObject, min, max);
	}

	public static final class Builder extends AbstractConfigOption.Builder<Integer, IntSliderConfigOption, Builder> {

		private final int min;
		private final int max;

		private Builder(@NonNull ConfigObject<Integer> configObject, int min, int max) {
			super(configObject);
			if (min > max) throw new IllegalArgumentException("min must be <= max");
			this.min = min;
			this.max = max;
			// Default validator: clamp to [min, max]
			validate(v -> v >= min && v <= max,
					Component.translatable("screenshot_utilities.settings.validation.out_of_range", min, max));
		}

		@Override
		public @NonNull IntSliderConfigOption build() {
			return new IntSliderConfigOption(this);
		}
	}
}
