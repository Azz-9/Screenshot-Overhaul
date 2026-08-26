package me.Azz_9.screenshot_overhaul.client.config.option;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import me.Azz_9.screenshot_overhaul.client.config.ConfigObject;
import me.Azz_9.screenshot_overhaul.client.gui.trackableChanges.TrackableChanges;

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

	@NonNull Component getLabel();

	@NonNull Optional<Component> getTooltip();

	@NonNull T getWorkingValue();

	void setWorkingValue(@NonNull T value);

	@Override
	default boolean isValid() {
		return validate().isValid();
	}

	void resetToDefault();

	@NonNull T getDefaultValue();

	@NonNull ValidationResult validate();

	boolean isDependencySatisfied();

	@Override
	boolean hasChanged();

	@Override
	void revertChanges();

	@Override
	void commitChanges();

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
