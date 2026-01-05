package me.Azz_9.screenshot_utilities.client;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class Screenshot_utilitiesClient implements ClientModInitializer {
	public static final String MOD_ID = "screenshot_utilities";

	public static KeyBinding openPhotoMode;

	@Override
	public void onInitializeClient() {
		KeyBinding.Category keybind_category = KeyBinding.Category.create(Identifier.of(MOD_ID, "screenshot-utilities"));

		openPhotoMode = KeyBindingHelper.registerKeyBinding(new KeyBinding("screenshot_utilities.controls.photo_mode", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_F10, keybind_category));
	}

	public static @NotNull KeyBinding getOpenPhotoModeKeybind() {
		if (openPhotoMode == null) {
			throw new RuntimeException("open photo mode not set!");
		}
		return openPhotoMode;
	}
}
