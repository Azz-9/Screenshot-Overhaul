package me.Azz_9.screenshot_overhaul.client.screenshot.panorama;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.CubeMapTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;

public class PanoramaHolder {

	public static final @NonNull Identifier PANORAMA_LOCATION = Identifier.withDefaultNamespace("textures/gui/title/background/panorama");
	private static final int[] ORDER = new int[]{1, 3, 5, 4, 0, 2};
	private static final @NonNull AtomicReference<Thread> currentTask = new AtomicReference<>(null);
	private static volatile @Nullable Panorama currentPanorama = null;

	public static void resetToDefaultAsync() {
		Thread previous = currentTask.getAndSet(null);
		if (previous != null) previous.interrupt();
		if (currentPanorama == null) return;

		Thread thread = new Thread(() -> {
			try {
				CubeMapTexture texture = new CubeMapTexture(PANORAMA_LOCATION);

				TextureContents contents;
				try {
					contents = texture.loadContents(MINECRAFT.getResourceManager());
				} catch (IOException e) {
					ScreenshotLogger.error("Failed to load vanilla panorama: {}", e.getMessage());
					return;
				}

				if (Thread.interrupted()) {
					contents.close();
					return;
				}

				MINECRAFT.execute(() -> {
					texture.apply(contents);
					MINECRAFT.getTextureManager().register(PANORAMA_LOCATION, texture);
					currentPanorama = null;
					Config.getInstance().selectedPanoramaUUID.setValue(null);
				});

			} finally {
				currentTask.compareAndSet(Thread.currentThread(), null);
			}
		}, "panorama-reset");

		thread.setDaemon(true);
		currentTask.set(thread);
		thread.start();
	}

	public static void usePanoramaAsync(final @NonNull Panorama panorama) {
		// Interrompt le thread précédent s'il tourne encore
		Thread previous = currentTask.getAndSet(null);
		if (previous != null) previous.interrupt();
		if (isSelected(panorama)) return;

		Thread thread = new Thread(() -> {
			try {
				NativeImage[] images = loadImages(panorama);
				if (images == null) return; // erreur de chargement

				// Vérifie qu'on n'a pas été supplanté pendant le chargement
				if (Thread.interrupted()) {
					closeAll(images, ORDER.length);
					return;
				}

				MINECRAFT.execute(() -> {
					// Dernier check sur le main thread : on est toujours le task courant ?
					register(images, PANORAMA_LOCATION);
					currentPanorama = panorama;
					Config.getInstance().selectedPanoramaUUID.setValue(currentPanorama.id());
				});
			} finally {
				// Se retire proprement de la référence (seulement si c'est encore nous)
				currentTask.compareAndSet(Thread.currentThread(), null);
			}
		}, "panorama-loader");

		thread.setDaemon(true);
		currentTask.set(thread);
		thread.start();
	}

	/** Charge les images depuis le disque. Retourne null en cas d'erreur ou d'interruption. */
	public static @Nullable NativeImage[] loadImages(final @NonNull Panorama panorama) {
		NativeImage[] images = new NativeImage[ORDER.length];
		int loaded = 0;
		try {
			for (int i = 0; i < ORDER.length; i++) {
				if (Thread.interrupted()) {
					closeAll(images, loaded);
					return null;
				}

				Screenshot face = panorama.faces()[ORDER[i]];
				if (face == null) {
					ScreenshotLogger.error("Panorama '{}' is missing face {}", panorama.folderName(), ORDER[i]);
					closeAll(images, loaded);
					return null;
				}

				try (InputStream in = Files.newInputStream(face.file().toPath())) {
					images[i] = flipVertical(NativeImage.read(in));
					loaded++;
				} catch (IOException e) {
					ScreenshotLogger.error("Failed to read panorama face {}: {}", ORDER[i], e.getMessage());
					closeAll(images, loaded);
					return null;
				}
			}
			return images;
		} catch (Exception e) {
			ScreenshotLogger.error("Unexpected error loading panorama: {}", e.getMessage());
			closeAll(images, loaded);
			return null;
		}
	}

	/** Libère les NativeImage déjà allouées en cas d'abandon. */
	private static void closeAll(final NativeImage[] images, int count) {
		for (int i = 0; i < count; i++) {
			if (images[i] != null) images[i].close();
		}
	}

	private static @NonNull NativeImage flipVertical(@NonNull NativeImage src) {
		int width = src.getWidth();
		int height = src.getHeight();

		NativeImage flipped = new NativeImage(width, height, false);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				flipped.setPixel(x, height - 1 - y, src.getPixel(x, y));
			}
		}

		src.close();
		return flipped;
	}

	public static boolean isSelected(final @Nullable Panorama panorama) {
		return Objects.equals(currentPanorama, panorama);
	}

	public static void register(@NonNull NativeImage[] images, @NonNull Identifier location) {
		DynamicCubeMapTexture texture = new DynamicCubeMapTexture();
		texture.upload(images);
		MINECRAFT.getTextureManager().register(
				location,
				texture
		);
	}

	public static void register(Panorama panorama, Identifier location) {
		NativeImage[] images = loadImages(panorama);
		if (images != null) register(images, location);
	}
}
