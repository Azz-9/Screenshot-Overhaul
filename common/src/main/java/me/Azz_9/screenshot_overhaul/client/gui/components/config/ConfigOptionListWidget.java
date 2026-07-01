package me.Azz_9.screenshot_overhaul.client.gui.components.config;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_overhaul.client.gui.components.SmoothScrollableWidget;

/**
 * Scrollable list of {@link ConfigOptionWidget} rows and section headers.
 * <p>
 * Now extends {@link SmoothScrollableWidget} — scroll, scissor, and event
 * routing are handled by the base class. This widget only needs to manage
 * content layout and loading.
 * <p>
 * Section headers are rendered as fixed overlays during the base-class render
 * pass via {@link #extractWidgetRenderState(GuiGraphicsExtractor, int, int, float)}
 * override, after the scrollable children have been drawn.
 */
public final class ConfigOptionListWidget extends SmoothScrollableWidget {

	// Layout
	private static final int MAX_CONTENT_WIDTH = 560;
	private static final int SECTION_HEIGHT = 18;
	private static final int SECTION_PADDING_V = 6;
	private static final int FULL_SECTION_HEIGHT = SECTION_HEIGHT + SECTION_PADDING_V * 2;

	// Content
	private final @NonNull List<ConfigOptionWidget<?>> optionWidgets = new ArrayList<>();
	private final @NonNull List<SectionEntry> sectionEntries = new ArrayList<>();
	private int totalContentHeight = 0;

	/**
	 * Union of widget + its pre-computed Y in content space.
	 */
	private record SectionEntry(int contentY, @NonNull Component title) {
	}

	// Constructor
	public ConfigOptionListWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	// Content loading

	/**
	 * Replaces all current entries with the content of the given tab.
	 * Resets scroll to top.
	 */
	public void loadContent(@NonNull ConfigTabContent content) {
		clearScrollableChildren();
		optionWidgets.clear();
		sectionEntries.clear();
		totalContentHeight = 0;

		int contentWidth = contentWidth();
		int contentX = getX() + (getWidth() - contentWidth) / 2;
		int cursor = 0;

		for (ConfigTabContent.Entry entry : content.getEntries()) {
			switch (entry) {
				case ConfigTabContent.Entry.SectionHeader header -> {
					sectionEntries.add(new SectionEntry(cursor, header.title()));
					cursor += FULL_SECTION_HEIGHT;
				}
				case ConfigTabContent.Entry.OptionEntry optEntry -> {
					ConfigOptionWidget<?> widget = ConfigOptionWidgetFactory.create(
							contentX, cursor, contentWidth, optEntry.option());
					optionWidgets.add(widget);
					addScrollableChild(widget);
					cursor += ConfigOptionWidget.FULL_ROW_HEIGHT;
				}
				default -> throw new IllegalStateException("Unknown entry type: " + entry);
			}
		}

		totalContentHeight = cursor;
	}

	@Override
	protected int getTotalScrollableHeight() {
		return totalContentHeight;
	}

	// Rendering
	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		// Base class renders fixed children (none here) + scrollable option rows
		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

		// Section headers must be drawn inside the scissored area too
		graphics.enableScissor(getX(), getY(), getRight(), getBottom());

		int baseY = getY() - (int) getScrollOffset();
		for (SectionEntry section : sectionEntries) {
			int y = baseY + section.contentY();
			int bottom = y + FULL_SECTION_HEIGHT;
			if (bottom < getY() || y > getBottom()) continue;
			renderSectionHeader(graphics, section.title(), y);
		}

		graphics.disableScissor();
	}

	private void renderSectionHeader(@NonNull GuiGraphicsExtractor graphics, @NonNull Component title, int y) {
		int cw = contentWidth();
		int contentX = getX() + (getWidth() - cw) / 2;
		int textY = y + SECTION_PADDING_V + (SECTION_HEIGHT - MINECRAFT.font.lineHeight) / 2;
		int lineY = y + SECTION_PADDING_V + SECTION_HEIGHT / 2;
		int textW = MINECRAFT.font.width(title);
		int textX = getX() + (getWidth() - textW) / 2;

		graphics.fill(contentX, lineY, textX - 4, lineY + 1, Colors.GRAY);
		graphics.fill(textX + textW + 4, lineY, contentX + cw, lineY + 1, Colors.GRAY);
		graphics.text(MINECRAFT.font, title, textX, textY, Colors.LIGHT_GRAY, true);
	}

	// Public API

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
		return optionWidgets.stream().allMatch(w -> w.getLastValidation().isValid());
	}

	/**
	 * Returns {@code true} if at least one option in the list has changed.
	 */
	public boolean hasAnyChanged() {
		return optionWidgets.stream().anyMatch(w -> w.getOption().hasChanged());
	}

	// Helpers
	private int contentWidth() {
		return Math.min(getWidth(), MAX_CONTENT_WIDTH);
	}

	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}
}
