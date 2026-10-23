package me.Azz_9.screenshot_overhaul.client.guiFlexLib.components.gallery;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

import me.Azz_9.flex_lib.client.gui.components.FlexClickableDiv;
import me.Azz_9.flex_lib.client.gui.style.Length;
import me.Azz_9.flex_lib.client.gui.style.PseudoState;
import me.Azz_9.flex_lib.client.gui.style.Sides;
import me.Azz_9.flex_lib.client.gui.style.StyleProperties;
import me.Azz_9.flex_lib.client.gui.style.properties.Position;

public class EntryToggleButton extends FlexClickableDiv {

	private static final int PADDING = 1;

	private final @NonNull Identifier baseTexture;
	private final @NonNull Identifier hoveredTexture;
	private final @NonNull Identifier toggledTexture;
	private final @NonNull Identifier toggledHoveredTexture;

	private final @Nullable Function<Boolean, String> tooltipFunction;

	private boolean toggled;

	private EntryToggleButton(
			int size, boolean toggled, @Nullable Function<Boolean, String> tooltipFunction,
			@NonNull Identifier baseTexture, @NonNull Identifier hoveredTexture,
			@NonNull Identifier toggledTexture, @NonNull Identifier toggledHoveredTexture
	) {
		setToggled(toggled);
		this.tooltipFunction = tooltipFunction;
		this.baseTexture = baseTexture;
		this.hoveredTexture = hoveredTexture;
		this.toggledTexture = toggledTexture;
		this.toggledHoveredTexture = toggledHoveredTexture;

		declaredStyle()
				.set(StyleProperties.PADDING, Sides.of(Length.px(PADDING)))
				.set(StyleProperties.WIDTH, Length.px(size))
				.set(StyleProperties.HEIGHT, Length.px(size))
				.set(StyleProperties.POSITION, Position.ABSOLUTE)
				.set(StyleProperties.Z_INDEX, 999);

		updateBackgroundImage();
	}

	private void updateBackgroundImage() {
		if (isToggled()) {
			declaredStyle()
					.set(StyleProperties.BACKGROUND_IMAGE, toggledTexture);
			declaredStyle().on(PseudoState.HOVER)
					.set(StyleProperties.BACKGROUND_IMAGE, toggledHoveredTexture);
		} else {
			declaredStyle()
					.set(StyleProperties.BACKGROUND_IMAGE, baseTexture);
			declaredStyle().on(PseudoState.HOVER)
					.set(StyleProperties.BACKGROUND_IMAGE, hoveredTexture);
		}
	}

	private void updateTooltip() {
		if (tooltipFunction != null) {
			tooltip(tooltipFunction.apply(isToggled()));
		}
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
		updateTooltip();

	}

	public void toggle() {
		setToggled(!isToggled());
	}

	public static EntryToggleButton createFavorite(int size, boolean filled) {
		return new EntryToggleButton(
				size, filled, null,
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_hovered"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_filled"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_filled_hovered"));
	}

	public static EntryToggleButton createHideFromMap(int size, boolean initiallyCrossed) {
		return new EntryToggleButton(
				size, initiallyCrossed,
				crossed -> crossed
						? "screenshot_overhaul.hidden_from_worldmap"
						: "screenshot_overhaul.hide_from_worldmap",
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_hovered"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_crossed"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_crossed_hovered"));
	}
}
