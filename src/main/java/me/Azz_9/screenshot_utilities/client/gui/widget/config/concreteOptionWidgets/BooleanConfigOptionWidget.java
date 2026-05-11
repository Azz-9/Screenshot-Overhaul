package me.Azz_9.screenshot_utilities.client.gui.widget.config.concreteOptionWidgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.BooleanConfigOption;

/**
 * Row widget for {@link BooleanConfigOption}.
 * Renders a toggle button showing "ON" / "OFF".
 */
@Environment(EnvType.CLIENT)
public final class BooleanConfigOptionWidget extends ConfigOptionWidget<Boolean> {

	private Button toggleButton;

	public BooleanConfigOptionWidget(int x, int y, int width, @NonNull BooleanConfigOption option) {
		super(x, y, width, option);
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
				? Component.translatable("screenshot_utilities.settings.on")
				: Component.translatable("screenshot_utilities.settings.off");
	}
}