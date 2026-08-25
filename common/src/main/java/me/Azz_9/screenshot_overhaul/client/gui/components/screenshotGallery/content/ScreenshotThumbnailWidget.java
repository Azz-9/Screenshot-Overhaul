package me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.content;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.CommonSprites;
import me.Azz_9.screenshot_overhaul.client.cache.ScreenshotTextureCache;
import me.Azz_9.screenshot_overhaul.client.gui.Loading;
import me.Azz_9.screenshot_overhaul.client.gui.ScreenshotDrawHelper;
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.AbstractThumbnailWidget;
import me.Azz_9.screenshot_overhaul.client.gui.rightClickMenu.RightClickMenu;
import me.Azz_9.screenshot_overhaul.client.gui.rightClickMenu.RightClickMenuScreen;
import me.Azz_9.screenshot_overhaul.client.screenshot.CopyScreenshot;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;
import me.Azz_9.screenshot_overhaul.client.texture.ScreenshotTexture;

public class ScreenshotThumbnailWidget extends AbstractThumbnailWidget {

	private static final float HOVER_SCALE = 1.04f;
	private static final float HOVER_SPEED = 10f;
	private static final float APPEAR_SPEED = 8f;

	private @Nullable ScreenshotTexture texture;
	private final @NonNull Screenshot screenshot;
	private final @NonNull File screenshotFile;
	private boolean loaded = false;
	private final @Nullable Consumer<Screenshot> onClick;
	private final @NonNull Consumer<Screenshot> onDeleteRequested;

	private float currentScale = 1.0f;
	private float appearProgress = 0f; // 0 → 1
	private boolean appeared = false;

	public ScreenshotThumbnailWidget(int x, int y, int width, int height, @NonNull Screenshot screenshot,
									 @Nullable Consumer<Screenshot> onClick, @NonNull Consumer<Screenshot> onDeleteRequested) {
		super(x, y, width, height, Component.literal(screenshot.file().getName()));
		this.screenshotFile = screenshot.file();
		this.screenshot = screenshot;
		this.onClick = onClick;
		this.onDeleteRequested = onDeleteRequested;
	}

	/**
	 * Déclenche le chargement de la texture. Idempotent.
	 */
	@Override
	public void load() {
		if (!loaded) {
			loaded = true;
			texture = ScreenshotTextureCache.getThumbnail(screenshotFile.toPath());
		}
	}

	// rendering

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (texture != null && texture.isClosed()) {
			loaded = false;
			texture = null;
		}
		if (texture == null || !texture.isReady()) {
			Loading.drawLoadingSpinner(
					graphics,
					getX() + getWidth() / 2,
					getY() + getHeight() / 2,
					(int) (Math.min(getWidth(), getHeight()) * 0.05f),
					10
			);
			return;
		}

		if (active) {
			float dt = delta / 20f;

			updateAppearProgress(dt);
			updateHoverScale(dt);

			graphics.enableScissor(getX(), getY(), getRight(), getBottom());

			Matrix3x2fStack matrices = graphics.pose();
			matrices.pushMatrix();

			float cx = getX() + getWidth() / 2f;
			float cy = getY() + getHeight() / 2f;

			matrices.translate(cx, cy);
			matrices.scale(currentScale, currentScale);
			matrices.translate(-cx, -cy);
		}

		ScreenshotDrawHelper.drawCover(
				graphics,
				texture,
				getX(), getY(),
				getWidth(), getHeight(),
				ARGB.color(appearProgress, Colors.WHITE)
		);

		if (active) {
			graphics.pose().popMatrix();
			graphics.disableScissor();
		}

		super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
	}

	private void updateAppearProgress(float dt) {
		if (!appeared) {
			appearProgress += (float) ((1f - appearProgress) * (1f - Math.exp(-APPEAR_SPEED * dt)));
			if (appearProgress > 0.999f) {
				appearProgress = 1f;
				appeared = true;
			}
		}
	}


	private void updateHoverScale(float dt) {
		float targetScale = (isHovered() && shouldTakeFocusAfterInteraction()) ? HOVER_SCALE : 1.0f;
		currentScale += (float) ((targetScale - currentScale) * (1f - Math.exp(-HOVER_SPEED * dt)));
	}

	// input

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (click.buttonInfo().button() == 1) {
			showRightClickMenu(click.x(), click.y());
		} else if (onClick != null && click.buttonInfo().button() == 0) {
			onClick.accept(screenshot);
		}
	}

	@Override
	public void showRightClickMenu(double mouseX, double mouseY) {
		if (MINECRAFT.screen instanceof RightClickMenuScreen screen) {
			screen.clearRightClickMenuItems();

			screen.addRightClickMenuItem(new RightClickMenu.MenuItem(
					CommonSprites.COPY_SPRITE,
					Component.translatable("screenshot_overhaul.copy"),
					_ -> {
						CopyScreenshot.copyToClipboardWithToastError(screenshotFile, null, null);
					}
			));
			screen.addRightClickMenuItem(new RightClickMenu.MenuItem(
					CommonSprites.DELETE_SPRITE,
					Component.translatable("screenshot_overhaul.delete"),
					_ -> onDeleteRequested.accept(screenshot)
			));

			screen.showRightClickMenu(mouseX, mouseY);
		}
	}
}
