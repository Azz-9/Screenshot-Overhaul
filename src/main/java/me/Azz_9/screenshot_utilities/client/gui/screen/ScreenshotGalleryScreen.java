package me.Azz_9.screenshot_utilities.client.gui.screen;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.NavigationButton;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryScreen extends Screen implements FocusableScreen {

	private static final int FULL_VIEW_PADDING = 40;
	private static final int BOTTOM_PADDING = 60;
	private static final int NAV_BUTTON_SIZE = 20;
	private static final int NAV_BUTTON_PADDING = 10;
	private static final float TRANSITION_DURATION = 0.2f; // secondes
	private final @NonNull FocusManager focusManager = new FocusManager();
	private @Nullable ScreenshotGalleryWidget gallery;
	// full view
	private NavigationButton backButton, nextButton;
	private @Nullable ScreenshotTexture selectedTexture = null;
	private @Nullable ScreenshotTexture outgoingTexture = null;

	private float transitionTime = 0f;
	private int transitionDirection = 0; // -1 = back, +1 = next
	private boolean inTransition = false;

	public ScreenshotGalleryScreen() {
		super(Text.translatable("screenshot_utilities.narrator.screenshot_gallery"));
	}

	@Override
	public void requestFocus(@Nullable Element widget) {
		focusManager.requestFocus(widget);
	}

	@Override
	public void clearFocus() {
		focusManager.clearFocus();
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	@Override
	protected void init() {
		File folder = Config.getInstance().getScreenshotsDir().toFile();

		gallery = new ScreenshotGalleryWidget(
				10, 10,
				width - 100, height - 20,
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

		addDrawableChild(backButton);
		addDrawableChild(nextButton);
		addDrawableChild(gallery);
	}

	public void selectScreenshot(@NonNull ScreenshotTexture texture) {
		selectedTexture = texture;

		backButton.visible = true;
		nextButton.visible = true;

		updateNavButtons();

		if (gallery != null) {
			gallery.setActive(false);
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
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

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
			context.fill(0, 0, width, height, Colors.BLACK_TRANSPARENT);

			if (!inTransition) {
				drawScreenshot(context, selectedTexture, 0);
			} else {
				float t = transitionTime / TRANSITION_DURATION;
				t = t * t * (3f - 2f * t); // smoothstep

				int slide = (int) (width * t);

				// ancien screenshot (sortant)
				drawScreenshot(
						context,
						outgoingTexture,
						-slide * transitionDirection
				);

				// nouveau screenshot (entrant)
				drawScreenshot(
						context,
						selectedTexture,
						(width - slide) * transitionDirection
				);
			}

			nextButton.render(context, mouseX, mouseY, deltaTicks);
			backButton.render(context, mouseX, mouseY, deltaTicks);
		}
	}

	private void drawScreenshot(@NonNull DrawContext context, @Nullable ScreenshotTexture texture, int offsetX) {
		if (texture == null) return;

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();

		matrices.translate(offsetX, 0);

		ScreenshotDrawHelper.drawCover(
				context,
				texture,
				FULL_VIEW_PADDING,
				FULL_VIEW_PADDING,
				width - FULL_VIEW_PADDING * 2,
				height - FULL_VIEW_PADDING - BOTTOM_PADDING
		);

		matrices.popMatrix();
	}

	/* ---------------- Inputs ---------------- */

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		boolean handled = false;

		Optional<Element> optional = this.hoveredElement(click.x(), click.y());
		if (optional.isPresent()) {
			Element element = optional.get();
			if (element.mouseClicked(click, doubled) && element.isClickable()) {
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
	public boolean keyPressed(KeyInput input) {
		if (selectedTexture != null && input.isEscape()) {
			deselectScreenshot();
			return true;
		}
		if (input.key() == InputUtil.GLFW_KEY_F5 && gallery != null) {
			gallery.refresh();
			return true;
		}
		return super.keyPressed(input);
	}

	/* ---------------- Cleanup ---------------- */

	@Override
	public void close() {
		if (gallery != null) {
			gallery.close();
			gallery = null;
		}

		selectedTexture = null;
		outgoingTexture = null;

		super.close();
	}
}
