package me.Azz_9.screenshot_overhaul.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import me.Azz_9.screenshot_overhaul.client.config.AddonConfigRegistry;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.config.ConfigLoader;
import me.Azz_9.screenshot_overhaul.client.gui.components.config.ConfigTabContent;
import me.Azz_9.screenshot_overhaul.platform.Services;

public class ScreenshotOverhaulApi {

	private ScreenshotOverhaulApi() {
	}

	/** Register a tab in the main config screen. */
	public static void registerConfigTab(@NonNull Component label,
										 @NonNull Function<SettingsContext, ConfigTabContent> contentSupplier) {
		registerConfigTab(label, contentSupplier, null);
	}

	/**
	 * @param onSave Callback called immediately after the core has saved
	 *                 its own config.json, use it to persist your own
	 *                 add-on configuration file.
	 */
	public static void registerConfigTab(@NonNull Component label,
										 @NonNull Function<SettingsContext, ConfigTabContent> contentSupplier,
										 @Nullable Runnable onSave) {
		AddonConfigRegistry.register(label, contentSupplier, onSave);
	}

	public static @NonNull Path getConfigDirectory() {
		return Services.PLATFORM.getConfigDir();
	}

	public static void saveAddonConfig(@NonNull Object configHolder, @NonNull Path file) {
		ConfigLoader.trySave(configHolder, file);
	}

	public static void loadAddonConfig(@NonNull Object configHolder, @NonNull Path file) throws IOException {
		ConfigLoader.load(configHolder, file);
	}

	public static @NonNull Screen getSettingsScreen(@Nullable Screen parent) {
		return Config.getInstance().getSettingsScreen(parent);
	}
}
