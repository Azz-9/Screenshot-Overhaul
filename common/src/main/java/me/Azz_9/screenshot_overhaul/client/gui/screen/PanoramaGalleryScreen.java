package me.Azz_9.screenshot_overhaul.client.gui.screen;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import me.Azz_9.screenshot_overhaul.client.config.ConfigLoader;
import me.Azz_9.screenshot_overhaul.client.gui.components.panoramaGallery.PanoramaGalleryWidget;
import me.Azz_9.screenshot_overhaul.client.gui.components.toasts.CustomToastId;
import me.Azz_9.screenshot_overhaul.client.panorama.Panorama;
import me.Azz_9.screenshot_overhaul.client.panorama.PanoramaHolder;
import me.Azz_9.screenshot_overhaul.client.screenshot.DeleteScreenshot;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotManager;

public class PanoramaGalleryScreen extends AbstractSavableScreen {

	private static final int GLOBAL_PADDING = 10;

	private @Nullable PanoramaGalleryWidget gallery;

	public PanoramaGalleryScreen(@NonNull Component title, @Nullable Screen parent) {
		super(title, parent);
	}

	@Override
	protected void initContent() {
		gallery = createGallery();

		addRenderableWidget(gallery);

		ScreenshotList.setOnChangeListener(_ -> {
			if (gallery != null) gallery.refresh(false);
		});
	}

	private @NonNull PanoramaGalleryWidget createGallery() {
		return new PanoramaGalleryWidget(
				GLOBAL_PADDING, GLOBAL_PADDING,
				width - GLOBAL_PADDING * 2, getBottomBarTop() - GLOBAL_PADDING,
				this::setTrackedItems, PanoramaHolder::usePanoramaAsync, this::onDeleteRequested,
				PanoramaHolder::resetToDefaultAsync
		);
	}

	private void onDeleteRequested(@NonNull Panorama panorama) {
		if (gallery == null) return;

		final double savedScroll = gallery.getScrollOffset();
		gallery.removeEntry(panorama);
		gallery.setScrollOffset(savedScroll);

		CompletableFuture.runAsync(() -> {
			boolean success = DeleteScreenshot.deletePanorama(panorama);
			if (!success) {
				MINECRAFT.execute(() -> {
					// Rollback : refresh remet le panorama dans la liste
					if (gallery != null) gallery.refresh(false, null);
					// Toaster
					SystemToast.add(
							MINECRAFT.getToastManager(),
							CustomToastId.SCREENSHOT_DELETE_FAILED,
							Component.translatable("screenshot_overhaul.delete_failed.title"),
							Component.translatable("screenshot_overhaul.delete_failed.message", panorama.folderName())
					);
				});
			}
			// Si succès : le WatchService va déclencher un refresh,
			// mais removeEntry a déjà retiré le screenshot — buildEntries
			// reconstruira la liste sans lui, aucun effet visible
		});
	}

	// input

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (event.key() == InputConstants.KEY_F5 && gallery != null) {
			gallery.refresh(true);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		ScreenshotManager.save();
		super.onClose();
		ConfigLoader.trySave();
	}
}
