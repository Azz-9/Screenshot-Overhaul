package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import com.mojang.blaze3d.platform.NativeImage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.DynamicCubeMapTexture;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	// add buttons on the title screen
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo info) {
		int realmsY = this.height / 4 + 48 + 24 * 2;

		int realmsRightX = this.width / 2 + 100;
		int margin = 4;

		SpriteIconButton screenshotViewerButton = this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_utilities.options.screenshots"),
								(btn) -> MINECRAFT.setScreen(new ScreenshotGalleryScreen()),
								true
						)
						.withTootip()
						.size(20, 20)
						.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/screenshot"), 15, 15)
						.build()
		);

		screenshotViewerButton.setPosition(realmsRightX + margin, realmsY);

		this.addRenderableWidget(Button.builder(
						Component.literal("Change panorama"),
						(btn) -> {
							Path[] paths = new Path[]{
									Path.of("E:\\code\\logiciels & langages\\java\\workspace\\mod\\Screenshot Utilities\\Screenshot Utilities\\run\\screenshots\\panorama_2026-05-01_15.11.08\\panorama_1.png"),
									Path.of("E:\\code\\logiciels & langages\\java\\workspace\\mod\\Screenshot Utilities\\Screenshot Utilities\\run\\screenshots\\panorama_2026-05-01_15.11.08\\panorama_3.png"),
									Path.of("E:\\code\\logiciels & langages\\java\\workspace\\mod\\Screenshot Utilities\\Screenshot Utilities\\run\\screenshots\\panorama_2026-05-01_15.11.08\\panorama_5.png"),
									Path.of("E:\\code\\logiciels & langages\\java\\workspace\\mod\\Screenshot Utilities\\Screenshot Utilities\\run\\screenshots\\panorama_2026-05-01_15.11.08\\panorama_4.png"),
									Path.of("E:\\code\\logiciels & langages\\java\\workspace\\mod\\Screenshot Utilities\\Screenshot Utilities\\run\\screenshots\\panorama_2026-05-01_15.11.08\\panorama_0.png"),
									Path.of("E:\\code\\logiciels & langages\\java\\workspace\\mod\\Screenshot Utilities\\Screenshot Utilities\\run\\screenshots\\panorama_2026-05-01_15.11.08\\panorama_2.png")
							};
							NativeImage[] images = new NativeImage[paths.length];
							for (int i = 0; i < paths.length; i++) {
								try (InputStream in = Files.newInputStream(paths[i])) {
									images[i] = flipVertical(NativeImage.read(in));
								} catch (Exception _) {
									ScreenshotLogger.error("Failed to change panorama");
									return;
								}
							}

							DynamicCubeMapTexture texture = new DynamicCubeMapTexture();
							texture.setImages(images);
							MINECRAFT.getTextureManager().register(Identifier.withDefaultNamespace("textures/gui/title/background/panorama"), texture);
						}
				)
				.bounds(width - 100 - 10, 10, 100, 20)
				.build());
	}

	private static NativeImage flipVertical(NativeImage src) {
		int width = src.getWidth();
		int height = src.getHeight();

		NativeImage flipped = new NativeImage(width, height, false);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				flipped.setPixel(x, height - 1 - y, src.getPixel(x, y));
			}
		}

		return flipped;
	}
}
