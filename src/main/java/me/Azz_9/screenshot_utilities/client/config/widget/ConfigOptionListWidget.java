package me.Azz_9.screenshot_utilities.client.config.widget;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;

/**
 * A scrollable list that renders all entries ({@link ConfigTabContent.Entry}) for one tab.
 *
 * <p>Smooth scrolling is achieved by interpolating {@link #scrollOffset} toward
 * {@link #targetScrollOffset} every frame. The widget clips its contents with a
 * scissor rect so nothing bleeds outside the list bounds.</p>
 *
 * <p>The list has a fixed maximum content width ({@value #MAX_CONTENT_WIDTH}) and
 * is always horizontally centred inside its parent screen.</p>
 */
@Environment(EnvType.CLIENT)
public final class ConfigOptionListWidget extends AbstractWidget {

	// -------------------------------------------------------------------------
	// Layout
	// -------------------------------------------------------------------------

	private static final int MAX_CONTENT_WIDTH = 560;
	private static final int SECTION_HEIGHT = 18;
	private static final int SECTION_PADDING_V = 6;
	private static final int FULL_SECTION_HEIGHT = SECTION_HEIGHT + SECTION_PADDING_V * 2;
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int SCROLLBAR_PADDING = 3;

	// -------------------------------------------------------------------------
	// Scrolling
	// -------------------------------------------------------------------------

	private static final double SCROLL_STEP = 20.0;
	private static final double SCROLL_SMOOTHING = 0.12; // lerp factor per frame

	private double scrollOffset = 0.0;
	private double targetScrollOffset = 0.0;

	// -------------------------------------------------------------------------
	// Content
	// -------------------------------------------------------------------------

	private final @NonNull List<ConfigOptionWidget<?>> optionWidgets = new ArrayList<>();
	private final @NonNull List<RenderedEntry> renderedEntries = new ArrayList<>();

	/**
	 * Union of widget + its pre-computed Y in content space.
	 */
	private record RenderedEntry(int contentY, int height, @Nullable ConfigOptionWidget<?> widget,
	                             @Nullable Component sectionTitle) {
	}

