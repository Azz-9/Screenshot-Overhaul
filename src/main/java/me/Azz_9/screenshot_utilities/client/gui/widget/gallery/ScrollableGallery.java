package me.Azz_9.screenshot_utilities.client.gui.widget.gallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.SmoothScrollableWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.TexturedCyclingButtonWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.headerWidget.SearchBar;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshotGallery.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotManager;

@Environment(EnvType.CLIENT)
public abstract class ScrollableGallery<T extends AbstractGalleryEntryWidget> extends SmoothScrollableWidget {

	protected static final int PADDING = 10;
	protected static final int ROW_SPACING = 14;
	private static final int PRELOAD_MARGIN = 200;

	private final int minThumbWidth;
	private final int maxThumbWidth;
	private final double aspectRatio;

	// header
	// search bar
	private static final int SEARCH_BAR_HEIGHT = 20;
	private static final int SEARCH_BAR_WIDTH = 150;
	private final @NonNull SearchBar searchBar;
	// sort button
	private static final int SORT_BUTTON_SIZE = SEARCH_BAR_HEIGHT;
	private final @NonNull TexturedCyclingButtonWidget<ScreenshotGalleryWidget.SortMode> sortButton;
	// open screenshot folder button
	private static final int OPEN_FOLDER_BUTTON_SIZE = SEARCH_BAR_HEIGHT;
	private final @NonNull SpriteIconButton openFolderButton;
	// filter
	private static final int FILTER_BUTTON_WIDTH = 100;
	private static final int FILTER_BUTTON_HEIGHT = SEARCH_BAR_HEIGHT;
	private final @NonNull CycleButton<ScreenshotGalleryWidget.FilterMode> filterButton;

	protected static final int HEADER_HEIGHT = SEARCH_BAR_HEIGHT + PADDING * 2;

	// separator
	private static final int SEPARATOR_HEIGHT = MINECRAFT.font.lineHeight;
	private final @NonNull List<DateSeparator> separators = new ArrayList<>();
	private @Nullable LocalDate lastLayoutDate = null;

	private record DateSeparator(int contentY, LocalDate date) {
	}

	private final @NonNull List<T> entries = new ArrayList<>();
	private int totalContentHeight = 0;

