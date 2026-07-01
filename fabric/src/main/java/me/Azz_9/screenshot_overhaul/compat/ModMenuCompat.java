package me.Azz_9.screenshot_overhaul.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.Azz_9.screenshot_overhaul.client.config.Config;

public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> Config.getInstance().getSettingsScreen(parent);
	}
}