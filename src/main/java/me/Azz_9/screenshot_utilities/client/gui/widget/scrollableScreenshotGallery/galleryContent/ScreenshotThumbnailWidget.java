package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.io.File;

import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;

@Environment(EnvType.CLIENT)
public class ScreenshotThumbnailWidget extends AbstractWidget implements AutoCloseable {

	private static final float HOVER_SCALE = 1.04f;

	private static final float HOVER_SPEED = 10f;
	private static final float APPEAR_SPEED = 8f;

	private final @NonNull ScreenshotTexture texture;
	private final @NonNull File screenshot;

	private float currentScale = 1.0f;
	private float appearProgress = 0f; // 0 → 1

	private boolean appeared = false;

	public ScreenshotThumbnailWidget(int x, int y, int width, int height, @NonNull File screenshot) {
		super(x, y, width, height, Component.literal(screenshot.getName()));
		this.screenshot = screenshot;
		this.texture = ScreenshotTextureCache.getThumbnail(screenshot.toPath());
	}

	// rendering

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (!texture.isReady()) {
			Loading.drawLoadingSpinner(
					graphics,
					getX() + getWidth() / 2,
					getY() + getHeight() / 2,
					(int) (Math.min(getWidth(), getHeight()) * 0.05f),
					10
			);
			return;
		}

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

		ScreenshotDrawHelper.drawCover(
				graphics,
				texture,
				getX(), getY(),
				getWidth(), getHeight(),
				ARGB.color(appearProgress, 0xffffff)
		);

		matrices.popMatrix();
		graphics.disableScissor();

		handleCursor(graphics);
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

	@Override
	protected void handleCursor(@NonNull GuiGraphicsExtractor graphics) {
		if (this.isHovered() && this.shouldTakeFocusAfterInteraction()) {
			graphics.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	// input

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			screen.clearFocus();
		}
		if (MINECRAFT.screen instanceof ScreenshotGalleryScreen screen) {
			screen.selectScreenshot(texture);
		}
	}

	@NonNull
	public ScreenshotTexture getTexture() {
		return texture;
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}

	@Override
	public void close() {
		texture.close();
	}
}
