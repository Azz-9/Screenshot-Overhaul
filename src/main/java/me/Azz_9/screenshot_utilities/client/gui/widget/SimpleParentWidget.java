package me.Azz_9.screenshot_utilities.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class SimpleParentWidget extends AbstractContainerEventHandler implements LayoutElement, Renderable, GuiEventListener, NarratableEntry {

	private final @NonNull List<@NonNull GuiEventListener> children = new ArrayList<>();
	private boolean visible = true;
	private int width;
	private int height;
	private int x;
	private int y;
	private boolean hovered;

	public SimpleParentWidget(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		if (this.visible) {
			this.hovered = graphics.containsPointInScissor(mouseX, mouseY) && this.isInBounds(mouseX, mouseY);
			this.renderWidget(graphics, mouseX, mouseY, deltaTicks);
		}
	}

	protected abstract void renderWidget(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks);

	private boolean isInBounds(double x, double y) {
		return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getBottom();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return visible && isInBounds(mouseX, mouseY);
	}

	public boolean isHovered() {
		return hovered;
	}

	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}

	public void setActive(boolean active) {
		for (GuiEventListener child : children) {
			if (child instanceof SimpleParentWidget simpleParentWidget) {
				simpleParentWidget.setActive(active);
			} else if (child instanceof AbstractWidget clickableWidget) {
				clickableWidget.active = active;
			}
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public @NonNull NarrationPriority narrationPriority() {
		if (this.isFocused()) {
			return NarrationPriority.FOCUSED;
		} else {
			return this.hovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
		}
	}

	public void addChild(@NonNull GuiEventListener child) {
		children.add(child);
	}

	public void addAllChildren(@NonNull GuiEventListener... children) {
		for (GuiEventListener child : children) {
			addChild(child);
		}
	}

	@Override
	public @NonNull List<GuiEventListener> children() {
		return children;
	}

	@Override
	public @NonNull ScreenRectangle getRectangle() {
		return LayoutElement.super.getRectangle();
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getRight() {
		return getX() + getWidth();
	}

	public int getBottom() {
		return getY() + getHeight();
	}

	public void setDimension(int width, int height) {
		setWidth(width);
		setHeight(height);
	}

	public void setPosition(int x, int y) {
		setX(x);
		setY(y);
	}

	@Override
	public void visitWidgets(@NonNull Consumer<AbstractWidget> widgetVisitor) {
		for (GuiEventListener child : children) {
			if (child instanceof AbstractWidget clickableWidget) {
				widgetVisitor.accept(clickableWidget);
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		Optional<GuiEventListener> optional = this.getChildAt(click.x(), click.y());
		if (optional.isPresent()) {
			GuiEventListener element = optional.get();
			if (element.mouseClicked(click, doubled)) {
				if (click.button() == 0) {
					this.setDragging(true);
				}

				if (shouldTakeFocusAfterInteraction()) {
					this.setFocused(element);
				}

				return true;
			}

		}

		return false;
	}
}
