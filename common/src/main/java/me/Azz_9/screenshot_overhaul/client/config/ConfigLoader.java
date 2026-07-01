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
					return Path.of(in.nextString());
				}
			})
			.create();
	private static final @NonNull Path CONFIG_FILE = Services.PLATFORM.getConfigDir().resolve(MOD_ID + ".json");

	public static void save() throws IOException {
		ScreenshotLogger.info("Saving config...");
		JsonObject root = new JsonObject();
		collectFields(root);
		Files.writeString(CONFIG_FILE, GSON.toJson(root));
	}

	public static void trySave() {
		try {
			save();
		} catch (IOException e) {
			ScreenshotLogger.error("Failed to save config file : {}", e.getMessage());
		}
	}

	public static void load() throws IOException {
		ScreenshotLogger.info("Loading config...");
		if (!Files.exists(CONFIG_FILE)) return;
		JsonObject root = JsonParser.parseString(Files.readString(CONFIG_FILE)).getAsJsonObject();
		applyFields(root);
	}

	private static void collectFields(@NonNull JsonObject root) {
		for (Field field : Config.getInstance().getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object val = field.get(Config.getInstance());
				if (!(val instanceof SavableObject<?> savable)) continue;

				String key = field.getName();
				root.add(key, GSON.toJsonTree(savable.getValue()));
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to read field: " + field.getName(), e);
			}
		}
	}

	private static void applyFields(@NonNull JsonObject root) {
		for (Field field : Config.getInstance().getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object val = field.get(Config.getInstance());
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
