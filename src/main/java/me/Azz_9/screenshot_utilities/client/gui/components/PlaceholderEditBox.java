package me.Azz_9.screenshot_utilities.client.gui.components;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

@Environment(EnvType.CLIENT)
public class PlaceholderEditBox extends EditBox {

	private final Font FONT;
	private int placeholderColor = 0xffa0a0a0;
	private Component placeholderText;

	public PlaceholderEditBox(Font font, int x, int y, int width, int height, Component text) {
		super(font, x, y, width, height, text);
		this.FONT = font;
	}

	public void setPlaceholderColor(int color) {
		this.placeholderColor = color;
	}

	public void setPlaceholder(Component placeholder) {
		this.placeholderText = placeholder;
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (this.isVisible()) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);

			if (this.placeholderText != null && this.getValue().isEmpty()) {
				int x = this.isBordered() ? this.getX() + 4 : this.getX();
				int y = this.isBordered() ? this.getY() + (this.height - 8) / 2 : this.getY();

				graphics.text(FONT, this.placeholderText, x, y, this.placeholderColor);
			}
		}
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		if (MINECRAFT.gui.screen() instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(event, doubleClick);
	}
}