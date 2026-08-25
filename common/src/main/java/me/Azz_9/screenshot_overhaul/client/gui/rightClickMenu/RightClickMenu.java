package me.Azz_9.screenshot_overhaul.client.gui.rightClickMenu;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.gui.components.SimpleParentWidget;

public class RightClickMenu extends SimpleParentWidget {

	private static final int ITEM_HEIGHT = 20;
	private static final int SHADOW_OFFSET = 3;
	private static final int PADDING = 2;

	private final @NonNull List<MenuItemWidget> items = new ArrayList<>();

	public RightClickMenu() {
		super(0, 0, 0, 0);
	}

	void addMenuItem(@NonNull MenuItem menuItem) {
		addRenderableChild(
				new MenuItemWidget(this, items.size(), menuItem)
		);

		updateDimension();
	}

	void clearMenuItems() {
		clearChildren();
	}

	@Override
	public <T extends GuiEventListener & Renderable> void addRenderableChild(T child) {
		if (child instanceof MenuItemWidget menuItemWidget) {
			items.add(menuItemWidget);
		}
		super.addRenderableChild(child);
	}

	@Override
	public void clearChildren() {
		items.clear();
		super.clearChildren();
	}

	private void updateDimension() {
		updateHeight();
		updateItemsWidth();
	}

	private void updateHeight() {
		setHeight(PADDING * 2 + ITEM_HEIGHT * items.size());
	}

	private void updateItemsWidth() {
		int maxWidth = 0;
		for (MenuItemWidget item : items) {
			maxWidth = Math.max(maxWidth, item.computeMinWidth());
		}

		setWidth(maxWidth + PADDING * 2);

		for (MenuItemWidget item : items) {
			item.setWidth(maxWidth);
		}
	}

	public void hide() {
		visible = false;
	}

	public void show(double mouseX, double mouseY) {
		setPosition((int) mouseX, (int) mouseY);
		visible = true;
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (visible) {
			graphics.fill(getX() + SHADOW_OFFSET, getY() + SHADOW_OFFSET, getRight() + SHADOW_OFFSET, getBottom() + SHADOW_OFFSET, Colors.BLACK_SEMI_TRANSPARENT);
			graphics.fill(getX(), getY(), getRight(), getBottom(), Colors.DARK_GRAY);
			for (MenuItemWidget item : items) {
				item.extractRenderState(graphics, mouseX, mouseY, delta);
			}
		}
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
	}

	public record MenuItem(@NonNull Identifier icon, @NonNull Component label,
						   @NonNull Consumer<MenuItemWidget> action) {
	}

	public static class MenuItemWidget extends AbstractWidget {

		private static final int ICON_SIZE = 16;
		private static final int PADDING = (ITEM_HEIGHT - ICON_SIZE) / 2;

		private @NonNull Identifier icon;
		private @NonNull Component label;
		private final @NonNull Consumer<MenuItemWidget> action;

		private final int TEXT_OFFSET;

		public MenuItemWidget(@NonNull RightClickMenu menu, int index, @NonNull MenuItem menuItem) {
			super(menu.getX() + RightClickMenu.PADDING, menu.getY() + RightClickMenu.PADDING + ITEM_HEIGHT * index, 0, ITEM_HEIGHT, Component.empty());
			this.icon = menuItem.icon;
			this.label = menuItem.label;
			this.action = menuItem.action;
			TEXT_OFFSET = (ITEM_HEIGHT - MINECRAFT.font.lineHeight) / 2;
		}

		public int computeMinWidth() {
			return PADDING * 3 + ICON_SIZE + MINECRAFT.font.width(label);
		}

		@Override
		public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
			action.accept(this);
		}

		@Override
		protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			graphics.fill(getX(), getY(), getRight(), getBottom(), isHovered() ? Colors.GRAY : Colors.DARK_GRAY);
			//graphics.outline(getX(), getY(), getWidth(), getHeight(), Colors.WHITE);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getX() + PADDING, getY() + PADDING, ICON_SIZE, ICON_SIZE);
			graphics.text(MINECRAFT.font, label, getX() + PADDING * 2 + ICON_SIZE, getY() + TEXT_OFFSET, Colors.WHITE);

			handleCursor(graphics);
		}

		public void setIcon(@NonNull Identifier icon) {
			this.icon = icon;
		}

		public void setLabel(@NonNull Component label) {
			this.label = label;
		}

		@Override
		protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
		}
	}
}
