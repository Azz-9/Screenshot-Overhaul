package me.Azz_9.screenshot_overhaul.client.guiFlexLib.components.config;

import org.jspecify.annotations.NonNull;

import me.Azz_9.flex_lib.client.gui.components.Div;
import me.Azz_9.flex_lib.client.gui.components.FlexButton;
import me.Azz_9.flex_lib.client.gui.events.EventType;
import me.Azz_9.flex_lib.client.gui.style.Length;
import me.Azz_9.flex_lib.client.gui.style.Sides;
import me.Azz_9.flex_lib.client.gui.style.StyleProperties;
import me.Azz_9.flex_lib.client.gui.style.properties.*;
import me.Azz_9.screenshot_overhaul.client.Colors;

public class UnsavedChangesOverlay extends Div {

	private static final int DIALOG_MIN_WIDTH = 280;
	private static final int DIALOG_MIN_HEIGHT = 100;
	private static final int BUTTON_WIDTH = 120;
	private static final int BUTTON_GAP = 8;

	private final @NonNull Runnable onCancel;
	private final @NonNull Runnable onDiscardAndQuit;

	private final @NonNull FlexButton cancelButton;
	private final @NonNull FlexButton discardButton;

	/**
	 * @param onCancel called when the user dismisses the overlay
	 * @param onDiscardAndQuit called when the user confirms quitting without saving
	 */
	public UnsavedChangesOverlay(
			@NonNull Runnable onCancel,
			@NonNull Runnable onDiscardAndQuit
	) {
		Div overlay = new Div();
		overlay.events().addEventListener(EventType.MOUSE_CLICK, _ -> onCancel.run());
		overlay.declaredStyle()
				.set(StyleProperties.POSITION, Position.ABSOLUTE)
				.set(StyleProperties.WIDTH, Length.percent(100))
				.set(StyleProperties.HEIGHT, Length.percent(100))
				.set(StyleProperties.BACKGROUND_COLOR, Colors.BLACK_TRANSPARENT)
				.set(StyleProperties.DISPLAY, Display.FLEX)
				.set(StyleProperties.JUSTIFY_CONTENT, JustifyContent.CENTER)
				.set(StyleProperties.ALIGN_ITEMS, AlignItems.CENTER);

		Div popup = new Div();
		popup.declaredStyle()
				.set(StyleProperties.WIDTH, Length.percent(40))
				.set(StyleProperties.MIN_WIDTH, Length.px(DIALOG_MIN_WIDTH))
				.set(StyleProperties.MIN_HEIGHT, Length.px(DIALOG_MIN_HEIGHT))
				.set(StyleProperties.BACKGROUND_COLOR, Colors.DARK_GRAY)
				.set(StyleProperties.BORDER, Sides.of(Border.of(Length.px(1), LineStyle.SOLID, Colors.GRAY)))
				.set(StyleProperties.PADDING, Sides.of(Length.px(11), Length.px(15)))
				.set(StyleProperties.DISPLAY, Display.FLEX)
				.set(StyleProperties.FLEX_DIRECTION, FlexDirection.COLUMN)
				.set(StyleProperties.GAP, Length.px(5));

		Div title = new Div();
		title.addText("screenshot_overhaul.settings.unsaved.title");
		title.declaredStyle()
				.set(StyleProperties.TEXT_ALIGN, TextAlign.CENTER);

		Div body = new Div();
		body.addText("screenshot_overhaul.settings.unsaved.title");
		body.declaredStyle()
				.set(StyleProperties.COLOR, Colors.LIGHT_GRAY)
				.set(StyleProperties.FLEX_GROW, 1f);

		Div buttonsRow = new Div();
		buttonsRow.declaredStyle()
				.set(StyleProperties.DISPLAY, Display.FLEX)
				.set(StyleProperties.JUSTIFY_CONTENT, JustifyContent.CENTER)
				.set(StyleProperties.GAP, Length.px(BUTTON_GAP));

		cancelButton = new FlexButton();
		cancelButton.addText("screenshot_overhaul.settings.unsaved.cancel");
		cancelButton.events().addEventListener(EventType.MOUSE_CLICK, _ -> onCancel.run());
		cancelButton.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(BUTTON_WIDTH));

		discardButton = new FlexButton();
		discardButton.addText("screenshot_overhaul.settings.unsaved.discard");
		discardButton.events().addEventListener(EventType.MOUSE_CLICK, _ -> onDiscardAndQuit.run());
		discardButton.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(BUTTON_WIDTH));

		buttonsRow.addAll(cancelButton, discardButton);

		popup.add(title);

		overlay.add(popup);

		this.onCancel = onCancel;
		this.onDiscardAndQuit = onDiscardAndQuit;
	}
}
