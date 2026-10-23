package me.Azz_9.screenshot_overhaul.client.guiFlexLib.components.gallery;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

import me.Azz_9.flex_lib.client.gui.components.Div;
import me.Azz_9.flex_lib.client.gui.style.Length;
import me.Azz_9.flex_lib.client.gui.style.StyleProperties;
import me.Azz_9.screenshot_overhaul.client.gui.trackableChanges.TrackableChanges;
import me.Azz_9.screenshot_overhaul.utils.PathUtils;

public abstract class AbstractGalleryEntryWidget extends Div implements TrackableChanges {

	private @Nullable Div topLeftButton;
	private @Nullable Div topRightButton;

	private final @NonNull Path parentFolder;

	public AbstractGalleryEntryWidget(@NotNull Path parentFolder) {
		this.parentFolder = parentFolder;
	}

	protected abstract boolean hasNameChanged();

	public abstract void triggerLoad();

	public abstract @NonNull String getName();

	public abstract @NonNull String getPathRelativeToScreenshotDir();

	public abstract long getTimestampOrLastModified();

	public abstract @Nullable String getWorldName();

	public abstract @Nullable String getServerIp();

	protected @NonNull Path getParentFolder() {
		return parentFolder;
	}

	@Override
	public boolean isValid() {
		return PathUtils.isValidFileName(getName()) && (!PathUtils.exists(getParentFolder(), getName()) || !hasNameChanged());
	}

	public void setTopLeftButton(@NotNull Div topLeftButton) {
		setTopButton(topLeftButton, true);
	}

	public void setTopRightButton(@NotNull Div topRightButton) {
		setTopButton(topRightButton, false);
	}

	public void setTopButton(@NotNull Div topButton, boolean left) {
		Div button;
		if (left) button = topLeftButton;
		else button = topRightButton;

		if (button != null) remove(button);

		if (left) {
			topButton.declaredStyle().set(StyleProperties.LEFT, Length.zero());
			topLeftButton = topButton;
		} else {
			topButton.declaredStyle().set(StyleProperties.RIGHT, Length.zero());
			topRightButton = topButton;
		}
	}
}
