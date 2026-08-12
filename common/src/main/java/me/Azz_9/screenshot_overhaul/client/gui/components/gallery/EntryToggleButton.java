package me.Azz_9.screenshot_overhaul.client.gui.components.gallery;

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
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusableScreen;

public class EntryToggleButton extends Button {
	private static final int PADDING = 1;

	private final @NonNull Identifier baseTexture;
	private final @NonNull Identifier hoveredTexture;
	private final @NonNull Identifier toggledTexture;
	private final @NonNull Identifier toggledHoveredTexture;

	private boolean toggled;
	private float progress;
	private final @Nullable Function<Boolean, Tooltip> tooltipFunction;

	protected EntryToggleButton(int x, int y, int width, int height, Component message, @NonNull BiConsumer<Button, Boolean> onPress,
								boolean toggled, @Nullable Function<Boolean, Tooltip> tooltipFunction,
								@NonNull Identifier baseTexture, @NonNull Identifier hoveredTexture,
								@NonNull Identifier toggledTexture, @NonNull Identifier toggledHoveredTexture) {
		super(x, y, width, height, message, button -> onPress.accept(button, ((EntryToggleButton) button).isToggled()), DEFAULT_NARRATION);
		setToggled(toggled);
		this.tooltipFunction = tooltipFunction;
		this.baseTexture = baseTexture;
		this.hoveredTexture = hoveredTexture;
		this.toggledTexture = toggledTexture;
		this.toggledHoveredTexture = toggledHoveredTexture;
	}

	private void updateTooltip() {
		if (this.tooltipFunction != null) {
			setTooltip(tooltipFunction.apply(isToggled()));
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
			if (isToggled()) {
				return toggledHoveredTexture;
			} else {
				return hoveredTexture;
			}
		} else {
			if (isToggled()) {
				return toggledTexture;
			} else {
				return baseTexture;
			}
		}
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		toggle();
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}

	public static EntryToggleButton createFavorite(int x, int y, int width, int height, @NonNull BiConsumer<Button, Boolean> onPress, boolean filled) {
		return new EntryToggleButton(
				x, y, width, height, Component.translatable("screenshot_overhaul.favorite"), onPress, filled, null,
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_hovered"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_filled"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/favorite_filled_hovered"));
	}

	public static EntryToggleButton createHideFromMap(int x, int y, int width, int height, @NonNull BiConsumer<Button, Boolean> onPress, boolean initiallyCrossed) {
		return new EntryToggleButton(
				x, y, width, height, Component.translatable("screenshot_overhaul.hide_from_worldmap"), onPress,
				initiallyCrossed, crossed -> Tooltip.create(crossed ? Component.translatable("screenshot_overhaul.hidden_from_worldmap") : Component.translatable("screenshot_overhaul.hide_from_worldmap")),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_hovered"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_crossed"),
				Identifier.fromNamespaceAndPath(MOD_ID, "icon/hide_from_map_crossed_hovered"));
	}
}
