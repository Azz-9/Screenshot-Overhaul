package me.Azz_9.screenshot_utilities.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.components.config.UnsavedChangesOverlay;
import me.Azz_9.screenshot_utilities.client.gui.trackableChanges.TrackableChanges;

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
@Environment(EnvType.CLIENT)
public abstract class AbstractSavableScreen extends AbstractBackNavigableScreen {

	// -------------------------------------------------------------------------
	// Layout
	// -------------------------------------------------------------------------

	protected static final int BOTTOM_BAR_HEIGHT = 36;
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 10;

	// -------------------------------------------------------------------------
	// Tracked items — identity set so distinct equal objects are both tracked
	// -------------------------------------------------------------------------

	private final Set<TrackableChanges> trackedItems = Collections.newSetFromMap(new IdentityHashMap<>());

	// -------------------------------------------------------------------------
	// Widgets
	// -------------------------------------------------------------------------

	private @Nullable Button saveButton;
	private @Nullable Button cancelButton;

	// -------------------------------------------------------------------------
	// Overlay
	// -------------------------------------------------------------------------

	private @Nullable UnsavedChangesOverlay unsavedOverlay;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	protected AbstractSavableScreen(@NonNull Component title, @Nullable Screen parent) {
		super(title, parent);
	}

	public AbstractSavableScreen(Component title) {
		super(title);
	}

	// -------------------------------------------------------------------------
	// Screen lifecycle
	// -------------------------------------------------------------------------

	@Override
	protected final void init() {
		initContent();

		int buttonsY = height - BOTTOM_BAR_HEIGHT + (BOTTOM_BAR_HEIGHT - BUTTON_HEIGHT) / 2;
		int totalW = BUTTON_WIDTH * 2 + BUTTON_GAP;
		int startX = (width - totalW) / 2;

		cancelButton = Button.builder(
						Component.translatable("screenshot_utilities.cancel"),
						btn -> onClose())
				.pos(startX, buttonsY)
				.size(BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();

		saveButton = Button.builder(
						Component.translatable("screenshot_utilities.save_and_quit"),
						btn -> onSavePressed())
				.pos(startX + BUTTON_WIDTH + BUTTON_GAP, buttonsY)
				.size(BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		saveButton.active = canSave();

		addRenderableWidget(cancelButton);
		addRenderableWidget(saveButton);

		if (unsavedOverlay != null) {
			unsavedOverlay.resize(width, height);
		}
	}

	/**
	 * Called inside init() before the bottom bar is created.
	 * Subclasses add their widgets here instead of overriding init().
	 */
	protected abstract void initContent();

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		// Suppress hover / tooltips on everything below the overlay
		int mx = unsavedOverlay != null ? -1 : mouseX;
		int my = unsavedOverlay != null ? -1 : mouseY;

		super.extractRenderState(graphics, mx, my, deltaTicks);

		// Bottom-bar separator
		graphics.fill(0, height - BOTTOM_BAR_HEIGHT, width, height - BOTTOM_BAR_HEIGHT + 1, Colors.GRAY);

		if (saveButton != null) {
			saveButton.active = canSave();
		}

		if (unsavedOverlay != null) {
			unsavedOverlay.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}
	}

	// -------------------------------------------------------------------------
	// Event routing — overlay takes absolute priority
	// -------------------------------------------------------------------------


	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		if (unsavedOverlay != null) return unsavedOverlay.mouseClicked(event, doubleClick);
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		if (unsavedOverlay != null) return unsavedOverlay.mouseReleased(event);
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
		if (unsavedOverlay != null) return unsavedOverlay.mouseDragged(event, dx, dy);
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
		if (unsavedOverlay != null) return unsavedOverlay.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
		return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (unsavedOverlay != null) return unsavedOverlay.keyPressed(event);
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(@NonNull CharacterEvent event) {
		if (unsavedOverlay != null) return unsavedOverlay.charTyped(event);
		return super.charTyped(event);
	}

	// -------------------------------------------------------------------------
	// Close guard
	// -------------------------------------------------------------------------

	@Override
	public void onClose() {
		if (hasUnsavedChanges()) {
			showUnsavedOverlay();
		} else {
			super.onClose();
		}
	}

	// -------------------------------------------------------------------------
	// Tracked-items API
	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------
	// Save hook
	// -------------------------------------------------------------------------

	/**
	 * <p>Called when the user confirms "Save and quit", before the screen closes.</p>
	 * <p>
	 * The default implementation is a no-op. Override to flush working values to
	 * disk, trigger async renames, etc.
	 * </p>
	 * <p>
	 * Note: the screen closes after this method returns. If your save is async and
	 * you need to delay the close, do not call super.onSave() and schedule
	 * super.onClose() yourself once the async work completes.
	 * </p>
	 */
	protected synchronized void onSave() {
		trackedItems.forEach(TrackableChanges::commitChanges);
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------
	// Layout helper for subclasses
	// -------------------------------------------------------------------------

	/**
	 * Returns the Y coordinate of the top edge of the bottom bar.
	 * Subclasses use this to avoid placing scrollable content behind the bar.
	 */
	protected int getBottomBarTop() {
		return height - BOTTOM_BAR_HEIGHT;
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
		if (saveButton != null) saveButton.active = active;
	}

	/**
	 * Activates or deactivates the Cancel button.
	 */
	protected void setCancelButtonActive(boolean active) {
		if (cancelButton != null) cancelButton.active = active;
	}

	/**
	 * Convenience: activates or deactivates both bottom-bar buttons at once.
	 * Useful when entering / leaving a full-screen overlay state.
	 */
	protected void setSaveAndCancelActive(boolean active) {
		setSaveButtonActive(active);
		setCancelButtonActive(active);
	}

	/**
	 * Renders only the Cancel and Save buttons with the provided mouse coordinates.
	 * Useful for subclasses that override {@link #extractRenderState(GuiGraphicsExtractor, int, int, float)} and need fine-grained
	 * control over which coord pair reaches the bottom bar (e.g. to suppress hover
	 * while a foreground overlay is active).
	 */
	protected void renderBottomBarButtons(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		if (cancelButton != null) cancelButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		if (saveButton != null) saveButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
	}
}