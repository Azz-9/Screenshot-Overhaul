package me.Azz_9.screenshot_utilities.client.gui.widget.panoramaGallery.content;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.CubeMapTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.AbstractThumbnailWidget;

public class DefaultPanoramaThumbnailWidget extends AbstractThumbnailWidget implements AutoCloseable {

	private static final float SPIN_SPEED = 0.3f;

	private final PanoramaCubeMap cubeMap;
	private float rotation = 0f;
	private final @Nullable Runnable onClick;

	public DefaultPanoramaThumbnailWidget(int x, int y, int width, int height, @Nullable Runnable onClick, @NonNull Identifier panoramaLocation) {
		super(x, y, width, height, Component.empty());
		this.onClick = onClick;
		this.cubeMap = new PanoramaCubeMap(panoramaLocation);
		this.cubeMap.registerTexture(MINECRAFT.getTextureManager(), new CubeMapTexture(panoramaLocation));
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		rotation += SPIN_SPEED * delta;

		cubeMap.renderToArea(getX(), getY(), getWidth(), getHeight(), 0, rotation);
		super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
	}

	// input

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (onClick != null) onClick.run();
	}

	@Override
	public void close() throws Exception {
		this.cubeMap.close();
	}
}
