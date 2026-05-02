package me.Azz_9.screenshot_utilities.client.screenshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.Config;

public class FavoriteManager {
	private static final Path FAVORITES_FILE = Config.getInstance().getScreenshotsDir().resolve("favorites.json");

	private static Set<String> favorites = new HashSet<>();

	public static void load() {
		if (!Files.exists(FAVORITES_FILE)) return;

		try (Reader reader = Files.newBufferedReader(FAVORITES_FILE)) {
			Type type = new TypeToken<Set<String>>() {
			}.getType();
			Set<String> result = new Gson().fromJson(reader, type);
			favorites = result != null ? result : new HashSet<>();
		} catch (IOException e) {
			ScreenshotLogger.error("Failed to load favorites.json {}", e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(FAVORITES_FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FAVORITES_FILE)) {
				new GsonBuilder().setPrettyPrinting().create().toJson(favorites, writer);
			}
		} catch (IOException e) {
			ScreenshotLogger.error("Failed to save favorites {}", e);
		}
	}

	public static void setFavorite(String filePath, boolean favorite) {
		if (favorite) {
			favorites.add(filePath);
		} else {
			favorites.remove(filePath);
		}
	}

	public static boolean isFavorite(String filePath) {
		return favorites.contains(filePath);
	}

	public static void changeFilePath(String oldFilePath, String newFilePath) {
		if (favorites.remove(oldFilePath)) {
			favorites.add(newFilePath);
		}
	}

	public static void changeFilePath(Path oldFilePath, Path newFilePath) {
		changeFilePath(oldFilePath.toString(), newFilePath.toString());
	}

	public static void changeAbsoluteFilePath(Path oldFilePath, Path newFilePath) {
		changeFilePath(
				Config.getInstance().getScreenshotsDir().relativize(oldFilePath),
				Config.getInstance().getScreenshotsDir().relativize(newFilePath)
		);
	}
}