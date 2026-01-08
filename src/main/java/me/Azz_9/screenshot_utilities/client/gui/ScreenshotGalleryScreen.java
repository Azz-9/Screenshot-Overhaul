package me.Azz_9.screenshot_utilities.client.gui;

import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.gui.widget.NavigationButton;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshot.ScreenshotGalleryWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ScreenshotGalleryScreen extends Screen {

	private static final int FULL_VIEW_PADDING = 40;
	private static final int BOTTOM_PADDING = 60;
	private static final int NAV_BUTTON_SIZE = 20;
	private static final int NAV_BUTTON_PADDING = 10;

	private final FocusManager focusManager = new FocusManager();

	private ScreenshotGalleryWidget gallery;

	private NavigationButton prevButton, nextButton;

	@Nullable
	private ScreenshotTexture selectedTexture = null;

	public ScreenshotGalleryScreen() {
		super(Text.translatable("screenshot_utilities.narrator.screenshot_gallery"));
	}

	@Override
	protected void init() {
		File folder = Config.getInstance().getScreenshotsDir().toFile();

		List<File> screenshots = Arrays.stream(
				Objects.requireNonNull(folder.listFiles(f ->
						f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg")
				))
		).toList();

		gallery = new ScreenshotGalleryWidget(
				10, 10,
				width - 100, height - 20,
				screenshots, focusManager
		);

		prevButton = new NavigationButton(
				(width - NAV_BUTTON_PADDING) / 2 - NAV_BUTTON_SIZE, height - 40,
				NAV_BUTTON_SIZE, NAV_BUTTON_SIZE,
				NavigationButton.NavigationTypes.BACK, (btn) -> {
			System.out.println("prev");
		});
		nextButton = new NavigationButton(
				(width + NAV_BUTTON_PADDING) / 2, height - 40,
				NAV_BUTTON_SIZE, NAV_BUTTON_SIZE,
				NavigationButton.NavigationTypes.NEXT, (btn) -> {
			System.out.println("next");
		});

		prevButton.active = false;
		prevButton.visible = false;
		nextButton.active = false;
		nextButton.visible = false;

		addDrawableChild(prevButton);
		addDrawableChild(nextButton);
		addDrawableChild(gallery);
	}

	public void selectScreenshot(ScreenshotTexture texture) {
		this.selectedTexture = texture;
		prevButton.active = true;
		prevButton.visible = true;
		nextButton.active = true;
		nextButton.visible = true;
		gallery.setActive(false);
	}

	public void deselectScreenshot() {
		this.selectedTexture = null;
		prevButton.active = false;
		prevButton.visible = false;
		nextButton.active = false;
		nextButton.visible = false;
		gallery.setActive(true);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);


		if (selectedTexture != null) {
			context.fill(0, 0, width, height, 0x7f000000);

			ScreenshotDrawHelper.drawCover(
					context,
					selectedTexture.id(),
					selectedTexture.width(), selectedTexture.height(),
					FULL_VIEW_PADDING, FULL_VIEW_PADDING,
					width - FULL_VIEW_PADDING * 2,
					height - FULL_VIEW_PADDING - BOTTOM_PADDING
			);

			nextButton.render(context, mouseX, mouseY, deltaTicks);
			prevButton.render(context, mouseX, mouseY, deltaTicks);
		}
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

	/* ---------------- Cleanup ---------------- */

	@Override
	public void close() {
		if (gallery != null) {
			gallery.close();
			gallery = null;
		}

		selectedTexture = null;

		super.close();
	}
}
