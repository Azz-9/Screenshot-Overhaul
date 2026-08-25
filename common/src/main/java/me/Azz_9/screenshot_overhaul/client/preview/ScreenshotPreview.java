package me.Azz_9.screenshot_overhaul.client.preview;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Ease;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.cache.ScreenshotTextureCache;
import me.Azz_9.screenshot_overhaul.client.gui.ScreenshotDrawHelper;
import me.Azz_9.screenshot_overhaul.client.panorama.Panorama;
import me.Azz_9.screenshot_overhaul.client.screenshot.CopyScreenshot;
import me.Azz_9.screenshot_overhaul.client.screenshot.DeleteScreenshot;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_overhaul.client.texture.ScreenshotTexture;

public class ScreenshotPreview {

	private static final int SLIDE_DURATION = 400;
	private static final int HOLD_DURATION = 5000;
	public static final int PREVIEW_WIDTH = 150;
	private static final int MARGIN = 19;
	public static final int BAR_HEIGHT = 3;
	private static final int SHADOW_OFFSET = 3;
	private static final int HOVER_FADE_DURATION = 200;

	private static @Nullable ScreenshotTexture screenshotTexture;
	private static @Nullable File screenshotFile;
	private static @Nullable String panoramaUuid;
	private static long startTime;
	private static long pausedElapsed = -1;

	private static long hoverStartTime = -1;
	private static long hoverEndTime = -1;
	private static float lastHoverProgress = 0f;

	private static @Nullable Runnable onScreenshotSet;

	public static void setOnScreenshotSet(@Nullable Runnable listener) {
		onScreenshotSet = listener;
	}

	// pause / resume

	public static void pause() {
		if (pausedElapsed == -1 && getElapsed() >= SLIDE_DURATION) {
			pausedElapsed = getElapsed();
		}
	}

	public static void resume() {
		if (pausedElapsed != -1) {
			startTime = System.currentTimeMillis() - pausedElapsed;
			pausedElapsed = -1;
		}
	}

	public static void dismiss() {
		// On force le début de la phase slide out en ajustant startTime
		startTime = System.currentTimeMillis() - SLIDE_DURATION - HOLD_DURATION;
		pausedElapsed = -1;
	}

	public static long getElapsed() {
		if (pausedElapsed != -1) return pausedElapsed;
		return System.currentTimeMillis() - startTime;
	}

	// hover progress

	public static void setHovered(boolean hovered) {
		if (hovered && hoverStartTime == -1) {
			// Démarre le fade in depuis la progression actuelle
			hoverStartTime = System.currentTimeMillis();
			hoverEndTime = -1;
		} else if (!hovered && hoverStartTime != -1) {
			// Démarre le fade out depuis la progression actuelle
			hoverEndTime = System.currentTimeMillis();
			hoverStartTime = -1;
		}
	}

	public static float getHoverProgress() {
		long now = System.currentTimeMillis();

		if (hoverStartTime != -1) {
			// Fade in
			float t = Math.min(1f, (now - hoverStartTime) / (float) HOVER_FADE_DURATION);
			lastHoverProgress = lastHoverProgress + (1f - lastHoverProgress) * Ease.outQuad(t);
			if (t >= 1f) lastHoverProgress = 1f;
		} else if (hoverEndTime != -1) {
			// Fade out
			float t = Math.min(1f, (now - hoverEndTime) / (float) HOVER_FADE_DURATION);
			lastHoverProgress = lastHoverProgress * (1f - Ease.inQuad(t));
			if (t >= 1f) {
				lastHoverProgress = 0f;
				hoverEndTime = -1;
			}
		}

		return lastHoverProgress;
	}

	// render

