package me.Azz_9.screenshot_overhaul.client.screenshot;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.metadata.Metadata;
import me.Azz_9.screenshot_overhaul.client.metadata.MetadataUtils;
import me.Azz_9.screenshot_overhaul.client.preview.ScreenshotPreview;
import me.Azz_9.screenshot_overhaul.mixin.NativeImageAccessor;

public class ScreenshotGrabber {

	public static volatile boolean capturingPanorama = false;
	private static final AtomicInteger pendingPanoramaFaces = new AtomicInteger(0);

	private static void grab(@NonNull File picDir, final @Nullable String forceName, final @NonNull RenderTarget target, final int downscaleFactor, final @NonNull Consumer<Component> callback) {
		Screenshot.takeScreenshot(target, downscaleFactor, (image) -> {
			picDir.mkdir();
			File file;
			if (forceName == null) {
				file = getFile(picDir.toPath());
			} else {
				file = new File(picDir, forceName);
			}

			Util.ioPool().execute(() -> {
				try {
					try {
						// adding metadata
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						try (WritableByteChannel channel = Channels.newChannel(baos)) {
							((NativeImageAccessor) (Object) image).invokeWriteToChannel(channel);
						}

						Metadata metadata = MetadataUtils.collect();
						byte[] pngWithMeta = MetadataUtils.injectIntoBytes(baos.toByteArray(), metadata);

						Files.createDirectories(file.toPath().getParent());
						Files.write(file.toPath(), pngWithMeta);

						// preview
						if (Config.getInstance().showPreview.getValue())
							ScreenshotPreview.setScreenshot(file);

						// chat message
						if (Config.getInstance().showChatMessage.getValue()) {
							Component fileName = Component.literal(file.getName())
									.withStyle(ChatFormatting.UNDERLINE)
									.withStyle((s) -> s.withClickEvent(new ClickEvent.OpenFile(file.getAbsoluteFile())));
							callback.accept(Component.translatable("screenshot.success", fileName));
						}
					} catch (Throwable throwable) {
						try {
							image.close();
						} catch (Throwable x2) {
							throwable.addSuppressed(x2);
						}

						throw throwable;
					}

					image.close();
				} catch (Exception e) {
					ScreenshotLogger.warn("Couldn't save screenshot {}", e.getMessage());
					if (Config.getInstance().showChatMessage.getValue())
						callback.accept(Component.translatable("screenshot.failure", e.getMessage()));
				}
			});
		});
	}

	public static void requestGrab(@NonNull File picDir, final @Nullable String forceName, final @NonNull RenderTarget target, final int downscaleFactor, final @NonNull Consumer<Component> callback) {
		if (!FutureScreenshotState.shouldRequestScreenshot()) {
			grab(picDir, forceName, target, downscaleFactor, callback);
			return;
		}

		FutureScreenshotState.suppressHud = Config.getInstance().hideHudOnScreenshot.getValue();
		FutureScreenshotState.suppressChat = !Config.getInstance().hideHudOnScreenshot.getValue() && Config.getInstance().hideChatOnScreenshot.getValue();
		FutureScreenshotState.suppressHand = Config.getInstance().hideHandOnScreenshot.getValue();
		FutureScreenshotState.captureRequested = true;
		FutureScreenshotState.pendingCapture = () -> grab(picDir, forceName, target, downscaleFactor, callback);
	}

