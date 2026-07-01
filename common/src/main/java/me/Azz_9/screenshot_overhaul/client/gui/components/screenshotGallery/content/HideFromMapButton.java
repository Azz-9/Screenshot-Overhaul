package me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.content;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusableScreen;

public class HideFromMapButton extends Button {
	private static final int PADDING = 1;

	private static final @NonNull Identifier BASE_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map");
	private static final @NonNull Identifier HOVERED_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_hovered");
	private static final @NonNull Identifier CROSSED_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_crossed");
	private static final @NonNull Identifier CROSSED_HOVERED_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_crossed_hovered");

	private boolean crossed;
	private float progress;

	protected HideFromMapButton(int x, int y, int width, int height, @NonNull OnPress onPress, boolean crossed) {
		super(x, y, width, height, Component.translatable("screenshot_overhaul.hide_from_worldmap"), onPress, DEFAULT_NARRATION);
		setCrossed(crossed);
	}

	private void updateTooltip() {
		setTooltip(Tooltip.create(crossed ? Component.translatable("screenshot_overhaul.hidden_from_worldmap") : Component.translatable("screenshot_overhaul.hide_from_worldmap")));
	}

	public boolean isCrossed() {
		return crossed;
	}

	public void setCrossed(boolean crossed) {
		this.crossed = crossed;
		updateTooltip();
	}

	public void toggle() {
		setCrossed(!isCrossed());
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	@Override
	protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getTexture(),
				getX() + PADDING, getY() + PADDING,
				getWidth() - PADDING * 2, getHeight() - PADDING * 2, progress);
	}

	private @NonNull Identifier getTexture() {
		if (isHovered() && active) {
			if (crossed) {
				return CROSSED_HOVERED_TEXTURE;
			} else {
				return HOVERED_TEXTURE;
			}
		} else {
			if (crossed) {
				return CROSSED_TEXTURE;
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
