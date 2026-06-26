package me.Azz_9.screenshot_utilities.client.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_utilities.client.Colors;

/**
 * Abstract base widget providing smooth delta-time scroll, scissored rendering,
 * and unified event routing for fixed + scrollable children.
 *
 * <h3>Usage</h3>
 * <ol>
 *   <li>Extend this class instead of {@link SimpleParentWidget}.</li>
 *   <li>Register fixed children via {@link #addFixedChild(AbstractWidget)} —
 *       they are rendered and receive events regardless of scroll position.</li>
 *   <li>Register scrollable children via {@link #addScrollableChild(AbstractWidget)} —
 *       they are translated by the current scroll offset and only receive events
 *       when the cursor is inside the scroll area.</li>
 *   <li>Implement {@link #getTotalScrollableHeight()} to tell the widget how tall
 *       the scrollable content is.</li>
 *   <li>Optionally override {@link #getScrollArea()} to restrict scroll rendering
 *       and hit-testing to a sub-region (e.g. below a fixed header). Defaults to
 *       the full widget bounds.</li>
 * </ol>
 *
 * <h3>Scroll behavior</h3>
 * Smooth scroll uses an exponential lerp driven by real elapsed time
 * ({@code 1 - exp(-SMOOTHING * dt)}), so it feels identical regardless of
 * frame rate. {@link #mouseScrolled} updates {@code targetScrollOffset};
 * the actual {@code scrollOffset} catches up each frame in
 * {@link #extractWidgetRenderState(GuiGraphicsExtractor, int, int, float)}.
 *
 * <h3>Scissor</h3>
 * The scroll area is scissored during the scrollable-children render pass so
 * content never bleeds outside the widget bounds (or the custom scroll area).
 */
@Environment(EnvType.CLIENT)
public abstract class SmoothScrollableWidget extends SimpleParentWidget {

	// Scroll constants
	private static final double SCROLL_SPEED = 20.0;
	private static final double SMOOTHING = 25.0;
	private static final double SNAP_DISTANCE = 0.5;

	// Scrollbar appearance
	private static final int SCROLLBAR_WIDTH = 4;
	private static final int SCROLLBAR_PADDING = 3;
	private static final int MIN_SCROLLBAR_HEIGHT = 16;

	// Scroll state
	private double scrollOffset = 0.0;
	private double targetScrollOffset = 0.0;
	private long lastUpdateNanos = System.nanoTime();

	// Scrollbar drag state
	private boolean draggingScrollbar = false;
	private double dragThumbOffsetY = 0.0;

	// Children — split into fixed (never translated) and scrollable (translated)
	private final List<AbstractWidget> fixedChildren = new ArrayList<>();
	private final List<AbstractWidget> scrollableChildren = new ArrayList<>();

	private @Nullable AbstractWidget dragCapture = null;

