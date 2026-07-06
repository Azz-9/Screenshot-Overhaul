package me.Azz_9.screenshot_overhaul.client.gui.components.config.concreteOptionWidgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOption;
import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_overhaul.client.config.option.options.BooleanConfigOption;

/**
 * Row widget for {@link BooleanConfigOption}.
 * Renders a toggle button showing "ON" / "OFF".
 */
public final class BooleanConfigOptionWidget extends ConfigOptionWidget<Boolean> {

	private Button toggleButton;

	public BooleanConfigOptionWidget(int x, int y, int width, @NonNull ConfigOption<Boolean> option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, option, onFocusRequested);
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		toggleButton = Button.builder(getToggleLabel(), btn -> {
					boolean next = !option.getWorkingValue();
					option.setWorkingValue(next);
					toggleButton.setMessage(getToggleLabel());
					refreshValidation();
				})
				.pos(x, y)
				.size(width, ROW_HEIGHT)
				.build();
		return toggleButton;
	}

	@Override
	protected void onValueReset() {
		toggleButton.setMessage(getToggleLabel());
	}

	private @NonNull Component getToggleLabel() {
		return option.getWorkingValue()
				? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
				: Component.translatable("options.off").withStyle(ChatFormatting.RED);
	}
}