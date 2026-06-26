package me.Azz_9.screenshot_utilities.client.gui.components.panoramaGallery.content;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.gui.Loading;
import me.Azz_9.screenshot_utilities.client.gui.components.gallery.AbstractThumbnailWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.*;

@Environment(EnvType.CLIENT)
public class PanoramaThumbnailWidget extends AbstractThumbnailWidget {

	private static final float DRAG_SENSITIVITY = 0.45f;
	private static final float PITCH_CLAMP = 85f;
	private static final double DRAG_THRESHOLD = 4.0;

	private double clickStartX;
	private double clickStartY;
	private boolean didDrag = false;

	private final Panorama panorama;
	private float yaw = 0f;
	private float pitch = 0f;
	private final @Nullable Consumer<Panorama> onClick;

	public PanoramaThumbnailWidget(int x, int y, int width, int height, final @NonNull Identifier panoramaLocation,
								   final @NonNull Panorama panorama, final @Nullable Consumer<Panorama> onClick) {
		super(x, y, width, height, Component.empty());
		this.panorama = panorama;
		this.onClick = onClick;
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		PanoramaTexture panoramaTexture = PanoramaTextureCache.getThumbnail(panorama);
		PanoramaCubeMap cubeMap = panoramaTexture.getCubeMap();
		if (!panoramaTexture.isReady() || cubeMap == null) {
			int min = Math.min(getWidth(), getHeight());
			Loading.drawLoadingSpinner(
					graphics,
					getX() + getWidth() / 2, getY() + getHeight() / 2,
					min / 20, min / 10
			);
			return;
		}

		PanoramaThumbnailRenderQueue.enqueue(
				new PanoramaThumbnailRenderState(getX(), getY(), getWidth(), getHeight(), pitch, yaw, cubeMap)
		);

		if (cubeMap.getOffscreenTarget() != null) {
			graphics.blit(
					cubeMap.getOffscreenTarget().getColorTextureView(),
					RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),
					getX(), getY(),
					getX() + getWidth(), getY() + getHeight(),
					0f, 1f,
					1f, 0f
			);
		}

		super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
	}

	// input


	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubleClick) {
		if (this.isActive()) {
			if (this.isValidClickButton(click.buttonInfo()) && this.isMouseOver(click.x(), click.y())) {
				clickStartX = click.x();
				clickStartY = click.y();
				didDrag = false;
				return true;
			}

		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
		if (click.button() == 0) {
			double dist = Math.hypot(click.x() - clickStartX, click.y() - clickStartY);
			if (dist > DRAG_THRESHOLD) didDrag = true;

			if (didDrag) {
				yaw = Mth.wrapDegrees(yaw + (float) dx * DRAG_SENSITIVITY);
				pitch = Mth.clamp(pitch + (float) dy * -DRAG_SENSITIVITY, -PITCH_CLAMP, PITCH_CLAMP);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		if (click.button() == 0 && !didDrag) {
			this.playDownSound(Minecraft.getInstance().getSoundManager());
			this.onClick(click, false);
		}
		didDrag = false;
		return true;
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (onClick != null) onClick.accept(panorama);
	}
}
