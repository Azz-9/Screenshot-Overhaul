package me.Azz_9.screenshot_utilities.client.config.widget;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;

/**
 * Declares the content (sections + options) of a single settings tab.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * ConfigTabContent content = ConfigTabContent.builder()
 *     .section(Component.literal("Appearance"))
 *     .option(BooleanConfigOption.builder(myBoolObj).build())
 *     .option(EnumConfigOption.builder(myEnumObj, MyEnum.class).build())
 *     .section(Component.literal("Advanced"))
 *     .option(IntSliderConfigOption.builder(myIntObj, 0, 100).build())
 *     .build();
 * }</pre>
 */
public final class ConfigTabContent {

	/**
	 * A single entry in the tab — either a section header or an option row.
	 */
	public sealed interface Entry {
		record SectionHeader(@NonNull Component title) implements Entry {
		}

		record OptionEntry(@NonNull ConfigOption<?> option) implements Entry {
		}
	}

	private final @NonNull List<Entry> entries;

	private ConfigTabContent(@NonNull List<Entry> entries) {
		this.entries = List.copyOf(entries);
	}

	public @NonNull List<Entry> getEntries() {
		return entries;
	}

	/**
	 * Returns all options (flattened, sections excluded).
	 */
	public @NonNull List<ConfigOption<?>> getAllOptions() {
		return entries.stream()
				.filter(e -> e instanceof Entry.OptionEntry)
				.map(e -> ((Entry.OptionEntry) e).option())
				.collect(java.util.stream.Collectors.toList());
	}

	// -------------------------------------------------------------------------
	// Builder
	// -------------------------------------------------------------------------

	public static @NonNull Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private final @NonNull List<Entry> entries = new ArrayList<>();

		private Builder() {
		}

		/**
		 * Inserts a section separator with a title.
		 */
		public @NonNull Builder section(@NonNull Component title) {
			entries.add(new Entry.SectionHeader(title));
			return this;
		}

		/**
		 * Appends a config option to the current section (or to the top if no section yet).
		 */
		public @NonNull Builder option(@NonNull ConfigOption<?> option) {
			entries.add(new Entry.OptionEntry(option));
			return this;
		}

		public @NonNull ConfigTabContent build() {
			return new ConfigTabContent(entries);
		}
	}
}
