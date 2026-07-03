package me.Azz_9.screenshot_overhaul;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.config.ConfigLoader;
import me.Azz_9.screenshot_overhaul.client.panorama.Panorama;
import me.Azz_9.screenshot_overhaul.client.panorama.PanoramaHolder;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_overhaul.client.photoMode.PhotoModeHud;
import me.Azz_9.screenshot_overhaul.client.preview.ScreenshotPreview;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotGrabber;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotList;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotManager;
import me.Azz_9.screenshot_overhaul.compat.CompatManager;
import me.Azz_9.screenshot_overhaul.compat.journeyMap.JourneyMapIntegration;
import me.Azz_9.screenshot_overhaul.platform.Services;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as NeoForge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

	public static final boolean PHOTO_MODE_ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_PHOTO_MODE", "false"));

	public static Minecraft MINECRAFT;

	private static KeyMapping openPhotoMode;
	private static KeyMapping rollLeft;
	private static KeyMapping rollRight;
	private static KeyMapping panoramaScreenshot;

	private static final List<RunnableState> runnableStates = new ArrayList<>();

	public static @NonNull KeyMapping getOpenPhotoModeKeybind() {
		return openPhotoMode;
	}

	public static @NonNull KeyMapping getRollLeftKeybind() {
		return rollLeft;
	}

	public static @NonNull KeyMapping getRollRightKeybind() {
		return rollRight;
	}

	public static @NonNull KeyMapping getPanoramaScreenshotKeybind() {
		return panoramaScreenshot;
	}

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.

		MINECRAFT = Minecraft.getInstance();

		try {
			ConfigLoader.load();
			Config.getInstance().screenshotsDir.addOnChangeListener(CommonClass::onScreenshotDirectoryChanged);
		} catch (IOException e) {
			ScreenshotLogger.error("Failed to load config file.", e.getMessage());
		}

		// load screenshots
		ScreenshotList.loadAsync();
		ScreenshotManager.load();
		if (Config.getInstance().selectedPanoramaUUID.getValue() != null) {
			ScreenshotList.whenPanoramasLoaded(panoramas -> {
				Panorama selectedPanorama = null;
				for (Panorama panorama : panoramas) {
					if (Config.getInstance().selectedPanoramaUUID.getValue().equals(panorama.id())) {
						selectedPanorama = panorama;
					}
				}
				if (selectedPanorama != null) PanoramaHolder.usePanoramaAsync(selectedPanorama);
			});
		}

		Services.PLATFORM.registerOnStartTickEvent(() -> {
			PhotoMode.startTick();

			Iterator<RunnableState> iterator = runnableStates.iterator();
			while (iterator.hasNext()) {
				RunnableState runnableState = iterator.next();
				runnableState.tick();
				if (runnableState.runIfEndOfCooldown()) {
					iterator.remove();
				}
			}
		});

		Services.PLATFORM.registerOnJoinWorldEvent(() -> {
			if (CompatManager.journeyMapPresent()) {
				JourneyMapIntegration.init();
			}
		});
	}

	public static void initKeyMappings() {
		KeyMapping.Category keybind_category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "screenshot-overhaul"));

		// photo mode
		if (PHOTO_MODE_ENABLED) {
			openPhotoMode = Services.PLATFORM.registerKeyMapping(new KeyMapping("screenshot_overhaul.controls.photo_mode", InputConstants.Type.KEYSYM, InputConstants.KEY_F10, keybind_category));
			rollLeft = Services.PLATFORM.registerKeyMapping(new KeyMapping("screenshot_overhaul.controls.roll_left", InputConstants.Type.KEYSYM, InputConstants.KEY_Q, keybind_category));
			rollRight = Services.PLATFORM.registerKeyMapping(new KeyMapping("screenshot_overhaul.controls.roll_right", InputConstants.Type.KEYSYM, InputConstants.KEY_E, keybind_category));
		}

		// panorama screenshot
		panoramaScreenshot = Services.PLATFORM.registerKeyMapping(new KeyMapping("screenshot_overhaul.controls.panorama_screenshot", InputConstants.Type.KEYSYM, InputConstants.KEY_F9, keybind_category));
	}

	public static void handleKeybindsHook() {
		while (PHOTO_MODE_ENABLED && getOpenPhotoModeKeybind().consumeClick()) {
			PhotoMode.toggle();
		}

		if (PhotoMode.isEnabled() && PhotoMode.getCamera() != null) {
			while (getRollLeftKeybind().consumeClick()) {
				PhotoMode.getCamera().rollLeft();
			}

			while (getRollRightKeybind().consumeClick()) {
				PhotoMode.getCamera().rollRight();
			}
		}
	}

	public static boolean handleGlobalKeyPressHook(InputConstants.Key key, boolean controlDown) {
		if (getPanoramaScreenshotKeybind().matches(key)) {
			Component text = ScreenshotGrabber.grabPanoramixScreenshot(Config.getInstance().getAbsoluteScreenshotsDir().toFile());
			if (Config.getInstance().showChatMessage.getValue()) {
				MINECRAFT.showDebugChat(text);
			}
			return true;
		}

		return false;
	}

	// return whether the base render method should be canceled
	public static boolean hudRenderHook(final @NonNull GuiGraphicsExtractor graphics, final @NonNull DeltaTracker deltaTracker) {
		if (!(MINECRAFT.gui.screen() instanceof LevelLoadingScreen)) {
			if (!MINECRAFT.gui.hud.isHidden()) {
				PhotoModeHud.render(graphics, deltaTracker);
			}
		}

		// screenshot preview
		if (MINECRAFT.gui.screen() == null && !MINECRAFT.gui.hud.isHidden())
			ScreenshotPreview.render(graphics, 0, 0, deltaTracker.getGameTimeDeltaPartialTick(true));

		// hide hud in PhotoMode
		return PhotoMode.isEnabled();
	}

	private static void onScreenshotDirectoryChanged(Path path) {
		ScreenshotList.onScreenshotDirectoryChanged();
		ScreenshotManager.onScreenshotDirectoryChanged(path);
	}

	public static void runLater(Runnable runnable, int delayTicks) {
		MINECRAFT.execute(() -> runnableStates.add(new RunnableState(runnable, delayTicks)));
	}

	private static class RunnableState {
		private final @NonNull Runnable runnable;
		private int cooldownTicks;

		private RunnableState(final @NonNull Runnable runnable, int delayTicks) {
			this.runnable = runnable;
			this.cooldownTicks = delayTicks;
		}

		private void tick() {
			if (cooldownTicks > 0) {
				cooldownTicks--;
			}
		}

		private boolean runIfEndOfCooldown() {
			if (cooldownTicks <= 0) {
				runnable.run();
				return true;
			}

			return false;
		}
	}
}