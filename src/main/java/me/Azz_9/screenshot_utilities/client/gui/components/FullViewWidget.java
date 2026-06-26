package me.Azz_9.screenshot_utilities.client.gui.components;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.StringUtil.pretty;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Ease;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.components.screenshotGallery.MetadataEditorPanel;
import me.Azz_9.screenshot_utilities.client.gui.components.screenshotGallery.NavigationButton;
import me.Azz_9.screenshot_utilities.client.screenshot.*;

/**
 * Overlay widget that renders a single screenshot in full view with navigation,
 * action buttons, and a slide-in metadata panel.
 * <p>
 * The widget covers the entire screen. While it is active, the parent screen
 * passes (-1, -1) as mouse coordinates to everything rendered beneath it, so
 * no underlying widget appears hovered or receives clicks.
 * <p>
 * Navigation arrows and action buttons are owned by this widget — the parent
 * screen never touches them directly.
 * <p>
 * MetadataEditorPanel overlap:
 * When the panel is open, nextButton must be fully non-interactive (no hover,
 * no click). This is achieved by intercepting mouse coordinates: if the panel
 * is open, (-1, -1) is forwarded to nextButton instead of the real coords.
 * This keeps the button visually neutral without changing its active/enabled
 * state or texture.
 */
@Environment(EnvType.CLIENT)
public class FullViewWidget extends AbstractWidget {

	// Layout
	private static final int FULL_VIEW_PADDING = 40;
	private static final int BOTTOM_PADDING = 60;
	private static final int NAV_BUTTON_SIZE = 20;
	private static final int NAV_BUTTON_MARGIN = 30;
	private static final int ACTION_BUTTON_HEIGHT = 20;
	private static final int ACTION_BUTTON_WIDTH = 80;
	private static final int ACTION_BUTTON_GAP = 10;
	private static final int ACTION_BUTTON_MARGIN_BOT = 10;
	private static final int METADATA_PANEL_WIDTH = 200;
	private static final int COPY_RESET_TIMER_MS = 5000;

	// Transition
	private static final float TRANSITION_DURATION = 0.2f; // seconds

	private float transitionTime = 0f;
	private int transitionDirection = 0;   // -1 = back, +1 = next
	private boolean inTransition = false;

	private @Nullable Screenshot currentScreenshot;
	private @Nullable Screenshot outgoingScreenshot;

	// Child widgets
	private final @NonNull NavigationButton backButton;
	private final @NonNull NavigationButton nextButton;
	private final @NonNull Button copyButton;
	private final @NonNull Button deleteButton;
	private final @NonNull Button editMetadataButton;
	private final @NonNull MetadataEditorPanel metadataPanel;

	private long copyResetAt = -1;

	// Callbacks supplied by the parent screen
	/**
	 * Called when the user navigates to a different screenshot.
	 */
	private final @NonNull Consumer<Screenshot> onScreenshotChanged;

	/**
	 * Called when the user requests deletion of the current screenshot.
	 */
	private final @NonNull Consumer<Screenshot> onDeleteRequested;

	/**
	 * Called when the user closes the full view (ESC or back-navigation to gallery).
	 */
	private final @NonNull Runnable onClose;

	/**
	 * Supplies the next screenshot relative to the given one, or null if none.
	 */
	private final @NonNull Function<Screenshot, @Nullable Screenshot> nextSupplier;

