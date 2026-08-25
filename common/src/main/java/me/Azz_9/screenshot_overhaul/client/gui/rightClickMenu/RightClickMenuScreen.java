package me.Azz_9.screenshot_overhaul.client.gui.rightClickMenu;

import net.minecraft.client.gui.components.Renderable;

import org.jetbrains.annotations.NotNull;

public interface RightClickMenuScreen extends Renderable {

	RightClickMenu getRightClickMenu();

	default void addRightClickMenuItem(@NotNull RightClickMenu.MenuItem menuItem) {
		getRightClickMenu().addMenuItem(menuItem);
	}

	default void clearRightClickMenuItems() {
		getRightClickMenu().clearMenuItems();
	}

	default void hideRightClickMenu() {
		getRightClickMenu().hide();
	}

	default void showRightClickMenu(double mouseX, double mouseY) {
		getRightClickMenu().show(mouseX, mouseY);
	}


}
