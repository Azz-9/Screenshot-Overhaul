package me.Azz_9.screenshot_utilities.client.gui.widget;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class TexturedCyclingButtonWidget<T> extends ButtonWidget {
	private final @NonNull T[] values;
	private int index;
	private final @NonNull Function<T, Identifier> iconGetter;
	private final @NonNull BiConsumer<TexturedCyclingButtonWidget<T>, T> callback;
	private SimpleOption.@Nullable TooltipFactory<T> tooltipFactory;

	public TexturedCyclingButtonWidget(int x, int y,
									   int width, int height,
									   int index,
									   @NonNull BiConsumer<TexturedCyclingButtonWidget<T>, T> callback,
									   @NonNull T[] values,
									   @NonNull Function<T, Identifier> iconGetter) {
		super(x, y, width, height, net.minecraft.text.Text.empty(), (btn) -> {
		}, DEFAULT_NARRATION_SUPPLIER);
		this.index = index;
		this.callback = callback;
		this.values = values;
		this.iconGetter = iconGetter;
	}

	public T getValue() {
		return values[index];
	}

	@Override
	protected void setCursor(DrawContext context) {
		if (this.isHovered() && this.isInteractable()) {
			context.setCursor(StandardCursors.POINTING_HAND);
		}
	}

	public void setTooltipFactory(SimpleOption.@Nullable TooltipFactory<T> tooltipFactory) {
		this.tooltipFactory = tooltipFactory;
		refreshTooltip();
	}

	private void refreshTooltip() {
		if (this.tooltipFactory != null) {
			this.setTooltip(tooltipFactory.apply(values[index]));
		}
	}

	@Override
	public void onPress(AbstractInput input) {
		if (input.hasShift()) {
			this.cycle(-1);
		} else {
			this.cycle(1);
		}
		this.refreshTooltip();
		callback.accept(this, values[index]);
	}

	private void cycle(int amount) {
		this.index = MathHelper.floorMod(this.index + amount, values.length);
	}

	@Override
	public void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		drawButton(context);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, iconGetter.apply(values[index]), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		if (CLIENT.currentScreen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}
}
