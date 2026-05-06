package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.Config;

public class ScreenshotList {
	private static final List<String> ACCEPTED_SCREENSHOT_FILE_EXTENSIONS = List.of(".png");

	private static final List<Screenshot> screenshots = new ArrayList<>();
	private static final Object lock = new Object();

	private static volatile boolean loaded = false;
	private static volatile boolean loading = false;

	private static Consumer<List<Screenshot>> onChangeListener = null;

	private ScreenshotList() {
	}

	public static void loadAsync() {
		synchronized (lock) {
			if (loading || loaded) return;
			loading = true;
		}
		runAsync("screenshot-loader", ScreenshotList::startWatchService);
	}

	public static void reloadAsync() {
		synchronized (lock) {
			if (!loaded) return;
			loaded = false;
		}
		runAsync("screenshot-reloader", ScreenshotList::notifyChange);
	}

	private static void runAsync(String threadName, Runnable onComplete) {
		Thread thread = new Thread(() -> {
			List<Screenshot> result;
			try {
				result = Files.walk(Config.getInstance().getScreenshotsDir())
						.filter(ScreenshotList::isScreenshot)
						.map(path -> {
							File file = path.toFile();
							ScreenshotMetadata metadata;
							try {
								metadata = ScreenshotMetadataUtils.read(file);
							} catch (Exception e) {
								metadata = ScreenshotMetadata.empty();
							}
							return new Screenshot(file, metadata);
						})
						.collect(Collectors.toCollection(ArrayList::new));
			} catch (IOException e) {
				ScreenshotLogger.warn("Could not load screenshots: {}", e.getMessage());
				result = new ArrayList<>();
			}

			synchronized (lock) {
				screenshots.clear();
				screenshots.addAll(result);
				loaded = true;
			}

			onComplete.run();
		}, threadName);
		thread.setDaemon(true);
		thread.start();
	}

	public static void whenLoaded(Consumer<List<Screenshot>> consumer) {
		synchronized (lock) {
			if (loaded) {
				consumer.accept(Collections.unmodifiableList(screenshots));
			} else {
				// exécuté à la fin du chargement via notifyChange, on enregistre temporairement
				Consumer<List<Screenshot>> previous = onChangeListener;
				onChangeListener = list -> {
					consumer.accept(list);
					onChangeListener = previous;
				};
			}
		}
	}

	public static List<Screenshot> getScreenshots() {
		synchronized (lock) {
			if (!loaded) return Collections.emptyList();
			return List.copyOf(screenshots);
		}
	}

	public static void setOnChangeListener(Consumer<List<Screenshot>> listener) {
		synchronized (lock) {
			onChangeListener = listener;
		}
	}

	public static boolean isLoaded() {
		return loaded;
	}

	private static void notifyChange() {
		Consumer<List<Screenshot>> listener;
		List<Screenshot> snapshot;
		synchronized (lock) {
			listener = onChangeListener;
			snapshot = List.copyOf(screenshots);
		}
		if (listener != null) {
			MINECRAFT.execute(() -> listener.accept(snapshot));
		}
	}

	private static void startWatchService() {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Config.getInstance().getScreenshotsDir().register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE
			);

			Thread watchThread = new Thread(() -> {
				try {
					while (true) {
						WatchKey key = watcher.take();

						for (WatchEvent<?> event : key.pollEvents()) {
							Path changed = (Path) event.context();
							Path fullPath = Config.getInstance().getScreenshotsDir().resolve(changed);

							if (!isScreenshot(fullPath)) continue;

							if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
								File file = fullPath.toFile();
								ScreenshotMetadata metadata;
								try {
									metadata = ScreenshotMetadataUtils.read(file);
								} catch (Exception e) {
									metadata = ScreenshotMetadata.empty();
								}
								Screenshot screenshot = new Screenshot(file, metadata);
								synchronized (lock) {
									screenshots.add(screenshot);
								}
							} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
								synchronized (lock) {
									screenshots.removeIf(s -> s.file().toPath().equals(fullPath));
								}
							}

							notifyChange();
						}

						key.reset();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					try {
						watcher.close();
					} catch (IOException ignored) {
					}
				}
			}, "screenshot-watcher");
			watchThread.setDaemon(true);
			watchThread.start();

		} catch (IOException e) {
			ScreenshotLogger.warn("Could not initialize watch service: {}", e.getMessage());
		}
	}

	private static boolean isScreenshot(Path path) {
		String name = path.getFileName().toString().toLowerCase();
		return ACCEPTED_SCREENSHOT_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
	}
}
