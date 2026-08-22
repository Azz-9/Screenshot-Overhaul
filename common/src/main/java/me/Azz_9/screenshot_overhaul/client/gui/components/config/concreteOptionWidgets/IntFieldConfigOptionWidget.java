package me.Azz_9.screenshot_overhaul.client.gui.components.config.concreteOptionWidgets;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_overhaul.client.config.option.options.IntFieldConfigOption;
import me.Azz_9.screenshot_overhaul.client.gui.components.CustomEditBox;

/**
 * Row widget for {@link IntFieldConfigOption}.
 */
public final class IntFieldConfigOptionWidget extends ConfigOptionWidget<Integer> {

	private CustomEditBox editBox;

	public IntFieldConfigOptionWidget(int x, int y, int width, @NonNull IntFieldConfigOption option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, option, onFocusRequested);
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		editBox = new CustomEditBox(x, y, width, ROW_HEIGHT, Component.empty());
		editBox.setValue(String.valueOf(option.getWorkingValue()));
		editBox.setFilter(s -> s.isEmpty() || s.matches("-?\\d*"));
		editBox.setResponder(text -> {
			try {
				int val = Integer.parseInt(text);
				option.setWorkingValue(val);
			} catch (NumberFormatException ignored) {
				// Leave working value unchanged; validation will catch it
			}
			refreshValidation();
			updateEditBoxColor();
		});
		updateEditBoxColor();
		return editBox;
	}

	@Override
	protected void onValueReset() {
		editBox.setValue(String.valueOf(option.getWorkingValue()));
		updateEditBoxColor();
	}

	private void updateEditBoxColor() {
		editBox.setTextColor(getLastValidation().isValid() ? Colors.WHITE : Colors.RED);
	}
}