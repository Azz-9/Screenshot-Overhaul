package me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import com.mojang.blaze3d.platform.NativeImage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.AbstractThumbnailWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.Panorama;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaHolder;

@Environment(EnvType.CLIENT)
public class PanoramaThumbnailWidget extends AbstractThumbnailWidget implements AutoCloseable {

	private static final float SPIN_SPEED = 0.1F;
	private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

	private final PanoramaCubeMap cubeMap;
	private final Panorama panorama;
	private float spin = 0f;
	private boolean loading = false;
	private final @Nullable Consumer<Panorama> onClick;

	public PanoramaThumbnailWidget(int x, int y, int width, int height, final @NonNull Identifier panoramaLocation,
								   final @NonNull Panorama panorama, final @Nullable Consumer<Panorama> onClick) {
		super(x, y, width, height, Component.empty());
		this.panorama = panorama;
		this.onClick = onClick;

		// Identifier unique par instance de widget
		Identifier location = Identifier.fromNamespaceAndPath(
				MOD_ID,
				"dynamic/panorama_widget_" + ID_COUNTER.getAndIncrement()
		);
		this.cubeMap = new PanoramaCubeMap(location);
		startLoading();
	}

	private void startLoading() {
		loading = true;
		Util.ioPool().execute(() -> {
			NativeImage[] images = PanoramaHolder.loadImages(panorama);
			if (images != null) {
				MINECRAFT.execute(() -> {
					cubeMap.uploadTexture(images);
					// Fermer les NativeImage après upload GPU
					for (NativeImage image : images) image.close();
					loading = false;
				});
			} else {
				loading = false;
			}
		});
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (loading) {
			// TODO remplacer par un loading
			graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xffff0000);
			return;
		}

		spin = Mth.wrapDegrees(spin + delta * SPIN_SPEED);
		PanoramaThumbnailRenderQueue.enqueue(
				new PanoramaThumbnailRenderState(getX(), getY(), getWidth(), getHeight(), spin, cubeMap)
		);
		super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
	}

	// input

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (onClick != null) onClick.accept(panorama);
	}

	@Override
	public void close() throws Exception {
		cubeMap.close();
	}
}
