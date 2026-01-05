package me.Azz_9.screenshot_utilities.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryWidget extends ClickableWidget implements AutoCloseable {

	private static final int PADDING = 10;
	private static final int ROW_SPACING = 14;

	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 9.0 / 16.0;

	private final List<ScreenshotEntryWidget> entries = new ArrayList<>();
	private int scrollY;
	private int contentHeight;

	public ScreenshotGalleryWidget(
			int x, int y,
			int width, int height,
			List<File> screenshots
	) {
		super(x, y, width, height, Text.empty());
		buildEntries(screenshots);
	}

	/* ---------------- Layout ---------------- */

	private void buildEntries(List<File> screenshots) {
		entries.clear();

		int availableWidth = getWidth() - PADDING;

		int columns = Math.max(1, availableWidth / (MIN_THUMB_WIDTH + PADDING));
		int thumbWidth = Math.min(MAX_THUMB_WIDTH, availableWidth / columns - PADDING);
		int thumbHeight = (int) (thumbWidth * ASPECT_RATIO);

		int xCursor = getX() + PADDING;
		int yCursor = getY() + PADDING;

		int col = 0;

		for (File file : screenshots) {
			ScreenshotEntryWidget entry = new ScreenshotEntryWidget(
					xCursor,
					yCursor,
					thumbWidth,
					thumbHeight,
					file
			);
			entries.add(entry);

			col++;
			xCursor += thumbWidth + PADDING;

			if (col >= columns) {
				col = 0;
				xCursor = getX() + PADDING;
				yCursor += thumbHeight + ScreenshotEntryWidget.bottomHeight + ROW_SPACING;
			}
		}

		contentHeight = yCursor - getY() + thumbHeight + ScreenshotEntryWidget.bottomHeight;
	}

	/* ---------------- Rendering ---------------- */

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(getX(), getY(), getRight(), getBottom(), 0x7fff0000);

		context.enableScissor(getX(), getY(), getX() + width, getY() + height);

		for (ScreenshotEntryWidget entry : entries) {
			entry.setY(entry.getY() - scrollY);

			if (entry.getY() + entry.getHeight() >= getY()
					&& entry.getY() <= getY() + getHeight()) {
				entry.render(context, mouseX, mouseY, delta);
			}

			entry.setY(entry.getY() + scrollY);
		}

		context.disableScissor();
	}

	/* ---------------- Scroll ---------------- */

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scrollY -= (int) (verticalAmount * 20);
		scrollY = Math.max(0, Math.min(scrollY, Math.max(0, contentHeight - height)));
		return true;
	}

	/* ---------------- Cleanup ---------------- */

	@Override
	public void close() {
		entries.forEach(ScreenshotEntryWidget::close);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