	private int totalContentHeight = 0;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	public ConfigOptionListWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.empty());
	}

	// -------------------------------------------------------------------------
	// Content loading
	// -------------------------------------------------------------------------

	/**
	 * Replaces all current entries with the content of the given tab.
	 * Resets scroll to top.
	 */
	public void loadContent(@NonNull ConfigTabContent content) {
		optionWidgets.clear();
		renderedEntries.clear();
		scrollOffset = 0;
		targetScrollOffset = 0;

		int contentWidth = Math.min(getWidth() - SCROLLBAR_WIDTH - SCROLLBAR_PADDING * 2, MAX_CONTENT_WIDTH);
		int contentX = getX() + (getWidth() - contentWidth) / 2;

		int cursor = 0;

		for (ConfigTabContent.Entry entry : content.getEntries()) {
			switch (entry) {
				case ConfigTabContent.Entry.SectionHeader header -> {
					renderedEntries.add(new RenderedEntry(cursor, FULL_SECTION_HEIGHT, null, header.title()));
					cursor += FULL_SECTION_HEIGHT;
				}
				case ConfigTabContent.Entry.OptionEntry optEntry -> {
					ConfigOptionWidget<?> widget = ConfigOptionWidgetFactory.create(
							contentX, 0, contentWidth, optEntry.option());
					optionWidgets.add(widget);
					renderedEntries.add(new RenderedEntry(cursor, ConfigOptionWidget.FULL_ROW_HEIGHT, widget, null));
					cursor += ConfigOptionWidget.FULL_ROW_HEIGHT;
				}
				default -> throw new IllegalStateException("Unknown entry type: " + entry);
			}
		}

		totalContentHeight = cursor;
	}

	// -------------------------------------------------------------------------
	// Rendering
	// -------------------------------------------------------------------------


	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		// Smooth scroll interpolation
		scrollOffset += (targetScrollOffset - scrollOffset) * SCROLL_SMOOTHING;

		int listTop = getY();
		int listBottom = getBottom();
		int listLeft = getX();
		int listRight = getRight();

		// Scissor — clip to list bounds
		graphics.enableScissor(listLeft, listTop, listRight, listBottom);

		int baseY = listTop - (int) scrollOffset;

		for (RenderedEntry entry : renderedEntries) {
			int entryTop = baseY + entry.contentY();
			int entryBottom = entryTop + entry.height();

			// Skip fully off-screen entries
			if (entryBottom < listTop || entryTop > listBottom) continue;

			if (entry.sectionTitle() != null) {
				renderSectionHeader(graphics, entry.sectionTitle(), entryTop);
			} else if (entry.widget() != null) {
				entry.widget().setY(entryTop);
				entry.widget().extractRenderState(graphics, mouseX, mouseY, deltaTicks);
			}
		}

		graphics.disableScissor();

		// Scrollbar
		renderScrollbar(graphics);
	}

	private void renderSectionHeader(@NonNull GuiGraphicsExtractor graphics, @NonNull Component title, int y) {
		int contentWidth = getContentWidth();
		int contentX = getX() + (getWidth() - contentWidth) / 2;
		int textY = y + SECTION_PADDING_V + (SECTION_HEIGHT - MINECRAFT.font.lineHeight) / 2;

		// Separator line + centred title
		int lineY = y + SECTION_PADDING_V + SECTION_HEIGHT / 2;
		int textWidth = MINECRAFT.font.width(title);
		int textX = getX() + (getWidth() - textWidth) / 2;

		graphics.fill(contentX, lineY, textX - 4, lineY + 1, Colors.GRAY);
		graphics.fill(textX + textWidth + 4, lineY, contentX + contentWidth, lineY + 1, Colors.GRAY);
		graphics.text(MINECRAFT.font, title, textX, textY, Colors.LIGHT_GRAY, true);
	}

	private void renderScrollbar(@NonNull GuiGraphicsExtractor graphics) {
		if (totalContentHeight <= getHeight()) return;

		int trackX = getRight() - SCROLLBAR_WIDTH - SCROLLBAR_PADDING;
		int trackTop = getY();
		int trackHeight = getHeight();

		double ratio = (double) getHeight() / totalContentHeight;
		int thumbH = Math.max(20, (int) (trackHeight * ratio));
		int thumbTop = trackTop + (int) ((trackHeight - thumbH) * (scrollOffset / maxScroll()));

		// Track
		graphics.fill(trackX, trackTop, trackX + SCROLLBAR_WIDTH, trackTop + trackHeight,
				Colors.DARK_GRAY);
		// Thumb
		graphics.fill(trackX, thumbTop, trackX + SCROLLBAR_WIDTH, thumbTop + thumbH,
				Colors.GRAY);
	}

	// -------------------------------------------------------------------------
	// Input forwarding
	// -------------------------------------------------------------------------


	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!isMouseOver(event.x(), event.y())) return false;

		int baseY = getY() - (int) scrollOffset;
		for (RenderedEntry entry : renderedEntries) {
			if (entry.widget() == null) continue;
			entry.widget().setY(baseY + entry.contentY());
			if (entry.widget().mouseClicked(event, doubleClick)) return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		int baseY = getY() - (int) scrollOffset;
		for (RenderedEntry entry : renderedEntries) {
			if (entry.widget() == null) continue;
			entry.widget().setY(baseY + entry.contentY());
			if (entry.widget().mouseReleased(event)) return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
		int baseY = getY() - (int) scrollOffset;
		for (RenderedEntry entry : renderedEntries) {
			if (entry.widget() == null) continue;
			entry.widget().setY(baseY + entry.contentY());
			if (entry.widget().mouseDragged(event, dx, dy)) return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!isMouseOver(mouseX, mouseY)) return false;
		targetScrollOffset = Mth.clamp(targetScrollOffset - verticalAmount * SCROLL_STEP, 0, maxScroll());
		return true;
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		for (RenderedEntry entry : renderedEntries) {
			if (entry.widget() != null && entry.widget().keyPressed(event)) return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(@NonNull CharacterEvent event) {
		for (RenderedEntry entry : renderedEntries) {
			if (entry.widget() != null && entry.widget().charTyped(event)) return true;
		}
		return false;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Returns all option widgets currently in the list.
	 */
	public @NonNull List<ConfigOptionWidget<?>> getOptionWidgets() {
		return Collections.unmodifiableList(optionWidgets);
	}

	/**
	 * Returns {@code true} if every option in the list has a valid working value.
	 */
	public boolean isAllValid() {
		return optionWidgets.stream()
				.allMatch(w -> w.getLastValidation().isValid());
	}

	/**
	 * Returns {@code true} if at least one option in the list has changed.
	 */
	public boolean hasAnyChanged() {
		return optionWidgets.stream()
				.anyMatch(w -> w.getOption().hasChanged());
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private double maxScroll() {
		return Math.max(0, totalContentHeight - getHeight());
	}

	private int getContentWidth() {
		return Math.min(getWidth() - SCROLLBAR_WIDTH - SCROLLBAR_PADDING * 2, MAX_CONTENT_WIDTH);
	}

	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		// Delegate to focused child if any
	}
}
