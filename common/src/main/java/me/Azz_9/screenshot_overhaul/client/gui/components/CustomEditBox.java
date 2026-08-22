package me.Azz_9.screenshot_overhaul.client.gui.components;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusableScreen;

public class CustomEditBox extends EditBox {

	private int placeholderColor = 0xffa0a0a0;
	private @Nullable Component placeholderText;
	private @Nullable Predicate<String> filter;

	public CustomEditBox(int x, int y, int width, int height, @NonNull Component text) {
		super(MINECRAFT.font, x, y, width, height, text);
	}

	public void setPlaceholderColor(int color) {
		this.placeholderColor = color;
	}

	public void setPlaceholder(@Nullable Component placeholder) {
		this.placeholderText = placeholder;
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (this.isVisible()) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);

			if (this.placeholderText != null && this.getValue().isEmpty()) {
				int x = this.isBordered() ? this.getX() + 4 : this.getX();
				int y = this.isBordered() ? this.getY() + (this.height - 8) / 2 : this.getY();

				graphics.text(MINECRAFT.font, this.placeholderText, x, y, this.placeholderColor);
			}
		}
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
		if (MINECRAFT.gui.screen() instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(event, doubleClick);
	}

	public void setFilter(@Nullable Predicate<String> filter) {
		this.filter = filter;
	}

	@Override
	public void insertText(@NonNull String input) {
		if (filter != null && filter.test(input)) {
			super.insertText(input);
		}
	}
}