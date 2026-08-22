package me.Azz_9.screenshot_overhaul.client.gui.components.config.concreteOptionWidgets;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.config.Config.MAX_WINDOW_SIZE;
import static me.Azz_9.screenshot_overhaul.client.config.Config.MIN_WINDOW_SIZE;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_overhaul.client.config.option.options.ResolutionConfigOption;
import me.Azz_9.screenshot_overhaul.client.gui.components.CustomEditBox;
import me.Azz_9.screenshot_overhaul.client.gui.components.SimpleParentWidget;

public class ResolutionConfigOptionWidget extends ConfigOptionWidget<Config.Resolution2D> {

	private final @NonNull ResolutionConfigOption resolutionConfigOption;

	private ResolutionWidget resolutionWidget;

	public ResolutionConfigOptionWidget(int x, int y, int width, @NonNull ResolutionConfigOption option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, option, onFocusRequested);
		this.resolutionConfigOption = option;
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		resolutionWidget = new ResolutionWidget(x, y, width, resolutionConfigOption.getPresets(), option.getWorkingValue(), resolutionConfigOption, this::refreshValidation);
		return resolutionWidget;
	}

	@Override
	protected void onValueReset() {
		resolutionWidget.setValue(option.getWorkingValue());
	}

	private static final class ResolutionWidget extends SimpleParentWidget {

		private static final int DIMENSION_WIDTH = 50;
		private static final int GAP = 5;

		private final @NonNull CustomEditBox widthWidget;
		private final @NonNull CustomEditBox heightWidget;

		private ResolutionConfigOption option;

		public ResolutionWidget(int x, int y, int width, @NonNull List<ResolutionConfigOption.ResolutionPreset> presets, @NotNull Config.Resolution2D currentValue, @NonNull ResolutionConfigOption option, @NonNull Runnable refreshValidation) {
			super(x, y, width, ROW_HEIGHT);
			this.option = option;

			int presetWidth = (width - DIMENSION_WIDTH * 2 - ROW_HEIGHT - GAP * presets.size()) / presets.size();

			widthWidget = new CustomEditBox(
					x + width - DIMENSION_WIDTH * 2 - ROW_HEIGHT, y, DIMENSION_WIDTH, ROW_HEIGHT,
					Component.translatable("screenshot_overhaul.width")
			);
			initializeEditBox(widthWidget, currentValue.width());
			widthWidget.setResponder(text -> {
				try {
					int val = Integer.parseInt(text);
					option.setWorkingValue(new Config.Resolution2D(val, option.getWorkingValue().height()));
				} catch (NumberFormatException ignored) {
					// Leave working value unchanged; validation will catch it
				}
				updateEditBoxColor();
				refreshValidation.run();
			});

			heightWidget = new CustomEditBox(
					x + width - DIMENSION_WIDTH, y, DIMENSION_WIDTH, ROW_HEIGHT,
					Component.translatable("screenshot_overhaul.height")
			);
			initializeEditBox(heightWidget, currentValue.height());
			heightWidget.setResponder(text -> {
				try {
					int val = Integer.parseInt(text);
					option.setWorkingValue(new Config.Resolution2D(option.getWorkingValue().width(), val));
				} catch (NumberFormatException ignored) {
					// Leave working value unchanged; validation will catch it
				}
				updateEditBoxColor();
				refreshValidation.run();
			});
			updateEditBoxColor();

			for (int i = 0; i < presets.size(); i++) {
				ResolutionConfigOption.ResolutionPreset preset = presets.get(i);

				addRenderableChild(Button.builder(preset.label(), _ -> {
							widthWidget.setValue(String.valueOf(preset.resolution().width()));
							heightWidget.setValue(String.valueOf(preset.resolution().height()));
						})
						.bounds(x + presetWidth * i + GAP * i, y, presetWidth, ROW_HEIGHT)
						.build());
			}

			addRenderableChild(widthWidget);
			addRenderableChild(heightWidget);
		}

		private void initializeEditBox(@NonNull CustomEditBox editBox, int value) {
			editBox.setValue(String.valueOf(value));
			editBox.setMaxLength(5);
			editBox.setFilter(s -> s.isEmpty() || s.matches("\\d*"));
		}

		@Override
		protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
			// propagate active state to children
			setActive(active);

			super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

			String symbol = "×";
			int symbolWidth = MINECRAFT.font.width(symbol);
			graphics.text(
					MINECRAFT.font, symbol,
					widthWidget.getRight() + (ROW_HEIGHT - symbolWidth) / 2,
					widthWidget.getY() + (ROW_HEIGHT - MINECRAFT.font.lineHeight) / 2,
					Colors.WHITE
			);
		}

		@Override
		public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
			super.mouseClicked(click, doubled);
			return false;
		}

		public void updateEditBoxColor() {
			updateEditBoxColor(widthWidget, option.getWorkingValue().width());
			updateEditBoxColor(heightWidget, option.getWorkingValue().height());
		}

		public void updateEditBoxColor(@NonNull EditBox editBox, int value) {
			editBox.setTextColor(
					value >= MIN_WINDOW_SIZE && value <= MAX_WINDOW_SIZE
							? Colors.WHITE
							: Colors.RED
			);
		}

		public void setValue(Config.Resolution2D resolution) {
			widthWidget.setValue(String.valueOf(resolution.width()));
			heightWidget.setValue(String.valueOf(resolution.height()));
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
		}
	}
}
