package me.Azz_9.screenshot_utilities.client.config.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

import me.Azz_9.screenshot_utilities.client.config.ConfigObject;
import me.Azz_9.screenshot_utilities.client.config.option.options.*;
import me.Azz_9.screenshot_utilities.client.gui.screen.SettingsScreen;
import me.Azz_9.screenshot_utilities.client.gui.widget.config.ConfigTabContent;

/**
 * Example showing how to build and open a SettingsScreen.
 * <p>
 * This class is for documentation purposes only — adapt it to wherever
 * you open the settings screen in your mod (key binding handler, pause menu, etc.).
 */
public final class SettingsScreenExample {

	// --- Your ConfigObjects (held somewhere persistent, e.g. a Config singleton) ---

	static final ConfigObject<Boolean> SHOW_PREVIEW = new ConfigObject<>(true, "screenshot_utilities.config.show_preview");

	static final ConfigObject<Integer> PREVIEW_DURATION = new ConfigObject<>(5, "screenshot_utilities.config.preview_duration");

	static final ConfigObject<Integer> PREVIEW_OPACITY = new ConfigObject<>(80, "screenshot_utilities.config.preview_opacity");

	static final ConfigObject<Path> SCREENSHOT_DIR = new ConfigObject<>(Path.of("screenshots"), "screenshot_utilities.config.screenshot_dir");

	static final ConfigObject<String> FILENAME_TEMPLATE = new ConfigObject<>("{date}_{time}", "screenshot_utilities.config.filename_template");

	enum Corner {TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT}

	static final ConfigObject<Corner> PREVIEW_CORNER = new ConfigObject<>(Corner.BOTTOM_RIGHT, "screenshot_utilities.config.preview_corner");


	public static void openSettings(Screen currentScreen) {
		SettingsScreen screen = new SettingsScreen(currentScreen);

		// ── General tab ──────────────────────────────────────────────────────

		BooleanConfigOption showPreview = BooleanConfigOption.builder(SHOW_PREVIEW)
				.tooltip(v -> Component.translatable(
						v ? "screenshot_utilities.config.show_preview.tooltip.on"
								: "screenshot_utilities.config.show_preview.tooltip.off"))
				.build();

		IntSliderConfigOption previewDuration = IntSliderConfigOption.builder(PREVIEW_DURATION, 1, 30)
				.tooltip(v -> Component.literal("Preview stays visible for " + v + " seconds"))
				.dependsOn(showPreview)           // disabled when preview is off
				.build();

		IntSliderConfigOption previewOpacity = IntSliderConfigOption.builder(PREVIEW_OPACITY, 10, 100)
				.dependsOn(showPreview)
				.build();

		EnumConfigOption<Corner> previewCorner = EnumConfigOption
				.builder(PREVIEW_CORNER, Corner.class)
				.style(EnumConfigOption.Style.CYCLIC)
				.valueName(c -> Component.literal(switch (c) {
					case TOP_LEFT -> "Top left";
					case TOP_RIGHT -> "Top right";
					case BOTTOM_LEFT -> "Bottom left";
					case BOTTOM_RIGHT -> "Bottom right";
				}))
				.dependsOn(showPreview)
				.build();

		ConfigTabContent generalContent = ConfigTabContent.builder()
				.section(Component.literal("Preview"))
				.option(showPreview)
				.option(previewDuration)
				.option(previewOpacity)
				.option(previewCorner)
				.build();

		screen.addConfigTab(Component.literal("General"), generalContent);

		// ── Screenshot tab ────────────────────────────────────────────────────

		PathConfigOption screenshotDir = PathConfigOption.builder(SCREENSHOT_DIR)
				.selectionMode(PathConfigOption.SelectionMode.DIRECTORIES_ONLY)
				.fileDialogTitle(Component.translatable("screenshot_utilities.config.screenshots_dir.file_dialog_title").getString())
				.tooltip(Component.literal("Directory where screenshots are saved"))
				.build();

		StringConfigOption filenameTemplate = StringConfigOption.builder(FILENAME_TEMPLATE)
				.maxLength(64)
				.validate(
						s -> !s.isBlank(),
						Component.literal("Template cannot be empty"))
				.tooltip(Component.literal("Use {date}, {time}, {index} as placeholders"))
				.build();

		IntFieldConfigOption jpegQuality = IntFieldConfigOption.builder(
						new ConfigObject<>(90, "screenshot_utilities.config.jpeg_quality"))
				.range(1, 100)
				.tooltip(v -> Component.literal("JPEG quality: " + v + "%"))
				.build();

		ConfigTabContent screenshotContent = ConfigTabContent.builder()
				.section(Component.literal("Storage"))
				.option(screenshotDir)
				.option(filenameTemplate)
				.section(Component.literal("Encoding"))
				.option(jpegQuality)
				.build();

		screen.addConfigTab(Component.literal("Screenshot"), screenshotContent);

		// ── Open ─────────────────────────────────────────────────────────────
		MINECRAFT.setScreen(screen);
	}
}
