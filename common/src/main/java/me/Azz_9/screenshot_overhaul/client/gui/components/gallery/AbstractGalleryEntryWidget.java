package me.Azz_9.screenshot_overhaul.client.gui.components.gallery;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Ease;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.gui.components.SimpleParentWidget;
import me.Azz_9.screenshot_overhaul.client.gui.trackableChanges.TrackableChanges;
import me.Azz_9.screenshot_overhaul.utils.PathUtils;

public abstract class AbstractGalleryEntryWidget extends SimpleParentWidget implements TrackableChanges {

	protected static final int BUTTON_SIZE = 10;
	private static final int BUTTON_FADE_DURATION = 250;
	public static final int DEFAULT_NAME_HEIGHT = 30;

	private long startTime;
	private boolean wasHovered;
	private float progress;

	private @Nullable EntryToggleButton topLeftButton;
	private @Nullable EntryToggleButton topRightButton;

	private final @NonNull Path parentFolder;

	protected AbstractGalleryEntryWidget(int x, int y, int width, int height, @NonNull Path parentFolder) {
		super(x, y, width, height);
		this.parentFolder = parentFolder;
	}

	protected @NonNull Path getParentFolder() {
		return parentFolder;
	}

	@Override
	public boolean isValid() {
		return PathUtils.isValidFileName(getName()) && (!PathUtils.exists(getParentFolder(), getName()) || !hasNameChanged());
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

	public void setTopLeftButton(@Nullable EntryToggleButton topLeftButton) {
		if (topLeftButton != null) {
			addChild(topLeftButton);
		}
		this.topLeftButton = topLeftButton;
	}

	public void setTopRightButton(@Nullable EntryToggleButton topRightButton) {
		if (topRightButton != null) {
			addChild(topRightButton);
		}
		this.topRightButton = topRightButton;
	}

	protected abstract boolean hasNameChanged();

	public abstract void triggerLoad();

	public abstract @NonNull String getName();

	public abstract @NonNull String getPathRelativeToScreenshotDir();

	public abstract long getTimestampOrLastModified();

	public abstract @Nullable String getWorldName();

	public abstract @Nullable String getServerIp();

	private void updateAnimation() {
		progress = Ease.outQuad(Math.clamp((float) (System.currentTimeMillis() - startTime) / BUTTON_FADE_DURATION, 0, 1));
		if (!isHovered())
			progress = 1.0f - progress;
	}

	protected void updateChildrenPos() {
		if (topLeftButton != null) topLeftButton.setPosition(getX(), getY());
		if (topRightButton != null) topRightButton.setPosition(getRight() - topRightButton.getWidth(), getY());
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		if (wasHovered != isHovered()) {
			startTime = System.currentTimeMillis();
		}

		updateAnimation();

		// fond global
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK_TRANSPARENT);

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

		if (topLeftButton != null) renderButton(topLeftButton, graphics, mouseX, mouseY, deltaTicks, progress);
		if (topRightButton != null) renderButton(topRightButton, graphics, mouseX, mouseY, deltaTicks, progress);

		wasHovered = isHovered();
	}

	private void renderButton(@NonNull EntryToggleButton button, @NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, float animationProgress) {
		if (isHovered() && button.active || button.isToggled()) {
			button.setProgress(button.isToggled() ? 1 : animationProgress);
			button.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}
	}
}
