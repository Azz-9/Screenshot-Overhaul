package me.Azz_9.screenshot_utilities.client.gui.widget.settings;

import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jspecify.annotations.NonNull;

public class SettingsScreen extends Screen implements FocusableScreen {

	// focus manager
	private final @NonNull FocusManager focusManager = new FocusManager();

	protected SettingsScreen(Text title) {
		super(title);
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}
}
