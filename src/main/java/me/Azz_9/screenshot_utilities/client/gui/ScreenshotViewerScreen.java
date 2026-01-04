package me.Azz_9.screenshot_utilities.client.gui;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.widget.ScreenshotListWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ScreenshotViewerScreen extends Screen {
	private static final Text NARRATOR_SCREEN_TITLE = Text.translatable("screenshot_utilities.narrator.screenshot_viewer");

	public ScreenshotViewerScreen() {
		super(NARRATOR_SCREEN_TITLE);

	}

	@Override
	protected void init() {
		super.init();

		this.addDrawableChild(new ScreenshotListWidget(
				this.client,
				this.client.getWindow().getScaledWidth() - 150, this.client.getWindow().getScaledHeight(),
				30, 30,
				Config.getScreenshotsDir())
		);
	}

	@Override
	public void close() {
		ScreenshotTextureManager.clear();
		super.close();
	}
}
