package me.Azz_9.screenshot_utilities.client.config.option.options;

import net.minecraft.network.chat.Component;

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
		FILES_ONLY("screenshot_utilities.settings.default_file_dialog_title.files_only"),
		DIRECTORIES_ONLY("screenshot_utilities.settings.default_file_dialog_title.directories_only");

		private final @NonNull String defaultDialogTitleTranslationKey;

		SelectionMode(@NonNull String defaultDialogTitleTranslationKey) {
			this.defaultDialogTitleTranslationKey = defaultDialogTitleTranslationKey;
		}

		public @NonNull String getDefaultDialogTitle() {
			return Component.translatable(defaultDialogTitleTranslationKey).getString();
		}
	}

	private final @NonNull SelectionMode selectionMode;
	private final @NonNull String dialogTitle;
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
		this.dialogTitle = builder.dialogTitle == null ? selectionMode.getDefaultDialogTitle() : builder.dialogTitle;
		this.fileExtensionFilter = builder.fileExtensionFilter;
		this.fileExtensionDescription = builder.fileExtensionDescription;
	}

	public @NonNull SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public @NonNull String getDialogTitle() {
		return dialogTitle;
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

		private @NonNull SelectionMode selectionMode = SelectionMode.FILES_ONLY;
		private @Nullable String dialogTitle;
		private @Nullable String fileExtensionFilter;
		private @Nullable String fileExtensionDescription;

		private Builder(@NonNull ConfigObject<Path> configObject) {
			super(configObject);
		}

		public @NonNull Builder selectionMode(@NonNull SelectionMode mode) {
			this.selectionMode = mode;
			return this;
		}

		public @NonNull Builder fileDialogTitle(@NonNull String title) {
			this.dialogTitle = title;
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
