package me.Azz_9.screenshot_utilities.client.config;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {
	private static final Gson GSON = new GsonBuilder()
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
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");

	public static void save() throws IOException {
		JsonObject root = new JsonObject();
		collectFields(root);
		Files.writeString(CONFIG_FILE, GSON.toJson(root));
	}

	public static void load() throws IOException {
		if (!Files.exists(CONFIG_FILE)) return;
		JsonObject root = JsonParser.parseString(Files.readString(CONFIG_FILE)).getAsJsonObject();
		applyFields(root);
	}

	private static void collectFields(JsonObject root) {
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

	private static void applyFields(JsonObject root) {
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
	private static <T> void setSavableValue(SavableObject<T> savable, Object value) {
		savable.setValue((T) value);
	}
}
