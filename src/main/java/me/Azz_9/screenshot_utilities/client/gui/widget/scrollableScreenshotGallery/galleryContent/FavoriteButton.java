package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.galleryContent;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

public class FavoriteButton extends ButtonWidget {
	private static final int PADDING = 1;

	private static final Identifier BASE_TEXTURE = Identifier.of(MOD_ID, "icon/favorite");
	private static final Identifier HOVERED_TEXTURE = Identifier.of(MOD_ID, "icon/favorite_hovered");
	private static final Identifier FILLED_TEXTURE = Identifier.of(MOD_ID, "icon/favorite_filled");
	private static final Identifier FILLED_HOVERED_TEXTURE = Identifier.of(MOD_ID, "icon/favorite_filled_hovered");

	private boolean filled;

	protected FavoriteButton(int x, int y, int width, int height, PressAction onPress, boolean filled) {
		super(x, y, width, height, net.minecraft.text.Text.translatable("screenshot_utilities.favorite"), onPress, DEFAULT_NARRATION_SUPPLIER);
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

	@Override
	protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, getTexture(),
				getX() + PADDING, getY() + PADDING,
				getWidth() - PADDING * 2, getHeight() - PADDING * 2);
	}

	private Identifier getTexture() {
		if (isHovered()) {
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
	public void onClick(Click click, boolean doubled) {
		toggle();
		if (CLIENT.currentScreen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}
}
