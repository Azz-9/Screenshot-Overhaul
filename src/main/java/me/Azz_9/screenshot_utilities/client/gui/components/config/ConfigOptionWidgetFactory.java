package me.Azz_9.screenshot_utilities.client.gui.components.config;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.*;
import me.Azz_9.screenshot_utilities.client.gui.components.config.concreteOptionWidgets.*;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

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
		Consumer<GuiEventListener> focusRequester = widget -> {
			if (MINECRAFT.screen instanceof FocusableScreen fs) fs.requestFocus(widget);
		};

		ConfigOptionWidget<?> widget = switch (option) {
			case BooleanConfigOption o -> new BooleanConfigOptionWidget(x, y, width, o, focusRequester);
			case EnumConfigOption<?> o -> createEnum(x, y, width, o, focusRequester);
			case IntSliderConfigOption o -> new IntSliderConfigOptionWidget(x, y, width, o, focusRequester);
			case IntFieldConfigOption o -> new IntFieldConfigOptionWidget(x, y, width, o, focusRequester);
			case StringConfigOption o -> new StringConfigOptionWidget(x, y, width, o, focusRequester);
			case PathConfigOption o -> new PathConfigOptionWidget(x, y, width, o, focusRequester);
			default -> throw new IllegalArgumentException(
					"No widget registered for option type: " + option.getClass().getName());
		};
		widget.init();
		return widget;
	}

	private static <E extends Enum<E>> @NonNull ConfigOptionWidget<?> createEnum(
			int x, int y, int width, @NonNull EnumConfigOption<E> option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		return switch (option.getStyle()) {
			case CYCLIC -> new EnumCyclicConfigOptionWidget<>(x, y, width, option, onFocusRequested);
			case DROPDOWN -> new EnumDropdownConfigOptionWidget<>(x, y, width, option, onFocusRequested);
		};
	}
}
