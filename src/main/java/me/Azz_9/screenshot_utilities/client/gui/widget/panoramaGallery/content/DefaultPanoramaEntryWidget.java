package me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.AbstractGalleryEntryWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshotGallery.content.ScreenshotNameWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaHolder;

public class DefaultPanoramaEntryWidget extends AbstractGalleryEntryWidget {

	private final @NonNull String NAME = "Default";

	private final @NonNull DefaultPanoramaThumbnailWidget thumbnailWidget;
	private final @NonNull ScreenshotNameWidget nameWidget;

	public DefaultPanoramaEntryWidget(@Nullable Runnable onThumbnailClicked) {
		this(0, 0, 0, 0, onThumbnailClicked);
	}

	public DefaultPanoramaEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, @Nullable Runnable onThumbnailClicked) {
		super(x, y, thumbnailWidth, thumbnailHeight, 0);

		this.thumbnailWidget = new DefaultPanoramaThumbnailWidget(
				x, y,
				thumbnailWidth, thumbnailHeight,
				onThumbnailClicked, PanoramaHolder.PANORAMA_LOCATION
		);

		this.nameWidget = new ScreenshotNameWidget(
				x, y + thumbnailHeight,
				thumbnailWidth, DEFAULT_NAME_HEIGHT,
				NAME, false
		);
		nameWidget.setEditable(false);

		addAllChildren(thumbnailWidget, nameWidget);
	}

	@Override
	protected void updateChildrenPos() {
		thumbnailWidget.setRectangle(getWidth(), getHeight() - DEFAULT_NAME_HEIGHT, getX(), getY());
		nameWidget.setRectangle(getWidth(), DEFAULT_NAME_HEIGHT, getX(), thumbnailWidget.getBottom());
	}

	@Override
	public void triggerLoad() {
	}

	@Override
	public @NonNull String getName() {
		return NAME;
	}

	@Override
	public @NonNull String getPathRelativeToScreenshotDir() {
		return "__default_panorama__";
	}

	@Override
	public long getTimestampOrLastModified() {
		return 0;
	}

	@Override
	protected void renderBeforeChildren(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, float animationProgress) {
		// fond global
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK_TRANSPARENT);

		thumbnailWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		nameWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean hasChanged() {
		return false;
	}

	@Override
	public void revertChanges() {
	}

	@Override
	public void commitChanges() {
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}
}
