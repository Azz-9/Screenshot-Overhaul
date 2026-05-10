package me.Azz_9.screenshot_utilities.client.config.option.options;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

import me.Azz_9.screenshot_utilities.client.config.ConfigObject;
import me.Azz_9.screenshot_utilities.client.config.option.AbstractConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;

/**
 * A {@link ConfigOption} backed by a {@link Path} value.
 *
 * <p>Rendered as a read-only text field showing the path string, plus a "Browse…"
 * button that opens a {@link javax.swing.JFileChooser} via AWT.</p>
 */
public final class PathConfigOption extends AbstractConfigOption<Path> {

	/**
	 * Controls what the file-chooser allows the user to select.
	 */
	public enum SelectionMode {
		FILES_ONLY,
		DIRECTORIES_ONLY,
		FILES_AND_DIRECTORIES
	}

	private final @NonNull SelectionMode selectionMode;
	private final @Nullable String fileExtensionFilter;
	private final @Nullable String fileExtensionDescription;

	private PathConfigOption(Builder builder) {
		super(
				builder.configObject,
				builder.label,
				builder.tooltipSupplier,
				builder.validator,
				builder.validationErrorMessage,
				builder.dependencies
		);
		this.selectionMode = builder.selectionMode;
		this.fileExtensionFilter = builder.fileExtensionFilter;
		this.fileExtensionDescription = builder.fileExtensionDescription;
	}

	public @NonNull SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public @Nullable String getFileExtensionFilter() {
		return fileExtensionFilter;
	}

	public @Nullable String getFileExtensionDescription() {
		return fileExtensionDescription;
	}

	// -------------------------------------------------------------------------
	// Builder
	// -------------------------------------------------------------------------

	public static @NonNull Builder builder(@NonNull ConfigObject<Path> configObject) {
		return new Builder(configObject);
	}

	public static final class Builder extends AbstractConfigOption.Builder<Path, PathConfigOption, Builder> {

		private @NonNull SelectionMode selectionMode = SelectionMode.FILES_AND_DIRECTORIES;
		private @Nullable String fileExtensionFilter;
		private @Nullable String fileExtensionDescription;

		private Builder(@NonNull ConfigObject<Path> configObject) {
			super(configObject);
		}

		public @NonNull Builder selectionMode(@NonNull SelectionMode mode) {
			this.selectionMode = mode;
			return this;
		}

		/**
		 * Restricts the file chooser to files with the given extension (e.g. {@code "png"}).
		 *
		 * @param extension   lowercase extension without the leading dot
		 * @param description human-readable description shown in the chooser
		 */
		public @NonNull Builder filterExtension(@NonNull String extension, @NonNull String description) {
			this.fileExtensionFilter = extension;
			this.fileExtensionDescription = description;
			return this;
		}

		@Override
		public @NonNull PathConfigOption build() {
			return new PathConfigOption(this);
		}
	}
}
