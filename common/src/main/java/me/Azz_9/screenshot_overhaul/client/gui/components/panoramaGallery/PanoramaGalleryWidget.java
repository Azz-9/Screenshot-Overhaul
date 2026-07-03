package me.Azz_9.screenshot_overhaul.client.gui.components.panoramaGallery;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.cache.PanoramaTextureCache;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.gui.Loading;
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.ScrollableGallery;
import me.Azz_9.screenshot_overhaul.client.gui.components.panoramaGallery.content.PanoramaEntryWidget;
import me.Azz_9.screenshot_overhaul.client.gui.screen.PanoramaGalleryScreen;
import me.Azz_9.screenshot_overhaul.client.panorama.Panorama;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotList;

public class PanoramaGalleryWidget extends ScrollableGallery<PanoramaEntryWidget> {

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 170;
	public static final int MAX_THUMB_WIDTH = 280;
	private static final double ASPECT_RATIO = 1;

	// reset to default button
	private static final int RESET_BUTTON_HEIGHT = SEARCH_BAR_HEIGHT;
	private static final int RESET_BUTTON_WIDTH = 120;

	private final @Nullable Consumer<List<PanoramaEntryWidget>> onEntriesChanged;
	private final @Nullable Consumer<Panorama> onThumbnailClicked;
	private final @Nullable Runnable onResetToDefault;

	private final @NonNull AtomicBoolean refreshing = new AtomicBoolean(false);

	public PanoramaGalleryWidget(int x, int y, int width, int height,
	                             @Nullable Consumer<List<PanoramaEntryWidget>> onEntriesChanged,
								 @Nullable Consumer<Panorama> onThumbnailClicked,
								 @Nullable Runnable onResetToDefault) {
		super(x, y, width, height, MIN_THUMB_WIDTH, MAX_THUMB_WIDTH, ASPECT_RATIO, Config.getInstance().panoramaSortOrder);
		this.onEntriesChanged = onEntriesChanged;
		this.onThumbnailClicked = onThumbnailClicked;
		this.onResetToDefault = onResetToDefault;

		if (onResetToDefault != null) {
			addFixedChild(createResetButton());
		}

		ScreenshotList.whenPanoramasLoaded(panoramas -> {
			// make sure the player didn't leave the screen before building entries
			if (MINECRAFT.gui.screen() instanceof PanoramaGalleryScreen) {
				buildEntries(panoramas.stream().filter(Panorama::isComplete).toList());
				searchAndFilter(getSearchBar().getValue(), getFilterButton().getValue());
				sortEntries(getSortButton().getValue());
				layoutEntries();
			}
		});
	}

	private @NonNull Button createResetButton() {
		return Button.builder(Component.translatable("screenshot_overhaul.panorama_gallery.reset"), _ -> {
					if (onResetToDefault != null) onResetToDefault.run();
				})
				.bounds(
						getRight() - OPEN_FOLDER_BUTTON_SIZE - PADDING * 2 - RESET_BUTTON_WIDTH, getY() + PADDING,
						RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT
				)
				.build();
	}

	private void buildEntries(@NonNull List<Panorama> panoramas) {
		List<PanoramaEntryWidget> newEntries = new ArrayList<>();
		for (Panorama panorama : panoramas) {
			newEntries.add(new PanoramaEntryWidget(panorama, onThumbnailClicked));
		}
		setEntries(newEntries);
		if (onEntriesChanged != null) onEntriesChanged.accept(getEntries());
	}

	// refresh

	public void refresh(final boolean clearCache, final @Nullable Runnable onRefreshComplete) {
		if (!refreshing.compareAndSet(false, true)) return;

		ScreenshotLogger.info("Refreshing panorama gallery entries");

		ScreenshotList.reloadAsync();

		ScreenshotList.whenPanoramasLoaded(panoramas -> {
			if (clearCache) PanoramaTextureCache.clearCache();

			buildEntries(panoramas);
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
			graphics.centeredText(MINECRAFT.font, Component.translatable("screenshot_overhaul.gallery_widget.no_panorama").withStyle(ChatFormatting.ITALIC),
					getX() + getWidth() / 2, getY() + getHeight() / 5, Colors.GRAY);
			return;
		}

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
	}
}
