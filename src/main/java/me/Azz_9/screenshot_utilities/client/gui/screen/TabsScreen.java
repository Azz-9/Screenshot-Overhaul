package me.Azz_9.screenshot_utilities.client.gui.screen;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.renderState.HorizontalGradientRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class TabsScreen extends AbstractBackNavigableScreen {

	private static final int TABS_X = 10;
	private static final int TABS_Y = 10;
	private static final int TABS_GAP = 4;
	private final List<Tab> tabs = new ArrayList<>();

	protected TabsScreen(Text title, Screen parent) {
		super(title, parent);
	}

	@Override
	protected void init() {
		tabs.clear();
	}

	public void addTab(Tab tab) {
		tabs.add(tab);

		addDrawableChild(tab);
	}

	public void addTab(Text tabText) {
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

	public static class Tab extends ButtonWidget {

		private static final int PADDING_HORIZONTAL = 8;
		private static final int PADDING_VERTICAL = 4;

		private final TabsScreen parent;

		private boolean selected;

		protected Tab(int x, int y, net.minecraft.text.Text message, TabsScreen parent) {
			super(x, y,
					CLIENT.textRenderer.getWidth(message) + PADDING_HORIZONTAL * 2,
					CLIENT.textRenderer.fontHeight + PADDING_VERTICAL * 2,
					message, (btn) -> {
					}, DEFAULT_NARRATION_SUPPLIER);
			this.parent = parent;
		}


		@Override
		public void onPress(AbstractInput input) {
			parent.selectTab(this);
		}

		@Override
		protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			int color = selected || isHovered() ? Colors.WHITE : Colors.GRAY;
			context.drawText(CLIENT.textRenderer, getMessage(), getX() + PADDING_HORIZONTAL, getY() + PADDING_VERTICAL, color, true);

			context.drawVerticalLine(getX(), getY(), getBottom(), color); // left line
			context.drawHorizontalLine(getX(), getRight() - 1, getY(), color); // top line
			context.drawVerticalLine(getRight() - 1, getY(), getBottom(), color); // right line

			if (selected) {
				context.state.addSimpleElement(
						new HorizontalGradientRenderState(
								RenderPipelines.GUI, TextureSetup.empty(),
								new Matrix3x2f(context.getMatrices()), getRight() - 1,
								getBottom(), getRight() + 200, getBottom() + 1,
								Colors.WHITE, ColorHelper.withAlpha(0, Colors.WHITE),
								context.scissorStack.peekLast()
						)
				);

				context.state.addSimpleElement(
						new HorizontalGradientRenderState(
								RenderPipelines.GUI, TextureSetup.empty(),
								new Matrix3x2f(context.getMatrices()), getX() - 200,
								getBottom(), getX() + 1, getBottom() + 1,
								ColorHelper.withAlpha(0, Colors.WHITE), Colors.WHITE,
								context.scissorStack.peekLast()
						)
				);
			}
		}
	}
}
