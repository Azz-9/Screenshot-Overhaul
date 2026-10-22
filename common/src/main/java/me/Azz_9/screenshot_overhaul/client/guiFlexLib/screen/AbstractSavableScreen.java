package me.Azz_9.screenshot_overhaul.client.guiFlexLib.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import me.Azz_9.flex_lib.client.gui.components.Div;
import me.Azz_9.flex_lib.client.gui.components.FlexButton;
import me.Azz_9.flex_lib.client.gui.events.EventType;
import me.Azz_9.flex_lib.client.gui.screens.FlexScreen;
import me.Azz_9.flex_lib.client.gui.style.Length;
import me.Azz_9.flex_lib.client.gui.style.StyleProperties;
import me.Azz_9.flex_lib.client.gui.style.properties.AlignItems;
import me.Azz_9.flex_lib.client.gui.style.properties.Display;
import me.Azz_9.flex_lib.client.gui.style.properties.JustifyContent;
import me.Azz_9.screenshot_overhaul.client.gui.components.config.UnsavedChangesOverlay;
import me.Azz_9.screenshot_overhaul.client.gui.trackableChanges.TrackableChanges;

/**
 * <p>
 * Abstract base screen for any screen that owns a collection of {@link TrackableChanges}
 * items and needs a "Cancel" / "Save and quit" bottom bar with an "unsaved changes"
 * overlay guard.
 * </p>
 *
 * <p>
 * Contract for subclasses:
 * <ol>
 *     <li>Call registerTracked / unregisterTracked to keep the tracked set in sync.</li>
 *     <li>Override initContent() instead of init() to add your own widgets.</li>
 * </ol>
 * </p>
 *
 * <p>
 * Save button state:
 * Active only when at least one tracked item hasChanged() AND all are isValid().
 * </p>
 * <p>
 * Overlay behavior:
 * While the overlay is visible every event is consumed before reaching underlying
 * widgets. Tooltips are suppressed by passing (-1,-1) as mouse coords.
 * </p>
 */
public abstract class AbstractSavableScreen extends FlexScreen {

	protected static final int BOTTOM_BAR_HEIGHT = 36;
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 10;

	private final @NonNull Set<TrackableChanges> trackedItems = Collections.newSetFromMap(new IdentityHashMap<>());

	private @Nullable Div bottomBar;
	private @Nullable FlexButton saveButton;
	private @Nullable FlexButton cancelButton;

	private @Nullable UnsavedChangesOverlay unsavedOverlay;

	protected AbstractSavableScreen(@NonNull Component title, @Nullable Screen parent) {
		super(title, parent);
	}

	public AbstractSavableScreen(@NonNull Component title) {
		super(title);
	}

	// Screen lifecycle

	@Override
	protected final void init() {
		// todo add top border
		bottomBar = new Div();
		bottomBar.declaredStyle()
				.set(StyleProperties.HEIGHT, Length.px(BOTTOM_BAR_HEIGHT))
				.set(StyleProperties.WIDTH, Length.percent(100))
				.set(StyleProperties.DISPLAY, Display.FLEX)
				.set(StyleProperties.JUSTIFY_CONTENT, JustifyContent.CENTER)
				.set(StyleProperties.ALIGN_ITEMS, AlignItems.CENTER)
				.set(StyleProperties.GAP, Length.px(BUTTON_GAP));

		cancelButton = new FlexButton();
		cancelButton.addText("screenshot_overhaul.cancel");
		cancelButton.events().addEventListener(EventType.MOUSE_DOWN, _ -> onClose());
		cancelButton.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(BUTTON_WIDTH))
				.set(StyleProperties.HEIGHT, Length.px(BUTTON_HEIGHT));

