package me.Azz_9.screenshot_overhaul.client.gui.components;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.NavigationButton;
import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_overhaul.client.photoMode.preset.PhotoPreset;
import me.Azz_9.screenshot_overhaul.client.photoMode.preset.PhotoPresetManager;

public class EffectsPanel extends SmoothScrollableWidget {

	private static final int PADDING = 10;
	private static final int NAVIGATION_BUTTON_SIZE = 20;
	private static final int HEADER_HEIGHT = PADDING * 2 + NAVIGATION_BUTTON_SIZE;

	private static final int FIELD_GAP = 6;
	private static final int FIELD_HEIGHT = 20;
	private static final int SETTING_WIDTH = 100;

	private static final int FOOTER_BUTTONS_HEIGHT = 20;
	private static final int FOOTER_HEIGHT = PADDING * 3 + FOOTER_BUTTONS_HEIGHT * 2;

	private final Button deleteButton;
	private final Button saveButton;
	private final Button saveAsNewPresetButton;

	private int totalFieldsHeight;

	public EffectsPanel(int x, int y, int width, int height) {
		super(x, y, width, height);

		// header
		addFixedChild(createNavigationButton(NavigationButton.NavigationType.BACK));
		addFixedChild(createNavigationButton(NavigationButton.NavigationType.NEXT));

		generateSettingsEntries();

		// footer
		deleteButton = createDeleteButton();
		saveButton = createSaveButton();
		saveAsNewPresetButton = createSaveAsNewPresetButton();

		addFixedChild(deleteButton);
		addFixedChild(saveButton);
		addFixedChild(saveAsNewPresetButton);

		updateFooterButtons();
	}

	private NavigationButton createNavigationButton(NavigationButton.NavigationType type) {
		return new NavigationButton(
				type == NavigationButton.NavigationType.NEXT
						? getRight() - PADDING - NAVIGATION_BUTTON_SIZE
						: getX() + PADDING,
				getY() + PADDING,
				NAVIGATION_BUTTON_SIZE, NAVIGATION_BUTTON_SIZE,
				type,
				type == NavigationButton.NavigationType.NEXT
						? _ -> selectNextPreset()
						: _ -> selectPreviousPreset()
		);
	}

	private Button createDeleteButton() {
		return Button.builder(Component.translatable("screenshot_overhaul.delete"), _ -> {
					int deletedIndex = PhotoMode.selectedPresetIndex();

					PhotoPresetManager.removePreset(deletedIndex);

					if (deletedIndex >= PhotoPresetManager.getPresets().size()) {
						selectLastPreset();
					} else {
						updateFooterButtons();
						generateSettingsEntries();
					}
				})
				.bounds(
						getX() + PADDING, getBottom() - PADDING * 2 - FOOTER_BUTTONS_HEIGHT * 2,
						(getWidth() - PADDING * 3) / 2, FOOTER_BUTTONS_HEIGHT
				)
				.build();
	}

	private Button createSaveButton() {
		return Button.builder(
						Component.translatable("screenshot_overhaul.save"),
						_ -> PhotoPresetManager.updatePreset(PhotoMode.selectedPreset(), PhotoMode.appliedSettings())
				)
				.bounds(
						getX() + (getWidth() + PADDING) / 2, getBottom() - PADDING * 2 - FOOTER_BUTTONS_HEIGHT * 2,
						(getWidth() - PADDING * 3) / 2, FOOTER_BUTTONS_HEIGHT
				)
				.build();
	}

	private Button createSaveAsNewPresetButton() {
		return Button.builder(Component.translatable("screenshot_overhaul.photo_mode.save_as_new_preset"), _ -> {
					PhotoPresetManager.addPreset(new PhotoPreset("custom", "Custom", PhotoMode.appliedSettings().copy(), false));
					selectLastPreset();
				})
				.bounds(
						getX() + PADDING, getBottom() - PADDING - FOOTER_BUTTONS_HEIGHT,
						getWidth() - PADDING * 2, FOOTER_BUTTONS_HEIGHT
				)
				.build();
	}

	private void generateSettingsEntries() {
		clearScrollableChildren();

		new EntriesBuilder(this)
				.intSlider(Component.literal("Exposure"), 0, 100, PhotoMode.selectedPreset().getSettings().exposure(), PhotoMode::setExposure)
				.intSlider(Component.literal("Contrast"), 0, 100, PhotoMode.selectedPreset().getSettings().contrast(), PhotoMode::setContrast)
				.intSlider(Component.literal("Saturation"), 0, 100, PhotoMode.selectedPreset().getSettings().saturation(), PhotoMode::setSaturation)
				.intSlider(Component.literal("Temperature"), 0, 100, PhotoMode.selectedPreset().getSettings().temperature(), PhotoMode::setTemperature)
				.intSlider(Component.literal("Brightness"), 0, 100, PhotoMode.selectedPreset().getSettings().brightness(), PhotoMode::setBrightness)
				.intSlider(Component.literal("Vignette"), 0, 100, PhotoMode.selectedPreset().getSettings().vignette(), PhotoMode::setVignette)
				.build();
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		extractBackground(graphics);

		extractHeader(graphics);

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

		graphics.fill(getX(), getBottom() - FOOTER_HEIGHT, getRight(), getBottom() - FOOTER_HEIGHT - 1, Colors.LIGHT_GRAY);
	}

