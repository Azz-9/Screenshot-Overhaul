package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.headerWidget;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.PlaceholderTextFieldWidget;

@Environment(EnvType.CLIENT)
public class SearchBar extends PlaceholderTextFieldWidget {

	public SearchBar(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, Component.translatable("screenshot_utilities.gallery_widget.search_bar"));
		setPlaceholder(Component.translatable("screenshot_utilities.gallery_widget.search_bar.placeholder"));
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}
}
