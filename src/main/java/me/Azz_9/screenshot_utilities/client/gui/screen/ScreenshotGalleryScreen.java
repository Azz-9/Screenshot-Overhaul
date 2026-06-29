package me.Azz_9.screenshot_utilities.client.gui.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.gui.components.FullViewWidget;
import me.Azz_9.screenshot_utilities.client.gui.components.screenshotGallery.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_utilities.client.gui.components.toasts.CustomToastId;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.screenshot.DeleteScreenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotManager;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryScreen extends AbstractSavableScreen implements FocusableScreen {

	// focus manager
	private final @NonNull FocusManager focusManager = new FocusManager();

	// layout
	private static final int GLOBAL_PADDING = 10;

	// widgets
	private @Nullable ScreenshotGalleryWidget gallery;
	private @Nullable FullViewWidget fullView;

	public ScreenshotGalleryScreen() {
		super(Component.translatable("screenshot_utilities.narrator.screenshot_gallery"));
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	@Override
	protected void initContent() {
		gallery = createGallery();
		fullView = createFullView();

		addRenderableWidget(gallery);
		// fullView is added last so it renders on top of everything, including the bottom bar
		addRenderableWidget(fullView);

		ScreenshotList.setOnChangeListener(_ -> {
			if (fullView == null || gallery == null) return;

			Screenshot current = fullView.getCurrentScreenshot();
			boolean currentDeleted = current != null && !current.file().exists();

			Screenshot next = currentDeleted ? gallery.getNextVisibleScreenshot(current) : null;
			Screenshot prev = (currentDeleted && next == null) ? gallery.getPreviousVisibleScreenshot(current) : null;

			gallery.refresh(false, () -> {
				if (currentDeleted) {
					if (next != null) fullView.show(next);
					else if (prev != null) fullView.show(prev);
					else closeFullView();
				}
				if (fullView.isVisible()) fullView.refreshNavButtons();
			});
		});
	}

	private ScreenshotGalleryWidget createGallery() {
		return new ScreenshotGalleryWidget(
				GLOBAL_PADDING, GLOBAL_PADDING,
				width - GLOBAL_PADDING * 2, getBottomBarTop() - GLOBAL_PADDING,
				this::setTrackedItems, this::openFullView);
	}

	private FullViewWidget createFullView() {
		FullViewWidget fullView = new FullViewWidget(
				width, height,
				this::onFullViewScreenshotChanged,
				this::onDeleteRequested,
				this::closeFullView,
				s -> gallery != null ? gallery.getNextVisibleScreenshot(s) : null,
				s -> gallery != null ? gallery.getPreviousVisibleScreenshot(s) : null);

		fullView.hide(); // starts hidden
		return fullView;
	}

	// -------------------------------------------------------------------------
	// Fullview open / close
	// -------------------------------------------------------------------------

	public void openFullView(@NonNull Screenshot screenshot) {
		if (fullView == null) return;
		fullView.show(screenshot);
		clearFocus();

		// Freeze the gallery layer and the settings button
		if (gallery != null) {
			gallery.preloadAround(screenshot);
		}
	}

	private void closeFullView() {
		if (fullView != null)
			fullView.hide();
	}

	private void onFullViewScreenshotChanged(@NonNull Screenshot screenshot) {
		if (gallery != null) gallery.preloadAround(screenshot);
	}

	private void onDeleteRequested(@NonNull Screenshot screenshot) {
		if (gallery == null || fullView == null) return;

		Screenshot next = gallery.getNextVisibleScreenshot(screenshot);
		Screenshot prev = next == null ? gallery.getPreviousVisibleScreenshot(screenshot) : null;

		final double savedScroll = gallery.getScrollOffset();
		gallery.removeEntry(screenshot);
		gallery.setScrollOffset(savedScroll);

		if (next != null) fullView.show(next);
		else if (prev != null) fullView.show(prev);
		else closeFullView();

		if (fullView.isVisible()) fullView.refreshNavButtons();

		CompletableFuture.runAsync(() -> {
			boolean success = DeleteScreenshot.delete(screenshot);
			if (!success) {
				ScreenshotLogger.error("Could not delete screenshot: " + screenshot.pathRelativeToScreenshotDir());
				MINECRAFT.execute(() -> {
					// Rollback : refresh remet le screenshot dans la liste
					gallery.refresh(false, null);
					// Toaster
					SystemToast.add(
							MINECRAFT.gui.toastManager(),
							CustomToastId.SCREENSHOT_DELETE_FAILED,
							Component.translatable("screenshot_utilities.delete_failed.title"),
							Component.translatable("screenshot_utilities.delete_failed.message", screenshot.file().getName())
					);
				});
			}
			// Si succès : le WatchService va déclencher un refresh,
			// mais removeEntry a déjà retiré le screenshot — buildEntries
			// reconstruira la liste sans lui, aucun effet visible
		});
	}

	// render

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		boolean fullViewActive = fullView != null && fullView.isVisible();

		// Pass (-1,-1) to every widget except fullView while the overlay is up,
		// so no thumbnail, no settings button, and no bottom-bar button shows hover.
		int bgMouseX = fullViewActive ? -1 : mouseX;
		int bgMouseY = fullViewActive ? -1 : mouseY;

		if (gallery != null) gallery.extractRenderState(graphics, bgMouseX, bgMouseY, deltaTicks);

		super.extractRenderState(graphics, bgMouseX, bgMouseY, deltaTicks);

		// FullView renders on top with real coords
		if (fullView != null) fullView.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
	}

	/* ---------------- Inputs ---------------- */

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		// FullView intercepts everything when active
		if (fullView != null && fullView.isVisible())
			return fullView.mouseClicked(event, doubleClick);
		boolean handled = super.mouseClicked(event, doubleClick);
		if (!handled) {
			focusManager.clearFocus();
		}

		return handled;
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		if (fullView != null && fullView.isVisible())
			return fullView.mouseReleased(event);
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
		if (fullView != null && fullView.isVisible())
			return fullView.mouseDragged(event, dx, dy);
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (fullView != null && fullView.isVisible())
			return fullView.mouseScrolled(x, y, scrollX, scrollY);
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (getGlobalFocused() instanceof GuiEventListener l && l.keyPressed(event))
			return true;

		if (fullView != null && fullView.isVisible())
			return fullView.keyPressed(event);

		if (event.key() == InputConstants.KEY_F5 && gallery != null) {
			gallery.refresh(true);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(@NonNull CharacterEvent event) {
		if (getGlobalFocused() instanceof GuiEventListener l && l.charTyped(event))
			return true;
		if (fullView != null && fullView.isVisible())
			return fullView.charTyped(event);
		return super.charTyped(event);
	}

	/* ---------------- Cleanup ---------------- */

	@Override
	public void onClose() {
		ScreenshotManager.save();

		gallery = null;
		fullView = null;
		super.onClose();
	}
}
