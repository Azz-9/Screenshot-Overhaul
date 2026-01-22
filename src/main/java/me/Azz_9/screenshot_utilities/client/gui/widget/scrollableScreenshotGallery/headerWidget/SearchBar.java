package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery.headerWidget;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.PlaceholderTextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class SearchBar extends PlaceholderTextFieldWidget {

	public SearchBar(TextRenderer textRenderer, int x, int y, int width, int height) {
		super(textRenderer, x, y, width, height, Text.translatable("screenshot_utilities.gallery_widget.search_bar"));
		setPlaceholder(Text.translatable("screenshot_utilities.gallery_widget.search_bar.placeholder"));
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		if (CLIENT.currentScreen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}
}
