package me.Azz_9.screenshot_utilities.client.gui.widget.config.concreteOptionWidgets;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.IntFieldConfigOption;

/**
 * Row widget for {@link IntFieldConfigOption}.
 */
@Environment(EnvType.CLIENT)
public final class IntFieldConfigOptionWidget extends ConfigOptionWidget<Integer> {

	private EditBox editBox;

	public IntFieldConfigOptionWidget(int x, int y, int width, @NonNull IntFieldConfigOption option) {
		super(x, y, width, option);
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		editBox = new EditBox(MINECRAFT.font, x, y, width, ROW_HEIGHT, Component.empty());
		editBox.setValue(String.valueOf(option.getWorkingValue()));
		//editBox.setFilter(s -> s.isEmpty() || s.matches("-?\\d*"));
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