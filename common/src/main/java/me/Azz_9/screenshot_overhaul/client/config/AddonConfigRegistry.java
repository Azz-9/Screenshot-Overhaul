package me.Azz_9.screenshot_overhaul.client.config;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import me.Azz_9.screenshot_overhaul.api.SettingsContext;
import me.Azz_9.screenshot_overhaul.client.gui.components.config.ConfigTabContent;

public final class AddonConfigRegistry {
	public record Entry(@NonNull Component label, @NonNull Function<SettingsContext, ConfigTabContent> contentSupplier,
	                    @Nullable Runnable onSave) {
	}

	private static final List<Entry> entries = new ArrayList<>();

	private AddonConfigRegistry() {
	}

	public static void register(@NonNull Component label, @NonNull Function<SettingsContext, ConfigTabContent> contentSupplier, @Nullable Runnable onSave) {
		entries.add(new Entry(label, contentSupplier, onSave));
	}

	public static @NonNull List<Entry> getEntries() {
		return List.copyOf(entries);
	}
}