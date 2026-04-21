package me.Azz_9.screenshot_utilities.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.config.Config;

@Environment(EnvType.CLIENT)
@Mixin(Screenshot.class)
public abstract class ScreenshotRecorderMixin {

	@ModifyVariable(
			method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V",
			at = @At("HEAD"),
			argsOnly = true,
			name = "callback"
	)
	private static Consumer<Component> modifyMessageReceiver(Consumer<Component> callback) {
		return (text) -> {
			if (Config.getInstance().showChatMessage.getValue()) {
				callback.accept(text);
			}
		};
	}
}
