package me.Azz_9.screenshot_utilities.client.screenshot.panorama;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.texture.AbstractTexture;

public class DynamicCubeMapTexture extends AbstractTexture {

	public void setImages(NativeImage[] images) {
		upload(images);
	}

	private void upload(NativeImage[] images) {
		GpuDevice device = RenderSystem.getDevice();

		int width = images[0].getWidth();
		int height = images[0].getHeight();

		this.close();

		this.texture = device.createTexture(
				() -> MOD_ID + "_panorama",
				21,
				GpuFormat.RGBA8_UNORM,
				width,
				height,
				6,
				1
		);

		this.textureView = device.createTextureView(this.texture);

		for (int face = 0; face < 6; face++) {
			device.createCommandEncoder().writeToTexture(
					this.texture,
					images[face].getPixelBytes(),
					0,
					face,
					0,
					0,
					width,
					height
			);
		}
	}

}
