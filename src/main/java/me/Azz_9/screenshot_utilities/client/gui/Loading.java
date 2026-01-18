package me.Azz_9.screenshot_utilities.client.gui;

import me.Azz_9.screenshot_utilities.client.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class Loading {

	public static void drawLoadingSpinner(@NonNull DrawContext context, int centerX, int centerY, int size, int distance) {
		float time = Util.getMeasuringTimeMs() / 1000f;

		float rotationSpeed = 1.2f;
		float pulseSpeed = 2.5f;

		float angle = time * MathHelper.TAU * rotationSpeed;
		float pulse = (MathHelper.sin(time * pulseSpeed) + 1f) * 0.5f;

		float scale = MathHelper.lerp(pulse, 0.6f, 1.5f);

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();

		matrices.translate(centerX, centerY);
		matrices.scale(scale);
		matrices.rotate(angle);

		drawSquare(context, distance, 0, size);
		drawSquare(context, -distance, 0, size);

		matrices.popMatrix();
	}

	private static void drawSquare(@NonNull DrawContext context, int x, int y, int size) {
		context.fill(
				x - size / 2,
				y - size / 2,
				x + size / 2,
				y + size / 2,
				Colors.WHITE
		);
	}
}
