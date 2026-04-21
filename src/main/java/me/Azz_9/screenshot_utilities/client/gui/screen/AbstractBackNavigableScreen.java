package me.Azz_9.screenshot_utilities.client.gui.screen;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractBackNavigableScreen extends Screen {
	protected final Screen PARENT;

	protected AbstractBackNavigableScreen(Component title, Screen parent) {
		super(title);
		this.PARENT = parent;
	}

	@Override
	public void onClose() {
		if (PARENT != null) {
			minecraft.setScreen(PARENT);
		} else {
			super.onClose();
		}
	}
}
