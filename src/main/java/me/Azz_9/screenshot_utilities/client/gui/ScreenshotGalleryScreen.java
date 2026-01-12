package me.Azz_9.screenshot_utilities.client.gui;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.widget.NavigationButton;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshot.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryScreen extends Screen {

	private static final int FULL_VIEW_PADDING = 40;
	private static final int BOTTOM_PADDING = 60;
	private static final int NAV_BUTTON_SIZE = 20;
	private static final int NAV_BUTTON_PADDING = 10;
	private static final float TRANSITION_DURATION = 0.25f; // secondes
	private final FocusManager focusManager = new FocusManager();
	private ScreenshotGalleryWidget gallery;
	// full view
	private NavigationButton backButton, nextButton;
	@Nullable
	private ScreenshotTexture selectedTexture = null;
	private ScreenshotTexture outgoingTexture;
	private ScreenshotTexture incomingTexture;

	private float transitionTime = 0f;
	private int transitionDirection = 0; // -1 = back, +1 = next
	private boolean inTransition = false;

	public ScreenshotGalleryScreen() {
		super(Text.translatable("screenshot_utilities.narrator.screenshot_gallery"));
	}

	@Override
	protected void init() {
		File folder = Config.getInstance().getScreenshotsDir().toFile();

		List<File> screenshots = Arrays.stream(
				Objects.requireNonNull(folder.listFiles(f ->
						f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg")
				))
		).toList();

		gallery = new ScreenshotGalleryWidget(
				10, 10,
				width - 100, height - 20,
				screenshots, focusManager
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

	public void selectScreenshot(ScreenshotTexture texture) {
		this.selectedTexture = texture;

		backButton.visible = true;
		nextButton.visible = true;

		updateNavButtons();
		gallery.setActive(false);
	}

	public void deselectScreenshot() {
		this.selectedTexture = null;
		backButton.active = false;
		backButton.visible = false;
		nextButton.active = false;
		nextButton.visible = false;
		gallery.setActive(true);
	}

	private void selectPrevious() {
		if (selectedTexture == null || inTransition) return;

		ScreenshotTexture prev = gallery.getPrevious(selectedTexture);
		if (prev != null) {
			startTransition(prev, -1);
		}
	}

	private void selectNext() {
		if (selectedTexture == null || inTransition) return;

		ScreenshotTexture next = gallery.getNext(selectedTexture);
		if (next != null) {
			startTransition(next, +1);
		}
	}

	private void startTransition(ScreenshotTexture next, int direction) {
		this.outgoingTexture = this.selectedTexture;
		this.incomingTexture = next;

		this.transitionDirection = direction;
		this.transitionTime = 0f;
		this.inTransition = true;

		updateNavButtons();
	}


	private void updateNavButtons() {
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
				selectedTexture = incomingTexture;
				outgoingTexture = null;
				incomingTexture = null;
			}
		}


		if (selectedTexture != null) {
			context.fill(0, 0, width, height, 0x7f000000);

			if (!inTransition) {
				drawScreenshot(context, selectedTexture, 0, 1f);
			} else {
				float t = transitionTime / TRANSITION_DURATION;
				t = t * t * (3f - 2f * t); // smoothstep

				int slide = (int) ((width + FULL_VIEW_PADDING) * t);

				// ancien screenshot (sortant)
				drawScreenshot(
						context,
						outgoingTexture,
						-slide * transitionDirection,
						1f - t
				);

				// nouveau screenshot (entrant)
				drawScreenshot(
						context,
						incomingTexture,
						(width - slide) * transitionDirection,
						t
				);
			}

			nextButton.render(context, mouseX, mouseY, deltaTicks);
			backButton.render(context, mouseX, mouseY, deltaTicks);
		}
	}

	private void drawScreenshot(DrawContext context, ScreenshotTexture texture, int offsetX, float alpha) {
		if (texture == null) return;

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();

		matrices.translate(offsetX, 0);

		int a = MathHelper.clamp((int) (alpha * 255), 0, 255);

		ScreenshotDrawHelper.drawCover(
				context,
				texture.id(),
				texture.width(),
				texture.height(),
				FULL_VIEW_PADDING,
				FULL_VIEW_PADDING,
				width - FULL_VIEW_PADDING * 2,
				height - FULL_VIEW_PADDING - BOTTOM_PADDING,
				ColorHelper.withAlpha(a, 0xffffff)
		);

		matrices.popMatrix();
	}


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

	/* ---------------- Cleanup ---------------- */

	@Override
	public void close() {
		if (gallery != null) {
			gallery.close();
			gallery = null;
		}

		selectedTexture = null;

		super.close();
	}
}
