package me.Azz_9.screenshot_utilities.client.config.option.options;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.function.Function;

import me.Azz_9.screenshot_utilities.client.config.ConfigObject;
import me.Azz_9.screenshot_utilities.client.config.option.AbstractConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;

/**
 * A {@link ConfigOption} backed by an {@link Enum} value.
 *
 * <p>Two rendering styles are available via {@link Style}:</p>
 * <ul>
 *   <li>{@link Style#CYCLIC} — a single button that cycles through values on click</li>
 *   <li>{@link Style#DROPDOWN} — a dropdown selector showing all values</li>
 * </ul>
 *
 * @param <E> the enum type
 */
public final class EnumConfigOption<E extends Enum<E>> extends AbstractConfigOption<E> {

	public enum Style {CYCLIC, DROPDOWN}

	private final @NonNull Style style;
	private final @NonNull Function<E, Component> valueNameSupplier;
	private final @NonNull E[] enumConstants;

	private EnumConfigOption(Builder<E> builder) {
		super(
				builder.configObject,
				builder.label,
				builder.tooltipSupplier,
				builder.validator,
				builder.validationErrorMessage,
				builder.dependencies
		);
		this.style = builder.style;
		this.valueNameSupplier = builder.valueNameSupplier;
		this.enumConstants = builder.enumConstants;
	}

	public @NonNull Style getStyle() {
		return style;
	}

	/**
	 * Returns the display name for a given enum constant.
	 */
	public @NonNull Component getValueName(@NonNull E value) {
		return valueNameSupplier.apply(value);
	}

	/**
	 * Returns all constants of the backing enum in declaration order.
	 */
	public @NonNull E[] getEnumConstants() {
		return enumConstants;
	}

	/**
	 * Returns the constant that follows the given value, wrapping around.
	 */
	public @NonNull E nextValue(@NonNull E current) {
		E[] constants = enumConstants;
		return constants[(current.ordinal() + 1) % constants.length];
	}

	// -------------------------------------------------------------------------
	// Builder
	// -------------------------------------------------------------------------

	public static <E extends Enum<E>> @NonNull Builder<E> builder(
			@NonNull ConfigObject<E> configObject,
			@NonNull Class<E> enumClass
	) {
		return new Builder<>(configObject, enumClass);
	}

	public static final class Builder<E extends Enum<E>>
			extends AbstractConfigOption.Builder<E, EnumConfigOption<E>, Builder<E>> {

		private @NonNull Style style = Style.CYCLIC;
		private @NonNull Function<E, Component> valueNameSupplier;
		private final @NonNull E[] enumConstants;

		private Builder(@NonNull ConfigObject<E> configObject, @NonNull Class<E> enumClass) {
			super(configObject);
			this.enumConstants = enumClass.getEnumConstants();
			this.valueNameSupplier = value -> Component.literal(value.name());
		}

		public @NonNull Builder<E> style(@NonNull Style style) {
			this.style = style;
			return this;
		}

		/**
		 * Customises how each enum constant is displayed.
		 * Defaults to {@link Enum#name()}.
		 */
		public @NonNull Builder<E> valueName(@NonNull Function<E, Component> supplier) {
			this.valueNameSupplier = supplier;
			return this;
		}

		@Override
		public @NonNull EnumConfigOption<E> build() {
			return new EnumConfigOption<>(this);
		}
	}
}
