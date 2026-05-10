package me.Azz_9.screenshot_utilities.client.config.option;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import me.Azz_9.screenshot_utilities.client.config.ConfigObject;

/**
 * Base implementation of {@link ConfigOption}.
 *
 * <p>Manages the working copy, the validator, the tooltip supplier, and the
 * dependency list. Concrete subclasses only need to concern themselves with
 * type-specific logic (widget creation, etc.).</p>
 *
 * @param <T> the type of the configuration value
 */
public abstract class AbstractConfigOption<T> implements ConfigOption<T> {

	private final @NonNull ConfigObject<T> configObject;
	private final @NonNull Component label;
	private final @Nullable Function<T, Component> tooltipSupplier;
	private final @Nullable Predicate<T> validator;
	private final @Nullable Component validationErrorMessage;
	private final @NonNull List<Supplier<Boolean>> dependencies;

	/**
	 * The mutable working copy — never touches the original until {@link #commitChanges()}.
	 */
	private @NonNull T workingValue;

	protected AbstractConfigOption(
			@NonNull ConfigObject<T> configObject,
			@NonNull Component label,
			@Nullable Function<T, Component> tooltipSupplier,
			@Nullable Predicate<T> validator,
			@Nullable Component validationErrorMessage,
			@NonNull List<Supplier<Boolean>> dependencies
	) {
		this.configObject = configObject;
		this.label = label;
		this.tooltipSupplier = tooltipSupplier;
		this.validator = validator;
		this.validationErrorMessage = validationErrorMessage;
		this.dependencies = List.copyOf(dependencies);
		this.workingValue = configObject.getValue();
	}

	// -------------------------------------------------------------------------
	// ConfigOption
	// -------------------------------------------------------------------------

	@Override
	public @NonNull Component getLabel() {
		return label;
	}

	@Override
	public @NonNull Optional<Component> getTooltip() {
		if (tooltipSupplier == null) return Optional.empty();
		return Optional.ofNullable(tooltipSupplier.apply(workingValue));
	}

	@Override
	public @NonNull T getWorkingValue() {
		return workingValue;
	}

	@Override
	public void setWorkingValue(@NonNull T value) {
		this.workingValue = value;
	}

	@Override
	public void resetToDefault() {
		this.workingValue = configObject.getDefaultValue();
	}

	@Override
	public ConfigOption.@NonNull ValidationResult validate() {
		if (validator == null) return ConfigOption.ValidationResult.valid();
		if (!validator.test(workingValue)) {
			Component msg = validationErrorMessage != null
					? validationErrorMessage
					: Component.translatable("screenshot_utilities.settings.validation.invalid");
			return ConfigOption.ValidationResult.invalid(msg);
		}
		return ConfigOption.ValidationResult.valid();
	}

	@Override
	public boolean isDependencySatisfied() {
		return dependencies.stream().allMatch(Supplier::get);
	}

	@Override
	public boolean hasChanged() {
		return !workingValue.equals(configObject.getValue());
	}

	@Override
	public void revertChanges() {
		this.workingValue = configObject.getValue();
	}

	@Override
	public void commitChanges() {
		configObject.setValue(workingValue);
	}

	// -------------------------------------------------------------------------
	// Accessor for subclasses
	// -------------------------------------------------------------------------

	protected @NonNull ConfigObject<T> getConfigObject() {
		return configObject;
	}

	// -------------------------------------------------------------------------
	// Builder base
	// -------------------------------------------------------------------------

	/**
	 * Fluent builder base shared by all concrete option builders.
	 *
	 * @param <T> the value type
	 * @param <O> the concrete {@link AbstractConfigOption} subtype
	 * @param <B> the concrete builder subtype (for covariant return)
	 */
	@SuppressWarnings("unchecked")
	public abstract static class Builder<T, O extends AbstractConfigOption<T>, B extends Builder<T, O, B>> {

		public final @NonNull ConfigObject<T> configObject;
		public @NonNull Component label;
		public @Nullable Function<T, Component> tooltipSupplier;
		public @Nullable Predicate<T> validator;
		public @Nullable Component validationErrorMessage;
		public final @NonNull List<Supplier<Boolean>> dependencies = new ArrayList<>();

		protected Builder(@NonNull ConfigObject<T> configObject) {
			this.configObject = configObject;
			this.label = configObject.getTranslationText();
		}

		/**
		 * Overrides the display label (defaults to the config object's translation key).
		 */
		public @NonNull B label(@NonNull Component label) {
			this.label = label;
			return (B) this;
		}

		/**
		 * Supplies a per-value tooltip shown on widget hover.
		 *
		 * @param supplier receives the current working value and returns the tooltip text
		 */
		public @NonNull B tooltip(@NonNull Function<T, Component> supplier) {
			this.tooltipSupplier = supplier;
			return (B) this;
		}

		/**
		 * Convenience overload for a static tooltip that never changes.
		 */
		public @NonNull B tooltip(@NonNull Component staticTooltip) {
			return tooltip(ignored -> staticTooltip);
		}

		/**
		 * Attaches a validator. The option is considered invalid when the predicate
		 * returns {@code false} for the current working value.
		 *
		 * @param validator              predicate returning {@code true} for valid values
		 * @param validationErrorMessage error text shown when invalid
		 */
		public @NonNull B validate(@NonNull Predicate<T> validator, @NonNull Component validationErrorMessage) {
			this.validator = validator;
			this.validationErrorMessage = validationErrorMessage;
			return (B) this;
		}

		/**
		 * Adds a dependency. The option is disabled unless all dependency suppliers
		 * return {@code true}.
		 */
		public @NonNull B dependsOn(@NonNull Supplier<Boolean> dependency) {
			this.dependencies.add(dependency);
			return (B) this;
		}

		/**
		 * Convenience: depends on another {@link ConfigOption} being a specific value.
		 */
		public @NonNull <D> B dependsOn(@NonNull ConfigOption<D> other, @NonNull D requiredValue) {
			return dependsOn(() -> requiredValue.equals(other.getWorkingValue()));
		}

		/**
		 * Convenience: depends on a boolean option being {@code true}.
		 */
		public @NonNull B dependsOn(@NonNull ConfigOption<Boolean> booleanOption) {
			return dependsOn(booleanOption::getWorkingValue);
		}

		public abstract @NonNull O build();
	}
}
