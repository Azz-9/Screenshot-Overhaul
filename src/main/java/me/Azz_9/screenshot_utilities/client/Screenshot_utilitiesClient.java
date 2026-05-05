package me.Azz_9.screenshot_utilities.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoModeHud;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotPreview;

@Environment(EnvType.CLIENT)
public class Screenshot_utilitiesClient implements ClientModInitializer {
	public static final @NonNull Minecraft MINECRAFT = Minecraft.getInstance();
	public static final @NonNull String MOD_ID = "screenshot_utilities";

	private static KeyMapping openPhotoMode;
	private static KeyMapping rollLeft;
	private static KeyMapping rollRight;
	private static KeyMapping panoramaScreenshot;

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

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
			PhotoMode.startTick();
		});

		KeyMapping.Category keybind_category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "screenshot-utilities"));

		// photo mode
		openPhotoMode = KeyMappingHelper.registerKeyMapping(new KeyMapping("screenshot_utilities.controls.photo_mode", InputConstants.Type.KEYSYM, InputConstants.KEY_F10, keybind_category));
		rollLeft = KeyMappingHelper.registerKeyMapping(new KeyMapping("screenshot_utilities.controls.roll_left", InputConstants.Type.KEYSYM, InputConstants.KEY_Q, keybind_category));
		rollRight = KeyMappingHelper.registerKeyMapping(new KeyMapping("screenshot_utilities.controls.roll_right", InputConstants.Type.KEYSYM, InputConstants.KEY_E, keybind_category));

		// panorama screenshot
		panoramaScreenshot = KeyMappingHelper.registerKeyMapping(new KeyMapping("screenshot_utilities.controls.panorama_screenshot", InputConstants.Type.KEYSYM, InputConstants.KEY_F9, keybind_category));
	}
	
	public static void handleKeybindsHook() {
		while (Screenshot_utilitiesClient.getOpenPhotoModeKeybind().consumeClick()) {
			PhotoMode.toggle();
		}

		if (PhotoMode.isEnabled() && PhotoMode.getCamera() != null) {
			while (Screenshot_utilitiesClient.getRollLeftKeybind().consumeClick()) {
				PhotoMode.getCamera().rollLeft();
			}

			while (Screenshot_utilitiesClient.getRollRightKeybind().consumeClick()) {
				PhotoMode.getCamera().rollRight();
			}
		}
	}

	// return whether the base render method should be canceled
	public static boolean hudRenderHook(final @NonNull GuiGraphicsExtractor graphics, final @NonNull DeltaTracker deltaTracker) {
		if (!(MINECRAFT.screen instanceof LevelLoadingScreen)) {
			if (!MINECRAFT.options.hideGui) {
				PhotoModeHud.render(graphics, deltaTracker);
			}
		}

		// screenshot preview
		if (MINECRAFT.screen == null)
			ScreenshotPreview.render(graphics, 0, 0, deltaTracker.getGameTimeDeltaPartialTick(true));

		// hide hud in PhotoMode
		return PhotoMode.isEnabled();
	}
}