	/**
	 * Supplies the previous screenshot relative to the given one, or null if none.
	 */
	private final @NonNull Function<Screenshot, @Nullable Screenshot> prevSupplier;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	public FullViewWidget(
			int screenWidth,
			int screenHeight,
			@NonNull Consumer<Screenshot> onScreenshotChanged,
			@NonNull Consumer<Screenshot> onDeleteRequested,
			@NonNull Runnable onClose,
			@NonNull Function<Screenshot, @Nullable Screenshot> nextSupplier,
			@NonNull Function<Screenshot, @Nullable Screenshot> prevSupplier
	) {
		super(0, 0, screenWidth, screenHeight, Component.empty());
		this.onScreenshotChanged = onScreenshotChanged;
		this.onDeleteRequested = onDeleteRequested;
		this.onClose = onClose;
		this.nextSupplier = nextSupplier;
		this.prevSupplier = prevSupplier;

		backButton = new NavigationButton(
				NAV_BUTTON_MARGIN,
				(screenHeight - NAV_BUTTON_SIZE) / 2,
				NAV_BUTTON_SIZE, NAV_BUTTON_SIZE,
				NavigationButton.NavigationType.BACK,
				btn -> navigatePrevious());

		nextButton = new NavigationButton(
				screenWidth - NAV_BUTTON_MARGIN - NAV_BUTTON_SIZE,
				(screenHeight - NAV_BUTTON_SIZE) / 2,
				NAV_BUTTON_SIZE, NAV_BUTTON_SIZE,
				NavigationButton.NavigationType.NEXT,
				btn -> navigateNext());

		int centerX = screenWidth / 2;
		int btnY = screenHeight - ACTION_BUTTON_HEIGHT - ACTION_BUTTON_MARGIN_BOT;

		deleteButton = Button.builder(
						Component.translatable("screenshot_utilities.delete"),
						btn -> {
							if (currentScreenshot != null) onDeleteRequested.accept(currentScreenshot);
						})
				.bounds(centerX - ACTION_BUTTON_WIDTH / 2 - ACTION_BUTTON_GAP - ACTION_BUTTON_WIDTH,
						btnY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
				.build();

		copyButton = Button.builder(
						Component.translatable("screenshot_utilities.copy"),
						btn -> {
							if (currentScreenshot != null) {
								btn.setMessage(Component.translatable("screenshot_utilities.copying"));
								CopyScreenshot.copyToClipboardWithToastError(currentScreenshot.file(), () -> {
									btn.setMessage(Component.translatable("screenshot_utilities.copied"));
									copyResetAt = System.currentTimeMillis() + COPY_RESET_TIMER_MS;
								}, () -> btn.setMessage(Component.translatable("screenshot_utilities.copy")));
							}
						})
				.bounds(centerX - ACTION_BUTTON_WIDTH / 2,
						btnY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
				.build();

		metadataPanel = new MetadataEditorPanel(METADATA_PANEL_WIDTH, screenHeight);
		metadataPanel.setVisible(false);
		// When the panel opens, it obscures nextButton — handled in mouse coord routing below.
		metadataPanel.setOnVisibilityChange(visible -> {
		});

		editMetadataButton = Button.builder(
						Component.translatable("screenshot_utilities.edit_metadata"),
						btn -> {
							if (currentScreenshot != null)
								metadataPanel.setVisible(!metadataPanel.isVisible());
						})
				.bounds(centerX + ACTION_BUTTON_WIDTH / 2 + ACTION_BUTTON_GAP,
						btnY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
				.build();
	}

	// Screenshot selection

	/**
	 * Opens the full view for the given screenshot.
	 * Preloads neighbors for smooth navigation.
	 */
	public void show(@NonNull Screenshot screenshot) {
		currentScreenshot = screenshot;
		metadataPanel.init(screenshot);
		refreshNavButtons();
		preloadNeighbours(screenshot);
		this.visible = true;
		// Reset transition state in case a previous one was interrupted
		inTransition = false;
		outgoingScreenshot = null;
	}

	/**
	 * Hides the full view and resets all state.
	 */
	public void hide() {
		currentScreenshot = null;
		outgoingScreenshot = null;
		inTransition = false;
		metadataPanel.setVisible(false);
		this.visible = false;
	}

	public boolean isVisible() {
		return visible;
	}

	public @Nullable Screenshot getCurrentScreenshot() {
		return currentScreenshot;
	}

	// Navigation
	private void navigateNext() {
		if (currentScreenshot == null || inTransition) return;
		Screenshot next = nextSupplier.apply(currentScreenshot);
		if (next != null) {
			preloadNeighbours(next);
			startTransition(next, +1);
		}
	}

	private void navigatePrevious() {
		if (currentScreenshot == null || inTransition) return;
		Screenshot prev = prevSupplier.apply(currentScreenshot);
		if (prev != null) {
			preloadNeighbours(prev);
			startTransition(prev, -1);
		}
	}

	private void startTransition(@NonNull Screenshot next, int direction) {
		outgoingScreenshot = currentScreenshot;
		currentScreenshot = next;
		metadataPanel.init(next);
		transitionDirection = direction;
		transitionTime = 0f;
		inTransition = true;
		refreshNavButtons();
		onScreenshotChanged.accept(next);
	}

	public void refreshNavButtons() {
		if (currentScreenshot == null) return;
		backButton.active = prevSupplier.apply(currentScreenshot) != null;
		nextButton.active = nextSupplier.apply(currentScreenshot) != null;
	}

	private void preloadNeighbours(@NonNull Screenshot screenshot) {
		Screenshot next = nextSupplier.apply(screenshot);
		Screenshot prev = prevSupplier.apply(screenshot);
		if (next != null) ScreenshotTextureCache.getFullView(next.file().toPath());
		if (prev != null) ScreenshotTextureCache.getFullView(prev.file().toPath());
	}

	// Rendering
	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		if (currentScreenshot == null) return;

		// Reset copy button label when timer expires
		if (copyResetAt != -1 && System.currentTimeMillis() >= copyResetAt) {
			copyButton.setMessage(Component.translatable("screenshot_utilities.copy"));
			copyResetAt = -1;
		}

		graphics.requestCursor(CursorTypes.ARROW);
		graphics.fill(0, 0, width, height, Colors.BLACK_TRANSPARENT);

		// Advance transition
		float dt = deltaTicks / 20f;
		if (inTransition) {
			transitionTime += dt;
			if (transitionTime >= TRANSITION_DURATION) {
				transitionTime = TRANSITION_DURATION;
				inTransition = false;
				outgoingScreenshot = null;
			}
		}

		// Draw screenshot(s)
		if (!inTransition || outgoingScreenshot == null) {
			drawScreenshotWithInfo(graphics, currentScreenshot, 0);
		} else {
			float t = Ease.outQuad(transitionTime / TRANSITION_DURATION);
			int slide = (int) (width * t);
			drawScreenshotWithInfo(graphics, outgoingScreenshot, -slide * transitionDirection);
			drawScreenshotWithInfo(graphics, currentScreenshot, (width - slide) * transitionDirection);
		}

		// Route mouse coords to children — suppress hover on nextButton when panel is open
		boolean occluded = metadataPanel.isMouseOver(mouseX, mouseY);
		int mxNext = occluded ? -1 : mouseX;
		int myNext = occluded ? -1 : mouseY;

		backButton.extractRenderState(graphics, mxNext, myNext, deltaTicks);
		nextButton.extractRenderState(graphics, mxNext, myNext, deltaTicks);
		deleteButton.extractRenderState(graphics, mxNext, myNext, deltaTicks);
		copyButton.extractRenderState(graphics, mxNext, myNext, deltaTicks);
		editMetadataButton.extractRenderState(graphics, mxNext, myNext, deltaTicks);

		metadataPanel.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
	}

	private void drawScreenshotWithInfo(@NonNull GuiGraphicsExtractor graphics, @NonNull Screenshot screenshot, int offsetX) {
		ScreenshotTexture texture = ScreenshotTextureCache.getFullView(screenshot.file().toPath());

		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(offsetX, 0);

		int fullViewWidth = width - FULL_VIEW_PADDING * 2;
		int fullViewHeight = height - FULL_VIEW_PADDING - BOTTOM_PADDING;

		if (texture == null || !texture.isReady()) {
			graphics.fill(
					FULL_VIEW_PADDING, FULL_VIEW_PADDING,
					FULL_VIEW_PADDING + fullViewWidth,
					FULL_VIEW_PADDING + fullViewHeight,
					Colors.BLACK_TRANSPARENT);
			Loading.drawLoadingSpinner(
					graphics,
					FULL_VIEW_PADDING + fullViewWidth / 2,
					FULL_VIEW_PADDING + fullViewHeight / 2,
					fullViewHeight / 20, fullViewHeight / 10);
		} else {
			ScreenshotDrawHelper.drawContain(
					graphics, texture,
					FULL_VIEW_PADDING, FULL_VIEW_PADDING,
					fullViewWidth, fullViewHeight);
		}

		int center = FULL_VIEW_PADDING + fullViewWidth / 2;
		graphics.centeredText(MINECRAFT.font,
				screenshot.pathRelativeToScreenshotDir(),
				center, FULL_VIEW_PADDING + fullViewHeight + 4, Colors.WHITE);

		StringBuilder line = new StringBuilder();
		ScreenshotMetadata meta = screenshot.getMetadata();
		if (meta.getX() != null && meta.getY() != null && meta.getZ() != null)
			line.append("X: ").append(meta.getX())
					.append(" Y: ").append(meta.getY())
					.append(" Z: ").append(meta.getZ());
		if (meta.getWorldName() != null) {
			if (!line.isEmpty()) line.append(" • ");
			line.append(meta.getWorldName());
		}
		if (meta.getDimension() != null) {
			if (!line.isEmpty()) line.append(" • ");
			line.append(pretty(meta.getDimension().getPath()));
		}
		if (meta.getBiome() != null) {
			if (!line.isEmpty()) line.append(" • ");
			line.append(pretty(meta.getBiome().getPath()));
		}
		graphics.centeredText(MINECRAFT.font, line.toString(),
				center, FULL_VIEW_PADDING + fullViewHeight + 15, Colors.WHITE);

		matrices.popMatrix();
	}

	// Input — all events are consumed while the fullview is visible so nothing
	// beneath it receives clicks, scrolls, or key presses.
	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		if (!visible) return false;

		// Panel first — it sits on top of everything else
		if (metadataPanel.isVisible() && metadataPanel.mouseClicked(event, doubleClick))
			return true;

		if (!backButton.mouseClicked(event, doubleClick) &&
				!nextButton.mouseClicked(event, doubleClick) &&
				!deleteButton.mouseClicked(event, doubleClick) &&
				!copyButton.mouseClicked(event, doubleClick)) {
			editMetadataButton.mouseClicked(event, doubleClick);
		}
		return true; // consume even if no child handled it
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		if (!visible) return false;
		metadataPanel.mouseReleased(event);
		backButton.mouseReleased(event);
		nextButton.mouseReleased(event);
		deleteButton.mouseReleased(event);
		copyButton.mouseReleased(event);
		editMetadataButton.mouseReleased(event);
		return true;
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
		if (!visible) return false;
		metadataPanel.mouseDragged(event, dx, dy);
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
		if (!visible) return false;
		metadataPanel.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
		return true; // consume, do not scroll gallery beneath
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (!visible) return false;
		if (event.isEscape()) {
			if (metadataPanel.isVisible()) {
				metadataPanel.setVisible(false);
			} else {
				onClose.run();
			}
			return true;
		}
		if (metadataPanel.isVisible()) {
			return metadataPanel.keyPressed(event);
		}
		return true; // consume
	}

	@Override
	public boolean charTyped(@NonNull CharacterEvent event) {
		if (!visible) return false;
		if (metadataPanel.isVisible()) return metadataPanel.charTyped(event);
		return true;
	}

	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		if (currentScreenshot != null)
			output.add(NarratedElementType.TITLE, Component.literal(currentScreenshot.pathRelativeToScreenshotDir()));
	}
}