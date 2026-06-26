package me.Azz_9.screenshot_utilities.client.gui.components.screenshotGallery.content;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

@Environment(EnvType.CLIENT)
public class FavoriteButton extends Button {
	private static final int PADDING = 1;

	private static final @NonNull Identifier BASE_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite");
	private static final @NonNull Identifier HOVERED_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_hovered");
	private static final @NonNull Identifier FILLED_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_filled");
	private static final @NonNull Identifier FILLED_HOVERED_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_filled_hovered");

	private boolean filled;
	private float progress;

	protected FavoriteButton(int x, int y, int width, int height, OnPress onPress, boolean filled) {
		super(x, y, width, height, Component.translatable("screenshot_utilities.favorite"), onPress, DEFAULT_NARRATION);
		this.filled = filled;
	}

	public boolean isFilled() {
		return filled;
	}

	public void setFilled(boolean filled) {
		this.filled = filled;
	}

	public void toggle() {
		this.filled = !this.filled;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getTexture(),
				getX() + PADDING, getY() + PADDING,
				getWidth() - PADDING * 2, getHeight() - PADDING * 2, progress);
	}

	private @NonNull Identifier getTexture() {
		if (isHovered() && active) {
			if (filled) {
				return FILLED_HOVERED_TEXTURE;
			} else {
				return HOVERED_TEXTURE;
			}
		} else {
			if (filled) {
				return FILLED_TEXTURE;
			} else {
				return BASE_TEXTURE;
			}
		}
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		toggle();
		if (MINECRAFT.gui.screen() instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}
}
