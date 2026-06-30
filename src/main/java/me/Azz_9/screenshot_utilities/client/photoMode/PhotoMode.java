package me.Azz_9.screenshot_utilities.client.photoMode;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.authlib.GameProfile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PhotoMode {

	private static final float SCROLL_SPEED_WITH_CTRL = 0.01f;
	private static final float SCROLL_SPEED_WITHOUT_CTRL = 0.05f;

	private static boolean enabled;
	private static long frozenTime;

	private static @Nullable PhotoCamera camera = null;

	private static @Nullable CameraType prevCameraType = null;

	public static @Nullable PhotoCamera getCamera() {
		return camera;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static long getFrozenTime() {
		return frozenTime;
	}

	public static void enable() {
		if (MINECRAFT.level == null || MINECRAFT.player == null) {
			return;
		}

		frozenTime = MINECRAFT.level.getOverworldClockTime();

		MINECRAFT.smartCull = false;

		camera = new PhotoCamera(MINECRAFT.level, new GameProfile(UUID.randomUUID(), "Camera"));
		camera.spawn();

		prevCameraType = MINECRAFT.options.getCameraType();
		if (MINECRAFT.gameRenderer.mainCamera().isDetached()) {
			MINECRAFT.options.setCameraType(CameraType.FIRST_PERSON);
		}

		MINECRAFT.setCameraEntity(camera);

		enabled = true;
	}

	public static void disable() {
		enabled = false;

		MINECRAFT.smartCull = true;

		if (camera != null) {
			camera.despawn();
			camera.input = new ClientInput();
			camera = null;
		}

		if (MINECRAFT.player != null) {
			MINECRAFT.player.input = new KeyboardInput(MINECRAFT.options);
			MINECRAFT.setCameraEntity(MINECRAFT.player);
		}

		if (prevCameraType != null) {
			MINECRAFT.options.setCameraType(prevCameraType);
		}
	}

	public static void toggle() {
		if (PhotoMode.isEnabled()) {
			PhotoMode.disable();
		} else {
			PhotoMode.enable();
		}
	}

	public static void startTick() {
		if (isEnabled()) {
			if (getCamera() != null) {
				camera.prevRoll = camera.roll;
			}

			// Prevent player from being controlled when PhotoMode is enabled
			if (MINECRAFT.player != null && MINECRAFT.player.input instanceof KeyboardInput) {
				ClientInput input = new ClientInput();
				Input keyPresses = MINECRAFT.player.input.keyPresses;
				input.keyPresses = new Input(
						false,
						false,
						false,
						false,
						false,
						keyPresses.shift(),
						false
				);
				MINECRAFT.player.input = input;
			}
		}
	}

	// return whether the base onScroll method should be canceled
	public static boolean onMouseScroll(long handle, double xoffset, double yoffset) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null || handle != MINECRAFT.getWindow().handle() || MINECRAFT.gui.screen() != null) {
			return false;
		}

		boolean discrete = MINECRAFT.options.discreteMouseScroll().get();
		double sensitivity = MINECRAFT.options.mouseWheelSensitivity().get();

		double scroll = (discrete ? Math.signum(yoffset) : yoffset) * sensitivity;

		if (scroll != 0.0) {
			float scrollSpeed = (MINECRAFT.hasControlDown() ? SCROLL_SPEED_WITH_CTRL : SCROLL_SPEED_WITHOUT_CTRL);
			PhotoMode.getCamera().setVelocity(Mth.clamp(PhotoMode.getCamera().getVelocity() + scroll * scrollSpeed, 0, 5.0));
		}
		
		return true;
	}
}
