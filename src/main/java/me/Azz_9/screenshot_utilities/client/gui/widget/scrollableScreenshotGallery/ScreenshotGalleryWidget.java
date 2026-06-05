package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.SmoothScrollableWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.TexturedCyclingButtonWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent.ScreenshotEntryWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.headerWidget.SearchBar;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotManager;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryWidget extends SmoothScrollableWidget {

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 9.0 / 16.0;

	// layout
	private static final int PADDING = 10;
	private static final int ROW_SPACING = 14;

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

	// separator
	private static final int SEPARATOR_HEIGHT = MINECRAFT.font.lineHeight;
	private final @NonNull List<DateSeparator> separators = new ArrayList<>();

	private record DateSeparator(int contentY, LocalDate date) {
	}

	// gallery content
	private final @NonNull List<ScreenshotEntryWidget> entries = new ArrayList<>();
	private final @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged;
	private final @Nullable Consumer<Screenshot> onThumbnailClicked;
	// Marge de préchargement en pixels au-delà de la zone visible
	private static final int PRELOAD_MARGIN = 200;
	private static final int FULLVIEW_PRELOAD_RADIUS = 2;

	private int totalContentHeight = 0;

	private final AtomicBoolean refreshing = new AtomicBoolean(false);

	public ScreenshotGalleryWidget(int x, int y, int width, int height,
	                               @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged,
	                               @Nullable Consumer<Screenshot> onThumbnailClicked) {
		super(x, y, width, height);
		this.onEntriesChanged = onEntriesChanged;
		this.onThumbnailClicked = onThumbnailClicked;

		this.searchBar = createSearchBar();
		this.filterButton = createFilterButton();
		this.sortButton = createSortButton();
		this.openFolderButton = createOpenFolderButton();
		addFixedChild(searchBar);
		addFixedChild(filterButton);
		addFixedChild(sortButton);
		addFixedChild(openFolderButton);

		ScreenshotList.whenScreenshotsLoaded((screenshots -> {
			// make sure the player didn't leave the screen before building entries
			if (MINECRAFT.screen instanceof ScreenshotGalleryScreen) {
				buildEntries(screenshots);
				searchAndFilter(searchBar.getValue(), filterButton.getValue());
				sortEntries(sortButton.getValue());
				layoutEntries();
			}
		}));
	}

	// header widget factories

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

							Util.getPlatform().openPath(Config.getInstance().getAbsoluteScreenshotsDir());
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

	// SmoothScrollableWidget contract

	@Override
	protected int getTotalScrollableHeight() {
		return totalContentHeight;
	}

	@Override
	protected @NonNull ScrollArea getScrollArea() {
		return new ScrollArea(getX(), getY() + HEADER_HEIGHT, getRight(), getBottom());
	}

	// rendering

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		// background
		graphics.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK_TRANSPARENT);

		if (!ScreenshotList.isLoaded()) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
			Loading.drawLoadingSpinner(
					graphics,
					getX() + getWidth() / 2,
					getY() + getHeight() / 2,
					getWidth() / 50, getWidth() / 20
			);
			return;
		} else if (entries.isEmpty()) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
			graphics.centeredText(MINECRAFT.font, Component.translatable("screenshot_utilities.gallery_widget.no_screenshot").withStyle(ChatFormatting.ITALIC),
					getX() + getWidth() / 2, getY() + getHeight() / 5, Colors.GRAY);
			return;
		}

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

		renderSeparators(graphics);
	}

	@Override
	protected void onBeforeRenderScrollableChild(@NonNull GuiGraphicsExtractor graphics, @NonNull AbstractWidget widget, int screenY, int mouseX, int mouseY, float deltaTicks) {
		if (!(widget instanceof ScreenshotEntryWidget entry)) return;

		ScrollArea area = getScrollArea();
		int entryBottom = screenY + entry.getHeight();

		// Preload if near the visible area
		if (entryBottom >= area.top() - PRELOAD_MARGIN && screenY <= area.bottom() + PRELOAD_MARGIN) {
			entry.triggerLoad();
		}
	}

	private void renderSeparators(@NonNull GuiGraphicsExtractor graphics) {
		ScrollArea area = getScrollArea();
		int baseY = area.top() - (int) getScrollOffset();

		graphics.enableScissor(area.left(), area.top(), area.right(), area.bottom());

		for (DateSeparator sep : separators) {
			int y = baseY + sep.contentY();
			if (y + SEPARATOR_HEIGHT < area.top() || y > area.bottom()) continue;

			String text = sep.date().format(DateTimeFormatter.ofPattern("dd LLLL yyyy"));
			int textWidth = MINECRAFT.font.width(text);
			int textLeft = getX() + (getWidth() - textWidth) / 2;
			int textRight = textLeft + textWidth;
			int textPad = 5;
			int lineY = y + MINECRAFT.font.lineHeight / 2;

			graphics.text(MINECRAFT.font, text, textLeft, y, Colors.GRAY, false);
			graphics.fill(getX() + PADDING, lineY, textLeft - textPad, lineY + 1, Colors.GRAY);
			graphics.fill(textRight + textPad, lineY, getRight() - PADDING, lineY + 1, Colors.GRAY);
		}

		graphics.disableScissor();
	}

	/* ---------------- Layout ---------------- */

	private void buildEntries(@NonNull List<Screenshot> screenshots) {
		clearScrollableChildren();
		entries.clear();

		for (Screenshot screenshot : screenshots) {
			ScreenshotEntryWidget entry = new ScreenshotEntryWidget(screenshot, onThumbnailClicked);
			entries.add(entry);
			addScrollableChild(entry);
		}
		if (onEntriesChanged != null) onEntriesChanged.accept(entries);
	}

	private void layoutEntries() {
		separators.clear();

		int availableWidth = getWidth() - PADDING;
		int columns = Math.max(1, availableWidth / (MIN_THUMB_WIDTH + PADDING));
		int thumbWidth = Math.min(MAX_THUMB_WIDTH, availableWidth / columns - PADDING);
		int thumbHeight = (int) (thumbWidth * ASPECT_RATIO);
		int entryHeight = thumbHeight + ScreenshotEntryWidget.NAME_HEIGHT;

		int xCursor = getX() + PADDING;
		int yCursor = PADDING;
		int col = 0;
		LocalDate lastDate = null;

		for (ScreenshotEntryWidget entry : entries) {
			if (!entry.isVisible()) continue;

			LocalDate entryDate = Instant.ofEpochMilli(entry.getTimestampOrLastModified()).atZone(ZoneId.systemDefault()).toLocalDate();

			// separator
			if (!entryDate.equals(lastDate)) {

				if (col != 0) {
					col = 0;
					xCursor = getX() + PADDING;
					yCursor += entryHeight + ROW_SPACING;
				}

				separators.add(new DateSeparator(yCursor, entryDate));
				yCursor += SEPARATOR_HEIGHT + ROW_SPACING;
			}

			lastDate = entryDate;

			entry.setX(xCursor);
			entry.setY(yCursor);
			entry.setWidth(thumbWidth);
			entry.setHeight(entryHeight);

			col++;
			xCursor += thumbWidth + PADDING;

			if (col >= columns) {
				col = 0;
				xCursor = getX() + PADDING;
				yCursor += entryHeight + ROW_SPACING;
			}
		}

		if (col == 0 && !entries.isEmpty()) yCursor -= entryHeight + ROW_SPACING;
		totalContentHeight = yCursor + entryHeight + PADDING;

		clampScroll();
	}

	private void searchAndFilter(@NonNull String query, FilterMode mode) {
		String q = query.trim().toLowerCase(Locale.ROOT);

		for (ScreenshotEntryWidget entry : entries) {
			boolean visible = (mode == FilterMode.ALL || mode == FilterMode.FAVORITES && ScreenshotManager.isFavorite(entry.getPathRelativeToScreenshotDir()))
					&& (q.isEmpty() || entry.getName().toLowerCase(Locale.ROOT).contains(q));

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

	// refresh

	public void refresh(final boolean clearCache, final @Nullable Runnable onRefreshComplete) {
		if (!refreshing.compareAndSet(false, true)) return;

		ScreenshotLogger.info("Refreshing screenshot gallery entries");
		if (clearCache) ScreenshotTextureCache.clear();

		ScreenshotList.reloadAsync();

		ScreenshotList.whenScreenshotsLoaded(screenshots -> {
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

	// navigation helpers

	public void preloadAround(@NonNull Screenshot screenshot) {
		int index = indexOf(screenshot);
		if (index < 0) return;

		for (int i = index - FULLVIEW_PRELOAD_RADIUS; i <= index + FULLVIEW_PRELOAD_RADIUS; i++) {
			if (i >= 0 && i < entries.size()) {
				entries.get(i).triggerLoad();
			}
		}
	}

	public int indexOf(@NonNull Screenshot screenshot) {
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getScreenshot() == screenshot) {
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

	public @NonNull List<ScreenshotEntryWidget> getEntries() {
		return Collections.unmodifiableList(entries);
	}


	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
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