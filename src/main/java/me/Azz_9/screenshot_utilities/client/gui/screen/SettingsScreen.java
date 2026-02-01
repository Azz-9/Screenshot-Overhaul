package me.Azz_9.screenshot_utilities.client.gui.screen;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jspecify.annotations.NonNull;

public class SettingsScreen extends TabsScreen implements FocusableScreen {

	// focus manager
	private final @NonNull FocusManager focusManager = new FocusManager();

	protected SettingsScreen(Screen parent) {
		super(Text.translatable("screenshot_utilities.settings"), parent);
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	@Override
	protected void init() {
		super.init();

		addTab(Text.of("General"));
		addTab(Text.of("Screenshot"));
		addTab(Text.of("Truc"));
		addTab(Text.of("Tab"));
	}
}
