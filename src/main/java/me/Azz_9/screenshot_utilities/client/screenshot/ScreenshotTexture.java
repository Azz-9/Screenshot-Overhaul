package me.Azz_9.screenshot_utilities.client.screenshot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ScreenshotTexture {

	private final Identifier id;
	private final NativeImageBackedTexture texture;

	public ScreenshotTexture(Identifier id, NativeImageBackedTexture texture) {
		this.id = id;
		this.texture = texture;
	}

	public Identifier getId() {
		return id;
	}

	public void close() {
		texture.close();
	}
}