package me.Azz_9.screenshot_utilities.client.photoMode;

import me.Azz_9.screenshot_utilities.client.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.jspecify.annotations.NonNull;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Environment(EnvType.CLIENT)
public class PhotoModeHud {
	public static void render(@NonNull DrawContext context, @NonNull RenderTickCounter tickCounter) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null) {
			return;
		}

		context.drawText(CLIENT.textRenderer, String.format("Speed: %.2f", PhotoMode.getCamera().getSpeed()), 20, context.getScaledWindowHeight() - 30, Colors.WHITE, true);

		float roll = (PhotoMode.getCamera().getRoll(tickCounter.getTickProgress(true)) % 360 + 360) % 360;
		context.drawText(CLIENT.textRenderer, String.format("Roll: %.1f°", roll), 20, context.getScaledWindowHeight() - 20, Colors.WHITE, true);
	}
}
