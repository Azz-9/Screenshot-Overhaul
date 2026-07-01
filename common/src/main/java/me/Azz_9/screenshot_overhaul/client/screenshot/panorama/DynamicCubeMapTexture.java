package me.Azz_9.screenshot_overhaul.client.screenshot.panorama;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;

import net.minecraft.client.renderer.texture.AbstractTexture;

import org.jspecify.annotations.NonNull;

public class DynamicCubeMapTexture extends AbstractTexture {

	public void upload(@NonNull NativeImage[] images) {
		GpuDevice device = RenderSystem.getDevice();

		int width = images[0].getWidth();
		int height = images[0].getHeight();

		this.close();

		this.texture = device.createTexture(
				() -> MOD_ID + "_panorama",
				21,
				TextureFormat.RGBA8,
				width,
				height,
				6,
				1
		);

		this.textureView = device.createTextureView(this.texture);

		for (int face = 0; face < 6; face++) {
			device.createCommandEncoder().writeToTexture(
					this.texture,
					images[face],
					0,
					face,
					0,
					0,
					width,
					height,
					0,
					0
			);
		}
	}
}
