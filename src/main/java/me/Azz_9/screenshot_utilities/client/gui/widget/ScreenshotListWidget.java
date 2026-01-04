package me.Azz_9.screenshot_utilities.client.gui.widget;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTexture;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotTextureManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class ScreenshotListWidget extends AbstractSmoothScrollableList<ScreenshotListWidget.Entry> {

	private static final int THUMB_WIDTH = 160;
	private static final int THUMB_HEIGHT = 90;
	private static final int PADDING = 6;

	private final Path screenshotDir;
	private int itemsPerRow;

	public ScreenshotListWidget(MinecraftClient client, int width, int height, int x, int y, Path screenshotDir) {
		super(client, width, height, y, THUMB_HEIGHT + PADDING);
		setX(x);
		this.screenshotDir = screenshotDir;

		reload();
	}

	public void reload() {
		this.clearEntries();

		List<Path> screenshots = collectScreenshots(screenshotDir);

		ScreenshotLogger.info("{} screenshots found", screenshots.size());

		this.itemsPerRow = Math.max(1, (this.width - PADDING) / (THUMB_WIDTH + PADDING));

		for (int i = 0; i < screenshots.size(); i += itemsPerRow) {
			int end = Math.min(i + itemsPerRow, screenshots.size());
			this.addEntry(new Entry(screenshots.subList(i, end)));
		}
	}


	private List<Path> collectScreenshots(Path root) {
		List<Path> result = new ArrayList<>();

		try (Stream<Path> stream = Files.walk(root)) {
			stream
					.filter(Files::isRegularFile)
					.filter(path -> {
						String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
						return name.endsWith(".png") || name.endsWith(".jpg");
					})
					.sorted(Comparator.comparingLong(this::getLastModified).reversed())
					.forEach(result::add);
		} catch (IOException e) {
			ScreenshotLogger.warn("Error while reading screenshots from {}", root);
		}

		return result;
	}

	private long getLastModified(Path path) {
		try {
			return Files.getLastModifiedTime(path).toMillis();
		} catch (IOException e) {
			return 0;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Entry extends ElementListWidget.Entry<Entry> {

		private final List<ScreenshotEntry> screenshots;

		public Entry(List<Path> paths) {
			this.screenshots = paths.stream()
					.map(ScreenshotEntry::new)
					.toList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int drawX = getX() + PADDING;

			for (ScreenshotEntry screenshot : screenshots) {
				screenshot.render(context, drawX, getY() + PADDING, mouseX, mouseY);
				drawX += THUMB_WIDTH + PADDING;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private record ScreenshotEntry(ScreenshotTexture texture) {

		private ScreenshotEntry(Path texture) {
			this(ScreenshotTextureManager.get(texture, THUMB_WIDTH, THUMB_HEIGHT));
		}

		public void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
			if (texture == null) return;

			context.drawTexture(RenderPipelines.GUI_TEXTURED, texture.getId(), x, y, 0, 0, THUMB_WIDTH, THUMB_HEIGHT, THUMB_WIDTH, THUMB_HEIGHT);
		}
	}
}
