package me.Azz_9.screenshot_utilities.client.photoMode;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.PlayerInput;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class PhotoMode {

	private static boolean enabled;
	private static long frozenTime;

	@Nullable
	private static PhotoCamera camera = null;

	@Nullable
	private static Perspective prevCameraType = null;

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
		if (CLIENT.world == null || CLIENT.player == null) {
			return;
		}

		ClientPlayNetworkHandler networkHandler = new ClientPlayNetworkHandler(
				CLIENT,
				new ClientConnection(NetworkSide.CLIENTBOUND),
				new ClientConnectionState(
						new ClientChunkLoadProgress(),
						new GameProfile(UUID.randomUUID(), "Camera"),
						CLIENT.getTelemetryManager().createWorldSession(false, null, null),
						CLIENT.player.getRegistryManager().toImmutable(),
						FeatureSet.empty(),
						null,
						null,
						null,
						Collections.emptyMap(),
						null,
						Collections.emptyMap(),
						ServerLinks.EMPTY,
						Collections.emptyMap(),
						false
				)
		);

		enabled = true;

		frozenTime = CLIENT.world.getTimeOfDay();

		CLIENT.chunkCullingEnabled = false;

		camera = new PhotoCamera(CLIENT, CLIENT.world, networkHandler, CLIENT.player.getStatHandler(), CLIENT.player.getRecipeBook(), PlayerInput.DEFAULT, false);
		camera.spawn();

		prevCameraType = CLIENT.options.getPerspective();
		if (CLIENT.gameRenderer.getCamera().isThirdPerson()) {
			CLIENT.options.setPerspective(Perspective.FIRST_PERSON);
		}

		CLIENT.setCameraEntity(camera);
	}

	public static void disable() {
		enabled = false;

		CLIENT.chunkCullingEnabled = true;

		if (camera != null) {
			camera.despawn();
			camera.input = new Input();
			camera = null;
		}

		if (CLIENT.player != null) {
			CLIENT.player.input = new KeyboardInput(CLIENT.options);
			CLIENT.setCameraEntity(CLIENT.player);
		}

		if (prevCameraType != null) {
			CLIENT.options.setPerspective(prevCameraType);
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
			if (CLIENT.player != null && CLIENT.player.input instanceof KeyboardInput) {
				Input input = new Input();
				PlayerInput keyPresses = CLIENT.player.input.playerInput;
				input.playerInput = new PlayerInput(
						false,
						false,
						false,
						false,
						false,
						keyPresses.sneak(),
						false
				);
				CLIENT.player.input = input;
			}
		}
	}
}
