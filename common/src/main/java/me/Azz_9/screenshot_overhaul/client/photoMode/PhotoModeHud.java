package me.Azz_9.screenshot_overhaul.client.photoMode;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.client.Colors;

public class PhotoModeHud {
	public static void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null) {
			return;
		}

		graphics.text(MINECRAFT.font, String.format("Speed: %.2f", PhotoMode.getCamera().getVelocity()), 20, graphics.guiHeight() - 30, Colors.WHITE, true);

		float roll = (PhotoMode.getCamera().getRoll(deltaTracker.getGameTimeDeltaPartialTick(true)) % 360 + 360) % 360;
		graphics.text(MINECRAFT.font, String.format("Roll: %.1f°", roll), 20, graphics.guiHeight() - 20, Colors.WHITE, true);
	}
}
