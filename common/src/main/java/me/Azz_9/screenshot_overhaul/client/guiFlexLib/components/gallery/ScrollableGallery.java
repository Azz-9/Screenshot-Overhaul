package me.Azz_9.screenshot_overhaul.client.guiFlexLib.components.gallery;

import net.minecraft.util.Util;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import me.Azz_9.flex_lib.client.ITranslatable;
import me.Azz_9.flex_lib.client.gui.FlexColors;
import me.Azz_9.flex_lib.client.gui.components.Div;
import me.Azz_9.flex_lib.client.gui.components.FlexButton;
import me.Azz_9.flex_lib.client.gui.components.FlexCycleButton;
import me.Azz_9.flex_lib.client.gui.events.EventType;
import me.Azz_9.flex_lib.client.gui.style.Dimension;
import me.Azz_9.flex_lib.client.gui.style.Length;
import me.Azz_9.flex_lib.client.gui.style.Sides;
import me.Azz_9.flex_lib.client.gui.style.StyleProperties;
import me.Azz_9.flex_lib.client.gui.style.properties.Display;
import me.Azz_9.flex_lib.client.gui.style.properties.Overflow;
import me.Azz_9.screenshot_overhaul.client.CommonSprites;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.config.SavableObject;

public abstract class ScrollableGallery<T extends AbstractGalleryEntryWidget> extends Div {

	public ScrollableGallery(final @NotNull SavableObject<SortMode> sortConfig) {
		this.declaredStyle()
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

		FlexCycleButton<FilterMode> filterBtn = FlexCycleButton.ofEnum(
				"screenshot_overhaul.gallery_widget.filter",
				FilterMode.ALL
		);
		filterBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(100))
				.set(StyleProperties.HEIGHT, Length.px(20));

		FlexCycleButton<SortMode> sortBtn = FlexCycleButton.ofEnum(
				"screenshot_overhaul.gallery_widget.sort_by",
				sortConfig.getValue()
		);
		sortBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(120))
				.set(StyleProperties.HEIGHT, Length.px(20));

		FlexButton openFolderBtn = new FlexButton();
		openFolderBtn.events().addEventListener(EventType.MOUSE_DOWN, _ ->
				Util.getPlatform().openPath(Config.getInstance().getAbsoluteScreenshotsDir()));
		openFolderBtn.declaredStyle()
				.set(StyleProperties.WIDTH, Length.px(20))
				.set(StyleProperties.HEIGHT, Length.px(20))
				.set(StyleProperties.MARGIN_LEFT, Dimension.auto())
				.set(StyleProperties.BACKGROUND_IMAGE, CommonSprites.FOLDER_SPRITE);

		topbar.addAll(searchBar, filterBtn, sortBtn, openFolderBtn);

		Div scrollableContent = new Div();
		scrollableContent.declaredStyle()
				.set(StyleProperties.WIDTH, Length.percent(100))
				.set(StyleProperties.HEIGHT, Length.percent(100))
				.set(StyleProperties.OVERFLOW_Y, Overflow.AUTO);

		this.add(topbar);
	}

	public enum SortMode implements ITranslatable {
		DATE_DESC("screenshot_overhaul.gallery_widget.sort_mode.date_desc"),
		DATE_ASC("screenshot_overhaul.gallery_widget.sort_mode.date_asc"),
		WORLD_NAME_DESC("screenshot_overhaul.gallery_widget.sort_mode.world_name_desc"),
		WORLD_NAME_ASC("screenshot_overhaul.gallery_widget.sort_mode.world_name_asc"),
		SERVER_IP_DESC("screenshot_overhaul.gallery_widget.sort_mode.server_ip_desc"),
		SERVER_IP_ASC("screenshot_overhaul.gallery_widget.sort_mode.server_ip_asc");

		private final @NonNull String translationKey;

		SortMode(@NonNull String translationKey) {
			this.translationKey = translationKey;
		}

		@Override
		public @NonNull String getTranslationKey() {
			return translationKey;
		}
	}

	public enum FilterMode implements ITranslatable {
		ALL("screenshot_overhaul.gallery_widget.filter.all"),
		FAVORITES("screenshot_overhaul.gallery_widget.filter.favorites");

		private final @NonNull String translationKey;

		FilterMode(@NonNull String translationKey) {
			this.translationKey = translationKey;
		}

		@Override
		public @NonNull String getTranslationKey() {
			return translationKey;
		}
	}
}
