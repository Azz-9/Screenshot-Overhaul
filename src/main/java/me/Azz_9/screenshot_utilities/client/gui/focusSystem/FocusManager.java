package me.Azz_9.screenshot_utilities.client.gui.focusSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class FocusManager {

	private @Nullable GuiEventListener focused;

	public void requestFocus(@Nullable final GuiEventListener element) {
		if (focused == element) return;

		if (focused != null) {
			focused.setFocused(false);
		}

		focused = element;

		if (focused != null) {
			focused.setFocused(true);
		}
	}

	public void clearFocus() {
		requestFocus(null);
	}

	public @Nullable GuiEventListener getFocused() {
		return focused;
	}
}

