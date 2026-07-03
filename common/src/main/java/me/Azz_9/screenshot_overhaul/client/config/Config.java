package me.Azz_9.screenshot_overhaul.client.config;

import static me.Azz_9.screenshot_overhaul.CommonClass.PHOTO_MODE_ENABLED;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.config.option.options.*;
import me.Azz_9.screenshot_overhaul.client.gui.components.config.ConfigTabContent;
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.ScrollableGallery;
import me.Azz_9.screenshot_overhaul.client.gui.screen.SettingsScreen;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotFileNameParser;
import me.Azz_9.screenshot_overhaul.compat.CompatManager;
import me.Azz_9.screenshot_overhaul.utils.PathUtils;

public final class Config {
	private static final @NonNull Config INSTANCE = new Config();

	public final @NonNull ConfigObject<Boolean> enableWholeMod = ConfigObject.nonNull(true, "screenshot_overhaul.config.enable_whole_mod", Boolean.class);

	public final @NonNull ConfigObject<Boolean> freezeInPhotoMode = ConfigObject.nonNull(true, "screenshot_overhaul.config.freeze_in_photo_mode", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showPlayer = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_player", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showNametags = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_nametags", Boolean.class);

	public final @NonNull ConfigObject<Boolean> showScreenshotsOnXaerosWorldMap = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_screenshots_on_xaeros_world_map", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showScreenshotsOnJourneyMap = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_screenshots_on_journey_map", Boolean.class);

	public final @NonNull ConfigObject<Path> screenshotsDir = ConfigObject.withApplier(Path.of("screenshots"), "screenshot_overhaul.config.screenshots_dir", Path.class, path -> path != null ? PathUtils.toStoredPath(path) : Path.of("screenshots"));
	public final @NonNull ConfigObject<String> screenshotsFileName = ConfigObject.withApplier("<datetime>", "screenshot_overhaul.config.screenshots_file_name", String.class, pattern -> ScreenshotFileNameParser.validate(pattern) ? pattern : "<datetime>");
	public final @NonNull ConfigObject<Boolean> showChatMessage = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_chat_message", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showPreview = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_screenshot_preview", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideChatOnScreenshot = ConfigObject.nonNull(false, "screenshot_overhaul.config.hide_chat_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideHudOnScreenshot = ConfigObject.nonNull(false, "screenshot_overhaul.config.hide_hud_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideHandOnScreenshot = ConfigObject.nonNull(false, "screenshot_overhaul.config.hide_hand_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> grabScreenshotOnAdvancement = ConfigObject.nonNull(false, "screenshot_overhaul.config.grab_on_advancement", Boolean.class);
	public final @NonNull ConfigObject<Integer> advancementScreenshotDelay = ConfigObject.withApplier(20, "screenshot_overhaul.config.advancement_screenshot_delay", Integer.class, integer -> integer != null ? Mth.clamp(integer, 0, 100) : 20);

	public final @NonNull ConfigObject<Integer> panoramaResolution = ConfigObject.withApplier(1024, "screenshot_overhaul.config.panorama_resolution", Integer.class, integer -> integer != null ? Mth.clamp(integer, 256, 4096) : 1024);
	// TODO peut être faire en sorte que cette option soit juste un raccourci pour minecraft.gameRenderer.getGameRenderState().optionsRenderState.panoramaSpeed qui est déjà une option de base du jeu
	public final @NonNull ConfigObject<Integer> rotationSpeed = ConfigObject.withApplier(10, "screenshot_overhaul.config.rotation_speed", Integer.class, integer -> integer != null ? Mth.clamp(integer, 0, 100) : 10);
	public final @NonNull ConfigObject<RotationDirection> rotationDirection = ConfigObject.nonNull(RotationDirection.TO_LEFT, "screenshot_overhaul.config.rotation_direction", RotationDirection.class);
	public final @NonNull ConfigObject<Integer> verticalAngle = ConfigObject.withApplier(10, "screenshot_overhaul.config.vertical_angle", Integer.class, integer -> integer != null ? Mth.clamp(integer, -180, 180) : 10);
	public final @NonNull ConfigObject<Integer> startingHorizontalAngle = ConfigObject.withApplier(0, "screenshot_overhaul.config.starting_horizontal_angle", Integer.class, integer -> integer != null ? Mth.clamp(integer, 0, 360) : 0);

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

	public @NonNull Screen getSettingsScreen(@Nullable Screen currentScreen) {
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

		screen.addConfigTab(Component.translatable("screenshot_overhaul.config.category.general"), generalContent);

		// Screenshot
		StringConfigOption screenshotsFileName = StringConfigOption.builder(Config.getInstance().screenshotsFileName)
				.maxLength(64)
				.tooltip(Component.translatable("screenshot_overhaul.config.screenshots_file_name.tooltip"))
				.validate(ScreenshotFileNameParser::validate, Component.translatable("screenshot_overhaul.config.screenshots_file_name.invalid"))
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
		IntSliderConfigOption advancementScreenshotDelay = IntSliderConfigOption.builder(Config.getInstance().advancementScreenshotDelay, 0, 100).build();

		ConfigTabContent screenshotContent = ConfigTabContent.builder()
				.option(screenshotsFileName)
				.option(screenshotsDir)
				.option(showChatMessage)
				.option(showPreview)
				.option(hideHudOnScreenshot)
				.option(hideChatOnScreenshot)
				.option(hideHandOnScreenshot)
				.option(grabScreenshotOnAdvancement)
				.option(advancementScreenshotDelay)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_overhaul.config.category.screenshot"), screenshotContent);

		// panorama
		IntSliderConfigOption panoramaSize = IntSliderConfigOption.builder(Config.getInstance().panoramaResolution, 256, 4096)
				.tooltip(integer -> {
					if (integer > 2048) {
						return Component.translatable("screenshot_overhaul.config.panorama_resolution.warning_high");
					}
					return null;
				})
				.build();
		IntSliderConfigOption rotationSpeed = IntSliderConfigOption.builder(Config.getInstance().rotationSpeed, 0, 100).buildLive();
		EnumConfigOption<RotationDirection> rotationDirection = EnumConfigOption.builder(Config.getInstance().rotationDirection, RotationDirection.class)
				.valueName(RotationDirection::getText)
				.buildLive();
		IntSliderConfigOption verticalAngle = IntSliderConfigOption.builder(Config.getInstance().verticalAngle, -180, 180).buildLive();
		IntSliderConfigOption startingHorizontalAngle = IntSliderConfigOption.builder(Config.getInstance().startingHorizontalAngle, 0, 360).buildLive();

		ConfigTabContent panoramaContent = ConfigTabContent.builder()
				.option(panoramaSize)
				.option(rotationSpeed)
				.option(rotationDirection)
				.option(verticalAngle)
				.option(startingHorizontalAngle)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_overhaul.config.category.panorama"), panoramaContent);

		if (PHOTO_MODE_ENABLED) {
			// Photo mode
			BooleanConfigOption freezeInPhotoMode = BooleanConfigOption.builder(Config.getInstance().freezeInPhotoMode).build();
			BooleanConfigOption showPlayer = BooleanConfigOption.builder(Config.getInstance().showPlayer).build();
			BooleanConfigOption showNametags = BooleanConfigOption.builder(Config.getInstance().showNametags).build();

			ConfigTabContent photoModeContent = ConfigTabContent.builder()
					.option(freezeInPhotoMode)
					.option(showPlayer)
					.option(showNametags)
					.build();

			screen.addConfigTab(Component.translatable("screenshot_overhaul.config.category.photo_mode"), photoModeContent);
		}

		return screen;
	}

	public enum RotationDirection {
		TO_LEFT("screenshot_overhaul.config.rotation_direction.to_left"),
		TO_RIGHT("screenshot_overhaul.config.rotation_direction.to_right");

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
