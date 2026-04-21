package me.Azz_9.screenshot_utilities.client.gui.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.InputConstants;

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

import java.io.File;
import java.util.Optional;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.NavigationButton;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.ScreenshotGalleryWidget;
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

		File folder = Config.getInstance().getScreenshotsDir().toFile();

		gallery = new ScreenshotGalleryWidget(
				GLOBAL_PADDING, GLOBAL_PADDING,
				width - SETTINGS_BUTTON_WIDTH - GLOBAL_PADDING * 2 - 20, height - GLOBAL_PADDING * 2,
				folder
		);

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

		addRenderableWidget(backButton);
		addRenderableWidget(nextButton);
		addRenderableWidget(gallery);
		addRenderableWidget(settingsButton);
	}

	public void selectScreenshot(@NonNull ScreenshotTexture texture) {
		selectedTexture = texture;

		backButton.visible = true;
		nextButton.visible = true;

		updateNavButtons();

		if (gallery != null) {
			gallery.setActive(false);

			// pre-load the next et previous screenshots
			ScreenshotTexture next = gallery.getNext(selectedTexture);
			ScreenshotTexture prev = gallery.getPrevious(selectedTexture);
			if (next != null) ScreenshotTextureCache.getScreenshot(next.getFile());
			if (prev != null) ScreenshotTextureCache.getScreenshot(prev.getFile());
		}
	}

	public void deselectScreenshot() {
		selectedTexture = null;
		backButton.active = false;
		backButton.visible = false;
		nextButton.active = false;
		nextButton.visible = false;

		if (gallery != null) {
			gallery.setActive(true);
		}
	}

	private void selectPrevious() {
		if (selectedTexture == null || inTransition || gallery == null) return;

		ScreenshotTexture prev = gallery.getPrevious(selectedTexture);
		if (prev != null) {
			startTransition(prev, -1);
		}
	}

	private void selectNext() {
		if (selectedTexture == null || inTransition || gallery == null) return;

		ScreenshotTexture next = gallery.getNext(selectedTexture);
		if (next != null) {
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

		backButton.active = gallery.getPrevious(selectedTexture) != null;
		nextButton.active = gallery.getNext(selectedTexture) != null;
	}

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
			graphics.fill(0, 0, width, height, Colors.BLACK_TRANSPARENT);

			if (!inTransition || outgoingTexture == null) {
				drawScreenshot(graphics, ScreenshotTextureCache.getScreenshot(selectedTexture.getFile()), 0);
			} else {
				float t = transitionTime / TRANSITION_DURATION;
				t = t * t * (3f - 2f * t); // smoothstep

				int slide = (int) (width * t);

				// ancien screenshot (sortant)
				drawScreenshot(
						graphics,
						ScreenshotTextureCache.getScreenshot(outgoingTexture.getFile()),
						-slide * transitionDirection
				);

				// nouveau screenshot (entrant)
				drawScreenshot(
						graphics,
						ScreenshotTextureCache.getScreenshot(selectedTexture.getFile()),
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
		if (gallery != null) {
			gallery.close();
			gallery = null;
		}

		selectedTexture = null;
		outgoingTexture = null;

		super.onClose();
	}
}