	private void extractBackground(@NonNull GuiGraphicsExtractor graphics) {
		graphics.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK_SEMI_TRANSPARENT);
	}

	private void extractHeader(@NonNull GuiGraphicsExtractor graphics) {
		graphics.centeredText(
				MINECRAFT.font, PhotoMode.selectedPreset().getName(),
				getX() + getWidth() / 2, getY() + (HEADER_HEIGHT - MINECRAFT.font.lineHeight) / 2,
				Colors.WHITE
		);

		graphics.fill(getX(), getY() + HEADER_HEIGHT, getRight(), getY() + HEADER_HEIGHT + 1, Colors.LIGHT_GRAY);
	}

	private void selectLastPreset() {
		PhotoMode.selectPreset(PhotoPresetManager.getPresets().size() - 1);
		updateFooterButtons();
		generateSettingsEntries();
	}

	private void selectNextPreset() {
		PhotoMode.selectPreset(PhotoPresetManager.getNextIndex(PhotoMode.selectedPresetIndex()));
		updateFooterButtons();
		generateSettingsEntries();
	}

	private void selectPreviousPreset() {
		PhotoMode.selectPreset(PhotoPresetManager.getPreviousIndex(PhotoMode.selectedPresetIndex()));
		updateFooterButtons();
		generateSettingsEntries();
	}

	private void updateFooterButtons() {
		boolean builtIn = PhotoMode.selectedPreset().isBuiltIn();
		deleteButton.active = !builtIn;
		saveButton.active = !builtIn;
	}

	@Override
	protected @NonNull ScrollArea getScrollArea() {
		return new ScrollArea(getX(), getY() + HEADER_HEIGHT + FIELD_GAP, getRight(), getBottom() - FOOTER_HEIGHT - FIELD_GAP);
	}

	void setTotalFieldsHeight(int height) {
		this.totalFieldsHeight = height;
	}

	@Override
	protected int getTotalScrollableHeight() {
		return totalFieldsHeight;
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
	}

	private static final class EntriesBuilder {

		private final @NonNull EffectsPanel effectsPanel;
		private int cursorY = 0;

		public EntriesBuilder(@NonNull EffectsPanel effectsPanel) {
			this.effectsPanel = effectsPanel;
		}

		private @NonNull EntriesBuilder intSlider(@NonNull Component label, int min, int max, int value, @NonNull Consumer<Integer> applier) {
			return addField(new IntSliderEntry(label, entryX(), cursorY, entryWidth(), FIELD_HEIGHT, min, max, value, applier));
		}

		private @NonNull EntriesBuilder addField(@NonNull SimpleParentWidget widget) {
			widget.setY(cursorY);
			effectsPanel.addScrollableChild(widget);
			cursorY += widget.getHeight() + FIELD_GAP;
			return this;
		}

		private int entryX() {
			return effectsPanel.getX() + PADDING;
		}

		private int entryWidth() {
			return effectsPanel.getWidth() - PADDING * 2;
		}

		private void build() {
			effectsPanel.setTotalFieldsHeight(cursorY);
		}
	}

	private static final class IntSliderEntry extends SimpleParentWidget {

		private static final int SLIDER_WIDTH = 100;
		private static final int GAP = 2;

		public IntSliderEntry(@NonNull Component label, int x, int y, int width, int height, int min, int max, int value, @NonNull Consumer<Integer> applier) {
			super(x, y, width, height);

			addRenderableChild(new StringWidget(
					x, y + (FIELD_HEIGHT - MINECRAFT.font.lineHeight) / 2,
					width - SETTING_WIDTH - GAP, MINECRAFT.font.lineHeight,
					label, MINECRAFT.font
			));

			addRenderableChild(new AbstractSliderButton(
					x + width - SETTING_WIDTH, y, SLIDER_WIDTH, FIELD_HEIGHT,
					Component.literal(String.valueOf(value)), (double) (value - min) / (max - min)
			) {
				@Override
				protected void updateMessage() {
					setMessage(Component.literal(String.valueOf(getIntValue())));
				}

				@Override
				protected void applyValue() {
					applier.accept(getIntValue());
				}

				@Override
				public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
					super.onClick(event, doubleClick);
					if (MINECRAFT.gui.screen() instanceof FocusableScreen screen) {
						screen.requestFocus(this);
					}
				}

				public int getIntValue() {
					return (int) Math.round(value * (max - min) + min);
				}
			});
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
		}
	}
}
