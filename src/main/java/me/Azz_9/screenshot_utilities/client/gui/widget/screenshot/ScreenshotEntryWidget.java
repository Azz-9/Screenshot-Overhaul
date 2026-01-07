package me.Azz_9.screenshot_utilities.client.gui.widget.screenshot;

import me.Azz_9.screenshot_utilities.client.gui.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

import java.io.File;

@Environment(EnvType.CLIENT)
public class ScreenshotEntryWidget extends SimpleParentWidget implements AutoCloseable, ParentElement {

	public static final int NAME_HEIGHT = 30;

	private final FocusManager focusManager;

	private final ScreenshotThumbnailWidget thumbnailWidget;
	private final ScreenshotNameWidget nameWidget;

	private final File screenshot;
	private final int baseY;

	public ScreenshotEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, File screenshot, FocusManager focusManager) {
		super(x, y, thumbnailWidth, thumbnailHeight + NAME_HEIGHT);
		this.focusManager = focusManager;

		this.baseY = y;
		this.screenshot = screenshot;

		this.thumbnailWidget = new ScreenshotThumbnailWidget(
				x, y,
				thumbnailWidth, thumbnailHeight,
				screenshot,
				focusManager
		);

		this.nameWidget = new ScreenshotNameWidget(
				x, y + thumbnailHeight,
				thumbnailWidth, NAME_HEIGHT,
				screenshot,
				focusManager
		);

		addAllChildren(thumbnailWidget, nameWidget);
	}

	public int getBaseY() {
		return baseY;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// fond global
		context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x801E1E1E);

		thumbnailWidget.render(context, mouseX, mouseY, delta);
		nameWidget.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		thumbnailWidget.setY(y);
		nameWidget.setY(thumbnailWidget.getBottom());
	}

	@Override
	public void close() {
		thumbnailWidget.close();
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
	}
}
