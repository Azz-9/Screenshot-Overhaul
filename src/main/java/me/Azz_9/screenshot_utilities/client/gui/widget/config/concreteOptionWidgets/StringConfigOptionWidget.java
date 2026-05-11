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
import me.Azz_9.screenshot_utilities.client.config.option.options.StringConfigOption;

/**
 * Row widget for {@link StringConfigOption}.
 */
@Environment(EnvType.CLIENT)
public final class StringConfigOptionWidget extends ConfigOptionWidget<String> {

	private final @NonNull StringConfigOption stringOption;
	private EditBox editBox;

	public StringConfigOptionWidget(int x, int y, int width, @NonNull StringConfigOption option) {
		super(x, y, width, option);
		this.stringOption = option;
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		editBox = new EditBox(MINECRAFT.font, x, y, width, ROW_HEIGHT, Component.empty());
		if (stringOption.getMaxLength() > 0) {
			editBox.setMaxLength(stringOption.getMaxLength());
		}
		editBox.setValue(option.getWorkingValue());
		editBox.setResponder(text -> {
			option.setWorkingValue(text);
			refreshValidation();
			updateEditBoxColor();
		});
		updateEditBoxColor();
		return editBox;
	}

	@Override
	protected void onValueReset() {
		editBox.setValue(option.getWorkingValue());
		updateEditBoxColor();
	}

	private void updateEditBoxColor() {
		editBox.setTextColor(getLastValidation().isValid() ? Colors.WHITE : Colors.RED);
	}
}