package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.TexturedCyclingButtonWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent.ScreenshotEntryWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.headerWidget.SearchBar;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotManager;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryWidget extends SimpleParentWidget {

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 9.0 / 16.0;

	// layout
	private static final int PADDING = 10;
	private static final int ROW_SPACING = 14;
	private int contentHeight;

	// smooth scroll
	private static final double SCROLL_SNAP_DISTANCE = 0.5;
	private static final double SCROLL_SPEED = 40.0;
	private static final double SMOOTHING = 25.0;
	private double currentScroll;
	private double targetScroll;
	private long lastUpdateTime = System.nanoTime();

	// separator
	private static final int SEPARATOR_HEIGHT = MINECRAFT.font.lineHeight;
	private final @NonNull List<DateSeparator> separators = new ArrayList<>();

	// header
	// search bar
	private static final int SEARCH_BAR_HEIGHT = 20;
	private static final int SEARCH_BAR_WIDTH = 150;
	private final @NonNull SearchBar searchBar;
	// sort button
	private static final int SORT_BUTTON_SIZE = SEARCH_BAR_HEIGHT;
	private final @NonNull TexturedCyclingButtonWidget<SortMode> sortButton;
	// open screenshot folder button
	private static final int OPEN_FOLDER_BUTTON_SIZE = SEARCH_BAR_HEIGHT;
	private final @NonNull SpriteIconButton openFolderButton;
	// filter
	private static final int FILTER_BUTTON_WIDTH = 100;
	private static final int FILTER_BUTTON_HEIGHT = SEARCH_BAR_HEIGHT;
	private final @NonNull CycleButton<FilterMode> filterButton;

	private static final int HEADER_HEIGHT = SEARCH_BAR_HEIGHT + PADDING * 2;

	// gallery content
	private final @NonNull List<ScreenshotEntryWidget> entries = new ArrayList<>();
	private final @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged;
	private final @Nullable Consumer<Screenshot> onThumbnailClicked;
	// Marge de préchargement en pixels au-delà de la zone visible
	private static final int PRELOAD_MARGIN = 200;
	private static final int FULLVIEW_PRELOAD_RADIUS = 2;

	private final AtomicBoolean refreshing = new AtomicBoolean(false);

	public ScreenshotGalleryWidget(int x, int y, int width, int height,
	                               @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged,
	                               @Nullable Consumer<Screenshot> onThumbnailClicked) {
		super(x, y, width, height);
		this.targetScroll = 0;
		this.currentScroll = 0;

		this.onEntriesChanged = onEntriesChanged;
		this.onThumbnailClicked = onThumbnailClicked;

		this.searchBar = createSearchBar();
		this.filterButton = createFilterButton();
		this.sortButton = createSortButton();
		this.openFolderButton = createOpenFolderButton();
		addAllChildren(searchBar, filterButton, sortButton, openFolderButton);

		ScreenshotList.whenLoaded((screenshots -> {
			// make sure the player didn't leave the screen before building entries
			if (MINECRAFT.screen instanceof ScreenshotGalleryScreen) {
				buildEntries(screenshots);
				searchAndFilter(searchBar.getValue(), filterButton.getValue());
				sortEntries(sortButton.getValue());
				layoutEntries();
			}
		}));
	}

	private SearchBar createSearchBar() {
		SearchBar searchBar = new SearchBar(
				MINECRAFT.font,
				getX() + PADDING, getY() + PADDING,
				SEARCH_BAR_WIDTH, SEARCH_BAR_HEIGHT
		);
		searchBar.setResponder((text) -> {
			searchAndFilter(text, filterButton.getValue());
			layoutEntries();
		});

		return searchBar;
	}

	private CycleButton<FilterMode> createFilterButton() {
		return CycleButton.builder(FilterMode::getText, FilterMode.ALL)
				.withValues(FilterMode.values())
				.create(getX() + SEARCH_BAR_WIDTH + PADDING * 2, getY() + PADDING,
						FILTER_BUTTON_WIDTH, FILTER_BUTTON_HEIGHT,
						Component.translatable("screenshot_utilities.gallery_widget.filter"),
						(btn, val) -> {
							if (MINECRAFT.screen instanceof FocusableScreen focusableScreen)
								focusableScreen.requestFocus(btn);

							searchAndFilter(searchBar.getValue(), val);
							layoutEntries();
						}
				);
	}

	private TexturedCyclingButtonWidget<SortMode> createSortButton() {
		TexturedCyclingButtonWidget<SortMode> cyclingButtonWidget = new TexturedCyclingButtonWidget<>(
				getX() + SEARCH_BAR_WIDTH + FILTER_BUTTON_WIDTH + PADDING * 3, getY() + PADDING,
				SORT_BUTTON_SIZE, SORT_BUTTON_SIZE,
				Config.getInstance().sortOrder.getValue().ordinal(),
				(btn, sortMode) -> {
					sortEntries(sortMode);
					layoutEntries();
				},
				SortMode.values(),
				SortMode::getIcon
		);
		cyclingButtonWidget.setTooltipFactory((value) -> Tooltip.create(value.getText()));

		return cyclingButtonWidget;
	}

	private SpriteIconButton createOpenFolderButton() {
		SpriteIconButton openFolderButton = SpriteIconButton.TextAndIcon.builder(
						Component.translatable("screenshot_utilities.gallery_widget.open_screenshot_folder"),
						(btn) -> {
							if (MINECRAFT.screen instanceof FocusableScreen focusableScreen)
								focusableScreen.requestFocus(btn);

							Util.getPlatform().openPath(Config.getInstance().getScreenshotsDir());
						},
						true
				)
				.withTootip()
				.size(OPEN_FOLDER_BUTTON_SIZE, OPEN_FOLDER_BUTTON_SIZE)
				.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/folder"), 15, 15)
				.build();

		openFolderButton.setPosition(getRight() - PADDING - OPEN_FOLDER_BUTTON_SIZE, getY() + PADDING);

		return openFolderButton;
	}

	/* ---------------- Layout ---------------- */

	private void buildEntries(@NonNull List<Screenshot> screenshots) {
		children().removeAll(entries);
		entries.clear();

		for (Screenshot screenshot : screenshots) {
			addEntry(new ScreenshotEntryWidget(screenshot, onThumbnailClicked));
		}
		if (onEntriesChanged != null) onEntriesChanged.accept(entries);
	}

	public void refresh(final boolean clearCache, final @Nullable Runnable onRefreshComplete) {
		if (!refreshing.compareAndSet(false, true)) return;

		ScreenshotLogger.info("Refreshing screenshot gallery entries");
		if (clearCache) ScreenshotTextureCache.clear();

		ScreenshotList.reloadAsync();

		ScreenshotList.whenLoaded(screenshots -> {
			buildEntries(screenshots);
			searchAndFilter(searchBar.getValue(), filterButton.getValue());
			sortEntries(sortButton.getValue());
			layoutEntries();

			if (onRefreshComplete != null) onRefreshComplete.run();

			refreshing.set(false);
		});
	}

	public void refresh(final boolean clearCache) {
		refresh(clearCache, null);
	}

	private void searchAndFilter(@NonNull String query, FilterMode mode) {
		String q = query.trim().toLowerCase(Locale.ROOT);

		for (ScreenshotEntryWidget entry : entries) {
			boolean visible = (mode == FilterMode.ALL || mode == FilterMode.FAVORITES && ScreenshotManager.isFavorite(entry.getPathRelativeToScreenshotDir())) &&
					(q.isEmpty() || entry.getName().toLowerCase(Locale.ROOT).contains(q));

			entry.setVisible(visible);
			entry.setActive(visible);
		}
	}

	private void sortEntries(SortMode mode) {
		entries.sort(switch (mode) {
			case DATE_ASC -> Comparator.comparingLong(e -> e.getTimestampOrLastModified());
			case DATE_DESC -> Comparator.comparingLong(e -> -e.getTimestampOrLastModified());
		});
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

			LocalDate entryDate = Instant.ofEpochMilli(entry.getTimestampOrLastModified()).atZone(ZoneId.systemDefault()).toLocalDate();

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
	public void renderWidget(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		updateScroll();

		// background
		graphics.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK_TRANSPARENT);

		renderHeader(graphics, mouseX, mouseY, delta);

		if (!ScreenshotList.isLoaded()) {
			Loading.drawLoadingSpinner(
					graphics,
					getX() + getWidth() / 2,
					getY() + getHeight() / 2,
					getWidth() / 50, getWidth() / 20
			);
			return;
		} else if (entries.isEmpty()) {
			graphics.centeredText(MINECRAFT.font, Component.translatable("screenshot_utilities.gallery_widget.no_screenshot").withStyle(ChatFormatting.ITALIC),
					getX() + getWidth() / 2, getY() + getHeight() / 5, Colors.GRAY);
			return;
		}

		graphics.enableScissor(getX(), getY() + HEADER_HEIGHT, getX() + getWidth(), getY() + getHeight());

		renderSeparators(graphics);
		renderEntries(graphics, mouseX, mouseY, delta);

		graphics.disableScissor();
	}

	private void renderHeader(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		this.searchBar.extractRenderState(graphics, mouseX, mouseY, delta);
		this.filterButton.extractRenderState(graphics, mouseX, mouseY, delta);
		this.sortButton.extractRenderState(graphics, mouseX, mouseY, delta);
		this.openFolderButton.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void renderSeparators(@NonNull GuiGraphicsExtractor graphics) {
		for (DateSeparator sep : separators) {
			int y = sep.y() - (int) currentScroll;

			if (y < getY() || y > getBottom()) continue;

			String text = sep.date().format(DateTimeFormatter.ofPattern("dd LLLL yyyy"));

			int textWidth = MINECRAFT.font.width(text);
			int textLeft = getX() + (getWidth() - textWidth) / 2;
			int textRight = textLeft + textWidth;
			int textPadding = 5;

			graphics.text(MINECRAFT.font, text, textLeft, y, Colors.GRAY, false);

			int lineY = y + MINECRAFT.font.lineHeight / 2;

			graphics.fill(getX() + PADDING, lineY, textLeft - textPadding, lineY + 1, Colors.GRAY);
			graphics.fill(textRight + textPadding, lineY, getRight() - PADDING, lineY + 1, Colors.GRAY);
		}
	}

	private void renderEntries(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int visibleTop = getY() + HEADER_HEIGHT;
		int visibleBottom = getY() + getHeight();

		for (ScreenshotEntryWidget entry : entries) {
			if (!entry.isVisible()) continue;

			int yRender = entry.getBaseY() - (int) currentScroll;
			entry.setY(yRender);

			int entryBottom = yRender + entry.getHeight();

			// Préchargement si l'entrée approche de la zone visible
			if (entryBottom >= visibleTop - PRELOAD_MARGIN && yRender <= visibleBottom + PRELOAD_MARGIN) {
				entry.triggerLoad();
			}

			// Rendu uniquement si réellement visible
			if (entryBottom >= visibleTop && yRender <= visibleBottom) {
				entry.extractRenderState(graphics, mouseX, mouseY, delta);
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

		currentScroll += (targetScroll - currentScroll) * alpha;
		if (Math.abs(targetScroll - currentScroll) < SCROLL_SNAP_DISTANCE) {
			currentScroll = targetScroll;
		}

		currentScroll = Mth.clamp(currentScroll, 0.0, getMaxScroll());
	}

	private void checkScroll() {
		if (currentScroll > this.getMaxScroll()) {
			currentScroll = this.getMaxScroll();
			targetScroll = this.getMaxScroll();
		}
	}

	public void preloadAround(@NonNull Screenshot screenshot) {
		int index = indexOf(screenshot);
		if (index < 0) return;

		for (int i = index - FULLVIEW_PRELOAD_RADIUS; i <= index + FULLVIEW_PRELOAD_RADIUS; i++) {
			if (i >= 0 && i < entries.size()) {
				entries.get(i).triggerLoad();
			}
		}
	}

	@Override
	public void updateNarration(@NonNull NarrationElementOutput output) {
	}

	private void addEntry(@NonNull ScreenshotEntryWidget entry) {
		entries.add(entry);
	}

	public @NonNull List<ScreenshotEntryWidget> getEntries() {
		return entries;
	}

	@Override
	public @NonNull List<GuiEventListener> children() {
		List<GuiEventListener> children = super.children();
		children.addAll(entries);
		return children;
	}

	public int indexOf(@NonNull Screenshot screenshot) {
		for (int i = 0; i < entries.size(); i++) {
			ScreenshotEntryWidget entry = entries.get(i);
			if (entry.getScreenshot() == screenshot) {
				return i;
			}
		}
		return -1;
	}

	@Nullable
	public Screenshot getPreviousVisibleScreenshot(@NonNull Screenshot screenshot) {
		int index = indexOf(screenshot);
		while (--index >= 0) {
			ScreenshotEntryWidget entry = entries.get(index);
			if (entry.isVisible()) {
				return entry.getScreenshot();
			}
		}
		return null;
	}

	@Nullable
	public Screenshot getNextVisibleScreenshot(@NonNull Screenshot screenshot) {
		int index = indexOf(screenshot);
		while (++index < entries.size()) {
			ScreenshotEntryWidget entry = entries.get(index);
			if (entry.isVisible()) {
				return entry.getScreenshot();
			}
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
			this.icon = Identifier.fromNamespaceAndPath(MOD_ID, "icon/" + icon);
		}

		public @NonNull String getTranslationKey() {
			return translationKey;
		}

		public @NonNull Component getText() {
			return Component.translatable(getTranslationKey());
		}

		public @NonNull Identifier getIcon() {
			return icon;
		}
	}

	public enum FilterMode {
		ALL("All"),
		FAVORITES("Favorites");

		private final @NonNull String translationKey;

		FilterMode(@NonNull String translationKey) {
			this.translationKey = translationKey;
		}

		public @NonNull String getTranslationKey() {
			return translationKey;
		}

		public @NonNull Component getText() {
			return Component.translatable(getTranslationKey());
		}
	}
}