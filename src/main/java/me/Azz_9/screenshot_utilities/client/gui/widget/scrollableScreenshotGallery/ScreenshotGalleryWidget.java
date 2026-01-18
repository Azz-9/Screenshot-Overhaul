package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryWidget extends SimpleParentWidget implements AutoCloseable {

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 9.0 / 16.0;
	// layout
	private static final int PADDING = 10;
	private static final int ROW_SPACING = 14;
	private static final int SEARCH_BAR_HEIGHT = 20;
	private static final int HEADER_HEIGHT = SEARCH_BAR_HEIGHT + PADDING * 2;
	// smooth scroll
	private static final double SCROLL_SPEED = 40.0;
	private static final double SMOOTHING = 25.0;

	// header
	private final @NonNull SearchBar searchBar;

	// gallery content
	private final @NonNull List<@NonNull ScreenshotEntryWidget> entries = new ArrayList<>();

	private final @NonNull CompletableFuture<List<File>> screenshotFiles;
	private final @NonNull File screenshotFolder;

	private boolean refreshing = false;

	private int currentScroll;
	private int targetScroll;
	private long lastUpdateTime = System.nanoTime();

	private int contentHeight;

	public ScreenshotGalleryWidget(int x, int y, int width, int height, @NonNull File screenshotFolder) {
		super(x, y, width, height);
		this.targetScroll = 0;
		this.currentScroll = 0;

		this.searchBar = new SearchBar(
				CLIENT.textRenderer,
				getX() + PADDING, getY() + PADDING,
				getWidth() - PADDING * 2, SEARCH_BAR_HEIGHT
		);
		this.searchBar.setChangedListener((text) -> {
			if (text != null) {
				filter(text);
			}
		});
		addChild(searchBar);

		this.screenshotFolder = screenshotFolder;

		this.screenshotFiles = CompletableFuture.supplyAsync(
				() -> Arrays.stream(
						Objects.requireNonNull(
								screenshotFolder.listFiles(f ->
										f.getName().endsWith(".png")
												|| f.getName().endsWith(".jpg")
												|| f.getName().endsWith(".jpeg")
								)
						)
				).toList(),
				Util.getMainWorkerExecutor()
		);

		this.screenshotFiles.thenAcceptAsync(screenshots -> {
			// make sure the player didn't leave the screen before building entries
			if (CLIENT.currentScreen instanceof ScreenshotGalleryScreen) {
				buildEntries(screenshots);
			}
		}, CLIENT);
	}

	/* ---------------- Layout ---------------- */

	private void buildEntries(@NonNull List<File> screenshots) {
		entries.forEach(ScreenshotEntryWidget::close);
		entries.clear();

		for (File file : screenshots) {
			addEntry(new ScreenshotEntryWidget(file));
		}

		layoutEntries();
	}

	public void refresh() {
		if (refreshing) return;

		refreshing = true;

		ScreenshotLogger.info("Refreshing screenshot gallery entries");
		buildEntries(
				Arrays.stream(
						Objects.requireNonNull(
								screenshotFolder.listFiles(f ->
										f.getName().endsWith(".png")
												|| f.getName().endsWith(".jpg")
												|| f.getName().endsWith(".jpeg")
								)
						)
				).toList()
		);

		refreshing = false;
	}

	private void filter(@NonNull String query) {
		String q = query.trim().toLowerCase(Locale.ROOT);

		for (ScreenshotEntryWidget entry : entries) {
			boolean matches = q.isEmpty() || entry.getName().toLowerCase(Locale.ROOT).contains(q);

			entry.setVisible(matches);
		}

		layoutEntries();
	}

	private void layoutEntries() {
		int availableWidth = getWidth() - PADDING;

		int columns = Math.max(1, availableWidth / (MIN_THUMB_WIDTH + PADDING));
		int thumbWidth = Math.min(MAX_THUMB_WIDTH, availableWidth / columns - PADDING);
		int thumbHeight = (int) (thumbWidth * ASPECT_RATIO);

		int xCursor = getX() + PADDING;
		int yCursor = getY() + HEADER_HEIGHT + PADDING;

		int col = 0;

		for (ScreenshotEntryWidget entry : entries) {

			if (!entry.isVisible()) {
				continue;
			}

			entry.setX(xCursor);
			entry.setBaseY(yCursor);
			entry.setWidth(thumbWidth);
			entry.setHeight(thumbHeight);

			col++;
			xCursor += thumbWidth + PADDING;

			if (col >= columns) {
				col = 0;
				xCursor = getX() + PADDING;
				yCursor += thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT + ROW_SPACING;
			}
		}

		contentHeight = yCursor - getY() + thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT;

		checkScroll();
	}


	/* ---------------- Rendering ---------------- */

	@Override
	public void renderWidget(@NonNull DrawContext context, int mouseX, int mouseY, float delta) {
		updateScroll();

		// background
		context.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK_TRANSPARENT);

		renderHeader(context, mouseX, mouseY, delta);

		if (!screenshotFiles.isDone()) {
			Loading.drawLoadingSpinner(
					context,
					getX() + getWidth() / 2,
					getY() + getHeight() / 2,
					getWidth() / 25, getWidth() / 10
			);
			return;
		} else if (entries.isEmpty()) {
			context.drawCenteredTextWithShadow(CLIENT.textRenderer, Text.translatable("screenshot_utilities.gallery_widget.no_screenshot").formatted(Formatting.ITALIC),
					getX() + getWidth() / 2, getY() + getHeight() / 5, Colors.GRAY);
			return;
		}

		context.enableScissor(getX(), getY() + HEADER_HEIGHT, getX() + getWidth(), getY() + getHeight());

		for (ScreenshotEntryWidget entry : entries) {
			if (!entry.isVisible()) continue;

			int yRender = entry.getBaseY() - currentScroll;
			entry.setY(yRender);

			if (yRender + entry.getHeight() >= getY() && yRender <= getBottom()) {
				entry.render(context, mouseX, mouseY, delta);
			}
		}

		context.disableScissor();
	}

	private void renderHeader(@NonNull DrawContext context, int mouseX, int mouseY, float delta) {
		this.searchBar.render(context, mouseX, mouseY, delta);
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

	private void checkScroll() {
		if (currentScroll > this.getMaxScroll()) {
			currentScroll = this.getMaxScroll();
			targetScroll = this.getMaxScroll();
		}
	}

	/* ---------------- Cleanup ---------------- */

	@Override
	public void close() {
		screenshotFiles.cancel(true);
		entries.forEach(ScreenshotEntryWidget::close);
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
	}

	private void addEntry(@NonNull ScreenshotEntryWidget entry) {
		entries.add(entry);
		addChild(entry);
	}

	public int indexOf(@NonNull ScreenshotTexture texture) {
		for (int i = 0; i < entries.size(); i++) {
			ScreenshotEntryWidget entry = entries.get(i);
			if (entry.getTexture() == texture) {
				return i;
			}
		}
		return -1;
	}

	@Nullable
	public ScreenshotTexture getPrevious(@NonNull ScreenshotTexture current) {
		int index = indexOf(current);
		if (index > 0) {
			return entries.get(index - 1).getTexture();
		}
		return null;
	}

	@Nullable
	public ScreenshotTexture getNext(@NonNull ScreenshotTexture current) {
		int index = indexOf(current);
		if (index >= 0 && index < entries.size() - 1) {
			return entries.get(index + 1).getTexture();
		}
		return null;
	}
}