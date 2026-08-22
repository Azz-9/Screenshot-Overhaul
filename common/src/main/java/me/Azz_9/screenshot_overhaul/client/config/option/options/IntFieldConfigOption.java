package me.Azz_9.screenshot_overhaul.client.config.option.options;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_overhaul.client.config.ConfigObject;
import me.Azz_9.screenshot_overhaul.client.config.option.AbstractConfigOption;
import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOption;

/**
 * A {@link ConfigOption} backed by an {@link Integer} value, rendered as a text field.
 *
 * <p>An optional {@code [min, max]} range can be provided; if present a default
 * range validator is registered automatically.</p>
 */
public final class IntFieldConfigOption extends AbstractConfigOption<Integer> {

	private final @Nullable Integer min;
	private final @Nullable Integer max;

	private IntFieldConfigOption(@NonNull Builder builder) {
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

	public @Nullable Integer getMin() {
		return min;
	}

	public @Nullable Integer getMax() {
		return max;
	}

	public static @NonNull Builder builder(@NonNull ConfigObject<Integer> configObject) {
		return new Builder(configObject);
	}

	public static final class Builder extends AbstractConfigOption.Builder<Integer, IntFieldConfigOption, Builder> {

		private @Nullable Integer min;
		private @Nullable Integer max;

		private Builder(@NonNull ConfigObject<Integer> configObject) {
			super(configObject);
		}

		public @NonNull Builder range(int min, int max) {
			if (min > max) throw new IllegalArgumentException("min must be <= max");
			this.min = min;
			this.max = max;
			validate(v -> v >= min && v <= max,
					Component.translatable("screenshot_overhaul.settings.validation.out_of_range", min, max));
			return this;
		}

		@Override
		public @NonNull IntFieldConfigOption build() {
			return new IntFieldConfigOption(this);
		}
	}
}
