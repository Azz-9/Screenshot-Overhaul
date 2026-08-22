package me.Azz_9.screenshot_overhaul.client.gui.components.gallery.headerWidget;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.gui.components.CustomEditBox;
import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusableScreen;

public class SearchBar extends CustomEditBox {

	public SearchBar(int x, int y, int width, int height) {
		super(x, y, width, height, Component.translatable("screenshot_overhaul.gallery_widget.search_bar"));
		setPlaceholder(Component.translatable("screenshot_overhaul.gallery_widget.search_bar.placeholder"));
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}
}
