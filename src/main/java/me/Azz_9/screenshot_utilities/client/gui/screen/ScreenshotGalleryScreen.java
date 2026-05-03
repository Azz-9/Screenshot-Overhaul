package me.Azz_9.screenshot_utilities.client.gui.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.NavigationButton;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent.ScreenshotEntryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.FavoriteManager;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryScreen extends Screen implements FocusableScreen {

	// focus manager
	private final @NonNull FocusManager focusManager = new FocusManager();

	// layout
	private static final int GLOBAL_PADDING = 10;
	private static final int FULL_VIEW_PADDING = 40;
	private static final int BOTTOM_PADDING = 60;

	// settings
	private static final int SETTINGS_BUTTON_WIDTH = 120;
	private static final int SETTINGS_BUTTON_HEIGHT = 20;
	private Button settingsButton;

	// gallery
	private @Nullable ScreenshotGalleryWidget gallery;

	// quit buttons
	private static final int QUIT_BUTTONS_WIDTH = 200;
	private static final int QUIT_BUTTONS_HEIGHT = 20;
	private Button saveAndQuitButton;
	private Button cancelButton;

	// full view
	private static final int NAV_BUTTON_SIZE = 20;
	private static final int NAV_BUTTON_PADDING = 10;
	private NavigationButton backButton, nextButton;
	private @Nullable ScreenshotTexture selectedTexture = null;
	private @Nullable ScreenshotTexture outgoingTexture = null;
	// transition
	private static final float TRANSITION_DURATION = 0.2f; // secondes
	private float transitionTime = 0f;
	private int transitionDirection = 0; // -1 = back, +1 = next
	private boolean inTransition = false;

	public ScreenshotGalleryScreen() {
		super(Component.translatable("screenshot_utilities.narrator.screenshot_gallery"));
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	@Override
	protected void init() {
		settingsButton = Button.builder(Component.translatable("screenshot_utilities.settings"), (btn) -> {
					MINECRAFT.setScreen(new SettingsScreen(this));
				})
				.bounds(width - SETTINGS_BUTTON_WIDTH - GLOBAL_PADDING, GLOBAL_PADDING, SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT)
				.build();

		gallery = new ScreenshotGalleryWidget(
				GLOBAL_PADDING, GLOBAL_PADDING,
				width - SETTINGS_BUTTON_WIDTH - GLOBAL_PADDING * 2 - 20, height - QUIT_BUTTONS_HEIGHT - GLOBAL_PADDING * 3,
				Config.getInstance().getScreenshotsDir().toFile()
		);

		saveAndQuitButton = Button.builder(
						Component.translatable("screenshot_utilities.save_and_quit"),
						(btn) -> saveAndQuit()
				).bounds(width / 2 + 10, height - QUIT_BUTTONS_HEIGHT - 10, QUIT_BUTTONS_WIDTH, QUIT_BUTTONS_HEIGHT)
				.build();

		cancelButton = Button.builder(Component.translatable("screenshot_utilities.cancel"), (btn) -> this.onClose())
				.bounds(width / 2 - 10 - QUIT_BUTTONS_WIDTH, height - QUIT_BUTTONS_HEIGHT - 10, QUIT_BUTTONS_WIDTH, QUIT_BUTTONS_HEIGHT)
				.build();

		backButton = new NavigationButton(
				(width - NAV_BUTTON_PADDING) / 2 - NAV_BUTTON_SIZE, height - 40,
				NAV_BUTTON_SIZE, NAV_BUTTON_SIZE,
				NavigationButton.NavigationTypes.BACK, (btn) -> selectPrevious());
		nextButton = new NavigationButton(
				(width + NAV_BUTTON_PADDING) / 2, height - 40,
				NAV_BUTTON_SIZE, NAV_BUTTON_SIZE,
				NavigationButton.NavigationTypes.NEXT, (btn) -> selectNext());

		backButton.active = false;
		backButton.visible = false;
		nextButton.active = false;
		nextButton.visible = false;

		addWidget(backButton);
		addWidget(nextButton);
		addRenderableWidget(gallery);
		addRenderableWidget(saveAndQuitButton);
		addRenderableWidget(cancelButton);
		addRenderableWidget(settingsButton);
	}

	// save and quit

	private void saveAndQuit() {
		if (gallery == null) return;

		for (ScreenshotEntryWidget entry : gallery.getEntries()) {
			if (entry.hasNameChanged()) {
				Path oldPath = entry.getScreenshotFile().toPath();
				Path newPath = entry.getScreenshotFile().toPath().resolveSibling(entry.getName());
				try {
					Files.move(oldPath, newPath);
					FavoriteManager.changeAbsoluteFilePath(oldPath, newPath);
				} catch (IOException e) {
					ScreenshotLogger.error("Failed to rename screenshot file : {}", e.getMessage());
				}
			}
		}
		this.onClose();
	}

	public void updateSaveAndQuit() {
		saveAndQuitButton.active = anyChanges();
	}

	private boolean anyChanges() {
		if (gallery == null) return false;

		return gallery.getEntries().stream().anyMatch(ScreenshotEntryWidget::hasNameChanged);
	}

	// Full view

	public void selectScreenshot(@NonNull ScreenshotTexture texture) {
		selectedTexture = texture;

		backButton.visible = true;
		nextButton.visible = true;

		updateNavButtons();

		saveAndQuitButton.active = false;
		cancelButton.active = false;
		settingsButton.active = false;
		if (gallery != null) {
			gallery.setActive(false);

			// pre-load the next et previous screenshots
			ScreenshotTexture next = gallery.getNextVisibleEntry(selectedTexture);
			ScreenshotTexture prev = gallery.getPreviousVisibleEntry(selectedTexture);
			if (next != null) ScreenshotTextureCache.getFullView(next.getFile());
			if (prev != null) ScreenshotTextureCache.getFullView(prev.getFile());

			gallery.preloadAround(selectedTexture);
		}
	}

	public void deselectScreenshot() {
		selectedTexture = null;
		backButton.active = false;
		backButton.visible = false;
		nextButton.active = false;
		nextButton.visible = false;

		updateSaveAndQuit();
		cancelButton.active = true;
		settingsButton.active = true;
		if (gallery != null) {
			gallery.setActive(true);
		}
	}

	private void selectPrevious() {
		if (selectedTexture == null || inTransition || gallery == null) return;

		ScreenshotTexture prev = gallery.getPreviousVisibleEntry(selectedTexture);
		if (prev != null) {
			gallery.preloadAround(prev);
			startTransition(prev, -1);
		}
	}

	private void selectNext() {
		if (selectedTexture == null || inTransition || gallery == null) return;

		ScreenshotTexture next = gallery.getNextVisibleEntry(selectedTexture);
		if (next != null) {
			gallery.preloadAround(next);
			startTransition(next, +1);
		}
	}

	private void startTransition(ScreenshotTexture next, int direction) {
		this.outgoingTexture = this.selectedTexture;
		this.selectedTexture = next;

		this.transitionDirection = direction;
		this.transitionTime = 0f;
		this.inTransition = true;

		updateNavButtons();
	}


	private void updateNavButtons() {
		if (gallery == null || selectedTexture == null) return;

		backButton.active = gallery.getPreviousVisibleEntry(selectedTexture) != null;
		nextButton.active = gallery.getNextVisibleEntry(selectedTexture) != null;
	}

	// render

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

		float dt = deltaTicks / 20f;

		if (inTransition) {
			transitionTime += dt;
			if (transitionTime >= TRANSITION_DURATION) {
				transitionTime = TRANSITION_DURATION;
				inTransition = false;
				outgoingTexture = null;
			}
		}

		if (selectedTexture != null) {
			graphics.requestCursor(CursorType.DEFAULT);

			graphics.fill(0, 0, width, height, Colors.BLACK_TRANSPARENT);

			if (!inTransition || outgoingTexture == null) {
				drawScreenshot(graphics, ScreenshotTextureCache.getFullView(selectedTexture.getFile()), 0);
			} else {
				float t = transitionTime / TRANSITION_DURATION;
				t = t * t * (3f - 2f * t); // smoothstep

				int slide = (int) (width * t);

				// ancien screenshot (sortant)
				drawScreenshot(
						graphics,
						ScreenshotTextureCache.getFullView(outgoingTexture.getFile()),
						-slide * transitionDirection
				);

				// nouveau screenshot (entrant)
				drawScreenshot(
						graphics,
						ScreenshotTextureCache.getFullView(selectedTexture.getFile()),
						(width - slide) * transitionDirection
				);
			}

			nextButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
			backButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}
	}

	private void drawScreenshot(@NonNull GuiGraphicsExtractor graphics, @Nullable ScreenshotTexture texture, int offsetX) {
		int fullViewWidth = width - FULL_VIEW_PADDING * 2;
		int fullViewHeight = height - FULL_VIEW_PADDING - BOTTOM_PADDING;

		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();

		matrices.translate(offsetX, 0);

		if (texture == null) {
			graphics.fill(
					FULL_VIEW_PADDING, FULL_VIEW_PADDING,
					FULL_VIEW_PADDING + fullViewWidth, FULL_VIEW_PADDING + fullViewHeight,
					Colors.BLACK_TRANSPARENT
			);

			Loading.drawLoadingSpinner(
					graphics,
					FULL_VIEW_PADDING + fullViewWidth / 2,
					FULL_VIEW_PADDING + fullViewHeight / 2,
					fullViewHeight / 20, fullViewHeight / 10
			);

		} else {

			ScreenshotDrawHelper.drawContain(
					graphics,
					texture,
					FULL_VIEW_PADDING,
					FULL_VIEW_PADDING,
					fullViewWidth,
					fullViewHeight
			);
		}

		matrices.popMatrix();
	}

	/* ---------------- Inputs ---------------- */

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		boolean handled = false;

		Optional<GuiEventListener> optional = this.getChildAt(click.x(), click.y());
		if (optional.isPresent()) {
			GuiEventListener element = optional.get();
			if (element.mouseClicked(click, doubled) && element.shouldTakeFocusAfterInteraction()) {
				this.setFocused(element);
				if (click.button() == 0) {
					this.setDragging(true);
				}

				handled = true;
			}

		}

		if (!handled) {
			focusManager.clearFocus();
		}

		return handled;
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent input) {
		if (selectedTexture != null && input.isEscape()) {
			deselectScreenshot();
			return true;
		}
		if (input.key() == InputConstants.KEY_F5 && gallery != null) {
			gallery.refresh();
			return true;
		}
		return super.keyPressed(input);
	}

	/* ---------------- Cleanup ---------------- */

	@Override
	public void onClose() {
		FavoriteManager.save();

		if (gallery != null) {
			gallery.close();
			gallery = null;
		}

		selectedTexture = null;
		outgoingTexture = null;

		super.onClose();
	}
}
