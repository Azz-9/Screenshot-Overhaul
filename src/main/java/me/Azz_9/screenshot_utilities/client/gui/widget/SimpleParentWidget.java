package me.Azz_9.screenshot_utilities.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class SimpleParentWidget extends AbstractParentElement implements Drawable, Element, Widget, Selectable {

	private final @NonNull List<@NonNull Element> children = new ArrayList<>();
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
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (this.visible) {
			this.hovered = context.scissorContains(mouseX, mouseY) && this.isInBounds(mouseX, mouseY);
			this.renderWidget(context, mouseX, mouseY, deltaTicks);
		}
	}

	protected abstract void renderWidget(@NonNull DrawContext context, int mouseX, int mouseY, float deltaTicks);

	private boolean isInBounds(double x, double y) {
		return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getBottom();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return isInBounds(mouseX, mouseY);
	}

	public boolean isHovered() {
		return hovered;
	}

	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}

	public void setActive(boolean active) {
		for (Element child : children) {
			if (child instanceof SimpleParentWidget simpleParentWidget) {
				simpleParentWidget.setActive(active);
			} else if (child instanceof ClickableWidget clickableWidget) {
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
	public Selectable.SelectionType getType() {
		if (this.isFocused()) {
			return Selectable.SelectionType.FOCUSED;
		} else {
			return this.hovered ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
		}
	}

	public void addChild(@NonNull Element child) {
		children.add(child);
	}

	public void addAllChildren(@NonNull Element... children) {
		for (Element child : children) {
			addChild(child);
		}
	}

	@Override
	public List<Element> children() {
		return children;
	}

	@Override
	public ScreenRect getNavigationFocus() {
		return Widget.super.getNavigationFocus();
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
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		for (Element child : children) {
			if (child instanceof ClickableWidget clickableWidget) {
				consumer.accept(clickableWidget);
			}
		}
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		Optional<Element> optional = this.hoveredElement(click.x(), click.y());
		if (optional.isPresent()) {
			Element element = optional.get();
			if (element.mouseClicked(click, doubled) && element.isClickable()) {
				this.setFocused(element);
				if (click.button() == 0) {
					this.setDragging(true);
				}

				return true;
			}

		}

		return false;
	}
}
