package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.TexturedCyclingButtonWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent.ScreenshotEntryWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.headerWidget.SearchBar;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryWidget extends SimpleParentWidget implements AutoCloseable {

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 9.0 / 16.0;
	private final @NonNull CompletableFuture<List<File>> screenshotFiles;
	private final @NonNull File screenshotFolder;

	// layout
	private static final int PADDING = 10;
	private static final int ROW_SPACING = 14;
	private int contentHeight;

	// smooth scroll
	private static final double SCROLL_SPEED = 40.0;
	private static final double SMOOTHING = 25.0;
	private int currentScroll;
	private int targetScroll;
	private long lastUpdateTime = System.nanoTime();

	// separator
	private static final int SEPARATOR_HEIGHT = CLIENT.textRenderer.fontHeight;
	private final @NonNull List<DateSeparator> separators = new ArrayList<>();

	// header
	// search bar
	private static final int SEARCH_BAR_HEIGHT = 20;
	private static final int SEARCH_BAR_WIDTH = 150;
	private final @NonNull SearchBar searchBar;
	// sort button
	private static final int SORT_BUTTON_SIZE = SEARCH_BAR_HEIGHT;
	private final @NonNull TexturedCyclingButtonWidget<SortMode> sortButton;

	private static final int HEADER_HEIGHT = SEARCH_BAR_HEIGHT + PADDING * 2;

	// gallery content
	private final @NonNull List<ScreenshotEntryWidget> entries = new ArrayList<>();

	private boolean refreshing = false;

	public ScreenshotGalleryWidget(int x, int y, int width, int height, @NonNull File screenshotFolder) {
		super(x, y, width, height);
		this.targetScroll = 0;
		this.currentScroll = 0;

		this.searchBar = createSearchBar();
		this.sortButton = createSortButton();
		addAllChildren(searchBar, sortButton);

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
				buildEntries(screenshots, false);
				filter(searchBar.getText(), false);
				sortEntries(sortButton.getValue(), false);
				layoutEntries();
			}
		}, CLIENT);
	}

	private SearchBar createSearchBar() {
		SearchBar searchBar = new SearchBar(
				CLIENT.textRenderer,
				getX() + PADDING, getY() + PADDING,
				SEARCH_BAR_WIDTH, SEARCH_BAR_HEIGHT
		);
		searchBar.setChangedListener((text) -> {
			if (text != null) {
				filter(text, true);
			}
		});

		return searchBar;
	}

	private TexturedCyclingButtonWidget<SortMode> createSortButton() {
		TexturedCyclingButtonWidget<SortMode> cyclingButtonWidget = new TexturedCyclingButtonWidget<>(
				getX() + SEARCH_BAR_WIDTH + PADDING * 2, getY() + PADDING,
				SORT_BUTTON_SIZE, SORT_BUTTON_SIZE,
				Config.getInstance().sortOrder.getValue().ordinal(),
				(btn, sortMode) -> sortEntries(sortMode, true),
				SortMode.values(),
				SortMode::getIcon
		);
		cyclingButtonWidget.setTooltipFactory((value) -> Tooltip.of(value.getText()));

		return cyclingButtonWidget;
	}

	private static LocalDate getScreenshotDate(File file) {
		return Instant.ofEpochMilli(file.lastModified())
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	/* ---------------- Layout ---------------- */

	private void buildEntries(@NonNull List<File> screenshots, boolean reloadLayout) {
		entries.forEach(ScreenshotEntryWidget::close);
		entries.clear();

		for (File file : screenshots) {
			addEntry(new ScreenshotEntryWidget(file));
		}

		if (reloadLayout) layoutEntries();
	}

	public void refresh() {
		if (refreshing) return;

		refreshing = true;

		ScreenshotLogger.info("Refreshing screenshot gallery entries");
		ScreenshotTextureCache.clear();
		buildEntries(
				Arrays.stream(
						Objects.requireNonNull(
								screenshotFolder.listFiles(f ->
										f.getName().endsWith(".png")
												|| f.getName().endsWith(".jpg")
												|| f.getName().endsWith(".jpeg")
								)
						)
				).toList(),
				true
		);

		refreshing = false;
	}

	private void filter(@NonNull String query, boolean reloadLayout) {
		String q = query.trim().toLowerCase(Locale.ROOT);

		for (ScreenshotEntryWidget entry : entries) {
			boolean matches = q.isEmpty() || entry.getName().toLowerCase(Locale.ROOT).contains(q);

			entry.setVisible(matches);
		}

		if (reloadLayout) layoutEntries();
	}

	private void sortEntries(SortMode mode, boolean reloadLayout) {
		entries.sort(switch (mode) {
			case DATE_ASC -> Comparator.comparingLong(e -> e.getScreenshotFile().lastModified());
			case DATE_DESC -> Comparator.comparingLong(e -> -e.getScreenshotFile().lastModified());
		});

		if (reloadLayout) layoutEntries();
	}


	private void layoutEntries() {
		separators.clear();

		int availableWidth = getWidth() - PADDING;

		int columns = Math.max(1, availableWidth / (MIN_THUMB_WIDTH + PADDING));
		int thumbWidth = Math.min(MAX_THUMB_WIDTH, availableWidth / columns - PADDING);
		int thumbHeight = (int) (thumbWidth * ASPECT_RATIO);

		int xCursor = getX() + PADDING;
		int yCursor = getY() + HEADER_HEIGHT + PADDING;

		int col = 0;
		LocalDate lastDate = null;

		for (ScreenshotEntryWidget entry : entries) {

			if (!entry.isVisible()) {
				continue;
			}

			LocalDate entryDate = getScreenshotDate(entry.getScreenshotFile());

			// separator
			if (!entryDate.equals(lastDate)) {

				if (col != 0) {
					col = 0;
					xCursor = getX() + PADDING;
					yCursor += thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT + ROW_SPACING;
				}

				separators.add(new DateSeparator(yCursor, entryDate));
				yCursor += SEPARATOR_HEIGHT + ROW_SPACING;
			}

			lastDate = entryDate;

			entry.setX(xCursor);
			entry.setBaseY(yCursor);
			entry.setWidth(thumbWidth);
			entry.setHeight(thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT);

			col++;
			xCursor += thumbWidth + PADDING;

			if (col >= columns) {
				col = 0;
				xCursor = getX() + PADDING;
				yCursor += thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT + ROW_SPACING;
			}
		}

		if (col == 0) yCursor -= thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT + ROW_SPACING;
		contentHeight = yCursor - getY() + thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT + PADDING;

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

		renderSeparators(context);
		renderEntries(context, mouseX, mouseY, delta);

		context.disableScissor();
	}

	private void renderHeader(@NonNull DrawContext context, int mouseX, int mouseY, float delta) {
		this.searchBar.render(context, mouseX, mouseY, delta);
		this.sortButton.render(context, mouseX, mouseY, delta);
	}

	private void renderSeparators(@NonNull DrawContext context) {
		for (DateSeparator sep : separators) {
			int y = sep.y() - currentScroll;

			if (y < getY() || y > getBottom()) continue;

			String text = sep.date().format(DateTimeFormatter.ofPattern("dd LLLL yyyy"));

			int textWidth = CLIENT.textRenderer.getWidth(text);
			int textLeft = getX() + (getWidth() - textWidth) / 2;
			int textRight = textLeft + textWidth;
			int textPadding = 5;

			context.drawText(CLIENT.textRenderer, text, textLeft, y, Colors.GRAY, false);

			int lineY = y + CLIENT.textRenderer.fontHeight / 2;

			context.fill(getX() + PADDING, lineY, textLeft - textPadding, lineY + 1, Colors.GRAY);
			context.fill(textRight + textPadding, lineY, getRight() - PADDING, lineY + 1, Colors.GRAY);
		}
	}

	private void renderEntries(@NonNull DrawContext context, int mouseX, int mouseY, float delta) {
		for (ScreenshotEntryWidget entry : entries) {
			if (!entry.isVisible()) continue;

			int yRender = entry.getBaseY() - currentScroll;
			entry.setY(yRender);

			if (yRender + entry.getHeight() >= getY() && yRender <= getBottom()) {
				entry.render(context, mouseX, mouseY, delta);
			}
		}
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

	@Override
	public void close() {
		screenshotFiles.cancel(true);
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

	private record DateSeparator(int y, LocalDate date) {
	}

	public enum SortMode {
		DATE_ASC("screenshot_utilities.gallery_widget.sort_mode.date_asc", "sort_asc"),
		DATE_DESC("screenshot_utilities.gallery_widget.sort_mode.date_desc", "sort_desc");

		private final @NonNull String translationKey;
		private final @NonNull Identifier icon;

		SortMode(@NonNull String translationKey, @NonNull String icon) {
			this.translationKey = translationKey;
			this.icon = Identifier.of(MOD_ID, "icon/" + icon);
		}

		public @NonNull String getTranslationKey() {
			return translationKey;
		}

		public @NonNull Text getText() {
			return Text.translatable(getTranslationKey());
		}

		public @NonNull Identifier getIcon() {
			return icon;
		}
	}
}