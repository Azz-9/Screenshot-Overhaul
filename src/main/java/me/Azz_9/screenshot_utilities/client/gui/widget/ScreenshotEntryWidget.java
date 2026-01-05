package me.Azz_9.screenshot_utilities.client.gui.widget;

import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotDrawHelper;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.io.File;

@Environment(EnvType.CLIENT)
public class ScreenshotEntryWidget extends ClickableWidget implements AutoCloseable {

	private final ScreenshotTexture texture;
	private final File screenshot;

	private final int thumbnailWidth, thumbnailHeight;

	public static final int bottomHeight = 30;

	public ScreenshotEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, File screenshot) {
		super(x, y, thumbnailWidth, thumbnailHeight + bottomHeight, Text.literal(screenshot.getName()));
		this.screenshot = screenshot;
		texture = new ScreenshotTexture(screenshot.toPath());
		this.thumbnailWidth = thumbnailWidth;
		this.thumbnailHeight = thumbnailHeight;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x801E1E1E);

		texture.uploadIfNeeded();

		if (texture.isReady()) {
			ScreenshotDrawHelper.drawCover(
					context,
					texture.id(),
					texture.width(),
					texture.height(),
					getX(), getY(),
					thumbnailWidth, thumbnailHeight
			);
		}

		context.drawCenteredTextWithShadow(
				MinecraftClient.getInstance().textRenderer,
				screenshot.getName(),
				getX() + getWidth() / 2, getY() + thumbnailHeight + 8,
				0xffffffff
		);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public void close() {
		texture.close();
	}
}
