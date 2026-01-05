package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.config.Config;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {

	@Inject(method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V", at = @At("HEAD"))
	private static void onSaveScreenshot(File gameDirectory, String fileName, Framebuffer framebuffer, int downscaleFactor, Consumer<Text> messageReceiver, CallbackInfo ci) {
		new Exception().printStackTrace();
	}

	@ModifyArg(
			method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V",
			at = @At("HEAD"),
			index = 4
	)
	private static Consumer<Text> wrapMessageReceiver(Consumer<Text> original) {
		return (text) -> {
			if (Config.showChatMessage.getValue()) {
				original.accept(text);
			}
		};
	}
}
