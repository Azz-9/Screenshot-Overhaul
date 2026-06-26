package me.Azz_9.screenshot_utilities.client.gui.components;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

@Environment(EnvType.CLIENT)
public abstract class SimpleParentWidget extends AbstractWidget implements ContainerEventHandler {

	private final @NonNull List<@NonNull GuiEventListener> children = new ArrayList<>();
	private final @NonNull List<@NonNull Renderable> renderables = new ArrayList<>();

	private @Nullable GuiEventListener focused;
	private boolean isDragging;

	public SimpleParentWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.empty());
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		for (Renderable renderable : renderables) {
			renderable.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}
	}

	protected boolean isInBounds(double x, double y) {
		return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getBottom();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return isVisible() && isInBounds(mouseX, mouseY);
	}

	public void setHovered(boolean hovered) {
		this.isHovered = hovered;
	}

	public void setActive(boolean active) {
		for (GuiEventListener child : children()) {
			if (child instanceof SimpleParentWidget simpleParentWidget) {
				simpleParentWidget.setActive(active);
			} else if (child instanceof AbstractWidget clickableWidget) {
				clickableWidget.active = active;
			}
		}
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public final boolean isDragging() {
		return this.isDragging;
	}

	@Override
	public final void setDragging(final boolean dragging) {
		this.isDragging = dragging;
	}

	@Override
	public @Nullable GuiEventListener getFocused() {
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			return screen.getGlobalFocused();
		}
		return focused;
	}

	@Override
	public void setFocused(final @Nullable GuiEventListener focused) {
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			screen.requestFocus(focused);
			return;
		}
		if (this.focused != focused) {
			if (this.focused != null) {
				this.focused.setFocused(false);
			}

			if (focused != null) {
				focused.setFocused(true);
			}

			this.focused = focused;
		}
	}

	@Override
	public @NonNull NarrationPriority narrationPriority() {
		if (this.isFocused()) {
			return NarrationPriority.FOCUSED;
		} else {
			return this.isHovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
		}
	}

	public <T extends GuiEventListener & Renderable> void addRenderableChild(T child) {
		renderables.add(child);
		addChild(child);
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

	public void clearChildren() {
		children.clear();
		renderables.clear();
	}

	@Override
	public void setX(int x) {
		for (GuiEventListener child : children()) {
			if (child instanceof LayoutElement layoutElement) {
				layoutElement.setX(x + layoutElement.getX() - getX());
			} else if (child instanceof SimpleParentWidget parent) {
				parent.setX(x + parent.getX() - getX());
			}
		}
		super.setX(x);
	}

	@Override
	public void setY(int y) {
		for (GuiEventListener child : children()) {
			if (child instanceof LayoutElement layoutElement) {
				layoutElement.setY(y + layoutElement.getY() - getY());
			} else if (child instanceof SimpleParentWidget parent) {
				parent.setY(y + parent.getY() - getY());
			}
		}
		super.setY(y);
	}

	public void setDimension(int width, int height) {
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void visitWidgets(@NonNull Consumer<AbstractWidget> widgetVisitor) {
		for (GuiEventListener child : children()) {
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

				return true;
			}

		}

		return false;
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
		return ContainerEventHandler.super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		return ContainerEventHandler.super.mouseReleased(event);
	}
}
