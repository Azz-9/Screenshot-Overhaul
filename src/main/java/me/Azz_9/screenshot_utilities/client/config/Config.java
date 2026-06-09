package me.Azz_9.screenshot_utilities.client.config;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.option.options.*;
import me.Azz_9.screenshot_utilities.client.gui.screen.SettingsScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.config.ConfigTabContent;
import me.Azz_9.screenshot_utilities.client.gui.widget.gallery.ScrollableGallery;
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
	public final @NonNull ConfigObject<Boolean> hideChatOnScreenshot = new ConfigObject<>(false, "screenshot_utilities.config.hide_chat_on_screenshot");
	public final @NonNull ConfigObject<Boolean> hideHudOnScreenshot = new ConfigObject<>(false, "screenshot_utilities.config.hide_hud_on_screenshot");
	public final @NonNull ConfigObject<Boolean> grabScreenshotOnAdvancement = new ConfigObject<>(false, "screenshot_utilities.config.grab_on_advancement");

	// TODO peut être faire en sorte que cette option soit juste un raccourci pour minecraft.gameRenderer.getGameRenderState().optionsRenderState.panoramaSpeed qui est déjà une option de base du jeu
	public final @NonNull ConfigObject<Integer> rotationSpeed = new ConfigObject<>(10, "screenshot_utilities.config.rotation_speed");
	public final @NonNull ConfigObject<RotationDirection> rotationDirection = new ConfigObject<>(RotationDirection.TO_LEFT, "screenshot_utilities.config.rotation_direction");
	public final @NonNull ConfigObject<Integer> verticalAngle = new ConfigObject<>(10, "screenshot_utilities.config.vertical_angle");
	public final @NonNull ConfigObject<Integer> startingHorizontalAngle = new ConfigObject<>(0, "screenshot_utilities.config.starting_horizontal_angle");

	public final @NonNull SavableObject<ScrollableGallery.SortMode> screenshotSortOrder = new SavableObject<>(ScrollableGallery.SortMode.DATE_DESC);
	public final @NonNull SavableObject<ScrollableGallery.SortMode> panoramaSortOrder = new SavableObject<>(ScrollableGallery.SortMode.DATE_DESC);

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

		screen.addConfigTab(Component.translatable("screenshot_utilities.config.category.general"), generalContent);

		// Screenshot
		PathConfigOption screenshotsDir = PathConfigOption.builder(Config.getInstance().screenshotsDir).build();
		StringConfigOption screenshotsFileName = StringConfigOption.builder(Config.getInstance().screenshotsFileName)
				.maxLength(64)
				.tooltip(Component.translatable("screenshot_utilities.config.screenshots_file_name.tooltip"))
				.validate(ScreenshotFileNameParser::validate, Component.translatable("screenshot_utilities.config.screenshots_file_name.invalid"))
				.build();
		BooleanConfigOption showChatMessage = BooleanConfigOption.builder(Config.getInstance().showChatMessage).build();
		BooleanConfigOption hideHudOnScreenshot = BooleanConfigOption.builder(Config.getInstance().hideHudOnScreenshot).build();
		BooleanConfigOption hideChatOnScreenshot = BooleanConfigOption.builder(Config.getInstance().hideChatOnScreenshot)
				.dependsOn(() -> !hideHudOnScreenshot.getWorkingValue())
				.build();
		BooleanConfigOption grabScreenshotOnAdvancement = BooleanConfigOption.builder(Config.getInstance().grabScreenshotOnAdvancement).build();

		ConfigTabContent screenshotContent = ConfigTabContent.builder()
				.option(screenshotsDir)
				.option(screenshotsFileName)
				.option(showChatMessage)
				.option(hideHudOnScreenshot)
				.option(hideChatOnScreenshot)
				.option(grabScreenshotOnAdvancement)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_utilities.config.category.screenshot"), screenshotContent);

		// panorama
		IntSliderConfigOption rotationSpeed = IntSliderConfigOption.builder(Config.getInstance().rotationSpeed, 0, 100).build();
		EnumConfigOption<RotationDirection> rotationDirection = EnumConfigOption.builder(Config.getInstance().rotationDirection, RotationDirection.class)
				.valueName(RotationDirection::getText)
				.build();
		IntSliderConfigOption verticalAngle = IntSliderConfigOption.builder(Config.getInstance().verticalAngle, -180, 180).build();
		IntSliderConfigOption startingHorizontalAngle = IntSliderConfigOption.builder(Config.getInstance().startingHorizontalAngle, 0, 360).build();

		ConfigTabContent panoramaContent = ConfigTabContent.builder()
				.option(rotationSpeed)
				.option(rotationDirection)
				.option(verticalAngle)
				.option(startingHorizontalAngle)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_utilities.config.category.panorama"), panoramaContent);

		// Photo mode
		BooleanConfigOption freezeInPhotoMode = BooleanConfigOption.builder(Config.getInstance().freezeInPhotoMode).build();
		BooleanConfigOption showPlayer = BooleanConfigOption.builder(Config.getInstance().showPlayer).build();
		BooleanConfigOption showNametags = BooleanConfigOption.builder(Config.getInstance().showNametags).build();

		ConfigTabContent photoModeContent = ConfigTabContent.builder()
				.option(freezeInPhotoMode)
				.option(showPlayer)
				.option(showNametags)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_utilities.config.category.photo_mode"), photoModeContent);

		return screen;
	}

	public enum RotationDirection {
		TO_LEFT("screenshot_utilities.config.rotation_direction.to_left"),
		TO_RIGHT("screenshot_utilities.config.rotation_direction.to_right");

		private final @NonNull String translationKey;

		RotationDirection(@NonNull String translationKey) {
			this.translationKey = translationKey;
		}

		public @NonNull String getTranslationKey() {
			return translationKey;
		}

		public @NonNull Component getText() {
			return Component.translatable(getTranslationKey());
		}
	}
}
