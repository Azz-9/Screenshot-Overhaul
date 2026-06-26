package me.Azz_9.screenshot_utilities.client.gui.components;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

@Environment(EnvType.CLIENT)
public class TexturedCyclingButtonWidget<T> extends Button {
	private final @NonNull T[] values;
	private int index;
	private final @NonNull Function<T, Identifier> iconGetter;
	private final @NonNull BiConsumer<TexturedCyclingButtonWidget<T>, T> callback;
	private OptionInstance.@Nullable TooltipSupplier<T> tooltipFactory;

	public TexturedCyclingButtonWidget(int x, int y,
	                                   int width, int height,
	                                   int index,
	                                   @NonNull BiConsumer<TexturedCyclingButtonWidget<T>, T> callback,
	                                   @NonNull T[] values,
	                                   @NonNull Function<T, Identifier> iconGetter) {
		super(x, y, width, height, Component.empty(), (btn) -> {
		}, DEFAULT_NARRATION);
		this.index = index;
		this.callback = callback;
		this.values = values;
		this.iconGetter = iconGetter;
	}

	public T getValue() {
		return values[index];
	}

	@Override
	protected void handleCursor(@NonNull GuiGraphicsExtractor graphics) {
		if (this.isHovered()) {
			graphics.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	public void setTooltipFactory(OptionInstance.@Nullable TooltipSupplier<T> tooltipFactory) {
		this.tooltipFactory = tooltipFactory;
		refreshTooltip();
	}

	private void refreshTooltip() {
		if (this.tooltipFactory != null) {
			this.setTooltip(tooltipFactory.apply(values[index]));
		}
	}

	@Override
	public void onPress(InputWithModifiers input) {
		if (input.hasShiftDown()) {
			this.cycle(-1);
		} else {
			this.cycle(1);
		}
		this.refreshTooltip();
		callback.accept(this, values[index]);
	}

	private void cycle(int amount) {
		this.index = Mth.positiveModulo(this.index + amount, values.length);
	}

	@Override
	public void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		extractDefaultSprite(graphics);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, iconGetter.apply(values[index]), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
}
