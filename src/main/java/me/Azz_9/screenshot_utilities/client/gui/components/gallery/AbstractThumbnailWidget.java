package me.Azz_9.screenshot_utilities.client.gui.components.gallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

public abstract class AbstractThumbnailWidget extends AbstractWidget {

	public AbstractThumbnailWidget(int x, int y, int width, int height, @NonNull Component message) {
		super(x, y, width, height, message);
	}

	public void load() {
	}

	// rendering

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		handleCursor(graphics);
	}

	@Override
	protected void handleCursor(@NonNull GuiGraphicsExtractor graphics) {
		if (this.isHovered() && this.shouldTakeFocusAfterInteraction()) {
			graphics.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	// input

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		if (MINECRAFT.screen instanceof FocusableScreen screen) {
			screen.clearFocus();
		}
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}
}
