package me.Azz_9.screenshot_utilities.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.client.Colors;

@Environment(EnvType.CLIENT)
public class Loading {

	public static void drawLoadingSpinner(@NonNull GuiGraphicsExtractor graphics, int centerX, int centerY, int size, int distance) {
		float time = Util.getMillis() / 1000f;

		float rotationSpeed = 1.2f;
		float pulseSpeed = 2.5f;

		float angle = time * Mth.TWO_PI * rotationSpeed;
		float pulse = (Mth.sin(time * pulseSpeed) + 1f) * 0.5f;

		float scale = Mth.lerp(pulse, 0.6f, 1.5f);

		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();

		matrices.translate(centerX, centerY);
		matrices.scale(scale);
		matrices.rotate(angle);

		drawSquare(graphics, distance, 0, size);
		drawSquare(graphics, -distance, 0, size);

		matrices.popMatrix();
	}

	private static void drawSquare(@NonNull GuiGraphicsExtractor graphics, int x, int y, int size) {
		graphics.fill(
				x - size / 2,
				y - size / 2,
				x + size / 2,
				y + size / 2,
				Colors.WHITE
		);
	}
}
