package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.io.File;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Environment(EnvType.CLIENT)
public class ScreenshotThumbnailWidget extends ClickableWidget implements AutoCloseable {

	private static final float HOVER_SCALE = 1.04f;

	private static final float HOVER_SPEED = 10f;
	private static final float APPEAR_SPEED = 8f;

	private final @NonNull ScreenshotTexture texture;
	private final @NonNull File screenshot;

	private float currentScale = 1.0f;
	private float appearProgress = 0f; // 0 → 1

	private boolean appeared = false;

	public ScreenshotThumbnailWidget(int x, int y, int width, int height, @NonNull File screenshot) {
		super(x, y, width, height, Text.literal(screenshot.getName()));
		this.screenshot = screenshot;
		this.texture = ScreenshotTexture.loadThumbnail(screenshot.toPath());
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (!texture.isReady()) {
			Loading.drawLoadingSpinner(
					context,
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

		context.enableScissor(getX(), getY(), getRight(), getBottom());

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();

		float cx = getX() + getWidth() / 2f;
		float cy = getY() + getHeight() / 2f;

		matrices.translate(cx, cy);
		matrices.scale(currentScale, currentScale);
		matrices.translate(-cx, -cy);

		ScreenshotDrawHelper.drawCover(
				context,
				texture,
				getX(), getY(),
				getWidth(), getHeight(),
				ColorHelper.withAlpha(appearProgress, Colors.WHITE)
		);

		matrices.popMatrix();
		context.disableScissor();

		setCursor(context);
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
		float targetScale = (isHovered() && isInteractable()) ? HOVER_SCALE : 1.0f;
		currentScale += (float) ((targetScale - currentScale) * (1f - Math.exp(-HOVER_SPEED * dt)));
	}

	@Override
	protected void setCursor(@NonNull DrawContext context) {
		if (this.isHovered() && this.isInteractable()) {
			context.setCursor(StandardCursors.POINTING_HAND);
		}
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		if (CLIENT.currentScreen instanceof FocusableScreen screen) {
			screen.clearFocus();
		}
		if (CLIENT.currentScreen instanceof ScreenshotGalleryScreen screen) {
			screen.selectScreenshot(texture);
		}
	}

	@NonNull
	public ScreenshotTexture getTexture() {
		return texture;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public void close() {
		texture.close();
	}
}
