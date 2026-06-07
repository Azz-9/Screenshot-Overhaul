package me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.AbstractThumbnailWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.DynamicCubeMapTexture;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.Panorama;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.PanoramaHolder;

@Environment(EnvType.CLIENT)
public class PanoramaThumbnailWidget extends AbstractThumbnailWidget implements AutoCloseable {

	private static final float SPIN_SPEED = 0.3f;

	private final PanoramaCubeMap cubeMap;
	private float rotation = 0f;

	private final @NonNull Panorama panorama;
	private final @Nullable Consumer<Panorama> onClick;

	public PanoramaThumbnailWidget(int x, int y, int width, int height, final @NonNull Identifier panoramaLocation,
								   final @NonNull Panorama panorama, final @Nullable Consumer<Panorama> onClick) {
		super(x, y, width, height, Component.empty());
		this.panorama = panorama;
		this.onClick = onClick;

		this.cubeMap = new PanoramaCubeMap(panoramaLocation);
		DynamicCubeMapTexture texture = new DynamicCubeMapTexture();
		texture.setImages(PanoramaHolder.loadImages(panorama));
		this.cubeMap.registerTexture(MINECRAFT.getTextureManager(), texture);
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		rotation += SPIN_SPEED * delta;

		cubeMap.renderToArea(getX(), getY(), getWidth(), getHeight(), 0, rotation);
		super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
	}

	@Override
	public void close() throws Exception {
		cubeMap.close();
	}

	// input

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (onClick != null) onClick.accept(panorama);
	}
}
