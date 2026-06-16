package me.Azz_9.screenshot_utilities.client.gui.widget.screenshotGallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.ScrollableGallery;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshotGallery.content.ScreenshotEntryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureCache;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryWidget extends ScrollableGallery<ScreenshotEntryWidget> {

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 9.0 / 16.0;

	// gallery content
	private final @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged;
	private final @Nullable Consumer<Screenshot> onThumbnailClicked;
	// Marge de préchargement en pixels au-delà de la zone visible
	private static final int FULLVIEW_PRELOAD_RADIUS = 2;

	private final AtomicBoolean refreshing = new AtomicBoolean(false);

	public ScreenshotGalleryWidget(int x, int y, int width, int height,
	                               @Nullable Consumer<List<ScreenshotEntryWidget>> onEntriesChanged,
	                               @Nullable Consumer<Screenshot> onThumbnailClicked) {
		super(x, y, width, height, MIN_THUMB_WIDTH, MAX_THUMB_WIDTH, ASPECT_RATIO, Config.getInstance().screenshotSortOrder);
		this.onEntriesChanged = onEntriesChanged;
		this.onThumbnailClicked = onThumbnailClicked;

		ScreenshotList.whenScreenshotsLoaded((screenshots -> {
			// make sure the player didn't leave the screen before building entries
			if (MINECRAFT.screen instanceof ScreenshotGalleryScreen) {
				buildEntries(screenshots);
				searchAndFilter(getSearchBar().getValue(), getFilterButton().getValue());
				sortEntries(getSortButton().getValue());
				layoutEntries();
			}
		}));
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
			graphics.centeredText(MINECRAFT.font, Component.translatable("screenshot_utilities.gallery_widget.no_screenshot").withStyle(ChatFormatting.ITALIC),
					getX() + getWidth() / 2, getY() + getHeight() / 5, Colors.GRAY);
			return;
		}

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
	}

	/* ---------------- Layout ---------------- */

	private void buildEntries(@NonNull List<Screenshot> screenshots) {
		List<ScreenshotEntryWidget> newEntries = new ArrayList<>();
		for (Screenshot screenshot : screenshots) {
			newEntries.add(new ScreenshotEntryWidget(screenshot, onThumbnailClicked));
		}
		setEntries(newEntries);
		if (onEntriesChanged != null) onEntriesChanged.accept(getEntries());
	}

	// refresh

	public void refresh(final boolean clearCache, final @Nullable Runnable onRefreshComplete) {
		if (!refreshing.compareAndSet(false, true)) return;

		ScreenshotLogger.info("Refreshing screenshot gallery entries");
		if (clearCache) ScreenshotTextureCache.clearCache();

		ScreenshotList.reloadAsync();

		ScreenshotList.whenScreenshotsLoaded(screenshots -> {
			buildEntries(screenshots);
			searchAndFilter(getSearchBar().getValue(), getFilterButton().getValue());
			sortEntries(getSortButton().getValue());
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
			ScreenshotEntryWidget entry = getEntries().get(index);
			if (entry.isVisible()) {
				return entry.getScreenshot();
			}
		}
		return null;
	}

	@Nullable
	public Screenshot getNextVisibleScreenshot(@NonNull Screenshot screenshot) {
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


	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}
}
