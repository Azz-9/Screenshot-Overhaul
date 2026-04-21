package me.Azz_9.screenshot_utilities.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

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
}