	public static @NonNull Component grabPanoramixScreenshot(final @NonNull File folder) {
		if (MINECRAFT.player == null)
			throw new IllegalStateException("Minecraft player is null!");

		File panoramaFolder = getPanoramaFolder(folder);

		int downscaleFactor = 4;
		int width = 4096;
		int height = 4096;
		int ow = MINECRAFT.getWindow().getWidth();
		int oh = MINECRAFT.getWindow().getHeight();
		RenderTarget target = MINECRAFT.gameRenderer.mainRenderTarget();
		float xRot = MINECRAFT.player.getXRot();
		float yRot = MINECRAFT.player.getYRot();
		float xRotO = MINECRAFT.player.xRotO;
		float yRotO = MINECRAFT.player.yRotO;
		MINECRAFT.gameRenderer.setRenderBlockOutline(false);
		Camera camera = MINECRAFT.gameRenderer.mainCamera();

		String panoramaId = UUID.randomUUID().toString();

		MutableComponent text;
		try {
			capturingPanorama = true;
			pendingPanoramaFaces.set(6);

			camera.enablePanoramicMode();
			MINECRAFT.getWindow().setWidth(width);
			MINECRAFT.getWindow().setHeight(height);
			target.resize(width, height);

			for (int i = 0; i < 6; ++i) {
				switch (i) {
					case 0:
						MINECRAFT.player.setYRot(yRot);
						MINECRAFT.player.setXRot(0.0F);
						break;
					case 1:
						MINECRAFT.player.setYRot((yRot + 90.0F) % 360.0F);
						MINECRAFT.player.setXRot(0.0F);
						break;
					case 2:
						MINECRAFT.player.setYRot((yRot + 180.0F) % 360.0F);
						MINECRAFT.player.setXRot(0.0F);
						break;
					case 3:
						MINECRAFT.player.setYRot((yRot - 90.0F) % 360.0F);
						MINECRAFT.player.setXRot(0.0F);
						break;
					case 4:
						MINECRAFT.player.setYRot(yRot);
						MINECRAFT.player.setXRot(-90.0F);
						break;
					case 5:
					default:
						MINECRAFT.player.setYRot(yRot);
						MINECRAFT.player.setXRot(90.0F);
				}

				MINECRAFT.player.yRotO = MINECRAFT.player.getYRot();
				MINECRAFT.player.xRotO = MINECRAFT.player.getXRot();
				MINECRAFT.gameRenderer.update(DeltaTracker.ONE);
				MINECRAFT.gameRenderer.extract(DeltaTracker.ONE, true);
				MINECRAFT.gameRenderer.renderLevel(DeltaTracker.ONE);

				try {
					Thread.sleep(10L);
				} catch (InterruptedException _) {
				}

				grabPanoramaFace(panoramaFolder, "panorama_" + i + ".png", target, downscaleFactor, panoramaId, i);
			}

			Component name = Component.literal(panoramaFolder.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((s) -> s.withClickEvent(new ClickEvent.OpenFile(panoramaFolder.getAbsoluteFile())));
			text = Component.translatable("screenshot.success", name);
			return text;
		} catch (Exception e) {
			ScreenshotLogger.error("Couldn't save image", e);
			text = Component.translatable("screenshot.failure", e.getMessage());
		} finally {

			MINECRAFT.player.setXRot(xRot);
			MINECRAFT.player.setYRot(yRot);
			MINECRAFT.player.xRotO = xRotO;
			MINECRAFT.player.yRotO = yRotO;
			MINECRAFT.gameRenderer.setRenderBlockOutline(true);
			MINECRAFT.getWindow().setWidth(ow);
			MINECRAFT.getWindow().setHeight(oh);
			target.resize(ow, oh);
			camera.disablePanoramicMode();
		}

		return text;
	}

	private static void grabPanoramaFace(@NonNull File picDir, @NonNull String name, @NonNull RenderTarget target, int downscaleFactor, @NonNull String panoramaId, int faceIndex) {
		Screenshot.takeScreenshot(target, downscaleFactor, (image) -> {
			picDir.mkdir();
			File file = new File(picDir, name);

			Util.ioPool().execute(() -> {
				try {
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						try (WritableByteChannel channel = Channels.newChannel(baos)) {
							((NativeImageAccessor) (Object) image).invokeWriteToChannel(channel);
						}

						byte[] pngWithMeta = MetadataUtils.injectIntoBytes(
								baos.toByteArray(),
								MetadataUtils.collect(panoramaId, faceIndex)
						);

						Files.createDirectories(file.toPath().getParent());
						Files.write(file.toPath(), pngWithMeta);

						if (Config.getInstance().showPreview.getValue())
							ScreenshotPreview.setScreenshot(file);

						// Hide every panorama screenshot by default
						ScreenshotManager.setHiddenFromMap(
								Config.getInstance().getAbsoluteScreenshotsDir().relativize(file.toPath()).toString(),
								true
						);
					} catch (Throwable t) {
						try {
							image.close();
						} catch (Throwable x) {
							t.addSuppressed(x);
						}
						throw t;
					}
					image.close();
				} catch (Exception e) {
					ScreenshotLogger.warn("Couldn't save panorama face {}: {}", faceIndex, e.getMessage());
				} finally {
					if (pendingPanoramaFaces.decrementAndGet() == 0) {
						capturingPanorama = false;
						ScreenshotManager.save();
					}
				}
			});
		});
	}

	private static @NonNull File getFile(final @NonNull Path folder) {
		String pattern = Config.getInstance().screenshotsFileName.getValue();
		return ScreenshotFileNameParser.resolve(folder, pattern).toFile();
	}

	private static @NonNull File getPanoramaFolder(final @NonNull File folder) {
		String name = "panorama_" + Util.getFilenameFormattedDateTime();
		int count = 1;

		while (true) {
			File file = new File(folder, name + (count == 1 ? "" : "_" + count));
			if (!file.exists()) {
				return file;
			}

			count++;
		}
	}
}
