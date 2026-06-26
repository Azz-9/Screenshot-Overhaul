package me.Azz_9.screenshot_utilities.client.gui.screen;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBackNavigableScreen extends Screen {
	protected final @Nullable Screen PARENT;

	protected AbstractBackNavigableScreen(@NonNull final Component title, @Nullable final Screen parent) {
		super(title);
		this.PARENT = parent;
	}

	public AbstractBackNavigableScreen(Component title) {
		this(title, null);
	}

	@Override
	public void onClose() {
		if (PARENT != null) {
			minecraft.gui.setScreen(PARENT);
		} else {
			super.onClose();
		}
	}
}
