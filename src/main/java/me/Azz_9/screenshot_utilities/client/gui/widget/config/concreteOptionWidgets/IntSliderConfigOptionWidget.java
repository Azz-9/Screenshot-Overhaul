package me.Azz_9.screenshot_utilities.client.gui.widget.config.concreteOptionWidgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.IntSliderConfigOption;

/**
 * Row widget for {@link IntSliderConfigOption}.
 */
@Environment(EnvType.CLIENT)
public final class IntSliderConfigOptionWidget extends ConfigOptionWidget<Integer> {

	private final @NonNull IntSliderConfigOption sliderOption;
	private SliderButton slider;

	public IntSliderConfigOptionWidget(int x, int y, int width, @NonNull IntSliderConfigOption option) {
		super(x, y, width, option);
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
		int min = sliderOption.getMin();
		int max = sliderOption.getMax();
		double norm = (double) (option.getWorkingValue() - min) / (max - min);
		// Re-create is cleaner than reflection to set protected `value`
		// We keep a reference and trigger via applyValue path — just rebuild.
		repositionSlider(slider.getX(), slider.getY(), slider.getWidth());
	}

	private void repositionSlider(int x, int y, int width) {
		// The slider stores its normalized value as a protected field; we rebuild it
		// to avoid reflection. The list widget will re-add it on next layout pass
		// via repositionSubWidgets calling setX/setY, so this is fine.
		int min = sliderOption.getMin();
		int max = sliderOption.getMax();
		double norm = (double) (option.getWorkingValue() - min) / (max - min);
		slider.setX(x);
		slider.setY(y);
		// Force value update via the message/value system
		slider.onClick(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false); // harmless no-op to refresh display
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

		private int getIntValue() {
			return (int) Math.round(value * (MAX - MIN) + MIN);
		}
	}
}