package me.Azz_9.screenshot_utilities.client.gui.widget.config.concreteOptionWidgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.IntSliderConfigOption;

/**
 * Row widget for {@link IntSliderConfigOption}.
 */
@Environment(EnvType.CLIENT)
public final class IntSliderConfigOptionWidget extends ConfigOptionWidget<Integer> {

	private final @NonNull IntSliderConfigOption sliderOption;
	private SliderButton slider;

	public IntSliderConfigOptionWidget(int x, int y, int width, @NonNull IntSliderConfigOption option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, option, onFocusRequested);
		this.sliderOption = option;
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		int min = sliderOption.getMin();
		int max = sliderOption.getMax();
		double norm = (double) (option.getWorkingValue() - min) / (max - min);

		slider = new SliderButton(x, y, width, ROW_HEIGHT, Component.empty(), norm, min, max);
		slider.updateMessage();
		return slider;
	}

	@Override
	protected void onValueReset() {
		slider.setIntValue(option.getWorkingValue());
	}

	private class SliderButton extends AbstractSliderButton {

		private final int MIN;
		private final int MAX;

		public SliderButton(int x, int y, int width, int height, Component message, double initialValue, int min, int max) {
			super(x, y, width, height, message, initialValue);
			this.MIN = min;
			this.MAX = max;
		}

		@Override
		public void updateMessage() {
			setMessage(Component.literal(String.valueOf(getIntValue())));
		}

		@Override
		protected void applyValue() {
			option.setWorkingValue(getIntValue());
			refreshValidation();
		}

		public void setIntValue(int value) {
			this.value = (double) (value - MIN) / (MAX - MIN);
			updateMessage();
		}

		private int getIntValue() {
			return (int) Math.round(value * (MAX - MIN) + MIN);
		}
	}
}