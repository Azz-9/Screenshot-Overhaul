package me.Azz_9.screenshot_utilities.client.gui.focusSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface FocusableScreen {

	default void requestFocus(@Nullable Element widget) {
		getFocusManager().requestFocus(widget);
	}

	default void clearFocus() {
		getFocusManager().clearFocus();
	}

	@NonNull FocusManager getFocusManager();
}