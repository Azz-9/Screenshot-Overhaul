package me.Azz_9.screenshot_utilities.client.gui.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.renderState.HorizontalGradientRenderState;

public class TabsScreen extends AbstractBackNavigableScreen {

	private static final int TABS_X = 10;
	private static final int TABS_Y = 10;
	private static final int TABS_GAP = 4;
	private final List<Tab> tabs = new ArrayList<>();

	protected TabsScreen(Component title, Screen parent) {
		super(title, parent);
	}

	@Override
	protected void init() {
		tabs.clear();
	}

	public void addTab(Tab tab) {
		tabs.add(tab);

		addRenderableWidget(tab);
	}

	public void addTab(Component tabText) {
		addTab(new Tab(getNextTabX(), TABS_Y, tabText, this));
	}

	private int getNextTabX() {
		if (tabs.isEmpty()) return TABS_X;
		return tabs.getLast().getRight() + TABS_GAP;
	}

	public void selectTab(Tab tab) {
		tabs.forEach(t -> t.selected = false);
		tab.selected = true;
	}

	public static class Tab extends Button {

		private static final int PADDING_HORIZONTAL = 8;
		private static final int PADDING_VERTICAL = 4;

		private final TabsScreen parent;

		private boolean selected;

		protected Tab(int x, int y, Component message, TabsScreen parent) {
			super(x, y,
					MINECRAFT.font.width(message) + PADDING_HORIZONTAL * 2,
					MINECRAFT.font.lineHeight + PADDING_VERTICAL * 2,
					message, (btn) -> {
					}, DEFAULT_NARRATION);
			this.parent = parent;
		}

		@Override
		public void onPress(@NonNull InputWithModifiers input) {
			parent.selectTab(this);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
			int color = selected || isHovered() ? Colors.WHITE : Colors.GRAY;
			graphics.text(MINECRAFT.font, getMessage(), getX() + PADDING_HORIZONTAL, getY() + PADDING_VERTICAL, color, true);

			graphics.verticalLine(getX(), getY(), getBottom(), color); // left line
			graphics.horizontalLine(getX(), getRight() - 1, getY(), color); // top line
			graphics.verticalLine(getRight() - 1, getY(), getBottom(), color); // right line

			if (selected) {
				graphics.guiRenderState.addGuiElement(
						new HorizontalGradientRenderState(
								RenderPipelines.GUI, TextureSetup.noTexture(),
								new Matrix3x2f(graphics.pose()), getRight() - 1,
								getBottom(), getRight() + 200, getBottom() + 1,
								Colors.WHITE, ARGB.color(0, Colors.WHITE),
								graphics.scissorStack.peek()
						)
				);

				graphics.guiRenderState.addGuiElement(
						new HorizontalGradientRenderState(
								RenderPipelines.GUI, TextureSetup.noTexture(),
								new Matrix3x2f(graphics.pose()), getX() - 200,
								getBottom(), getX() + 1, getBottom() + 1,
								ARGB.color(0, Colors.WHITE), Colors.WHITE,
								graphics.scissorStack.peek()
						)
				);
			}
		}
	}
}
