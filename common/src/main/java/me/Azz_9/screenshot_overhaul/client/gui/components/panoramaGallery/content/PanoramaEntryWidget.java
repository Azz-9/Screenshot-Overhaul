package me.Azz_9.screenshot_overhaul.client.gui.components.panoramaGallery.content;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.AbstractGalleryEntryWidget;
import me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.content.ScreenshotNameWidget;
import me.Azz_9.screenshot_overhaul.client.panorama.Panorama;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;
import me.Azz_9.screenshot_overhaul.utils.PathUtils;

public class PanoramaEntryWidget extends AbstractGalleryEntryWidget {

	private final @NonNull String INITIAL_NAME;

	private final @NonNull PanoramaThumbnailWidget thumbnailWidget;
	private final @NonNull ScreenshotNameWidget nameWidget;

	private final @NonNull Panorama panorama;

	public PanoramaEntryWidget(@NonNull Panorama panorama, @Nullable Consumer<Panorama> onThumbnailClicked) {
		this(0, 0, 0, 0, panorama, onThumbnailClicked);
	}

	public PanoramaEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, @NonNull Panorama panorama,
	                           @Nullable Consumer<Panorama> onThumbnailClicked) {
		super(x, y, thumbnailWidth, thumbnailHeight, 0, panorama.folder().getParentFile().toPath());
		this.INITIAL_NAME = panorama.folderName();

		this.panorama = panorama;

		this.thumbnailWidget = new PanoramaThumbnailWidget(
				x, y,
				thumbnailWidth, thumbnailHeight,
				panorama,
				onThumbnailClicked
		);

		this.nameWidget = new ScreenshotNameWidget(
				x, y + thumbnailHeight,
				thumbnailWidth, DEFAULT_NAME_HEIGHT,
				INITIAL_NAME
		);
		nameWidget.setTextPredicate(PathUtils::isValidString);
		nameWidget.setChangedListener(string -> {
			nameWidget.clearChatFormattings();
			if (hasChanged()) {
				nameWidget.addChatFormatting(ChatFormatting.ITALIC);
			}
			boolean nameAlreadyTaken = PathUtils.exists(getParentFolder(), getName()) && hasNameChanged();
			if (!PathUtils.isValidFileName(string) || nameAlreadyTaken) {
				nameWidget.addChatFormatting(ChatFormatting.RED);
				if (nameAlreadyTaken) {
					nameWidget.setTooltip(Tooltip.create(Component.translatable("screenshot_overhaul.gallery_widget.entry.name_already_taken")));
				}
			}
		});

		addRenderableChild(thumbnailWidget);
		addRenderableChild(nameWidget);
	}

	@Override
	protected void updateChildrenPos() {
		thumbnailWidget.setRectangle(getWidth(), getHeight() - DEFAULT_NAME_HEIGHT, getX(), getY());
		nameWidget.setRectangle(getWidth(), DEFAULT_NAME_HEIGHT, getX(), thumbnailWidget.getBottom());
	}

	@Override
	public void triggerLoad() {
		thumbnailWidget.load();
	}

	@Override
	public @NonNull String getName() {
		return nameWidget.getText();
	}

	@Override
	public @NonNull String getPathRelativeToScreenshotDir() {
		return panorama.folder().getPath();
	}

	@Override
	public long getTimestampOrLastModified() {
		for (Screenshot screenshot : panorama.faces()) {
			if (screenshot != null && screenshot.getMetadata().getTimestamp() != null) {
				return screenshot.getMetadata().getTimestamp();
			}
		}

		return panorama.folder().lastModified();
	}

	@Override
	public @Nullable String getWorldName() {
		if (panorama.presentFaces().isEmpty()) return null;
		return panorama.presentFaces().getFirst().getMetadata().getWorldName();
	}

	@Override
	public @Nullable String getServerIp() {
		if (panorama.presentFaces().isEmpty()) return null;
		return panorama.presentFaces().getFirst().getMetadata().getServerIp();
	}

	@Override
	protected void renderBeforeChildren(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, float animationProgress) {
		// fond global
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK_TRANSPARENT);
	}

	@Override
	public boolean hasChanged() {
		return hasNameChanged();
	}

	@Override
	protected boolean hasNameChanged() {
		return !INITIAL_NAME.equals(getName());
	}

	@Override
	public void revertChanges() {
		nameWidget.setText(INITIAL_NAME);
	}

	@Override
	public void commitChanges() {
		if (hasChanged()) {
			Path oldPath = panorama.folder().toPath();
			Path newPath = panorama.folder().toPath().resolveSibling(getName());
			try {
				Files.move(oldPath, newPath);
			} catch (IOException e) {
				ScreenshotLogger.error("Failed to rename panorama folder : {}", e.getMessage());
			}
		}
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}
}
