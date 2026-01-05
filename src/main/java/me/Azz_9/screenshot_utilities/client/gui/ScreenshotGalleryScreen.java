package me.Azz_9.screenshot_utilities.client.gui;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.widget.ScreenshotEntryWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.ScreenshotGalleryWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryScreen extends Screen {

	private ScreenshotGalleryWidget gallery;

	public ScreenshotGalleryScreen() {
		super(Text.literal("Screenshots"));
	}

	@Override
	protected void init() {
		File folder = Config.getInstance().getScreenshotsDir().toFile();

		List<File> screenshots = Arrays.stream(
				Objects.requireNonNull(folder.listFiles(f ->
						f.getName().endsWith(".png")
								|| f.getName().endsWith(".jpg")
								|| f.getName().endsWith(".jpeg")
				))
		).toList();

		gallery = new ScreenshotGalleryWidget(
				10,
				10,
				width - 100,
				height - 20,
				screenshots
		);

		addDrawableChild(gallery);
	}

	@Override
	public void close() {
		if (gallery != null) {
			gallery.close();
		}
		super.close();
	}
}
