package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import org.jspecify.annotations.NonNull;

import java.io.File;

@Environment(EnvType.CLIENT)
public class ScreenshotEntryWidget extends SimpleParentWidget implements ParentElement {

	public static final int NAME_HEIGHT = 30;

	private final @NonNull ScreenshotThumbnailWidget thumbnailWidget;
	private final @NonNull ScreenshotNameWidget nameWidget;

	private final @NonNull File screenshotFile;
	private int baseY;

	public ScreenshotEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, @NonNull File screenshotFile) {
		super(x, y, thumbnailWidth, thumbnailHeight + NAME_HEIGHT);

		this.baseY = y;
		this.screenshotFile = screenshotFile;

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

		addAllChildren(thumbnailWidget, nameWidget);
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
	public void renderWidget(@NonNull DrawContext context, int mouseX, int mouseY, float delta) {
		// fond global
		context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK_TRANSPARENT);

		thumbnailWidget.render(context, mouseX, mouseY, delta);
		nameWidget.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void setX(int x) {
		if (getX() != x) {
			super.setX(x);
			thumbnailWidget.setX(x);
			nameWidget.setX(x);
		}
	}

	@Override
	public void setY(int y) {
		if (getY() != y) {
			super.setY(y);
			thumbnailWidget.setY(y);
			nameWidget.setY(thumbnailWidget.getBottom());
		}
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		thumbnailWidget.setWidth(width);
		nameWidget.setWidth(width);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		thumbnailWidget.setHeight(height - NAME_HEIGHT);
	}

	public void close() {
		thumbnailWidget.close();
	}

	@NonNull
	public ScreenshotTexture getTexture() {
		return thumbnailWidget.getTexture();
	}

	public @NonNull File getScreenshotFile() {
		return screenshotFile;
	}

	public String getName() {
		return nameWidget.getText();
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
	}
}
