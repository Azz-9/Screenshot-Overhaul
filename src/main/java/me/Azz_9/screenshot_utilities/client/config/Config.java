package me.Azz_9.screenshot_utilities.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.option.options.*;
import me.Azz_9.screenshot_utilities.client.gui.components.config.ConfigTabContent;
import me.Azz_9.screenshot_utilities.client.gui.components.gallery.ScrollableGallery;
import me.Azz_9.screenshot_utilities.client.gui.screen.SettingsScreen;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotFileNameParser;
import me.Azz_9.screenshot_utilities.compat.CompatManager;
import me.Azz_9.screenshot_utilities.utils.PathUtils;

public final class Config {
	private static final @NonNull Config INSTANCE = new Config();

	public final @NonNull ConfigObject<Boolean> enableWholeMod = new ConfigObject<>(true, "screenshot_utilities.config.enable_whole_mod", Boolean.class);

	public final @NonNull ConfigObject<Boolean> freezeInPhotoMode = new ConfigObject<>(true, "screenshot_utilities.config.freeze_in_photo_mode", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showPlayer = new ConfigObject<>(true, "screenshot_utilities.config.show_player", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showNametags = new ConfigObject<>(true, "screenshot_utilities.config.show_nametags", Boolean.class);

	public final @NonNull ConfigObject<Boolean> showScreenshotsOnXaerosWorldMap = new ConfigObject<>(true, "screenshot_utilities.config.show_screenshots_on_xaeros_world_map", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showScreenshotsOnJourneyMap = new ConfigObject<>(true, "screenshot_utilities.config.show_screenshots_on_journey_map", Boolean.class);

	public final @NonNull ConfigObject<Path> screenshotsDir = new ConfigObject<>(Path.of("screenshots"), "screenshot_utilities.config.screenshots_dir", Path.class);
	public final @NonNull ConfigObject<String> screenshotsFileName = new ConfigObject<>("<datetime>", "screenshot_utilities.config.screenshots_file_name", String.class);
	public final @NonNull ConfigObject<Boolean> showChatMessage = new ConfigObject<>(true, "screenshot_utilities.config.show_chat_message", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showPreview = new ConfigObject<>(true, "screenshot_utilities.config.show_screenshot_preview", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideChatOnScreenshot = new ConfigObject<>(false, "screenshot_utilities.config.hide_chat_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideHudOnScreenshot = new ConfigObject<>(false, "screenshot_utilities.config.hide_hud_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideHandOnScreenshot = new ConfigObject<>(false, "screenshot_utilities.config.hide_hand_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> grabScreenshotOnAdvancement = new ConfigObject<>(false, "screenshot_utilities.config.grab_on_advancement", Boolean.class);
	public final @NonNull ConfigObject<Integer> advancementScreenshotDelay = new ConfigObject<>(20, "screenshot_utilities.config.advancement_screenshot_delay", Integer.class);

	// TODO peut être faire en sorte que cette option soit juste un raccourci pour minecraft.gameRenderer.getGameRenderState().optionsRenderState.panoramaSpeed qui est déjà une option de base du jeu
	public final @NonNull ConfigObject<Integer> rotationSpeed = new ConfigObject<>(10, "screenshot_utilities.config.rotation_speed", Integer.class);
	public final @NonNull ConfigObject<RotationDirection> rotationDirection = new ConfigObject<>(RotationDirection.TO_LEFT, "screenshot_utilities.config.rotation_direction", RotationDirection.class);
	public final @NonNull ConfigObject<Integer> verticalAngle = new ConfigObject<>(10, "screenshot_utilities.config.vertical_angle", Integer.class);
	public final @NonNull ConfigObject<Integer> startingHorizontalAngle = new ConfigObject<>(0, "screenshot_utilities.config.starting_horizontal_angle", Integer.class);

	public final @NonNull SavableObject<ScrollableGallery.SortMode> screenshotSortOrder = SavableObject.nonNull(ScrollableGallery.SortMode.DATE_DESC, ScrollableGallery.SortMode.class);
	public final @NonNull SavableObject<ScrollableGallery.SortMode> panoramaSortOrder = SavableObject.nonNull(ScrollableGallery.SortMode.DATE_DESC, ScrollableGallery.SortMode.class);

	public final @NonNull SavableObject<UUID> selectedPanoramaUUID = SavableObject.nullable(null, UUID.class);

	public static @NonNull Config getInstance() {
		return INSTANCE;
	}

	public @NonNull Path getAbsoluteScreenshotsDir() {
		Path absolute = PathUtils.toAbsolutePath(screenshotsDir.getValue());
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

		BooleanConfigOption showScreenshotsOnJourneyMap = BooleanConfigOption.builder(Config.getInstance().showScreenshotsOnJourneyMap)
				.dependsOn(CompatManager::journeyMapPresent)
				.build();

		ConfigTabContent generalContent = ConfigTabContent.builder()
				.option(enableWholeMod)
				.option(showScreenshotsOnXaerosWorldMap)
				.option(showScreenshotsOnJourneyMap)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_utilities.config.category.general"), generalContent);

		// Screenshot
		StringConfigOption screenshotsFileName = StringConfigOption.builder(Config.getInstance().screenshotsFileName)
				.maxLength(64)
				.tooltip(Component.translatable("screenshot_utilities.config.screenshots_file_name.tooltip"))
				.validate(ScreenshotFileNameParser::validate, Component.translatable("screenshot_utilities.config.screenshots_file_name.invalid"))
				.build();
		PathConfigOption screenshotsDir = PathConfigOption.builder(Config.getInstance().screenshotsDir)
				.selectionMode(PathConfigOption.SelectionMode.DIRECTORIES_ONLY)
				.build();
		BooleanConfigOption showChatMessage = BooleanConfigOption.builder(Config.getInstance().showChatMessage).build();
		BooleanConfigOption showPreview = BooleanConfigOption.builder(Config.getInstance().showPreview).build();
		BooleanConfigOption hideHudOnScreenshot = BooleanConfigOption.builder(Config.getInstance().hideHudOnScreenshot).build();
		BooleanConfigOption hideChatOnScreenshot = BooleanConfigOption.builder(Config.getInstance().hideChatOnScreenshot)
				.dependsOn(() -> !hideHudOnScreenshot.getWorkingValue())
				.build();
		BooleanConfigOption hideHandOnScreenshot = BooleanConfigOption.builder(Config.getInstance().hideHandOnScreenshot).build();
		BooleanConfigOption grabScreenshotOnAdvancement = BooleanConfigOption.builder(Config.getInstance().grabScreenshotOnAdvancement).build();

		ConfigTabContent screenshotContent = ConfigTabContent.builder()
				.option(screenshotsFileName)
				.option(screenshotsDir)
				.option(showChatMessage)
				.option(showPreview)
				.option(hideHudOnScreenshot)
				.option(hideChatOnScreenshot)
				.option(hideHandOnScreenshot)
				.option(grabScreenshotOnAdvancement)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_utilities.config.category.screenshot"), screenshotContent);

		// panorama
		IntSliderConfigOption rotationSpeed = IntSliderConfigOption.builder(Config.getInstance().rotationSpeed, 0, 100).buildLive();
		EnumConfigOption<RotationDirection> rotationDirection = EnumConfigOption.builder(Config.getInstance().rotationDirection, RotationDirection.class)
				.valueName(RotationDirection::getText)
				.buildLive();
		IntSliderConfigOption verticalAngle = IntSliderConfigOption.builder(Config.getInstance().verticalAngle, -180, 180).buildLive();
		IntSliderConfigOption startingHorizontalAngle = IntSliderConfigOption.builder(Config.getInstance().startingHorizontalAngle, 0, 360).buildLive();

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
