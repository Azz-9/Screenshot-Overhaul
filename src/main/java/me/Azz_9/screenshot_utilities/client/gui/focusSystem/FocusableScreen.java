package me.Azz_9.screenshot_utilities.client.gui.focusSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface FocusableScreen {

	default void requestFocus(@Nullable final GuiEventListener widget) {
		getFocusManager().requestFocus(widget);
	}

	default void clearFocus() {
		getFocusManager().clearFocus();
	}

	@NonNull FocusManager getFocusManager();
}