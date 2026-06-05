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

	public abstract void triggerLoad();

	public abstract @NonNull String getName();

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

		render(graphics, mouseX, mouseY, deltaTicks, progress);

		wasHovered = isHovered();
	}

	protected abstract void render(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, float animationProgress);
}
