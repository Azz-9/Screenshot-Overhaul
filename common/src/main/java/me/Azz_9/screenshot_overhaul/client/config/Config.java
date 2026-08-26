package me.Azz_9.screenshot_overhaul.client.config;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotFileNameParser.MAX_FILE_STEM_LENGTH;

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
import me.Azz_9.screenshot_overhaul.api.SettingsContext;
import me.Azz_9.screenshot_overhaul.client.config.option.options.*;
import me.Azz_9.screenshot_overhaul.client.gui.components.config.ConfigTabContent;
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.ScrollableGallery;
import me.Azz_9.screenshot_overhaul.client.gui.screen.SettingsScreen;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotFileNameParser;
import me.Azz_9.screenshot_overhaul.compat.CompatManager;
import me.Azz_9.screenshot_overhaul.utils.PathUtils;

public final class Config {
	public static final int MIN_WINDOW_SIZE = 1;
	public static final int MAX_WINDOW_SIZE = 16_384;

	private static final @NonNull Config INSTANCE = new Config();

	public final @NonNull ConfigObject<Boolean> enableWholeMod = ConfigObject.nonNull(true, "screenshot_overhaul.config.enable_whole_mod", Boolean.class);

	public final @NonNull ConfigObject<Boolean> showScreenshotsOnXaerosWorldMap = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_screenshots_on_xaeros_world_map", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showScreenshotsOnJourneyMap = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_screenshots_on_journey_map", Boolean.class);

	public final @NonNull ConfigObject<Path> screenshotsDir = ConfigObject.withApplier(Path.of("screenshots"), "screenshot_overhaul.config.screenshots_dir", Path.class, path -> path != null ? PathUtils.toStoredPath(path) : Path.of("screenshots"));
	public final @NonNull ConfigObject<String> screenshotsFileName = ConfigObject.withApplier("<datetime>", "screenshot_overhaul.config.screenshots_file_name", String.class, pattern -> ScreenshotFileNameParser.validate(pattern) ? pattern : "<datetime>");
	public final @NonNull ConfigObject<Boolean> useCustomResolution = ConfigObject.nonNull(false, "screenshot_overhaul.config.use_custom_resolution", Boolean.class);
	public final @NonNull ConfigObject<Resolution2D> screenshotResolution = ConfigObject.withApplier(Resolution2D.QHD(), "screenshot_overhaul.config.resolution", Resolution2D.class, this::applyScreenshotResolution);
	public final @NonNull ConfigObject<Boolean> showChatMessage = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_chat_message", Boolean.class);
	public final @NonNull ConfigObject<Boolean> showPreview = ConfigObject.nonNull(true, "screenshot_overhaul.config.show_screenshot_preview", Boolean.class);
	public final @NonNull ConfigObject<PreviewPlacement> previewPlacement = ConfigObject.nonNull(PreviewPlacement.BOTTOM_RIGHT, "screenshot_overhaul.config.preview_placement", PreviewPlacement.class);
	public final @NonNull ConfigObject<Boolean> screenshotSound = ConfigObject.nonNull(false, "screenshot_overhaul.config.screenshot_sound", Boolean.class);
	public final @NonNull ConfigObject<Boolean> screenshotFlash = ConfigObject.nonNull(false, "screenshot_overhaul.config.screenshot_flash", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideChatOnScreenshot = ConfigObject.nonNull(false, "screenshot_overhaul.config.hide_chat_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideHudOnScreenshot = ConfigObject.nonNull(false, "screenshot_overhaul.config.hide_hud_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> hideHandOnScreenshot = ConfigObject.nonNull(false, "screenshot_overhaul.config.hide_hand_on_screenshot", Boolean.class);
	public final @NonNull ConfigObject<Boolean> grabScreenshotOnAdvancement = ConfigObject.nonNull(false, "screenshot_overhaul.config.grab_on_advancement", Boolean.class);
	public final @NonNull ConfigObject<Integer> advancementScreenshotDelay = ConfigObject.withApplier(20, "screenshot_overhaul.config.advancement_screenshot_delay", Integer.class, integer -> integer != null ? Mth.clamp(integer, 0, 100) : 20);

	public final @NonNull ConfigObject<String> panoramaFolderName = ConfigObject.withApplier("panorama_<datetime>", "screenshot_overhaul.config.panorama_folder_name", String.class, pattern -> ScreenshotFileNameParser.validate(pattern) ? pattern : "panorama_<datetime>");
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

	public Resolution2D applyScreenshotResolution(@Nullable Resolution2D resolution) {
		if (resolution == null) {
			return Resolution2D.QHD();
		}

		if (resolution.width() < MIN_WINDOW_SIZE || resolution.width() > MAX_WINDOW_SIZE || resolution.height() < MIN_WINDOW_SIZE || resolution.height() > MAX_WINDOW_SIZE) {
			return new Resolution2D(Math.clamp(resolution.width(), MIN_WINDOW_SIZE, MAX_WINDOW_SIZE), Math.clamp(resolution.height(), MIN_WINDOW_SIZE, MAX_WINDOW_SIZE));
		}

		return resolution;
	}

	public boolean allowCustomResolution() {
		return useCustomResolution.getValue() && MINECRAFT.level != null;
	}

	public @NonNull Screen getSettingsScreen(@Nullable Screen currentScreen) {
		SettingsScreen screen = new SettingsScreen(currentScreen);

		// General
		BooleanConfigOption enableWholeMod = BooleanConfigOption.builder(getInstance().enableWholeMod).build();
		BooleanConfigOption showScreenshotsOnXaerosWorldMap = BooleanConfigOption.builder(getInstance().showScreenshotsOnXaerosWorldMap)
				.dependsOn(CompatManager::xaerosWorldMapPresent)
				.dependsOn(enableWholeMod)
				.build();

		BooleanConfigOption showScreenshotsOnJourneyMap = BooleanConfigOption.builder(getInstance().showScreenshotsOnJourneyMap)
				.dependsOn(CompatManager::journeyMapPresent)
				.dependsOn(enableWholeMod)
				.build();

		ConfigTabContent generalContent = ConfigTabContent.builder()
				.option(enableWholeMod)
				.option(showScreenshotsOnXaerosWorldMap)
				.option(showScreenshotsOnJourneyMap)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_overhaul.config.category.general"), generalContent);

		// Screenshot
		StringConfigOption screenshotsFileName = StringConfigOption.builder(getInstance().screenshotsFileName)
				.maxLength(MAX_FILE_STEM_LENGTH)
				.tooltip(Component.translatable("screenshot_overhaul.config.screenshots_file_name.tooltip"))
				.validate(ScreenshotFileNameParser::validate, Component.translatable("screenshot_overhaul.config.screenshots_file_name.invalid"))
				.dependsOn(enableWholeMod)
				.build();
		PathConfigOption screenshotsDir = PathConfigOption.builder(getInstance().screenshotsDir)
				.selectionMode(PathConfigOption.SelectionMode.DIRECTORIES_ONLY)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption useCustomResolution = BooleanConfigOption.builder(getInstance().useCustomResolution)
				.dependsOn(enableWholeMod)
				.build();
		ResolutionConfigOption screenshotResolution = ResolutionConfigOption.builder(getInstance().screenshotResolution)
				.addPreset(Resolution2D.fullHD(), Component.literal("Full HD"))
				.addPreset(Resolution2D.QHD(), Component.literal("QHD"))
				.addPreset(Resolution2D.UHD(), Component.literal("UHD"))
				.dependsOn(useCustomResolution)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption showChatMessage = BooleanConfigOption.builder(getInstance().showChatMessage)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption showPreview = BooleanConfigOption.builder(getInstance().showPreview)
				.dependsOn(enableWholeMod)
				.build();
		EnumConfigOption<PreviewPlacement> previewPlacement = EnumConfigOption.builder(getInstance().previewPlacement, PreviewPlacement.class)
				.valueName(PreviewPlacement::getText)
				.dependsOn(showPreview)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption screenshotSound = BooleanConfigOption.builder(getInstance().screenshotSound)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption screenshotFlash = BooleanConfigOption.builder(getInstance().screenshotFlash)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption hideHudOnScreenshot = BooleanConfigOption.builder(getInstance().hideHudOnScreenshot)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption hideChatOnScreenshot = BooleanConfigOption.builder(getInstance().hideChatOnScreenshot)
				.dependsOn(() -> !hideHudOnScreenshot.getWorkingValue())
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption hideHandOnScreenshot = BooleanConfigOption.builder(getInstance().hideHandOnScreenshot)
				.dependsOn(enableWholeMod)
				.build();
		BooleanConfigOption grabScreenshotOnAdvancement = BooleanConfigOption.builder(getInstance().grabScreenshotOnAdvancement)
				.dependsOn(enableWholeMod)
				.build();
		IntSliderConfigOption advancementScreenshotDelay = IntSliderConfigOption.builder(getInstance().advancementScreenshotDelay, 0, 100)
				.dependsOn(grabScreenshotOnAdvancement)
				.dependsOn(enableWholeMod)
				.build();

		ConfigTabContent screenshotContent = ConfigTabContent.builder()
				.option(screenshotsFileName)
				.option(screenshotsDir)
				.option(useCustomResolution)
				.option(screenshotResolution)
				.option(showChatMessage)
				.option(showPreview)
				.option(previewPlacement)
				.option(screenshotSound)
				.option(screenshotFlash)
				.section(Component.translatable("screenshot_overhaul.config.section.hidden_element"))
				.option(hideHudOnScreenshot)
				.option(hideChatOnScreenshot)
				.option(hideHandOnScreenshot)
				.section(Component.translatable("screenshot_overhaul.config.section.advancement"))
				.option(grabScreenshotOnAdvancement)
				.option(advancementScreenshotDelay)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_overhaul.config.category.screenshot"), screenshotContent);

		// panorama
		StringConfigOption panoramaFolderName = StringConfigOption.builder(getInstance().panoramaFolderName)
				.maxLength(MAX_FILE_STEM_LENGTH)
				.tooltip(Component.translatable("screenshot_overhaul.config.screenshots_file_name.tooltip"))
				.validate(ScreenshotFileNameParser::validate, Component.translatable("screenshot_overhaul.config.panorama_folder_name.invalid"))
				.dependsOn(enableWholeMod)
				.build();
		IntFieldConfigOption panoramaResolution = IntFieldConfigOption.builder(getInstance().panoramaResolution)
				.range(256, 4096)
				.tooltip(integer -> {
					if (integer > 2048) {
						return Component.translatable("screenshot_overhaul.config.panorama_resolution.warning_high");
					}
					return null;
				})
				.dependsOn(enableWholeMod)
				.build();
		IntSliderConfigOption rotationSpeed = IntSliderConfigOption.builder(getInstance().rotationSpeed, 0, 100)
				.dependsOn(enableWholeMod)
				.buildLive();
		EnumConfigOption<RotationDirection> rotationDirection = EnumConfigOption.builder(getInstance().rotationDirection, RotationDirection.class)
				.valueName(RotationDirection::getText)
				.dependsOn(enableWholeMod)
				.buildLive();
		IntSliderConfigOption verticalAngle = IntSliderConfigOption.builder(getInstance().verticalAngle, -180, 180)
				.dependsOn(enableWholeMod)
				.buildLive();
		IntSliderConfigOption startingHorizontalAngle = IntSliderConfigOption.builder(getInstance().startingHorizontalAngle, 0, 360)
				.dependsOn(enableWholeMod)
				.buildLive();

		ConfigTabContent panoramaContent = ConfigTabContent.builder()
				.option(panoramaFolderName)
				.option(panoramaResolution)
				.section(Component.translatable("screenshot_overhaul.config.section.panorama_animation"))
				.option(rotationSpeed)
				.option(rotationDirection)
				.option(verticalAngle)
				.option(startingHorizontalAngle)
				.build();

		screen.addConfigTab(Component.translatable("screenshot_overhaul.config.category.panorama"), panoramaContent);

		SettingsContext context = () -> enableWholeMod;
		for (AddonConfigRegistry.Entry entry : AddonConfigRegistry.getEntries()) {
			screen.addConfigTab(entry.label(), entry.contentSupplier().apply(context));
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

	public enum PreviewPlacement {
		TOP_LEFT("screenshot_overhaul.config.preview_placement.top_left"),
		TOP_RIGHT("screenshot_overhaul.config.preview_placement.top_right"),
		BOTTOM_RIGHT("screenshot_overhaul.config.preview_placement.bottom_right"),
		BOTTOM_LEFT("screenshot_overhaul.config.preview_placement.bottom_left");

		private final @NonNull String translationKey;

		PreviewPlacement(@NonNull String translationKey) {
			this.translationKey = translationKey;
		}

		public @NonNull String getTranslationKey() {
			return translationKey;
		}

		public @NonNull Component getText() {
			return Component.translatable(getTranslationKey());
		}
	}

	public record Resolution2D(int width, int height) {
		public static Resolution2D fullHD() {
			return new Resolution2D(1920, 1080);
		}

		public static Resolution2D QHD() {
			return new Resolution2D(2560, 1440);
		}

		public static Resolution2D UHD() {
			return new Resolution2D(3840, 2160);
		}

		@Override
		public @NonNull String toString() {
			return width + "x" + height;
		}
	}
}
