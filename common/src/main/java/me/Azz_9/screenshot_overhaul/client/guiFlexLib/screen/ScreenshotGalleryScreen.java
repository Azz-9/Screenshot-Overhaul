package me.Azz_9.screenshot_overhaul.client.guiFlexLib.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import me.Azz_9.flex_lib.client.gui.FlexColors;
import me.Azz_9.flex_lib.client.gui.components.Div;
import me.Azz_9.flex_lib.client.gui.components.FlexButton;
import me.Azz_9.flex_lib.client.gui.style.Dimension;
import me.Azz_9.flex_lib.client.gui.style.Length;
import me.Azz_9.flex_lib.client.gui.style.Sides;
import me.Azz_9.flex_lib.client.gui.style.StyleProperties;
import me.Azz_9.flex_lib.client.gui.style.properties.Display;
import me.Azz_9.flex_lib.client.gui.style.properties.FlexDirection;
import me.Azz_9.flex_lib.client.gui.style.properties.Overflow;

public class ScreenshotGalleryScreen extends AbstractSavableScreen {

	public ScreenshotGalleryScreen(@Nullable Screen parent) {
		super(Component.translatable("screenshot_overhaul.narrator.screenshot_gallery"), parent);
	}

	@Override
	protected void onInit() {
		root().declaredStyle()
				.set(StyleProperties.DISPLAY, Display.FLEX)
				.set(StyleProperties.FLEX_DIRECTION, FlexDirection.COLUMN);

		FlexButton panoramasBtn = new FlexButton();
		panoramasBtn.addText("screenshot_overhaul.options.panoramas");
		panoramasBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(200))
				.set(StyleProperties.HEIGHT, Length.px(20))
				.set(StyleProperties.MARGIN, Sides.of(Length.px(5), Length.zero()))
				.set(StyleProperties.MARGIN_LEFT, Length.px(10));

		Div gallery = new Div();
		gallery.declaredStyle()
				.set(StyleProperties.FLEX_GROW, 1f)
				.set(StyleProperties.PADDING, Sides.of(Length.px(10)))
				.set(StyleProperties.BACKGROUND_COLOR, FlexColors.alpha(0x7f, FlexColors.BLACK))
				.set(StyleProperties.MARGIN, Sides.of(Length.zero(), Length.px(10)));

		Div topbar = new Div();
		topbar.declaredStyle()
				.set(StyleProperties.WIDTH, Length.percent(100))
				.set(StyleProperties.DISPLAY, Display.FLEX)
				.set(StyleProperties.GAP, Length.px(10));
		FlexButton searchBar = new FlexButton();
		searchBar.addText("screenshot_overhaul.gallery_widget.search_bar.placeholder");
		searchBar.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(150))
				.set(StyleProperties.HEIGHT, Length.px(20));
		FlexButton filterBtn = new FlexButton();
		filterBtn.addText("Filter: All");
		filterBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(100))
				.set(StyleProperties.HEIGHT, Length.px(20));
		FlexButton sortBtn = new FlexButton();
		sortBtn.addText("Sort by: Newest first");
		sortBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(120))
				.set(StyleProperties.HEIGHT, Length.px(20));
		FlexButton openFolderBtn = new FlexButton();
		openFolderBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(20))
				.set(StyleProperties.HEIGHT, Length.px(20))
				.set(StyleProperties.MARGIN_LEFT, Dimension.auto());
		FlexButton settingsBtn = new FlexButton();
		settingsBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(20))
				.set(StyleProperties.HEIGHT, Length.px(20));

		topbar.addAll(searchBar, filterBtn, sortBtn, openFolderBtn, settingsBtn);

		Div scrollableContent = new Div();
		scrollableContent.declaredStyle()
				.set(StyleProperties.WIDTH, Length.percent(100))
				.set(StyleProperties.HEIGHT, Length.percent(100))
				.set(StyleProperties.OVERFLOW_Y, Overflow.AUTO);

		gallery.add(topbar);

		root().addAll(panoramasBtn, gallery);

		super.onInit();
	}
}
