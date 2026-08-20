package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.TracingExecutor;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.client.Sounds;
import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.metadata.MetadataUtils;
import me.Azz_9.screenshot_overhaul.client.panorama.PanoramaFaceContext;
import me.Azz_9.screenshot_overhaul.client.panorama.ScreenshotContext;
import me.Azz_9.screenshot_overhaul.client.preview.ScreenshotPreview;
import me.Azz_9.screenshot_overhaul.client.screenshot.FutureScreenshotState;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotFileNameParser;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotManager;

@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {

	@WrapMethod(method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V")
	private static void requestGrabIfNeeded(File workDir, @Nullable String forceName, RenderTarget target, int downscaleFactor, Consumer<Component> callback, Operation<Void> original) {
		if (ScreenshotContext.PANORAMA.get() != null) {
			original.call(workDir, forceName, target, downscaleFactor, callback);
			return;
		}

		if (FutureScreenshotState.shouldRequestScreenshot()) {
			FutureScreenshotState.suppressHud = Config.getInstance().hideHudOnScreenshot.getValue();
			FutureScreenshotState.suppressChat = !Config.getInstance().hideHudOnScreenshot.getValue() && Config.getInstance().hideChatOnScreenshot.getValue();
			FutureScreenshotState.suppressHand = Config.getInstance().hideHandOnScreenshot.getValue();
			FutureScreenshotState.captureRequested = true;
			FutureScreenshotState.pendingCapture = () -> original.call(workDir, forceName, target, downscaleFactor, callback);
		} else {
			original.call(workDir, forceName, target, downscaleFactor, callback);
		}
	}

	@Definition(id = "File", type = File.class)
	@Definition(id = "workDir", local = @Local(type = File.class))
	@Expression("new File(workDir, 'screenshots')")
	@ModifyExpressionValue(
			method = "lambda$grab$2",
			at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private static File screenshotDir(File original) {
		PanoramaFaceContext faceCtx = ScreenshotContext.PANORAMA_FACE.get();
		if (faceCtx != null) {
			return faceCtx.panoramaFolder();
		}

		return Config.getInstance().getAbsoluteScreenshotsDir().toFile();
	}

	@WrapOperation(
			method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Screenshot;takeScreenshot(Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"
			)
	)
	private static void wrapTakeScreenshot(RenderTarget target, int downscaleFactor, Consumer<NativeImage> consumer, Operation<Void> original) {
		PanoramaFaceContext faceCtx = ScreenshotContext.PANORAMA_FACE.get();

		if (faceCtx == null) {
			original.call(target, downscaleFactor, consumer);
			return;
		}

		original.call(target, downscaleFactor, (Consumer<NativeImage>) image -> {
			ScreenshotContext.PANORAMA_FACE.set(faceCtx);

			try {
				consumer.accept(image);
			} finally {
				ScreenshotContext.PANORAMA_FACE.remove();
			}
		});
	}

	@WrapOperation(
			method = "lambda$grab$2",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/TracingExecutor;execute(Ljava/lang/Runnable;)V"
			)
	)
	private static void wrapIoTask(TracingExecutor instance, Runnable command, Operation<Void> original) {
		PanoramaFaceContext faceCtx = ScreenshotContext.PANORAMA_FACE.get();

		if (faceCtx == null) {
			original.call(instance, command);
			return;
		}

		original.call(instance, (Runnable) () -> {
			ScreenshotContext.PANORAMA_FACE.set(faceCtx);

			try {
				command.run();
			} finally {
				if (faceCtx.pendingFaces().decrementAndGet() == 0) {
					ScreenshotContext.capturingPanorama = false;
					ScreenshotManager.save();
				}

				ScreenshotContext.PANORAMA_FACE.remove();
			}
		});
	}

	@WrapOperation(
			method = "lambda$grab$3",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V"
			)
	)
	private static void writeToFile(NativeImage instance, File file, Operation<Void> original) throws Exception {
		PanoramaFaceContext faceCtx = ScreenshotContext.PANORAMA_FACE.get();

		// adding metadata
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (WritableByteChannel channel = Channels.newChannel(baos)) {
			((NativeImageAccessor) (Object) instance).invokeWriteToChannel(channel);
		}

		byte[] pngWithMeta = MetadataUtils.injectIntoBytes(
				baos.toByteArray(),
				faceCtx != null ? MetadataUtils.collect(faceCtx.panoramaId(), faceCtx.faceIndex()) : MetadataUtils.collect()
		);

		Files.createDirectories(file.toPath().getParent());
		Files.write(file.toPath(), pngWithMeta);

		// preview
		if (Config.getInstance().showPreview.getValue()) {
			if (faceCtx != null) {
				if (faceCtx.faceIndex() == 0) {
					ScreenshotPreview.setPanorama(file, faceCtx.panoramaId());
				}
			} else {
				ScreenshotPreview.setScreenshot(file);
			}
		}

		if (faceCtx != null) {
			ScreenshotManager.setHiddenFromMap(
					Config.getInstance().getAbsoluteScreenshotsDir().relativize(file.toPath()).toString(),
					true
			);
		}

		if (Config.getInstance().screenshotSound.getValue() && (faceCtx == null || faceCtx.faceIndex() == 0)) { // play once for panorama
			MINECRAFT.player.playSound(Sounds.SHUTTER, 1, 1);
		}
	}

	@WrapWithCondition(
			method = "lambda$grab$3",
			at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V")
	)
	private static boolean shouldSendMessage(Consumer<Component> instance, Object component) {
		return Config.getInstance().showChatMessage.getValue();
	}

	@Inject(method = "getFile", at = @At("HEAD"), cancellable = true)
	private static void getFile(File picDir, CallbackInfoReturnable<File> cir) {
		String pattern = Config.getInstance().screenshotsFileName.getValue();
		cir.setReturnValue(ScreenshotFileNameParser.resolve(picDir.toPath(), pattern).toFile());
	}
}