	// Constructor
	protected SmoothScrollableWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
	}


	// Child registration

	/**
	 * Registers a widget as a fixed child.
	 * Fixed children are rendered first, without any scroll translation, and
	 * receive all events unconditionally (modulo their own {@code isMouseOver}).
	 */
	protected void addFixedChild(@NonNull AbstractWidget widget) {
		fixedChildren.add(widget);
		addRenderableChild(widget);
	}

	/**
	 * Registers a widget as a scrollable child.
	 * The widget's Y position is expected to be in <em>content-space</em>
	 * (i.e. 0 = top of the scrollable area). This class translates it to
	 * screen-space each frame using {@link SimpleParentWidget#setY}, which
	 * propagates to all descendants automatically.
	 *
	 * <p>The widget is also added to the standard {@link SimpleParentWidget}
	 * children list so Minecraft's focus / narration systems still work.</p>
	 */
	protected void addScrollableChild(@NonNull AbstractWidget widget) {
		scrollableChildren.add(widget);
		addRenderableChild(widget);
	}

	/**
	 * Removes all scrollable children and resets scroll to zero.
	 * Fixed children are left untouched.
	 */
	protected void clearScrollableChildren() {
		scrollableChildren.forEach(widget -> children().remove(widget));
		scrollableChildren.clear();
		resetScroll();
	}


	// Abstract / overridable contract

	/**
	 * Returns the total pixel height of all scrollable content.
	 * Used to compute {@link #maxScroll()} and the scrollbar thumb size.
	 */
	protected abstract int getTotalScrollableHeight();

	/**
	 * Returns the region of this widget that scrolls and is scissored.
	 *
	 * <p>Override to restrict scroll rendering to a sub-area, e.g. below a
	 * fixed header:</p>
	 * <pre>{@code
	 * protected ScrollArea getScrollArea() {
	 *     return new ScrollArea(getX(), getY() + HEADER_HEIGHT, getRight(), getBottom());
	 * }
	 * }</pre>
	 * <p>
	 * Defaults to the full widget bounds.
	 */
	@NonNull
	protected ScrollArea getScrollArea() {
		return new ScrollArea(getX(), getY(), getRight(), getBottom());
	}


	// Rendering
	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		updateScroll();

		// Fixed children — rendered without translation, no scissor
		for (AbstractWidget widget : fixedChildren) {
			widget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}

		renderScrollableArea(graphics, mouseX, mouseY, deltaTicks);
		renderScrollbar(graphics, mouseX, mouseY);
	}

	private void renderScrollableArea(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		ScrollArea area = getScrollArea();

		graphics.enableScissor(area.left(), area.top(), area.right(), area.bottom());

		// Translate every scrollable child to its current screen-space Y,
		// render it, then restore the content-space Y.
		int areaTop = area.top();

		for (AbstractWidget widget : scrollableChildren) {
			int contentY = widget.getY(); // content-space offset
			int screenY = areaTop - (int) scrollOffset + contentY;
			int bottom = screenY + widget.getHeight();

			// Skip fully off-screen widgets (optimization — scissor still clips)
			if (bottom < area.top() || screenY > area.bottom()) {
				widget.setY(contentY); // keep content-space Y intact
				continue;
			}

			widget.setY(screenY);
			onBeforeRenderScrollableChild(graphics, widget, screenY, mouseX, mouseY, deltaTicks);
			widget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
			widget.setY(contentY); // restore content-space Y after render
		}

		graphics.disableScissor();
	}

	private void renderScrollbar(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		ScrollArea area = getScrollArea();
		int areaHeight = area.bottom() - area.top();

		if (getTotalScrollableHeight() <= areaHeight) return;

		ScrollbarGeometry sb = computeScrollbar();

		int trackColor = Colors.DARK_GRAY;
		int thumbColor = draggingScrollbar ? Colors.WHITE : Colors.GRAY;

		graphics.fill(sb.trackX(), area.top(), sb.trackX() + SCROLLBAR_WIDTH, area.bottom(), trackColor);

		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(0, sb.thumbTop());
		graphics.fill(sb.trackX(), 0, sb.trackX() + SCROLLBAR_WIDTH, sb.thumbHeight(), thumbColor);
		matrices.popMatrix();

		if (sb.containsTrack(mouseX, mouseY)) {
			graphics.requestCursor(draggingScrollbar ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
		}
	}

	/**
	 * Returns the scrollbar geometry (track X, thumb top, thumb height) derived
	 * from the current scroll state. Used both for rendering and hit-testing.
	 */
	private @NonNull ScrollbarGeometry computeScrollbar() {
		ScrollArea area = getScrollArea();
		int areaHeight = area.height();
		double ratio = (double) areaHeight / getTotalScrollableHeight();
		int thumbH = Math.max(MIN_SCROLLBAR_HEIGHT, (int) (areaHeight * ratio));
		double max = maxScroll();
		float thumbTop = area.top() + (max > 0 ? (float) ((areaHeight - thumbH) * (scrollOffset / max)) : 0);
		int trackX = area.right() - SCROLLBAR_WIDTH - SCROLLBAR_PADDING;
		int trackY = area.top();
		return new ScrollbarGeometry(trackX, trackY, areaHeight, thumbTop, thumbH);
	}

	private record ScrollbarGeometry(int trackX, int trackY, int trackHeight, float thumbTop, int thumbHeight) {
		boolean containsThumb(double x, double y) {
			return x >= trackX && x < trackX + SCROLLBAR_WIDTH && y >= thumbTop && y < thumbTop + thumbHeight;
		}

		boolean containsTrack(double x, double y) {
			return x >= trackX && x < trackX + SCROLLBAR_WIDTH && y >= trackY && y < trackY + trackHeight;
		}
	}


	// Input routing
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!isMouseOver(click.x(), click.y())) return false;

		// Scrollbar — check before forwarding to children so the track area
		// doesn't accidentally activate a scrollable child underneath.
		if (getTotalScrollableHeight() > getScrollArea().height()) {
			ScrollbarGeometry sb = computeScrollbar();

			if (sb.containsThumb(click.x(), click.y())) {
				// Start drag: record offset within the thumb so it doesn't jump
				draggingScrollbar = true;
				dragThumbOffsetY = click.y() - sb.thumbTop();
				return true;
			}

			if (sb.containsTrack(click.x(), click.y())) {
				// Click on the track outside the thumb — jump to that position
				ScrollArea area = getScrollArea();
				int areaH = area.height();
				int thumbH = sb.thumbHeight();
				double clickPos = click.y() - area.top() - thumbH / 2.0;
				double newScroll = (clickPos / (areaH - thumbH)) * maxScroll();
				targetScrollOffset = Mth.clamp(newScroll, 0, maxScroll());
				return true;
			}
		}

		// Fixed children receive the event unconditionally
		for (AbstractWidget widget : fixedChildren) {
			if (widget.mouseClicked(click, doubled)) return true;
		}

		// Scrollable children only if the click is inside the scroll area
		ScrollArea area = getScrollArea();
		if (!area.contains(click.x(), click.y())) return true; // consume, don't forward

		for (AbstractWidget widget : scrollableChildren) {
			// Temporarily move to screen-space so isMouseOver works correctly
			int contentY = widget.getY();
			int screenY = area.top() - (int) scrollOffset + contentY;
			widget.setY(screenY);
			boolean handled = widget.mouseClicked(click, doubled);
			widget.setY(contentY);
			if (handled) {
				dragCapture = widget;
				return true;
			}
		}

		return true;
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent click) {
		draggingScrollbar = false;
		dragCapture = null;
		for (AbstractWidget w : fixedChildren) w.mouseReleased(click);
		for (AbstractWidget w : scrollableChildren) w.mouseReleased(click);
		return true;
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent click, double dx, double dy) {
		// Scrollbar drag takes absolute priority
		if (draggingScrollbar) {
			ScrollArea area = getScrollArea();
			int areaH = area.height();
			int thumbH = computeScrollbar().thumbHeight();
			double thumbTop = click.y() - dragThumbOffsetY;
			double ratio = (thumbTop - area.top()) / (double) (areaH - thumbH);
			targetScrollOffset = Mth.clamp(ratio * maxScroll(), 0, maxScroll());
			// Snap immediately — dragging should feel direct, not smooth
			scrollOffset = targetScrollOffset;
			return true;
		}

		if (dragCapture != null) {
			return dragCapture.mouseDragged(click, dx, dy);
		}

		for (AbstractWidget w : fixedChildren) {
			if (w.mouseDragged(click, dx, dy)) return true;
		}
		ScrollArea area = getScrollArea();
		if (!area.contains(click.x(), click.y())) return false;
		for (AbstractWidget w : scrollableChildren) {
			if (w.mouseDragged(click, dx, dy)) return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		ScrollArea area = getScrollArea();
		if (!area.contains(x, y)) return false;
		targetScrollOffset = Mth.clamp(targetScrollOffset - scrollY * SCROLL_SPEED, 0, maxScroll());
		return true;
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		for (AbstractWidget widget : fixedChildren) {
			if (widget.keyPressed(event)) return true;
		}
		for (AbstractWidget widget : scrollableChildren) {
			if (widget.keyPressed(event)) return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(@NonNull CharacterEvent event) {
		for (AbstractWidget widget : fixedChildren) {
			if (widget.charTyped(event)) return true;
		}
		for (AbstractWidget widget : scrollableChildren) {
			if (widget.charTyped(event)) return true;
		}
		return false;
	}


	// Scroll helpers
	private void updateScroll() {
		long now = System.nanoTime();
		double dt = (now - lastUpdateNanos) / 1_000_000_000.0;
		lastUpdateNanos = now;

		double alpha = 1.0 - Math.exp(-SMOOTHING * dt);
		scrollOffset += (targetScrollOffset - scrollOffset) * alpha;

		if (Math.abs(targetScrollOffset - scrollOffset) < SNAP_DISTANCE) {
			scrollOffset = targetScrollOffset;
		}
		scrollOffset = Mth.clamp(scrollOffset, 0.0, maxScroll());
	}

	/**
	 * Resets both scroll values to zero.
	 */
	protected void resetScroll() {
		scrollOffset = 0.0;
		targetScrollOffset = 0.0;
	}

	/**
	 * Clamps the current scroll if content shrinks (e.g. after a filter removes entries).
	 * Call this after any operation that may reduce {@link #getTotalScrollableHeight()}.
	 */
	protected void clampScroll() {
		double max = maxScroll();
		targetScrollOffset = Mth.clamp(targetScrollOffset, 0, max);
		scrollOffset = Mth.clamp(scrollOffset, 0, max);
	}

	private double maxScroll() {
		ScrollArea area = getScrollArea();
		return Math.max(0, getTotalScrollableHeight() - (area.bottom() - area.top()));
	}

	/**
	 * Called just before a scrollable child is rendered, after its Y has been
	 * translated to screen-space. Override to hook in per-child logic such as
	 * texture preloading.
	 *
	 * <p>Default implementation is a no-op.</p>
	 *
	 * @param widget  the child about to be rendered
	 * @param screenY the child's current screen-space Y coordinate
	 */
	protected void onBeforeRenderScrollableChild(
			@NonNull GuiGraphicsExtractor graphics,
			@NonNull AbstractWidget widget,
			int screenY,
			int mouseX, int mouseY, float deltaTicks
	) {
	}

	/**
	 * Exposes the current (interpolated) scroll offset for subclasses that need it.
	 */
	protected double getScrollOffset() {
		return scrollOffset;
	}

	protected void setScrollOffset(double offset) {
		targetScrollOffset = offset;
		scrollOffset = offset;
		clampScroll();
	}


	// ScrollArea record

	/**
	 * Axis-aligned rectangle describing the scrollable + scissored area.
	 *
	 * @param left   inclusive left edge in screen pixels
	 * @param top    inclusive top edge in screen pixels
	 * @param right  exclusive right edge in screen pixels
	 * @param bottom exclusive bottom edge in screen pixels
	 */
	public record ScrollArea(int left, int top, int right, int bottom) {

		public boolean contains(double x, double y) {
			return x >= left && x < right && y >= top && y < bottom;
		}

		public int width() {
			return right - left;
		}

		public int height() {
			return bottom - top;
		}
	}
}