		saveButton = new FlexButton();
		saveButton.addText("screenshot_overhaul.save_and_quit");
		saveButton.events().addEventListener(EventType.MOUSE_DOWN, _ -> onSavePressed());
		saveButton.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(BUTTON_WIDTH))
				.set(StyleProperties.HEIGHT, Length.px(BUTTON_HEIGHT));

		saveButton.setDisabled(!canSave());

		bottomBar.addAll(cancelButton, saveButton);

		root().addAll(bottomBar);
	}

	// Close guard

	@Override
	public void onClose() {
		if (hasUnsavedChanges()) {
			showUnsavedOverlay();
		} else {
			super.onClose();
		}
	}

	// Tracked-items API

	/**
	 * Registers an item so this screen considers it when computing dirty / valid state.
	 * Safe to call at any point in the screen lifetime.
	 */
	public synchronized void registerTracked(@NonNull TrackableChanges item) {
		trackedItems.add(item);
	}

	/**
	 * Unregisters an item. No-op if the item was not registered.
	 */
	public synchronized void unregisterTracked(@NonNull TrackableChanges item) {
		trackedItems.remove(item);
	}

	/**
	 * Replaces the entire tracked set.
	 * Useful when the visible content changes wholesale (e.g. page change in a gallery).
	 */
	public synchronized void setTrackedItems(@NonNull Collection<? extends TrackableChanges> items) {
		trackedItems.clear();
		trackedItems.addAll(items);
	}

	/**
	 * Returns an unmodifiable snapshot of the currently tracked items.
	 */
	public synchronized @NonNull Set<TrackableChanges> getTrackedItems() {
		return Collections.unmodifiableSet(trackedItems);
	}

	// Save hook

	/**
	 * <p>Called when the user confirms "Save and quit", before the screen closes.</p>
	 * <p>
	 * The default implementation is a no-op. Override to flush working values to
	 * disk, trigger async renames, etc.
	 * </p>
	 * <p>
	 * Note: the screen closes after this method returns. If your save is async, and
	 * you need to delay the close, do not call super.onSave() and schedule
	 * super.onClose() yourself once the async work completes.
	 * </p>
	 */
	protected synchronized void onSave() {
		trackedItems.forEach(TrackableChanges::commitChanges);
	}

	// Internal helpers

	private synchronized boolean hasUnsavedChanges() {
		return trackedItems.stream().anyMatch(TrackableChanges::hasChanged);
	}

	private synchronized boolean canSave() {
		return hasUnsavedChanges() && trackedItems.stream().allMatch(TrackableChanges::isValid);
	}

	private void onSavePressed() {
		onSave();
		super.onClose();
	}

	private synchronized void showUnsavedOverlay() {
		unsavedOverlay = new UnsavedChangesOverlay(
				width, height,
				/* onCancel  */ () -> unsavedOverlay = null,
				/* onDiscard */ () -> {
			trackedItems.forEach(TrackableChanges::revertChanges);
			unsavedOverlay = null;
			super.onClose();
		}
		);
	}

	/**
	 * Activates or deactivates the Save button independently of the normal
	 * dirty/valid computation. Use this when the screen has a secondary overlay
	 * (e.g. a full-view widget) that should temporarily lock the bottom bar.
	 * <p>
	 * Note: even when forced active here, the save button will still be
	 * deactivated by the dirty/valid check on the next render frame unless
	 * there are pending changes. Prefer {@link #setSaveAndCancelActive(boolean)}
	 * to act on both buttons at once.
	 */
	protected void setSaveButtonActive(boolean active) {
		if (saveButton != null)
			saveButton.setDisabled(!active);
	}

	/**
	 * Activates or deactivates the Cancel button.
	 */
	protected void setCancelButtonActive(boolean active) {
		if (cancelButton != null)
			cancelButton.setDisabled(!active);
	}

	/**
	 * Convenience: activates or deactivates both bottom-bar buttons at once.
	 * Useful when entering / leaving a full-screen overlay state.
	 */
	protected void setSaveAndCancelActive(boolean active) {
		setSaveButtonActive(active);
		setCancelButtonActive(active);
	}
}