package me.Azz_9.screenshot_overhaul.client.gui;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.mixin.AbstractWidgetAccessor;

/**
 * Renders a single-line tooltip as a filled rectangle with text, positioned
 * above a reference widget and clamped so it never escapes the screen bounds.
 *
 * <h3>Positioning rules</h3>
 * <ol>
 *   <li>Preferred position: centred horizontally over {@code anchorWidget},
 *       placed just above it with a small gap.</li>
 *   <li>If that would clip the top edge of the screen, the tooltip is placed
 *       below the widget instead.</li>
 *   <li>Horizontal position is clamped so the box stays within
 *       {@code [0, screenWidth]}.</li>
 * </ol>
 *
 * <p>This class has no state — all methods are static.</p>
 */
public final class TooltipRenderer {

	private static final int PADDING_H = 4;
	private static final int PADDING_V = 3;
	private static final int GAP = 2;   // gap between tooltip and anchor widget
	private static final int BG_COLOR = Colors.BLACK_SEMI_TRANSPARENT;
	private static final int TEXT_COLOR = Colors.WHITE;
	private static final int BORDER_COLOR = Colors.GRAY;

	private TooltipRenderer() {
	}

	/**
	 * Renders a tooltip above (or below) {@code anchor} if the mouse is
	 * currently hovering over it.
	 *
	 * @param graphics     the current graphics context
	 * @param anchor       the widget to attach the tooltip to
	 * @param text         the text to display
	 * @param mouseX       current mouse X
	 * @param mouseY       current mouse Y
	 * @param rectangle    box that the tooltip should not overflow
	 */
	public static void renderIfHovered(
			@NonNull GuiGraphicsExtractor graphics,
			@NonNull AbstractWidget anchor,
			@NonNull String text,
			int mouseX, int mouseY,
			@NonNull ScreenRectangle rectangle
	) {
		if (!((AbstractWidgetAccessor) anchor).invokeAreCoordinatesInRectangle(mouseX, mouseY)) return;
		render(graphics, anchor, text, rectangle);
	}

	/**
	 * Renders a tooltip above (or below) {@code anchor} unconditionally.
	 * Use this when you have already determined the tooltip should be shown.
	 */
	public static void render(
			@NonNull GuiGraphicsExtractor graphics,
			@NonNull AbstractWidget anchor,
			@NonNull String text,
			@NonNull ScreenRectangle rectangle
	) {
		if (text.isBlank()) return;

		int textW = MINECRAFT.font.width(text);
		int textH = MINECRAFT.font.lineHeight;
		int boxW = textW + PADDING_H * 2;
		int boxH = textH + PADDING_V * 2;

		// Preferred: centred above the anchor
		int boxX = anchor.getX() + (anchor.getWidth() - boxW) / 2;
		int boxY = anchor.getY() - boxH - GAP;

		// Flip below if it would clip the top
		if (boxY < 0) {
			boxY = anchor.getBottom() + GAP;
		}

		// Clamp horizontally within screen bounds
		boxX = Mth.clamp(boxX, rectangle.left(), rectangle.right() - boxW);

		// Clamp vertically — last resort if both above and below would clip
		boxY = Mth.clamp(boxY, rectangle.top(), rectangle.bottom() - boxH);

		// Background + border
		graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, BG_COLOR);
		graphics.outline(boxX, boxY, boxW, boxH, BORDER_COLOR);

		// Text
		graphics.text(
				MINECRAFT.font, text,
				boxX + PADDING_H, boxY + PADDING_V,
				TEXT_COLOR, false);
	}
}