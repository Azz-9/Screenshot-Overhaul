package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {

	@ModifyVariable(
			method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V",
			at = @At("HEAD"),
			argsOnly = true,
			index = 4
	)
	private static Consumer<Text> modifyMessageReceiver(Consumer<Text> original) {
		return (text) -> {
			if (Config.getInstance().showChatMessage.getValue()) {
				original.accept(text);
			}
		};
	}
}
