package me.Azz_9.screenshot_overhaul.client.gui.focusSystem;

import net.minecraft.client.gui.components.events.GuiEventListener;

import org.jetbrains.annotations.Nullable;

public final class FocusManager {

	private @Nullable GuiEventListener globalFocused;

	public void requestFocus(@Nullable final GuiEventListener newFocus) {
		if (globalFocused == newFocus) return;
		if (globalFocused != null) globalFocused.setFocused(false);
		if (newFocus != null) newFocus.setFocused(true);
		globalFocused = newFocus;
	}

	public void clearFocus() {
		requestFocus(null);
	}

	public @Nullable GuiEventListener getGlobalFocused() {
		return globalFocused;
	}
}

