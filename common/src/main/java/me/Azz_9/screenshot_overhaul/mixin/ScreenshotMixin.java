package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotGrabber;

@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {

	@Inject(
			method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void grab(File workDir, @Nullable String forceName, RenderTarget target, int downscaleFactor, Consumer<Component> callback, CallbackInfo ci) {
		ci.cancel();
		ScreenshotGrabber.requestGrab(
				Config.getInstance().getAbsoluteScreenshotsDir().toFile(),
				forceName,
				target,
				downscaleFactor,
				callback
		);
	}
}
