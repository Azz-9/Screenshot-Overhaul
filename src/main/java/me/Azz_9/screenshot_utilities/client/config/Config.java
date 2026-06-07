package me.Azz_9.screenshot_utilities.client.config;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.option.options.BooleanConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.options.PathConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.options.StringConfigOption;
import me.Azz_9.screenshot_utilities.client.gui.screen.SettingsScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.config.ConfigTabContent;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshotGallery.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotFileNameParser;
import me.Azz_9.screenshot_utilities.compat.CompatManager;

public class Config {
	private static final @NonNull Config INSTANCE = new Config();

	public final @NonNull ConfigObject<Boolean> enableWholeMod = new ConfigObject<>(true, "screenshot_utilities.config.enable_whole_mod");

	public final @NonNull ConfigObject<Boolean> freezeInPhotoMode = new ConfigObject<>(true, "screenshot_utilities.config.freeze_in_photo_mode");
	public final @NonNull ConfigObject<Boolean> showPlayer = new ConfigObject<>(true, "screenshot_utilities.config.show_player");
	public final @NonNull ConfigObject<Boolean> showNametags = new ConfigObject<>(true, "screenshot_utilities.config.show_nametags");

	public final @NonNull ConfigObject<Boolean> showScreenshotsOnXaerosWorldMap = new ConfigObject<>(true, "screenshot_utilities.config.show_screenshots_on_xaeros_world_map");

	public final @NonNull ConfigObject<Path> screenshotsDir = new ConfigObject<>(Path.of("screenshots"), "screenshot_utilities.config.screenshots_dir");
	public final @NonNull ConfigObject<String> screenshotsFileName = new ConfigObject<>("<datetime>", "screenshot_utilities.config.screenshots_file_name");
	public final @NonNull ConfigObject<Boolean> showChatMessage = new ConfigObject<>(true, "screenshot_utilities.config.show_chat_message");
	public final @NonNull SavableObject<ScreenshotGalleryWidget.SortMode> sortOrder = new SavableObject<>(ScreenshotGalleryWidget.SortMode.DATE_DESC);

	public static @NonNull Config getInstance() {
		return INSTANCE;
	}

	public @NonNull Path getAbsoluteScreenshotsDir() {
		Path absolute = MINECRAFT.gameDirectory.toPath().resolve(screenshotsDir.getValue());
		if (!Files.exists(absolute)) {
			try {
				Files.createDirectories(absolute);
			} catch (IOException e) {
				ScreenshotLogger.error("Could not create screenshotsDir: {}, error: {}", screenshotsDir.getValue(), e.getMessage());
			}
		}

		return absolute;
	}

	public Screen getSettingsScreen(Screen currentScreen) {
		SettingsScreen screen = new SettingsScreen(currentScreen);

		// General
		BooleanConfigOption enableWholeMod = BooleanConfigOption.builder(Config.getInstance().enableWholeMod).build();
		BooleanConfigOption showScreenshotsOnXaerosWorldMap = BooleanConfigOption.builder(Config.getInstance().showScreenshotsOnXaerosWorldMap)
				.dependsOn(CompatManager::xaerosWorldMapPresent)
				.build();

		ConfigTabContent generalContent = ConfigTabContent.builder()
				.option(enableWholeMod)
				.option(showScreenshotsOnXaerosWorldMap)
				.build();

		screen.addConfigTab(Component.literal("General"), generalContent);

		// Screenshot
		PathConfigOption screenshotsDir = PathConfigOption.builder(Config.getInstance().screenshotsDir).build();
		StringConfigOption screenshotsFileName = StringConfigOption.builder(Config.getInstance().screenshotsFileName)
				.maxLength(64)
				.tooltip(Component.translatable("screenshot_utilities.config.screenshots_file_name.tooltip"))
				.validate(ScreenshotFileNameParser::validate, Component.translatable("screenshot_utilities.config.screenshots_file_name.invalid"))
				.build();
		BooleanConfigOption showChatMessage = BooleanConfigOption.builder(Config.getInstance().showChatMessage).build();

		ConfigTabContent screenshotContent = ConfigTabContent.builder()
				.option(screenshotsDir)
				.option(screenshotsFileName)
				.option(showChatMessage)
				.build();

		//TODO trad
		screen.addConfigTab(Component.literal("Screenshot"), screenshotContent);

		// Photo mode
		BooleanConfigOption freezeInPhotoMode = BooleanConfigOption.builder(Config.getInstance().freezeInPhotoMode).build();
		BooleanConfigOption showPlayer = BooleanConfigOption.builder(Config.getInstance().showPlayer).build();
		BooleanConfigOption showNametags = BooleanConfigOption.builder(Config.getInstance().showNametags).build();

		ConfigTabContent photoModeContent = ConfigTabContent.builder()
				.option(freezeInPhotoMode)
				.option(showPlayer)
				.option(showNametags)
				.build();

		screen.addConfigTab(Component.literal("Photo mode"), photoModeContent);

		return screen;
	}
}
