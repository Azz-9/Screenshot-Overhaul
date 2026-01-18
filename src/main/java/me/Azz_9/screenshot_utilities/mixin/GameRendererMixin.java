package me.Azz_9.screenshot_utilities.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	// Hide hand in PhotoMode
	@Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
	private void onRenderHand(CallbackInfo ci) {
		if (PhotoMode.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(
			method = "renderWorld",
			at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;mul(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;")
	)
	private void applyRoll(RenderTickCounter tickCounter, CallbackInfo ci, @Local MatrixStack matrixStack) {
		if (!PhotoMode.isEnabled() || PhotoMode.getCamera() == null) return;

		float roll = PhotoMode.getCamera().getRoll(tickCounter.getTickProgress(true));
		if (roll != 0.0f) {
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
		}
	}
}
