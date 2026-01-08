package me.Azz_9.screenshot_utilities.client.gui.widget.screenshot;

import me.Azz_9.screenshot_utilities.client.gui.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.io.File;

@Environment(EnvType.CLIENT)
public class ScreenshotThumbnailWidget extends ClickableWidget implements AutoCloseable {

	private static final float HOVER_SCALE = 1.04f;
	private static final float SCALE_SPEED = 0.15f;

	private final FocusManager focusManager;

	private final ScreenshotTexture texture;
	private final File screenshot;

	private float currentScale = 1.0f;

	public ScreenshotThumbnailWidget(int x, int y, int width, int height, File screenshot, FocusManager focusManager) {
		super(x, y, width, height, Text.literal(screenshot.getName()));
		this.focusManager = focusManager;
		this.screenshot = screenshot;
		this.texture = new ScreenshotTexture(screenshot.toPath());
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		texture.uploadIfNeeded();

		float targetScale = isHovered() && isInteractable() ? HOVER_SCALE : 1.0f;
		currentScale += (targetScale - currentScale) * SCALE_SPEED;

		if (!texture.isReady()) {
			return;
		}

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
				texture.id(),
				texture.width(),
				texture.height(),
				getX(), getY(),
				getWidth(), getHeight()
		);

		matrices.popMatrix();
		context.disableScissor();

		setCursor(context);
	}

	@Override
	protected void setCursor(DrawContext context) {
		if (this.isHovered() && this.isInteractable()) {
			context.setCursor(StandardCursors.POINTING_HAND);
		}
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		this.focusManager.clearFocus();
		if (MinecraftClient.getInstance().currentScreen instanceof ScreenshotGalleryScreen screen) {
			screen.selectScreenshot(texture);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public void close() {
		texture.close();
	}
}
