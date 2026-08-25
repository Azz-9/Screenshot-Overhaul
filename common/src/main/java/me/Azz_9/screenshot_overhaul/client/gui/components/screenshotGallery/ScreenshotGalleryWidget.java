package me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.SETTINGS_SPRITE;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.cache.ScreenshotTextureCache;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.gui.Loading;
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.ScrollableGallery;
import me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.content.ScreenshotEntryWidget;
import me.Azz_9.screenshot_overhaul.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotList;

public class ScreenshotGalleryWidget extends ScrollableGallery<ScreenshotEntryWidget> {

	// settings
	private static final int SETTINGS_BUTTON_SIZE = 20;

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 9.0 / 16.0;

	// gallery content
	private final @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged;
	private final @Nullable Consumer<Screenshot> onThumbnailClicked;
	private final @NonNull Consumer<Screenshot> onDeleteRequested;
	// Marge de préchargement en pixels au-delà de la zone visible
	private static final int FULLVIEW_PRELOAD_RADIUS = 2;

	private final @NonNull AtomicBoolean refreshing = new AtomicBoolean(false);

	public ScreenshotGalleryWidget(int x, int y, int width, int height,
	                               @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged,
								   @Nullable Consumer<Screenshot> onThumbnailClicked,
								   @NonNull Consumer<Screenshot> onDeleteRequested) {
		super(x, y, width, height, MIN_THUMB_WIDTH, MAX_THUMB_WIDTH, ASPECT_RATIO, Config.getInstance().screenshotSortOrder);
		this.onEntriesChanged = onEntriesChanged;
		this.onThumbnailClicked = onThumbnailClicked;
		this.onDeleteRequested = onDeleteRequested;

		addFixedChild(createSettingsButton());

		ScreenshotList.whenScreenshotsLoaded((screenshots -> {
			// make sure the player didn't leave the screen before building entries
			if (MINECRAFT.gui.screen() instanceof ScreenshotGalleryScreen) {
				buildEntries(screenshots);
				searchAndFilter(getSearchBar().getValue(), getFilterButton().getValue());
				sortEntries(getSortButton().getValue());
				layoutEntries();
			}
		}));
	}

	@Override
	protected @NonNull SpriteIconButton createOpenFolderButton() {
		SpriteIconButton openFolderButton = super.createOpenFolderButton();
		openFolderButton.setPosition(
				getRight() - SETTINGS_BUTTON_SIZE - OPEN_FOLDER_BUTTON_SIZE - PADDING * 2,
				getY() + PADDING
		);
		return openFolderButton;
	}

	private @NonNull Button createSettingsButton() {
		SpriteIconButton button = SpriteIconButton.TextAndIcon.builder(
						Component.translatable("screenshot_overhaul.settings"),
						(_) -> MINECRAFT.gui.setScreen(Config.getInstance().getSettingsScreen(MINECRAFT.gui.screen())),
						true
				)
				.withTootip()
				.size(SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE)
				.sprite(SETTINGS_SPRITE, 15, 15)
				.build();
		button.setPosition(getRight() - SETTINGS_BUTTON_SIZE - PADDING, getY() + PADDING);
		return button;
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
		} else if (getEntries().isEmpty()) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
			graphics.centeredText(MINECRAFT.font, Component.translatable("screenshot_overhaul.gallery_widget.no_screenshot").withStyle(ChatFormatting.ITALIC),
					getX() + getWidth() / 2, getY() + getHeight() / 5, Colors.GRAY);
			return;
		}

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
	}

	/* ---------------- Layout ---------------- */

	private void buildEntries(@NonNull List<Screenshot> screenshots) {
		List<ScreenshotEntryWidget> newEntries = new ArrayList<>();
		for (Screenshot screenshot : screenshots) {
			newEntries.add(new ScreenshotEntryWidget(screenshot, onThumbnailClicked, onDeleteRequested));
		}
		setEntries(newEntries);
		if (onEntriesChanged != null) onEntriesChanged.accept(getEntries());
	}

	// refresh

	public void refresh(final boolean clearCache, final @Nullable Runnable onRefreshComplete) {
		if (!refreshing.compareAndSet(false, true)) return;

		final double savedScroll = getScrollOffset();

		ScreenshotLogger.info("Refreshing screenshot gallery entries");

		ScreenshotList.reloadAsync();
		ScreenshotList.whenScreenshotsLoaded(screenshots -> {
			if (clearCache) ScreenshotTextureCache.clearCache();

			buildEntries(screenshots);
			searchAndFilter(getSearchBar().getValue(), getFilterButton().getValue());
			sortEntries(getSortButton().getValue());
			layoutEntries();

			setScrollOffset(savedScroll);

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

		List<ScreenshotEntryWidget> entries = getEntries();
		for (int i = index - FULLVIEW_PRELOAD_RADIUS; i <= index + FULLVIEW_PRELOAD_RADIUS; i++) {
			if (i >= 0 && i < entries.size()) {
				entries.get(i).triggerLoad();
			}
		}
	}

	public int indexOf(@NonNull Screenshot screenshot) {
		List<ScreenshotEntryWidget> entries = getEntries();
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getScreenshot().file().equals(screenshot.file())) {
				return i;
			}
		}
		return -1;
	}

	public @Nullable Screenshot getPreviousVisibleScreenshot(@NonNull Screenshot screenshot) {
		int index = indexOf(screenshot);
		while (--index >= 0) {
			ScreenshotEntryWidget entry = getEntries().get(index);
			if (entry.isVisible()) {
				return entry.getScreenshot();
			}
		}
		return null;
	}

	public @Nullable Screenshot getNextVisibleScreenshot(@NonNull Screenshot screenshot) {
		List<ScreenshotEntryWidget> entries = getEntries();
		int index = indexOf(screenshot);
		while (++index < entries.size()) {
			ScreenshotEntryWidget entry = entries.get(index);
			if (entry.isVisible()) {
				return entry.getScreenshot();
			}
		}
		return null;
	}

	public void removeEntry(@NonNull Screenshot screenshot) {
		int index = indexOf(screenshot);
		if (index < 0) return;

		List<ScreenshotEntryWidget> entries = new ArrayList<>(getEntries());
		entries.remove(index);
		setEntries(entries);
		layoutEntries();

		if (onEntriesChanged != null) onEntriesChanged.accept(getEntries());
	}
}
