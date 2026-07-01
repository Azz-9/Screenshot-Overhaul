package me.Azz_9.screenshot_overhaul.client.gui.focusSystem;

import net.minecraft.client.gui.components.events.GuiEventListener;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface FocusableScreen {

	default void requestFocus(@Nullable final GuiEventListener widget) {
		getFocusManager().requestFocus(widget);
	}

	default void clearFocus() {
		getFocusManager().clearFocus();
	}

	default @Nullable GuiEventListener getGlobalFocused() {
		return getFocusManager().getGlobalFocused();
	}

	@NonNull FocusManager getFocusManager();
}