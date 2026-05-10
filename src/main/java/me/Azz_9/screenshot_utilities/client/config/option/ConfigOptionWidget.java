package me.Azz_9.screenshot_utilities.client.config.option;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.Colors;

/**
 * Abstract widget representing a single row in the config option list.
 *
 * <p>Layout (left → right):</p>
 * <pre>
 *  [label ................] [   control widget   ] [↺]
 * </pre>
 *
 * <ul>
 *   <li>Label occupies the left {@value #LABEL_WIDTH_FRACTION} of the available width.</li>
 *   <li>The control widget fills the remaining space minus the reset button.</li>
 *   <li>A small reset button sits on the right edge.</li>
 * </ul>
 *
 * <p>When the option's dependency is not satisfied the entire row is rendered at
 * reduced opacity and mouse events are suppressed.</p>
 *
 * @param <T> the value type of the backing {@link ConfigOption}
 */
@Environment(EnvType.CLIENT)
public abstract class ConfigOptionWidget<T> extends AbstractWidget {

	// -------------------------------------------------------------------------
	// Layout constants
	// -------------------------------------------------------------------------

	public static final int ROW_HEIGHT = 20;
	public static final int ROW_PADDING_V = 4;
	public static final int FULL_ROW_HEIGHT = ROW_HEIGHT + ROW_PADDING_V * 2;

	private static final float LABEL_WIDTH_FRACTION = 0.40f;
	private static final int RESET_BUTTON_WIDTH = 20;
	private static final int CONTROL_RESET_GAP = 4;
	private static final int LABEL_CONTROL_GAP = 8;

	// -------------------------------------------------------------------------
	// State
	// -------------------------------------------------------------------------

	protected final @NonNull ConfigOption<T> option;

	private final @NonNull AbstractWidget controlWidget;
	private final @NonNull Button resetButton;

	/**
	 * Cached last-known validation result so we don't validate every frame.
	 */
	private ConfigOption.@NonNull ValidationResult lastValidation = ConfigOption.ValidationResult.valid();

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	protected ConfigOptionWidget(int x, int y, int width, @NonNull ConfigOption<T> option) {
		super(x, y, width, FULL_ROW_HEIGHT, Component.empty());
		this.option = option;

		int labelWidth = (int) (width * LABEL_WIDTH_FRACTION);
		int resetX = x + width - RESET_BUTTON_WIDTH;
		int controlWidth = width - labelWidth - LABEL_CONTROL_GAP - RESET_BUTTON_WIDTH - CONTROL_RESET_GAP;
		int controlX = x + labelWidth + LABEL_CONTROL_GAP;
		int widgetY = y + ROW_PADDING_V;

		this.controlWidget = createControlWidget(controlX, widgetY, controlWidth);
		this.resetButton = createResetButton(resetX, widgetY);

		// Initial validation
		refreshValidation();
	}

	// -------------------------------------------------------------------------
	// Abstract factory method — subclasses provide the type-specific control
	// -------------------------------------------------------------------------

	/**
	 * Creates the control widget (toggle, slider, text field, etc.) for this option.
	 *
	 * @param x     left edge of the available control area
	 * @param y     top edge
	 * @param width available width for the control
	 * @return a non-null widget
	 */
	protected abstract @NonNull AbstractWidget createControlWidget(int x, int y, int width);

	// -------------------------------------------------------------------------
	// Rendering
	// -------------------------------------------------------------------------


	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		boolean enabled = option.isDependencySatisfied();

		// Determine label color
		int labelColor;
		if (!enabled) {
			labelColor = Colors.GRAY;
		} else if (!lastValidation.isValid()) {
			labelColor = Colors.RED;
		} else if (option.hasChanged()) {
			labelColor = Colors.YELLOW;
		} else {
			labelColor = Colors.WHITE;
		}

		// Draw label
		int labelY = getY() + ROW_PADDING_V + (ROW_HEIGHT - MINECRAFT.font.lineHeight) / 2;
		graphics.text(MINECRAFT.font, option.getLabel(), getX(), labelY, labelColor, true);

		// Propagate enabled state to sub-widgets
		controlWidget.active = enabled;
		resetButton.active = enabled;

		// Render sub-widgets (they manage their own position)
		controlWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		resetButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

		// Tooltip (on control widget hover only)
		if (enabled && controlWidget.isHovered()) {
			option.getTooltip().ifPresent(tip ->
					graphics.setTooltipForNextFrame(MINECRAFT.font, tip, mouseX, mouseY));

			// Validation error tooltip
			if (!lastValidation.isValid() && lastValidation.errorMessage() != null) {
				graphics.setTooltipForNextFrame(MINECRAFT.font, lastValidation.errorMessage(), mouseX, mouseY);
			}
		}
	}

	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, option.getLabel());
	}

	// -------------------------------------------------------------------------
	// Mouse / keyboard forwarding
	// -------------------------------------------------------------------------


	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		if (!option.isDependencySatisfied()) return false;
		return controlWidget.mouseClicked(event, doubleClick)
				|| resetButton.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
		if (!option.isDependencySatisfied()) return false;
		return controlWidget.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		if (!option.isDependencySatisfied()) return false;
		return controlWidget.mouseReleased(event)
				|| resetButton.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (!option.isDependencySatisfied()) return false;
		return controlWidget.keyPressed(event);
	}

	@Override
	public boolean charTyped(@NonNull CharacterEvent event) {
		if (!option.isDependencySatisfied()) return false;
		return controlWidget.charTyped(event);
	}

	// -------------------------------------------------------------------------
	// Repositioning (called by the list widget on layout)
	// -------------------------------------------------------------------------

	@Override
	public void setX(int x) {
		super.setX(x);
		repositionSubWidgets();
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		repositionSubWidgets();
	}

	private void repositionSubWidgets() {
		int labelWidth = (int) (getWidth() * LABEL_WIDTH_FRACTION);
		int resetX = getX() + getWidth() - RESET_BUTTON_WIDTH;
		int controlWidth = getWidth() - labelWidth - LABEL_CONTROL_GAP - RESET_BUTTON_WIDTH - CONTROL_RESET_GAP;
		int controlX = getX() + labelWidth + LABEL_CONTROL_GAP;
		int widgetY = getY() + ROW_PADDING_V;

		controlWidget.setX(controlX);
		controlWidget.setY(widgetY);
		controlWidget.setWidth(controlWidth);
		resetButton.setX(resetX);
		resetButton.setY(widgetY);
	}

	// -------------------------------------------------------------------------
	// Validation helper
	// -------------------------------------------------------------------------

	/**
	 * Re-runs validation and stores the result. Called by concrete subclasses
	 * whenever the working value changes.
	 */
	protected void refreshValidation() {
		lastValidation = option.validate();
	}

	public ConfigOption.@NonNull ValidationResult getLastValidation() {
		return lastValidation;
	}

	public @NonNull ConfigOption<T> getOption() {
		return option;
	}

	// -------------------------------------------------------------------------
	// Reset button factory
	// -------------------------------------------------------------------------

	private @NonNull Button createResetButton(int x, int y) {
		return Button.builder(Component.literal("↺"), btn -> {
					option.resetToDefault();
					onValueReset();
					refreshValidation();
				})
				.pos(x, y)
				.size(RESET_BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("screenshot_utilities.settings.reset_to_default")))
				.build();
	}

	/**
	 * Called after the reset button has been pressed and the working value
	 * has been reset. Subclasses should synchronise their control widget's
	 * visual state here.
	 */
	protected abstract void onValueReset();
}
