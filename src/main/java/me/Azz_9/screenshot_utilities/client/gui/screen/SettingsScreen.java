package me.Azz_9.screenshot_utilities.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends TabsScreen implements FocusableScreen {

	// focus manager
	private final @NonNull FocusManager focusManager = new FocusManager();

	protected SettingsScreen(final Screen parent) {
		super(Component.translatable("screenshot_utilities.settings"), parent);
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	@Override
	protected void init() {
		super.init();

		selectTab(addTab(Component.literal("General")));
		addTab(Component.literal("Screenshot"));
		addTab(Component.literal("Truc"));
		addTab(Component.literal("Tab"));
	}
}
