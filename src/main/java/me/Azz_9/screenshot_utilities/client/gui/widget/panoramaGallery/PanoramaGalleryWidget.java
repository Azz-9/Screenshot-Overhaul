package me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.screen.PanoramaGalleryScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.AbstractGalleryEntryWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.ScrollableGallery;
import me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content.PanoramaEntryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.Panorama;

public class PanoramaGalleryWidget extends ScrollableGallery<AbstractGalleryEntryWidget> {

	// thumbnail
	public static final int MIN_THUMB_WIDTH = 140;
	public static final int MAX_THUMB_WIDTH = 260;
	private static final double ASPECT_RATIO = 1;

	private final @Nullable Consumer<Panorama> onThumbnailClicked;
	private final @Nullable Runnable onDefaultClicked;

	public PanoramaGalleryWidget(int x, int y, int width, int height, @Nullable Consumer<Panorama> onThumbnailClicked, @Nullable Runnable onDefaultClick) {
		super(x, y, width, height, MIN_THUMB_WIDTH, MAX_THUMB_WIDTH, ASPECT_RATIO);
		this.onThumbnailClicked = onThumbnailClicked;
		this.onDefaultClicked = onDefaultClick;

		ScreenshotList.whenPanoramasLoaded((panoramas -> {
			// make sure the player didn't leave the screen before building entries
			if (MINECRAFT.screen instanceof PanoramaGalleryScreen) {
				buildEntries(panoramas.stream().filter(Panorama::isComplete).toList());
				searchAndFilter(getSearchBar().getValue(), getFilterButton().getValue());
				sortEntries(getSortButton().getValue());
				layoutEntries();
			}
		}));
	}

	private void buildEntries(@NonNull List<Panorama> panoramas) {
		List<AbstractGalleryEntryWidget> newEntries = new ArrayList<>();
		//newEntries.add(new DefaultPanoramaEntryWidget(onDefaultClicked));
		for (Panorama panorama : panoramas) {
			newEntries.add(new PanoramaEntryWidget(panorama, onThumbnailClicked));
		}
		setEntries(newEntries);
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
			graphics.centeredText(MINECRAFT.font, Component.translatable("screenshot_utilities.gallery_widget.no_panorama").withStyle(ChatFormatting.ITALIC),
					getX() + getWidth() / 2, getY() + getHeight() / 5, Colors.GRAY);
			return;
		}

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
	}
}
