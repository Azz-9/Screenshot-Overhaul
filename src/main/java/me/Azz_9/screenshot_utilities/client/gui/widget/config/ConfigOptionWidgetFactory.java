package me.Azz_9.screenshot_utilities.client.gui.widget.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.*;
import me.Azz_9.screenshot_utilities.client.gui.widget.config.concreteOptionWidgets.*;

/**
 * Creates the appropriate {@link ConfigOptionWidget} for a given {@link ConfigOption}.
 *
 * <p>Add new option types here — no other class needs to change.</p>
 */
@Environment(EnvType.CLIENT)
public final class ConfigOptionWidgetFactory {

	private ConfigOptionWidgetFactory() {
	}

	/**
	 * Instantiates the correct widget for the supplied option.
	 *
	 * @param x      left edge
	 * @param y      top edge
	 * @param width  total row width
	 * @param option the option to render
	 * @return a concrete {@link ConfigOptionWidget}
	 * @throws IllegalArgumentException if the option type is not recognized
	 */
	public static @NonNull ConfigOptionWidget<?> create(int x, int y, int width, @NonNull ConfigOption<?> option) {
		return switch (option) {
			case BooleanConfigOption o -> new BooleanConfigOptionWidget(x, y, width, o);
			case EnumConfigOption<?> o -> createEnum(x, y, width, o);
			case IntSliderConfigOption o -> new IntSliderConfigOptionWidget(x, y, width, o);
			case IntFieldConfigOption o -> new IntFieldConfigOptionWidget(x, y, width, o);
			case StringConfigOption o -> new StringConfigOptionWidget(x, y, width, o);
			case PathConfigOption o -> new PathConfigOptionWidget(x, y, width, o);
			default -> throw new IllegalArgumentException(
					"No widget registered for option type: " + option.getClass().getName());
		};
	}

	private static <E extends Enum<E>> @NonNull ConfigOptionWidget<?> createEnum(
			int x, int y, int width, @NonNull EnumConfigOption<E> option) {
		return switch (option.getStyle()) {
			case CYCLIC -> new EnumCyclicConfigOptionWidget<>(x, y, width, option);
			case DROPDOWN -> new EnumDropdownConfigOptionWidget<>(x, y, width, option);
		};
	}
}
