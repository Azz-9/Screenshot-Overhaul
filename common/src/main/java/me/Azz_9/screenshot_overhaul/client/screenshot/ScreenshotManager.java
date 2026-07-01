package me.Azz_9.screenshot_overhaul.client.screenshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import me.Azz_9.screenshot_overhaul.ScreenshotLogger;
import me.Azz_9.screenshot_overhaul.client.config.Config;

public class ScreenshotManager {

	private static final @NonNull String STATES_FILE_NAME = "screenshot-states.json";

	private static @NonNull Path statesFile = Config.getInstance().getAbsoluteScreenshotsDir().resolve(STATES_FILE_NAME);

	private static @NonNull HashMap<String, ScreenshotState> states = new HashMap<>();

	public static void load() {
		if (!Files.exists(statesFile)) return;

		ScreenshotLogger.info("Loading screenshot-states.json");
		try (Reader reader = Files.newBufferedReader(statesFile)) {
			Type type = new TypeToken<HashMap<String, ScreenshotState>>() {
			}.getType();
			HashMap<String, ScreenshotState> result = new Gson().fromJson(reader, type);
			states = result != null ? result : new HashMap<>();
		} catch (IOException e) {
			ScreenshotLogger.error("Failed to load screenshot-states.json {}", e.getMessage());
		}
	}

	public static void save() {
		ScreenshotLogger.info("Saving screenshot-states.json");
		try {
			Files.createDirectories(statesFile.getParent());
			try (Writer writer = Files.newBufferedWriter(statesFile)) {
				new GsonBuilder().setPrettyPrinting().create().toJson(states, writer);
			}
		} catch (IOException e) {
			ScreenshotLogger.error("Failed to save favorites {}", e.getMessage());
		}
	}

	public static void setFavorite(@NonNull String filePathRelativeToScreenshotDir, boolean favorite) {
		if (states.containsKey(filePathRelativeToScreenshotDir)) {
			states.get(filePathRelativeToScreenshotDir).favorite = favorite;
		} else {
			states.put(filePathRelativeToScreenshotDir, new ScreenshotState(favorite, false));
		}
	}

	public static void setHiddenFromMap(@NonNull String filePathRelativeToScreenshotDir, boolean hiddenFromMap) {
		if (states.containsKey(filePathRelativeToScreenshotDir)) {
			states.get(filePathRelativeToScreenshotDir).hiddenFromMap = hiddenFromMap;
		} else {
			states.put(filePathRelativeToScreenshotDir, new ScreenshotState(false, hiddenFromMap));
		}
	}

	public static boolean isFavorite(@NonNull String filePathRelativeToScreenshotDir) {
		if (!states.containsKey(filePathRelativeToScreenshotDir)) return false;
		return states.get(filePathRelativeToScreenshotDir).favorite;
	}

	public static boolean isHiddenFromMap(@NonNull String filePathRelativeToScreenshotDir) {
		if (!states.containsKey(filePathRelativeToScreenshotDir)) return false;
		return states.get(filePathRelativeToScreenshotDir).hiddenFromMap;
	}

	public static void remove(@NonNull String filePath) {
		states.remove(filePath);
	}

	public static void changeFilePath(@NonNull String oldFilePathRelativeToScreenshotDir, @NonNull String newFilePathRelativeToScreenshotDir) {
		ScreenshotState state = states.remove(oldFilePathRelativeToScreenshotDir);
		if (state != null) {
			states.put(newFilePathRelativeToScreenshotDir, state);
		}
	}

	public static void changeFilePath(@NonNull Path oldFilePathRelativeToScreenshotDir, @NonNull Path newFilePathRelativeToScreenshotDir) {
		changeFilePath(oldFilePathRelativeToScreenshotDir.toString(), newFilePathRelativeToScreenshotDir.toString());
	}

	public static void changeAbsoluteFilePath(@NonNull Path oldAbsoluteFilePath, @NonNull Path newAbsoluteFilePath) {
		changeFilePath(
				Config.getInstance().getAbsoluteScreenshotsDir().relativize(oldAbsoluteFilePath),
				Config.getInstance().getAbsoluteScreenshotsDir().relativize(newAbsoluteFilePath)
		);
	}

	public static void onScreenshotDirectoryChanged(@NonNull Path path) {
		statesFile = path.resolve(STATES_FILE_NAME);
	}

	private static class ScreenshotState {
		private boolean favorite;
		private boolean hiddenFromMap;

		public ScreenshotState(boolean favorite, boolean hiddenFromMap) {
			this.favorite = favorite;
			this.hiddenFromMap = hiddenFromMap;
		}

		@Override
		public String toString() {
			return "ScreenshotState{" +
					"favorite=" + favorite +
					", hiddenFromMap=" + hiddenFromMap +
					'}';
		}
	}
}
