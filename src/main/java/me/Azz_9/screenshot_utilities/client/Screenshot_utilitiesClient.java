package me.Azz_9.screenshot_utilities.client;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class Screenshot_utilitiesClient implements ClientModInitializer {
	public static final @NonNull MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static final @NonNull String MOD_ID = "screenshot_utilities";

	private static KeyBinding openPhotoMode;
	private static KeyBinding rollLeft;
	private static KeyBinding rollRight;

	public static @NonNull KeyBinding getOpenPhotoModeKeybind() {
		return openPhotoMode;
	}

	public static @NonNull KeyBinding getRollLeftKeybind() {
		return rollLeft;
	}

	public static @NonNull KeyBinding getRollRightKeybind() {
		return rollRight;
	}

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			PhotoMode.startTick();
		});

		KeyBinding.Category keybind_category = KeyBinding.Category.create(Identifier.of(MOD_ID, "screenshot-utilities"));

		openPhotoMode = KeyBindingHelper.registerKeyBinding(new KeyBinding("screenshot_utilities.controls.photo_mode", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_F10, keybind_category));

		rollLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("screenshot_utilities.controls.roll_left", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_Q, keybind_category));
		rollRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("screenshot_utilities.controls.roll_right", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_E, keybind_category));
	}
}
