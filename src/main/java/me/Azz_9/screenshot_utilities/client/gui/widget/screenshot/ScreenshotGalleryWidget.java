package me.Azz_9.screenshot_utilities.client.gui.widget.screenshot;

import me.Azz_9.screenshot_utilities.client.gui.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryWidget extends SimpleParentWidget implements AutoCloseable {

	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final int PADDING = 10;
	private static final int ROW_SPACING = 14;
	private static final double ASPECT_RATIO = 9.0 / 16.0;
	private static final double SCROLL_SPEED = 40.0;
	private static final double SMOOTHING = 25.0;

	private final FocusManager focusManager;
	private final List<ScreenshotEntryWidget> entries = new ArrayList<>();

	private int currentScroll;
	private int targetScroll;
	private long lastUpdateTime = System.nanoTime();

	private int contentHeight;

	public ScreenshotGalleryWidget(int x, int y, int width, int height, List<File> screenshots, FocusManager focusManager) {
		super(x, y, width, height);
		this.targetScroll = 0;
		this.currentScroll = 0;
		this.focusManager = focusManager;
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
			ScreenshotEntryWidget entry = new ScreenshotEntryWidget(xCursor, yCursor, thumbWidth, thumbHeight, file, focusManager);
			addEntry(entry);

			col++;
			xCursor += thumbWidth + PADDING;

			if (col >= columns) {
				col = 0;
				xCursor = getX() + PADDING;
				yCursor += thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT + ROW_SPACING;
			}
		}

		contentHeight = yCursor - getY() + thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT;
	}

	/* ---------------- Rendering ---------------- */

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		updateScroll();

		context.fill(getX(), getY(), getRight(), getBottom(), 0x7fff0000);

		context.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());

		for (ScreenshotEntryWidget entry : entries) {
			int yRender = entry.getBaseY() - currentScroll;
			entry.setY(entry.getY() - currentScroll);

			if (yRender + entry.getHeight() >= getY() && yRender <= getBottom()) {
				entry.setY(yRender);
				entry.render(context, mouseX, mouseY, delta);
			}
		}

		context.disableScissor();
	}

	/* ---------------- Scroll ---------------- */

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		targetScroll -= (int) (verticalAmount * SCROLL_SPEED);
		targetScroll = Math.clamp(targetScroll, 0, getMaxScroll());
		return true;
	}

	private int getMaxScroll() {
		return Math.max(0, contentHeight - getHeight());
	}

	private void updateScroll() {
		long currentTime = System.nanoTime();
		double deltaSeconds = (currentTime - lastUpdateTime) / 1_000_000_000.0;
		lastUpdateTime = currentTime;

		double alpha = 1.0 - Math.exp(-SMOOTHING * deltaSeconds);

		currentScroll += (int) ((targetScroll - currentScroll) * alpha);
	}

	/* ---------------- Cleanup ---------------- */

	@Override
	public void close() {
		entries.forEach(ScreenshotEntryWidget::close);
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
	}

	private void addEntry(ScreenshotEntryWidget entry) {
		entries.add(entry);
		addChild(entry);
	}

	public FocusManager getFocusManager() {
		return focusManager;
	}
}