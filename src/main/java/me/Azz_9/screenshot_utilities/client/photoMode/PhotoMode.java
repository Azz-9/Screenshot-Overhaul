package me.Azz_9.screenshot_utilities.client.photoMode;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class PhotoMode {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private static boolean enabled;

	public static void enable()  {
		enabled = true;
	}

	public static void disable()  {
		enabled = false;
	}

	public static boolean isEnabled() {
		return enabled;
	}
}
