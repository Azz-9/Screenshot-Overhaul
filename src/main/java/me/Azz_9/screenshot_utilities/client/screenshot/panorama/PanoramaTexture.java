package me.Azz_9.screenshot_utilities.client.screenshot.panorama;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;
import static me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaHolder.loadImages;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import me.Azz_9.screenshot_utilities.client.screenshot.ImageScaler;

public class PanoramaTexture implements AutoCloseable {
	private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

	private final @NonNull CompletableFuture<PanoramaCubeMap> cubeMapFuture;

	private volatile boolean closed = false;

	public PanoramaTexture(Panorama panorama, int size) {
		this.cubeMapFuture = CompletableFuture.supplyAsync(
				() -> loadData(panorama, size),
				Util.backgroundExecutor()
		).thenApplyAsync(
				data -> {
					if (data == null) return null;
					return createCubeMap(data);
				},
				MINECRAFT
		);
	}

	public static PanoramaTexture loadMaxResolution(Panorama panorama) {
		return new PanoramaTexture(panorama, 4096);
	}

	public static PanoramaTexture loadThumbnail(Panorama panorama) {
		return new PanoramaTexture(panorama, 1024);
	}

	private static LoadedPanoramaData loadData(Panorama panorama, int size) {
		NativeImage[] images = loadImages(panorama);
		if (images == null) return null;

		List<CompletableFuture<NativeImage>> futures = Arrays.stream(images)
				.map(nativeImage -> CompletableFuture.supplyAsync(
						() -> ImageScaler.downscale(nativeImage, size),
						Util.backgroundExecutor()
				))
				.toList();

		NativeImage[] downscaledImages = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream()
						.map(CompletableFuture::join)
						.toArray(NativeImage[]::new)
				)
				.join();

		return new LoadedPanoramaData(panorama, downscaledImages);
	}

	private static PanoramaCubeMap createCubeMap(LoadedPanoramaData data) {
		PanoramaCubeMap cubeMap = new PanoramaCubeMap(
				Identifier.fromNamespaceAndPath(MOD_ID,
						"dynamic/panorama_widget_" + ID_COUNTER.getAndIncrement())
		);

		cubeMap.uploadTexture(data.images());

		for (NativeImage image : data.images()) {
			image.close();
		}

		return cubeMap;
	}

	public boolean isReady() {
		return cubeMapFuture.isDone();
	}

	public PanoramaCubeMap getCubeMap() {
		if (closed || !cubeMapFuture.isDone()) {
			return null;
		}

		return cubeMapFuture.join();
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;

		cubeMapFuture.cancel(true);

		if (cubeMapFuture.isDone()) {
			try {
				PanoramaCubeMap cubeMap = cubeMapFuture.join();
				if (cubeMap != null) {
					cubeMap.close();
				}
			} catch (Exception ignored) {
			}
		}
	}

	private record LoadedPanoramaData(Panorama panorama, NativeImage[] images) {
	}
}