	protected ScrollableGallery(int x, int y, int width, int height, int minThumbWidth, int maxThumbWidth, double aspectRatio) {
		super(x, y, width, height);
		this.minThumbWidth = minThumbWidth;
		this.maxThumbWidth = maxThumbWidth;
		this.aspectRatio = aspectRatio;

		this.searchBar = createSearchBar();
		this.filterButton = createFilterButton();
		this.sortButton = createSortButton();
		this.openFolderButton = createOpenFolderButton();
		addFixedChild(searchBar);
		addFixedChild(filterButton);
		addFixedChild(sortButton);
		addFixedChild(openFolderButton);
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

	private CycleButton<ScreenshotGalleryWidget.FilterMode> createFilterButton() {
		return CycleButton.builder(ScreenshotGalleryWidget.FilterMode::getText, ScreenshotGalleryWidget.FilterMode.ALL)
				.withValues(ScreenshotGalleryWidget.FilterMode.values())
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

	private TexturedCyclingButtonWidget<ScreenshotGalleryWidget.SortMode> createSortButton() {
		TexturedCyclingButtonWidget<ScreenshotGalleryWidget.SortMode> cyclingButtonWidget = new TexturedCyclingButtonWidget<>(
				getX() + SEARCH_BAR_WIDTH + FILTER_BUTTON_WIDTH + PADDING * 3, getY() + PADDING,
				SORT_BUTTON_SIZE, SORT_BUTTON_SIZE,
				Config.getInstance().sortOrder.getValue().ordinal(),
				(btn, sortMode) -> {
					sortEntries(sortMode);
					layoutEntries();
				},
				ScreenshotGalleryWidget.SortMode.values(),
				ScreenshotGalleryWidget.SortMode::getIcon
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

	protected @NonNull SearchBar getSearchBar() {
		return searchBar;
	}

	protected @NonNull TexturedCyclingButtonWidget<SortMode> getSortButton() {
		return sortButton;
	}

	protected @NonNull SpriteIconButton getOpenFolderButton() {
		return openFolderButton;
	}

	protected @NonNull CycleButton<FilterMode> getFilterButton() {
		return filterButton;
	}

	@Override
	protected @NonNull ScrollArea getScrollArea() {
		return new ScrollArea(getX(), getY() + HEADER_HEIGHT, getRight(), getBottom());
	}

	protected void searchAndFilter(@NonNull String query, @NonNull FilterMode mode) {
		String q = query.trim().toLowerCase(Locale.ROOT);

		for (T entry : entries) {
			boolean visible = isEntryVisible(entry, mode, q);

			entry.setVisible(visible);
			entry.setActive(visible);
		}
	}

	protected void sortEntries(@NonNull SortMode mode) {
		entries.sort(switch (mode) {
			case DATE_ASC -> Comparator.comparingLong(e -> e.getTimestampOrLastModified());
			case DATE_DESC -> Comparator.comparingLong(e -> -e.getTimestampOrLastModified());
		});
	}

	protected boolean isEntryVisible(@NonNull T entry, @NonNull FilterMode mode, @NonNull String query) {
		return (mode == FilterMode.ALL || mode == FilterMode.FAVORITES && ScreenshotManager.isFavorite(entry.getPathRelativeToScreenshotDir()))
				&& (query.isEmpty() || entry.getName().toLowerCase(Locale.ROOT).contains(query));
	}

	@Override
	protected int getTotalScrollableHeight() {
		return totalContentHeight;
	}

	protected void setEntries(@NonNull List<T> newEntries) {
		clearScrollableChildren();
		entries.clear();
		for (T entry : newEntries) {
			entries.add(entry);
			addScrollableChild(entry);
		}
		layoutEntries();
	}

	protected void clearEntries() {
		clearScrollableChildren();
		entries.clear();
		totalContentHeight = 0;
	}

	protected void layoutEntries() {
		beforeLayoutEntries();

		int availableWidth = getWidth() - PADDING;
		int columns = Math.max(1, availableWidth / (minThumbWidth + PADDING));
		int thumbWidth = Math.min(maxThumbWidth, availableWidth / columns - PADDING);
		int thumbHeight = (int) (thumbWidth * aspectRatio);
		int entryHeight = thumbHeight + getEntryNameHeight();

		int xCursor = getX() + PADDING;
		int yCursor = PADDING;
		int col = 0;
		boolean laidOutAny = false;

		for (T entry : entries) {
			if (!entry.isVisible()) continue;

			LayoutCursor cursor = beforeLayoutEntry(entry, xCursor, yCursor, col, entryHeight);
			xCursor = cursor.x();
			yCursor = cursor.y();
			col = cursor.column();

			entry.setX(xCursor);
			entry.setY(yCursor);
			entry.setWidth(thumbWidth);
			entry.setHeight(entryHeight);
			laidOutAny = true;

			col++;
			xCursor += thumbWidth + PADDING;

			if (col >= columns) {
				col = 0;
				xCursor = getX() + PADDING;
				yCursor += entryHeight + ROW_SPACING;
			}
		}

		if (!laidOutAny) {
			totalContentHeight = 0;
		} else {
			if (col == 0) yCursor -= entryHeight + ROW_SPACING;
			totalContentHeight = yCursor + entryHeight + PADDING;
		}

		afterLayoutEntries();
		clampScroll();
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
		renderSeparators(graphics);
	}

	protected void renderSeparators(@NonNull GuiGraphicsExtractor graphics) {
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

	protected int getEntryNameHeight() {
		return AbstractGalleryEntryWidget.DEFAULT_NAME_HEIGHT;
	}

	protected void beforeLayoutEntries() {
		separators.clear();
		lastLayoutDate = null;
	}

	protected @NonNull LayoutCursor beforeLayoutEntry(@NonNull T entry, int xCursor, int yCursor, int column, int entryHeight) {
		LocalDate entryDate = Instant.ofEpochMilli(entry.getTimestampOrLastModified()).atZone(ZoneId.systemDefault()).toLocalDate();

		if (!entryDate.equals(lastLayoutDate)) {
			if (column != 0) {
				column = 0;
				xCursor = getX() + PADDING;
				yCursor += entryHeight + ROW_SPACING;
			}

			separators.add(new DateSeparator(yCursor, entryDate));
			yCursor += SEPARATOR_HEIGHT + ROW_SPACING;
		}

		lastLayoutDate = entryDate;

		return new LayoutCursor(xCursor, yCursor, column);
	}

	protected void afterLayoutEntries() {
	}

	@Override
	protected void onBeforeRenderScrollableChild(@NonNull GuiGraphicsExtractor graphics, @NonNull AbstractWidget widget, int screenY, int mouseX, int mouseY, float deltaTicks) {
		if (!(widget instanceof AbstractGalleryEntryWidget entry)) return;

		ScrollArea area = getScrollArea();
		int entryBottom = screenY + entry.getHeight();

		if (entryBottom >= area.top() - PRELOAD_MARGIN && screenY <= area.bottom() + PRELOAD_MARGIN) {
			entry.triggerLoad();
		}
	}

	protected @NonNull List<T> getMutableEntries() {
		return entries;
	}

	public @NonNull List<T> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}

	public record LayoutCursor(int x, int y, int column) {
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
