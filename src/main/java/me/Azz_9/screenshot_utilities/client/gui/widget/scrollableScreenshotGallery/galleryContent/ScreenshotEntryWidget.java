package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.FavoriteManager;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;

@Environment(EnvType.CLIENT)
public class ScreenshotEntryWidget extends SimpleParentWidget implements ContainerEventHandler {

	public static final int NAME_HEIGHT = 30;

	private static final int FAVORITE_BUTTON_SIZE = 10;

	private final @NonNull ScreenshotThumbnailWidget thumbnailWidget;
	private final @NonNull ScreenshotNameWidget nameWidget;
	private final @NonNull FavoriteButton favoriteButton;

	private final @NonNull File screenshotFile;
	private final String pathRelativeToScreenshotDir;
	private int baseY;

	public ScreenshotEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, @NonNull File screenshotFile) {
		super(x, y, thumbnailWidth, thumbnailHeight + NAME_HEIGHT);

		this.baseY = y;
		this.screenshotFile = screenshotFile;
		this.pathRelativeToScreenshotDir = Config.getInstance().getScreenshotsDir().relativize(screenshotFile.toPath()).toString();

		this.thumbnailWidget = new ScreenshotThumbnailWidget(
				x, y,
				thumbnailWidth, thumbnailHeight,
				screenshotFile
		);

		this.nameWidget = new ScreenshotNameWidget(
				x, y + thumbnailHeight,
				thumbnailWidth, NAME_HEIGHT,
				screenshotFile
		);

		this.favoriteButton = new FavoriteButton(
				getRight() - FAVORITE_BUTTON_SIZE,
				getY(),
				FAVORITE_BUTTON_SIZE, FAVORITE_BUTTON_SIZE,
				(btn) -> FavoriteManager.setFavorite(pathRelativeToScreenshotDir, ((FavoriteButton) btn).isFilled()),
				FavoriteManager.isFavorite(pathRelativeToScreenshotDir));

		addAllChildren(favoriteButton, thumbnailWidget, nameWidget);
	}

	public ScreenshotEntryWidget(@NonNull File screenshotFile) {
		this(0, 0, 0, 0, screenshotFile);
	}

	public int getBaseY() {
		return baseY;
	}

	public void setBaseY(int baseY) {
		this.baseY = baseY;
	}

	@Override
	public void renderWidget(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// fond global
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK_TRANSPARENT);

		thumbnailWidget.extractRenderState(graphics, mouseX, mouseY, delta);
		nameWidget.extractRenderState(graphics, mouseX, mouseY, delta);

		if (isHovered() && favoriteButton.active || favoriteButton.isFilled()) {
			favoriteButton.extractRenderState(graphics, mouseX, mouseY, delta);
		}
	}

	public void triggerLoad() {
		thumbnailWidget.load();
	}

	@Override
	public void setX(int x) {
		if (getX() != x) {
			super.setX(x);
			updateChildrenPos();
		}
	}

	@Override
	public void setY(int y) {
		if (getY() != y) {
			super.setY(y);
			updateChildrenPos();
		}
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
		favoriteButton.setPosition(thumbnailWidget.getRight() - FAVORITE_BUTTON_SIZE, thumbnailWidget.getY());
		nameWidget.setRectangle(getWidth(), NAME_HEIGHT, getX(), thumbnailWidget.getBottom());
	}

	public void close() {
		thumbnailWidget.close();
	}

	@Nullable
	public ScreenshotTexture getTextureOrNull() {
		return thumbnailWidget.isLoaded() ? thumbnailWidget.getTexture() : null;
	}

	public @NonNull File getScreenshotFile() {
		return screenshotFile;
	}

	public String getName() {
		return nameWidget.getText();
	}

	public boolean hasNameChanged() {
		return nameWidget.hasChanged();
	}

	public String getPathRelativeToScreenshotDir() {
		return pathRelativeToScreenshotDir;
	}

	@Override
	public void updateNarration(@NonNull NarrationElementOutput output) {
	}

	public @NonNull ScreenshotThumbnailWidget getThumbnailWidget() {
		return thumbnailWidget;
	}

	public @NonNull ScreenshotNameWidget getNameWidget() {
		return nameWidget;
	}
}
