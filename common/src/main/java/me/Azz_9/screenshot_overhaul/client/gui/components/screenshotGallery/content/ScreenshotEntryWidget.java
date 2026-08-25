package me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery.content;

import net.minecraft.ChatFormatting;
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
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.AbstractGalleryEntryWidget;
import me.Azz_9.screenshot_overhaul.client.gui.components.gallery.EntryToggleButton;
import me.Azz_9.screenshot_overhaul.client.metadata.Metadata;
import me.Azz_9.screenshot_overhaul.client.metadata.MetadataUtils;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;
import me.Azz_9.screenshot_overhaul.client.screenshot.ScreenshotManager;
import me.Azz_9.screenshot_overhaul.utils.PathUtils;

public class ScreenshotEntryWidget extends AbstractGalleryEntryWidget {

	private final @NonNull String INITIAL_NAME;
	private final @NonNull Metadata INITIAL_METADATA;

	private final @NonNull ScreenshotThumbnailWidget thumbnailWidget;
	private final @NonNull ScreenshotNameWidget nameWidget;

	private final @NonNull Screenshot screenshot;

	public ScreenshotEntryWidget(int x, int y, int thumbnailWidth, int thumbnailHeight, @NonNull Screenshot screenshot,
								 @Nullable Consumer<Screenshot> onThumbnailClicked, @NonNull Consumer<Screenshot> onDeleteRequested) {
		super(x, y, thumbnailWidth, thumbnailHeight + DEFAULT_NAME_HEIGHT, screenshot.file().getParentFile().toPath());
		this.INITIAL_NAME = screenshot.file().getName();
		this.INITIAL_METADATA = Metadata.copyOf(screenshot.getMetadata());

		this.screenshot = screenshot;

		this.thumbnailWidget = new ScreenshotThumbnailWidget(
				x, y,
				thumbnailWidth, thumbnailHeight,
				screenshot,
				onThumbnailClicked,
				onDeleteRequested
		);

		this.nameWidget = new ScreenshotNameWidget(
				x, y + thumbnailHeight,
				thumbnailWidth, DEFAULT_NAME_HEIGHT,
				INITIAL_NAME
		);
		int dot = INITIAL_NAME.lastIndexOf('.');
		String extension = dot == -1 ? "" : INITIAL_NAME.substring(dot);
		nameWidget.setTextPredicate((text) -> text.endsWith(extension) && PathUtils.isValidString(text));
		nameWidget.setChangedListener(string -> {
			nameWidget.clearChatFormattings();
			if (hasNameChanged()) {
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

		setTopLeftButton(EntryToggleButton.createHideFromMap(
				getX(), getY(), BUTTON_SIZE, BUTTON_SIZE,
				(btn, hidden) -> ScreenshotManager.setHiddenFromMap(screenshot.pathRelativeToScreenshotDir(), hidden),
				ScreenshotManager.isHiddenFromMap(screenshot.pathRelativeToScreenshotDir())
		));

		setTopRightButton(EntryToggleButton.createFavorite(
				getRight() - BUTTON_SIZE, getY(), BUTTON_SIZE, BUTTON_SIZE,
				(btn, favorite) -> ScreenshotManager.setFavorite(screenshot.pathRelativeToScreenshotDir(), favorite),
				ScreenshotManager.isFavorite(screenshot.pathRelativeToScreenshotDir())
		));

		addRenderableChild(thumbnailWidget);
		addRenderableChild(nameWidget);
	}

	public ScreenshotEntryWidget(@NonNull Screenshot screenshot, @Nullable Consumer<Screenshot> onThumbnailClicked, @NonNull Consumer<Screenshot> onDeleteRequested) {
		this(0, 0, 0, 0, screenshot, onThumbnailClicked, onDeleteRequested);
	}

	@Override
	public void triggerLoad() {
		thumbnailWidget.load();
	}

	@Override
	protected void updateChildrenPos() {
		super.updateChildrenPos();
		thumbnailWidget.setRectangle(getWidth(), getHeight() - DEFAULT_NAME_HEIGHT, getX(), getY());
		nameWidget.setRectangle(getWidth(), DEFAULT_NAME_HEIGHT, getX(), thumbnailWidget.getBottom());
	}

	public @NonNull Screenshot getScreenshot() {
		return screenshot;
	}

	public @NonNull Metadata getMetadata() {
		return screenshot.getMetadata();
	}

	@Override
	public @NonNull String getName() {
		return nameWidget.getText();
	}

	public @NonNull String getPathRelativeToScreenshotDir() {
		return screenshot.pathRelativeToScreenshotDir();
	}

	@Override
	public long getTimestampOrLastModified() {
		Long timestamp = screenshot.getMetadata().getTimestamp();
		if (timestamp != null) {
			return timestamp;
		}
		return screenshot.file().lastModified();
	}

	@Override
	public @Nullable String getWorldName() {
		return screenshot.getMetadata().getWorldName();
	}

	@Override
	public @Nullable String getServerIp() {
		return screenshot.getMetadata().getServerIp();
	}

	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}

	@Override
	public boolean hasChanged() {
		return hasNameChanged() || hasMetadataChanged();
	}

	@Override
	public boolean hasNameChanged() {
		return !INITIAL_NAME.equals(getName());
	}

	public boolean hasMetadataChanged() {
		return !INITIAL_METADATA.equals(getMetadata());
	}

	@Override
	public void revertChanges() {
		nameWidget.setText(INITIAL_NAME);
		screenshot.setMetadata(INITIAL_METADATA);
	}

	@Override
	public void commitChanges() {
		if (hasMetadataChanged()) {
			try {
				MetadataUtils.update(screenshot.file(), screenshot.getMetadata());
			} catch (Exception e) {
				ScreenshotLogger.error("Could not save metadata for {}", screenshot.file().getName());
			}
		}
		if (hasNameChanged()) {
			Path oldPath = screenshot.file().toPath();
			Path newPath = screenshot.file().toPath().resolveSibling(getName());
			try {
				Files.move(oldPath, newPath);
				ScreenshotManager.changeAbsoluteFilePath(oldPath, newPath);
			} catch (IOException e) {
				ScreenshotLogger.error("Failed to rename screenshot file : {}", e.getMessage());
			}
		}
	}
}
