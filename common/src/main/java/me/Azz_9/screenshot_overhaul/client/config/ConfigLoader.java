package me.Azz_9.screenshot_overhaul.client.config;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.platform.Services;


public class ConfigLoader {
	private static final @NonNull Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.registerTypeHierarchyAdapter(Path.class, new TypeAdapter<Path>() {
				@Override
				public void write(JsonWriter out, Path value) throws IOException {
					if (value == null) out.nullValue();
					else out.value(value.toString());
				}

				@Override
				public Path read(JsonReader in) throws IOException {
					if (in.peek() == JsonToken.NULL) {
						in.nextNull();
						return null;
					}
					try {
						return Path.of(in.nextString());
					} catch (Exception e) {
						return null;
					}
				}
			})
			.create();
	private static final @NonNull Path CONFIG_FILE = Services.PLATFORM.getConfigDir().resolve(MOD_ID + ".json");

	public static void save(@NonNull Object holder, @NonNull Path file) throws IOException {
		ScreenshotLogger.info("Saving config...");
		JsonObject root = new JsonObject();
		collectFields(holder, root);
		Files.writeString(file, GSON.toJson(root));
	}

	public static void save() throws IOException {
		save(Config.getInstance(), CONFIG_FILE);
	}

	public static void trySave(@NonNull Object holder, @NonNull Path file) {
		try {
			save(holder, file);
		} catch (IOException e) {
			ScreenshotLogger.error("Failed to save config file : {}", e.getMessage());
		}
	}

	public static void trySave() {
		trySave(Config.getInstance(), CONFIG_FILE);
	}

	public static void load(@NonNull Object holder, @NonNull Path file) throws IOException {
		ScreenshotLogger.info("Loading config...");
		if (!Files.exists(file)) {
			ScreenshotLogger.info("Config file does not exist, creating a new one...");
			trySave(holder, file);
			return;
		}
		applyFields(holder, JsonParser.parseString(Files.readString(file)).getAsJsonObject());
	}

	public static void load() throws IOException {
		load(Config.getInstance(), CONFIG_FILE);
	}

	private static void collectFields(@NonNull Object holder, @NonNull JsonObject root) {
		for (Field field : holder.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object val = field.get(holder);
				if (!(val instanceof SavableObject<?> savable)) continue;

				String key = field.getName();
				root.add(key, GSON.toJsonTree(savable.getValue()));
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to read field: " + field.getName(), e);
			}
		}
	}

	private static void applyFields(@NonNull Object holder, @NonNull JsonObject root) {
		for (Field field : holder.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object val = field.get(holder);
				if (!(val instanceof SavableObject<?> savable)) continue;

				String key = field.getName();
				if (!root.has(key)) continue;

				Object deserialized = GSON.fromJson(root.get(key), savable.getValueType());
				setSavableValue(savable, deserialized);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to write field: " + field.getName(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void setSavableValue(@NonNull SavableObject<T> savable, @Nullable Object value) {
		savable.setValue((T) value);
	}
}
