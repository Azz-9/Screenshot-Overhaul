package me.Azz_9.screenshot_utilities.client.gui.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.renderState.HorizontalGradientRenderState;

/**
 * Generic screen with a horizontal row of tabs at the top.
 *
 * <p>This class is intentionally kept free of any config-specific logic so it
 * can be reused for other tabbed screens in the mod.</p>
 */
@Environment(EnvType.CLIENT)
public class TabsScreen extends AbstractBackNavigableScreen {

	// Package-accessible so SettingsScreen can compute layout without hard-coding
	static final int TABS_X = 10;
	static final int TABS_Y = 10;
	static final int TABS_GAP = 4;

	private final @NonNull List<Tab> tabs = new ArrayList<>();

	protected TabsScreen(@NonNull Component title, @Nullable Screen parent) {
		super(title, parent);
	}

	@Override
	protected void init() {
		tabs.clear();
	}

	// -------------------------------------------------------------------------
	// Tab management
	// -------------------------------------------------------------------------

	public @NonNull Tab addTab(@NonNull Tab tab) {
		tab.setPosition(getNextTabX(), TABS_Y);
		tabs.add(tab);
		addRenderableWidget(tab);
		return tab;
	}

	public @NonNull Tab addTab(@NonNull Component tabText) {
		return addTab(new Tab(getNextTabX(), TABS_Y, tabText, this));
	}

	private int getNextTabX() {
		if (tabs.isEmpty()) return TABS_X;
		return tabs.getLast().getRight() + TABS_GAP;
	}

	/**
	 * Selects {@code tab} and deselects all others.
	 * Override to react to selection changes (e.g. to load option lists).
	 */
	public void selectTab(@NonNull Tab tab) {
		tabs.forEach(t -> t.selected = false);
		tab.selected = true;
	}

	// -------------------------------------------------------------------------
	// Tab widget
	// -------------------------------------------------------------------------

	public static class Tab extends Button {

		static final int PADDING_HORIZONTAL = 8;
		static final int PADDING_VERTICAL = 4;

		private final @NonNull TabsScreen parent;
		boolean selected;

		protected Tab(int x, int y, @NonNull Component message, @NonNull TabsScreen parent) {
			super(x, y,
					MINECRAFT.font.width(message) + PADDING_HORIZONTAL * 2,
					MINECRAFT.font.lineHeight + PADDING_VERTICAL * 2,
					message, btn -> {
					}, DEFAULT_NARRATION);
			this.parent = parent;
		}

		// Allow SettingsScreen.addConfigTab to reposition after the x is known
		void repositionX(int x) {
			this.setX(x);
		}

		@Override
		public void onPress(@NonNull InputWithModifiers input) {
			parent.selectTab(this);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
			int color = selected || isHovered() ? Colors.WHITE : Colors.GRAY;

			graphics.text(MINECRAFT.font, getMessage(),
					getX() + PADDING_HORIZONTAL, getY() + PADDING_VERTICAL, color, true);

			graphics.verticalLine(getX(), getY(), getBottom(), color); // left
			graphics.horizontalLine(getX(), getRight() - 1, getY(), color);    // top
			graphics.verticalLine(getRight() - 1, getY(), getBottom(), color); // right

			if (selected) {
				// Fade the bottom border outward on both sides so the selected tab
				// visually merges with the content area below it.
				graphics.guiRenderState.addGuiElement(
						new HorizontalGradientRenderState(
								RenderPipelines.GUI, TextureSetup.noTexture(),
								new Matrix3x2f(graphics.pose()),
								getRight() - 1, getBottom(), getRight() + 200, getBottom() + 1,
								Colors.WHITE, ARGB.color(0, Colors.WHITE),
								graphics.scissorStack.peek()
						)
				);
				graphics.guiRenderState.addGuiElement(
						new HorizontalGradientRenderState(
								RenderPipelines.GUI, TextureSetup.noTexture(),
								new Matrix3x2f(graphics.pose()),
								getX() - 200, getBottom(), getX() + 1, getBottom() + 1,
								ARGB.color(0, Colors.WHITE), Colors.WHITE,
								graphics.scissorStack.peek()
						)
				);
			}
		}
	}
}
