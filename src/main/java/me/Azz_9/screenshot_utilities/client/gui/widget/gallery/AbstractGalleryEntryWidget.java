package me.Azz_9.screenshot_utilities.client.gui.widget.gallery;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Ease;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.gui.trackableChanges.TrackableChanges;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;

@Environment(EnvType.CLIENT)
public abstract class AbstractGalleryEntryWidget extends SimpleParentWidget implements TrackableChanges {
	public static final int DEFAULT_NAME_HEIGHT = 30;

	private final int animationDurationMs;
	private long startTime;
	private boolean wasHovered;
	private float progress;

	protected AbstractGalleryEntryWidget(int x, int y, int width, int height, int animationDurationMs) {
		super(x, y, width, height);
		this.animationDurationMs = animationDurationMs;
	}

	@Override
	public void setWidth(int width) {
		if (getWidth() != width) {
			super.setWidth(width);
			updateChildrenPos();
		}
	}

	@Override
	public void setHeight(int height) {
		if (getHeight() != height) {
			super.setHeight(height);
			updateChildrenPos();
		}
	}

	protected abstract void updateChildrenPos();

	public abstract void triggerLoad();

	public abstract @NonNull String getName();

	public abstract @NonNull String getPathRelativeToScreenshotDir();

	public abstract long getTimestampOrLastModified();

	private void updateAnimation() {
		progress = Ease.outQuad(Math.clamp((float) (System.currentTimeMillis() - startTime) / animationDurationMs, 0, 1));
		if (!isHovered())
			progress = 1.0f - progress;
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		if (wasHovered != isHovered()) {
			startTime = System.currentTimeMillis();
		}

		updateAnimation();

		renderBeforeChildren(graphics, mouseX, mouseY, deltaTicks, progress);
		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
		renderAfterChildren(graphics, mouseX, mouseY, deltaTicks, progress);

		wasHovered = isHovered();
	}

	protected void renderBeforeChildren(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, float animationProgress) {
	}

	protected void renderAfterChildren(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, float animationProgress) {
	}
}
