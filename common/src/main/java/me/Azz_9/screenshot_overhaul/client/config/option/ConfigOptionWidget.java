package me.Azz_9.screenshot_overhaul.client.config.option;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.Colors;

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
public abstract class ConfigOptionWidget<T> extends AbstractWidget {

	private static final Identifier RESET_BUTTON_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/reset");

	// -------------------------------------------------------------------------
	// Layout constants
	// -------------------------------------------------------------------------

	public static final int ROW_HEIGHT = 20;
	public static final int ROW_PADDING_V = 4;
	public static final int FULL_ROW_HEIGHT = ROW_HEIGHT + ROW_PADDING_V * 2;

	private static final float LABEL_WIDTH_FRACTION = 0.40f;
	private static final int LABEL_MARGIN = 2;
	private static final int RESET_BUTTON_WIDTH = 20;
	private static final int CONTROL_RESET_GAP = 4;
	private static final int LABEL_CONTROL_GAP = 8;

	// -------------------------------------------------------------------------
	// State
	// -------------------------------------------------------------------------

	protected final @NonNull ConfigOption<T> option;

	private AbstractWidget controlWidget;
	private Button resetButton;

	private GuiEventListener activeWidget;

	/**
	 * Cached last-known validation result so we don't validate every frame.
	 */
	private ConfigOption.@NonNull ValidationResult lastValidation = ConfigOption.ValidationResult.valid();

	private final @NonNull Consumer<GuiEventListener> onFocusRequested;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	protected ConfigOptionWidget(int x, int y, int width, @NonNull ConfigOption<T> option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, FULL_ROW_HEIGHT, Component.empty());
		this.option = option;
		this.onFocusRequested = onFocusRequested;
	}

	public final void init() {
		int labelWidth = (int) (width * LABEL_WIDTH_FRACTION);
		int resetX = getX() + width - RESET_BUTTON_WIDTH;
		int controlWidth = width - labelWidth - LABEL_CONTROL_GAP - RESET_BUTTON_WIDTH - CONTROL_RESET_GAP;
		int controlX = getX() + labelWidth + LABEL_CONTROL_GAP;
		int widgetY = getY() + ROW_PADDING_V;

		this.controlWidget = createControlWidget(controlX, widgetY, controlWidth);
		this.resetButton = createResetButton(resetX, widgetY);
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
		graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
				.acceptScrolling(
						option.getLabel().copy().withColor(labelColor),
						getX() + LABEL_MARGIN,
						getX() + LABEL_MARGIN, controlWidget.getX() - LABEL_MARGIN,
						getY(), getBottom()
				);

		// Propagate enabled state to sub-widgets
		controlWidget.active = enabled;
		resetButton.active = enabled && !option.getWorkingValue().equals(option.getDefaultValue());

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

		if (controlWidget.mouseClicked(event, doubleClick)) {
			activeWidget = controlWidget;
			onFocusRequested.accept(controlWidget);
			return true;
		}

		if (resetButton.mouseClicked(event, doubleClick)) {
			activeWidget = resetButton;
			return true;
		}

		activeWidget = null;
		return false;
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
		if (!option.isDependencySatisfied()) return false;
		return activeWidget instanceof AbstractWidget widget
				&& widget.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		if (!option.isDependencySatisfied()) return false;
		boolean handled = false;

		if (activeWidget instanceof AbstractWidget widget) {
			handled = widget.mouseReleased(event);
		}

		activeWidget = null;

		return handled;
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
		Button button = SpriteIconButton.CenteredIcon.builder(Component.translatable("screenshot_overhaul.settings.reset_to_default"), btn -> {
					option.resetToDefault();
					onValueReset();
					refreshValidation();
				}, true)
				.withTootip()
				.sprite(RESET_BUTTON_SPRITE, 15, 15)
				.size(RESET_BUTTON_WIDTH, ROW_HEIGHT)
				.build();
		button.setPosition(x, y);
		button.active = !option.getWorkingValue().equals(option.getDefaultValue());
		return button;
	}

	/**
	 * Called after the reset button has been pressed and the working value
	 * has been reset. Subclasses should synchronize their control widget's
	 * visual state here.
	 */
	protected abstract void onValueReset();
}
