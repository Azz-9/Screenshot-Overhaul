package me.Azz_9.screenshot_utilities.client.photoMode;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class PhotoModeHud {
	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null) {
			return;
		}

		context.drawText(CLIENT.textRenderer, String.format("Speed: %.2f", PhotoMode.getCamera().getSpeed()), 20, context.getScaledWindowHeight() - 30, 0xffffffff, true);

		float roll = (PhotoMode.getCamera().getRoll(tickCounter.getTickProgress(true)) % 360 + 360) % 360;
		context.drawText(CLIENT.textRenderer, String.format("Roll: %.1f°", roll), 20, context.getScaledWindowHeight() - 20, 0xffffffff, true);
	}
}
