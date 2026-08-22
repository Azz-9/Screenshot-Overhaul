package me.Azz_9.screenshot_overhaul.client.config.option.options;

import static me.Azz_9.screenshot_overhaul.client.config.Config.MAX_WINDOW_SIZE;
import static me.Azz_9.screenshot_overhaul.client.config.Config.MIN_WINDOW_SIZE;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.config.ConfigObject;
import me.Azz_9.screenshot_overhaul.client.config.option.AbstractConfigOption;
import me.Azz_9.screenshot_overhaul.client.config.option.ConfigOption;

/**
 * A {@link ConfigOption} backed by a {@link Config.Resolution2D} value.
 */
public final class ResolutionConfigOption extends AbstractConfigOption<Config.Resolution2D> {

	private final @NonNull List<ResolutionPreset> presets;

	private ResolutionConfigOption(@NonNull Builder builder) {
		super(
				builder.configObject,
				builder.label,
				builder.tooltipSupplier,
				builder.validator,
				builder.validationErrorMessage,
				builder.dependencies
		);
		presets = builder.presets;
	}

	public @NonNull List<ResolutionPreset> getPresets() {
		return presets;
	}

	public static @NonNull Builder builder(@NonNull ConfigObject<Config.Resolution2D> configObject) {
		return new Builder(configObject);
	}

	public static final class Builder extends AbstractConfigOption.Builder<Config.Resolution2D, ResolutionConfigOption, Builder> {

		private final @NonNull List<ResolutionPreset> presets = new ArrayList<>();

		private Builder(@NonNull ConfigObject<Config.Resolution2D> configObject) {
			super(configObject);
		}

		public @NonNull Builder addPreset(@NotNull Config.Resolution2D resolution, @NonNull Component label) {
			presets.add(new ResolutionPreset(resolution, label));
			return this;
		}

		@Override
		public @NonNull ResolutionConfigOption build() {
			validate(
					res -> res.width() >= MIN_WINDOW_SIZE && res.width() <= MAX_WINDOW_SIZE &&
							res.height() >= MIN_WINDOW_SIZE && res.height() <= MAX_WINDOW_SIZE,
					Component.translatable("screenshot_overhaul.settings.validation.out_of_range", MIN_WINDOW_SIZE, MAX_WINDOW_SIZE)
			);
			return new ResolutionConfigOption(this);
		}
	}

	public record ResolutionPreset(@NotNull Config.Resolution2D resolution, @NonNull Component label) {
	}
}
