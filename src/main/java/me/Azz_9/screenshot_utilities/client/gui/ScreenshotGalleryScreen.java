package me.Azz_9.screenshot_utilities.client.gui;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshot.ScreenshotGalleryWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryScreen extends Screen {

	private final FocusManager focusManager = new FocusManager();

	private ScreenshotGalleryWidget gallery;

	public ScreenshotGalleryScreen() {
		super(Text.translatable("screenshot_utilities.narrator.screenshot_gallery"));
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
				10, 10,
				width - 100, height - 20,
				screenshots, focusManager
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

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		boolean handled = false;

		Optional<Element> optional = this.hoveredElement(click.x(), click.y());
		if (optional.isPresent()) {
			Element element = optional.get();
			if (element.mouseClicked(click, doubled) && element.isClickable()) {
				this.setFocused(element);
				if (click.button() == 0) {
					this.setDragging(true);
				}

				handled = true;
			}

		}

		if (!handled) {
			focusManager.clearFocus();
		}

		return handled;
	}
}
