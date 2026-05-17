package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Ease;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.trackableChanges.TrackableChanges;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotManager;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotMetadata;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotMetadataUtils;

@Environment(EnvType.CLIENT)
public class ScreenshotEntryWidget extends SimpleParentWidget implements TrackableChanges {

	public static final int NAME_HEIGHT = 30;

	private static final int BUTTON_SIZE = 10;
	private static final int BUTTON_FADE_DURATION = 250;
	private long startTime;
	private boolean wasHovered;
	private float progress;

	private final @NonNull String INITIAL_NAME;
	private final @NonNull ScreenshotMetadata INITIAL_METADATA;

	private final @NonNull ScreenshotThumbnailWidget thumbnailWidget;
	private final @NonNull ScreenshotNameWidget nameWidget;
	private final @NonNull HideFromMapButton hideFromMapButton;
	private final @NonNull FavoriteButton favoriteButton;

	private final @NonNull Screenshot screenshot;

	private int baseY;

	public ScreenshotEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, @NonNull Screenshot screenshot,
	                             @Nullable Consumer<Screenshot> onThumbnailClicked) {
		super(x, y, thumbnailWidth, thumbnailHeight + NAME_HEIGHT);
		this.INITIAL_NAME = screenshot.file().getName();
		this.INITIAL_METADATA = ScreenshotMetadata.copyOf(screenshot.getMetadata());

		this.baseY = y;
		this.screenshot = screenshot;

		this.thumbnailWidget = new ScreenshotThumbnailWidget(
				x, y,
				thumbnailWidth, thumbnailHeight,
				screenshot,
				onThumbnailClicked
		);

		this.nameWidget = new ScreenshotNameWidget(
				x, y + thumbnailHeight,
				thumbnailWidth, NAME_HEIGHT,
				INITIAL_NAME
		);
		nameWidget.setChangedListener(s ->
				nameWidget.setChatFormatting(hasChanged() ? ChatFormatting.ITALIC : ChatFormatting.RESET));

		this.hideFromMapButton = new HideFromMapButton(
				getX(), getY(), BUTTON_SIZE, BUTTON_SIZE,
				(btn) -> ScreenshotManager.setHiddenFromMap(screenshot.pathRelativeToScreenshotDir(), ((HideFromMapButton) btn).isCrossed()),
				ScreenshotManager.isHiddenFromMap(screenshot.pathRelativeToScreenshotDir())
		);

		this.favoriteButton = new FavoriteButton(
				getRight() - BUTTON_SIZE,
				getY(),
				BUTTON_SIZE, BUTTON_SIZE,
				(btn) -> ScreenshotManager.setFavorite(screenshot.pathRelativeToScreenshotDir(), ((FavoriteButton) btn).isFilled()),
				ScreenshotManager.isFavorite(screenshot.pathRelativeToScreenshotDir())
		);

		addAllChildren(hideFromMapButton, favoriteButton, thumbnailWidget, nameWidget);
	}

	public ScreenshotEntryWidget(@NonNull Screenshot screenshot, @Nullable Consumer<Screenshot> onThumbnailClicked) {
		this(0, 0, 0, 0, screenshot, onThumbnailClicked);
	}

	public ScreenshotEntryWidget(@NonNull Screenshot screenshot) {
		this(0, 0, 0, 0, screenshot, null);
	}

	public int getBaseY() {
		return baseY;
	}

	public void setBaseY(int baseY) {
		this.baseY = baseY;
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		// fond global
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK_TRANSPARENT);

		thumbnailWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		nameWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

		if (wasHovered != isHovered()) {
			startTime = System.currentTimeMillis();
		}

		updateAnimation();

		if (isHovered() && favoriteButton.active || favoriteButton.isFilled()) {
			favoriteButton.setProgress(favoriteButton.isFilled() ? 1 : progress);
			favoriteButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}
		if (isHovered() && hideFromMapButton.active || hideFromMapButton.isCrossed()) {
			hideFromMapButton.setProgress(hideFromMapButton.isCrossed() ? 1 : progress);
			hideFromMapButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}

		wasHovered = isHovered();
	}

	private void updateAnimation() {
		progress = Ease.outQuad(Math.clamp((float) (System.currentTimeMillis() - startTime) / BUTTON_FADE_DURATION, 0, 1));
		if (!isHovered())
			progress = 1.0f - progress;
	}

	public void triggerLoad() {
		thumbnailWidget.load();
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

	private void updateChildrenPos() {
		thumbnailWidget.setRectangle(getWidth(), getHeight() - NAME_HEIGHT, getX(), getY());
		hideFromMapButton.setPosition(thumbnailWidget.getX(), thumbnailWidget.getY());
		favoriteButton.setPosition(thumbnailWidget.getRight() - BUTTON_SIZE, thumbnailWidget.getY());
		nameWidget.setRectangle(getWidth(), NAME_HEIGHT, getX(), thumbnailWidget.getBottom());
	}

	public @NonNull Screenshot getScreenshot() {
		return screenshot;
	}

	public ScreenshotMetadata getMetadata() {
		return screenshot.getMetadata();
	}

	public String getName() {
		return nameWidget.getText();
	}

	public String getPathRelativeToScreenshotDir() {
		return screenshot.pathRelativeToScreenshotDir();
	}

	public long getTimestampOrLastModified() {
		Long timestamp = screenshot.getMetadata().getTimestamp();
		if (timestamp != null) {
			return timestamp;
		}
		return screenshot.file().lastModified();
	}

	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}

	@Override
	public boolean hasChanged() {
		return hasNameChanged() || hasMetadataChanged();
	}

	public boolean hasNameChanged() {
		return !INITIAL_NAME.equals(getName());
	}

	public boolean hasMetadataChanged() {
		return !INITIAL_METADATA.equals(getMetadata());
	}

	@Override
	public void revertChanges() {
		nameWidget.setText(INITIAL_NAME);
		screenshot.setMetadata(INITIAL_METADATA);
	}

	@Override
	public void commitChanges() {
		//TODO qu'est ce qu'il se passe si on renomme un fichier par le même nom qu'un autre et que l'autre on le renomme par le même nom que le premier ?
		if (hasMetadataChanged()) {
			try {
				ScreenshotMetadataUtils.update(screenshot.file(), screenshot.getMetadata());
			} catch (Exception e) {
				ScreenshotLogger.error("Could not save metadata for {}", screenshot.file().getName());
			}
		}
		if (hasNameChanged()) {
			Path oldPath = screenshot.file().toPath();
			Path newPath = screenshot.file().toPath().resolveSibling(getName());
			try {
				Files.move(oldPath, newPath);
				ScreenshotManager.changeAbsoluteFilePath(oldPath, newPath);
			} catch (IOException e) {
				ScreenshotLogger.error("Failed to rename screenshot file : {}", e.getMessage());
			}
		}
	}
}
