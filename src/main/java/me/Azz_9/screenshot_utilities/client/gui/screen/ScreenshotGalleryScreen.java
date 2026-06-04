package me.Azz_9.screenshot_utilities.client.gui.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.FullViewWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.ScreenshotGalleryWidget;
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
	private static final int SETTINGS_BUTTON_WIDTH = 120;
	private static final int SETTINGS_BUTTON_HEIGHT = 20;

	// widgets
	private @Nullable ScreenshotGalleryWidget gallery;
	private @Nullable FullViewWidget fullView;
	private @Nullable Button settingsButton;

	public ScreenshotGalleryScreen() {
		super(Component.translatable("screenshot_utilities.narrator.screenshot_gallery"));
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	@Override
	protected void initContent() {
		settingsButton = createSettingsButton();
		gallery = createGallery();
		fullView = createFullView();

		addRenderableWidget(gallery);
		addRenderableWidget(settingsButton);
		// fullView is added last so it renders on top of everything, including the bottom bar
		addRenderableWidget(fullView);

		ScreenshotList.setOnChangeListener(_ -> {
			if (fullView == null || gallery == null) return;

			Screenshot current = fullView.getCurrentScreenshot();
			if (current != null && !current.file().exists()) {
				Screenshot next = gallery.getNextVisibleScreenshot(current);
				if (next != null) {
					fullView.show(next);
				} else {
					Screenshot prev = gallery.getPreviousVisibleScreenshot(current);
					if (prev != null) fullView.show(prev);
					else closeFullView();
				}
			}

			gallery.refresh(false, () -> {
				if (fullView.isVisible()) fullView.refreshNavButtons();
			});
		});
	}

	private Button createSettingsButton() {
		return Button.builder(Component.translatable("screenshot_utilities.settings"), (_) ->
						MINECRAFT.setScreen(Config.getInstance().getSettingsScreen(this)))
				.bounds(width - SETTINGS_BUTTON_WIDTH - GLOBAL_PADDING, GLOBAL_PADDING, SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT)
				.build();
	}

	private ScreenshotGalleryWidget createGallery() {
		return new ScreenshotGalleryWidget(
				GLOBAL_PADDING, GLOBAL_PADDING,
				width - SETTINGS_BUTTON_WIDTH - GLOBAL_PADDING * 2 - 20, getBottomBarTop() - GLOBAL_PADDING,
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
		if (!DeleteScreenshot.delete(screenshot))
			ScreenshotLogger.error("Could not delete screenshot: " + screenshot.pathRelativeToScreenshotDir());
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
		if (settingsButton != null) settingsButton.extractRenderState(graphics, bgMouseX, bgMouseY, deltaTicks);

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
		if (fullView != null && fullView.isVisible())
			return fullView.keyPressed(event);

		if (event.key() == InputConstants.KEY_F5 && gallery != null) {
			gallery.refresh(true);
			return true;
		}
		return super.keyPressed(event);
	}

	/* ---------------- Cleanup ---------------- */

	@Override
	protected void onSave() {
		// TrackedItems (screenshot title edits) are committed by super
		super.onSave();
	}

	@Override
	public void onClose() {
		ScreenshotManager.save();

		gallery = null;
		fullView = null;
		super.onClose();
	}
}
