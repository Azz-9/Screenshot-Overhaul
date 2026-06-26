package me.Azz_9.screenshot_utilities.client.gui.components.config.concreteOptionWidgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.EnumConfigOption;

/**
 * Row widget for {@link EnumConfigOption} with {@link EnumConfigOption.Style#CYCLIC}.
 */
@Environment(EnvType.CLIENT)
public final class EnumCyclicConfigOptionWidget<E extends Enum<E>> extends ConfigOptionWidget<E> {

	private final @NonNull EnumConfigOption<E> enumOption;
	private Button cycleButton;

	public EnumCyclicConfigOptionWidget(int x, int y, int width, @NonNull EnumConfigOption<E> option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, option, onFocusRequested);
		this.enumOption = option;
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		cycleButton = Button.builder(getCurrentLabel(), btn -> {
					E next = enumOption.nextValue(option.getWorkingValue());
					option.setWorkingValue(next);
					cycleButton.setMessage(getCurrentLabel());
					refreshValidation();
				})
				.pos(x, y)
				.size(width, ROW_HEIGHT)
				.build();
		return cycleButton;
	}

	@Override
	protected void onValueReset() {
		cycleButton.setMessage(getCurrentLabel());
	}

	private @NonNull Component getCurrentLabel() {
		return enumOption.getValueName(option.getWorkingValue());
	}
}