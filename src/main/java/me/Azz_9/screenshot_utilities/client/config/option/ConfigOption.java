package me.Azz_9.screenshot_utilities.client.config.option;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import me.Azz_9.screenshot_utilities.client.config.ConfigObject;
import me.Azz_9.screenshot_utilities.client.gui.trackableChanges.TrackableChanges;

/**
 * Represents a single configuration option displayed in a settings screen.
 *
 * <p>A {@code ConfigOption} wraps a {@link ConfigObject} and maintains a working copy
 * of the value so that changes can be previewed and either committed or discarded.
 * It also carries metadata needed for rendering: label, tooltip supplier, dependencies,
 * and an optional validator.</p>
 *
 * @param <T> the type of the configuration value
 */
public interface ConfigOption<T> extends TrackableChanges {

	// -------------------------------------------------------------------------
	// Identity & metadata
	// -------------------------------------------------------------------------

	/**
	 * Human-readable label shown on the left side of the option row.
	 */
	@NonNull Component getLabel();

	/**
	 * Returns the tooltip for the current working value, or {@link Optional#empty()}
	 * if no tooltip supplier was provided.
	 */
	@NonNull Optional<Component> getTooltip();

	// -------------------------------------------------------------------------
	// Working copy
	// -------------------------------------------------------------------------

	/**
	 * The current working (unsaved) value.
	 */
	@NonNull T getWorkingValue();

	/**
	 * Updates the working value.
	 *
	 * @param value the new value; must not be {@code null}
	 */
	void setWorkingValue(@NonNull T value);

	@Override
	default boolean isValid() {
		return validate().isValid();
	}

	/**
	 * Resets the working value to the {@link ConfigObject}'s {@code defaultValue}.
	 */
	void resetToDefault();

	@NonNull T getDefaultValue();

	// -------------------------------------------------------------------------
	// Validation
	// -------------------------------------------------------------------------

	/**
	 * Validates the current working value.
	 *
	 * @return {@link ValidationResult#valid()} if the value is acceptable,
	 * or a {@link ValidationResult#invalid(Component)} describing the error
	 */
	@NonNull ValidationResult validate();

	// -------------------------------------------------------------------------
	// Dependencies
	// -------------------------------------------------------------------------

	/**
	 * Returns {@code true} if all dependencies of this option are currently satisfied
	 * (i.e. the option should be enabled and interactive).
	 */
	boolean isDependencySatisfied();

	// -------------------------------------------------------------------------
	// TrackableChanges
	// -------------------------------------------------------------------------

	/**
	 * Returns {@code true} if the working value differs from the value currently
	 * stored in the backing {@link ConfigObject}.
	 */
	@Override
	boolean hasChanged();

	/**
	 * Discards the working copy and resets it to the value currently stored
	 * in the backing {@link ConfigObject}.
	 */
	@Override
	void revertChanges();

	/**
	 * Commits the working value back to the backing {@link ConfigObject}.
	 * Call this when the user confirms they want to save.
	 */
	@Override
	void commitChanges();

	// -------------------------------------------------------------------------
	// Immutable result type for validation
	// -------------------------------------------------------------------------

	/**
	 * Holds the result of validating a config option's working value.
	 */
	record ValidationResult(boolean isValid, @Nullable Component errorMessage) {

		public static @NonNull ValidationResult valid() {
			return new ValidationResult(true, null);
		}

		public static @NonNull ValidationResult invalid(@NonNull Component errorMessage) {
			return new ValidationResult(false, errorMessage);
		}
	}
}