	public static void render(@NonNull GuiGraphicsExtractor graphics, double mouseX, double mouseY, float delta) {
		if (screenshotTexture == null || !screenshotTexture.isReady()) return;

		long elapsed = getElapsed();
		long totalDuration = SLIDE_DURATION + HOLD_DURATION + SLIDE_DURATION;

		if (elapsed > totalDuration) {
			if (pausedElapsed != -1) return;
			screenshotTexture = null;
			return;
		}

		int screenW = MINECRAFT.getWindow().getGuiScaledWidth();
		int screenH = MINECRAFT.getWindow().getGuiScaledHeight();

		int previewH = getPreviewHeight();

		float slideOffset = computeSlideOffset(elapsed);

		int x = (int) (getX(screenW) + slideOffset);
		int y = getY(screenH);

		// Shadow
		graphics.fill(
				x + SHADOW_OFFSET, y + SHADOW_OFFSET,
				x + PREVIEW_WIDTH + SHADOW_OFFSET, y + previewH + SHADOW_OFFSET,
				Colors.BLACK_SEMI_TRANSPARENT
		);

		// Image
		ScreenshotDrawHelper.drawCover(graphics, screenshotTexture, x, y, PREVIEW_WIDTH, previewH);

		// Barre de progression (temps restant)
		float holdProgress;
		if (elapsed < SLIDE_DURATION) {
			holdProgress = 1f;
		} else if (elapsed < SLIDE_DURATION + HOLD_DURATION) {
			holdProgress = 1f - (elapsed - SLIDE_DURATION) / (float) HOLD_DURATION;
		} else {
			holdProgress = 0f;
		}

		int barY = y + previewH - BAR_HEIGHT;
		// Barre de progression
		graphics.fill(x, barY, x + (int) (PREVIEW_WIDTH * holdProgress), barY + BAR_HEIGHT, Colors.WHITE);
	}

	public static void setScreenshot(@NonNull File file) {
		screenshotFile = file;
		panoramaUuid = null;

		MINECRAFT.execute(() -> {
			screenshotTexture = ScreenshotTextureCache.getThumbnail(file.toPath());
			screenshotTexture.whenReady((_, _) -> {
				startTime = System.currentTimeMillis();

				pausedElapsed = -1;
				if (onScreenshotSet != null) {
					onScreenshotSet.run();
				}
			});
		});
	}

	public static void setPanorama(@NonNull File file, @NonNull String uuid) {
		setScreenshot(file);
		panoramaUuid = uuid;
	}

	private static float computeSlideOffset(long elapsed) {
		if (elapsed < SLIDE_DURATION) {
			float t = elapsed / (float) SLIDE_DURATION;
			return (PREVIEW_WIDTH + MARGIN) * (1f - Ease.outQuad(t));
		} else if (elapsed < SLIDE_DURATION + HOLD_DURATION || pausedElapsed != -1) {
			// Figé en position visible si on est dans le hold OU en pause
			return 0f;
		} else {
			float t = (elapsed - SLIDE_DURATION - HOLD_DURATION) / (float) SLIDE_DURATION;
			return (PREVIEW_WIDTH + MARGIN) * Ease.inQuad(Math.min(t, 1f));
		}
	}

	public static int getX(int screenW) {
		// Même calcul que dans render()
		long elapsed = System.currentTimeMillis() - startTime;
		float slideOffset = computeSlideOffset(elapsed);
		return (int) (screenW - PREVIEW_WIDTH - MARGIN + slideOffset);
	}

	public static int getY(int screenH) {
		int previewH = getPreviewHeight();
		return screenH - previewH - MARGIN;
	}

	public static int getPreviewHeight() {
		if (screenshotTexture == null) return 0;
		return Math.min(
				Math.round(PREVIEW_WIDTH * screenshotTexture.height() / (float) screenshotTexture.width()),
				MINECRAFT.getWindow().getGuiScaledHeight() - MARGIN * 2
		);
	}

	public static boolean isVisible() {
		return screenshotTexture != null;
	}

	public static boolean isSlidingOut() {
		return getElapsed() >= SLIDE_DURATION + HOLD_DURATION;
	}

	public static void copyCurrentScreenshot() {
		if (screenshotFile != null) {
			CopyScreenshot.copyToClipboardWithToastError(screenshotFile, null, null);
		}
	}

	public static void deleteCurrentScreenshot() {
		if (screenshotFile != null) {
			if (panoramaUuid != null) {
				List<Panorama> panoramas = ScreenshotList.getPanoramas();
				for (int i = panoramas.size() - 1; i >= 0; i--) {
					if (panoramas.get(i).uuid().toString().equals(panoramaUuid)) {
						Panorama panorama = panoramas.get(i);
						DeleteScreenshot.deletePanorama(panorama);
						break;
					}
				}
			} else {
				DeleteScreenshot.delete(screenshotFile);
			}
		}
	}
}