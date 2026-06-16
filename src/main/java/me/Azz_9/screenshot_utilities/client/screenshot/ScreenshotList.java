package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.screenshot.panorama.Panorama.firstFace;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import me.Azz_9.screenshot_utilities.ScreenshotLogger;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.screenshot.panorama.Panorama;

public class ScreenshotList {
	private static final List<String> ACCEPTED_SCREENSHOT_FILE_EXTENSIONS = List.of(".png");
	private static final int MAX_READ_ATTEMPTS = 10;
	private static final int READ_DELAY_MS = 50;

	private static final List<Screenshot> screenshots = new ArrayList<>();
	private static final Object lock = new Object();

	private static volatile boolean loaded = false;
	private static volatile boolean loading = false;

	private static Consumer<List<Screenshot>> onChangeListener = null;

	private static final Map<WatchKey, Path> watchedDirs = new HashMap<>();
	private static WatchService currentWatcher;
	private static Thread currentWatchThread;


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
				result = Files.walk(Config.getInstance().getAbsoluteScreenshotsDir())
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

	public static void whenScreenshotsLoaded(Consumer<List<Screenshot>> onLoaded) {
		whenLoadedInternal(onLoaded, ScreenshotList::getScreenshots);
	}

	public static void whenPanoramasLoaded(Consumer<List<Panorama>> onLoaded) {
		whenLoadedInternal(onLoaded, ScreenshotList::getPanoramas);
	}

	private static <T> void whenLoadedInternal(Consumer<T> onLoaded, Supplier<T> supplier) {
		synchronized (lock) {
			if (loaded) {
				onLoaded.accept(supplier.get());
			} else {
				Consumer<List<Screenshot>> previous = onChangeListener;
				onChangeListener = list -> {
					onLoaded.accept(supplier.get());
					onChangeListener = previous;
				};
			}
		}
	}

	public static @NonNull List<Screenshot> getScreenshots() {
		synchronized (lock) {
			if (!loaded) return Collections.emptyList();
			return List.copyOf(screenshots);
		}
	}

	/**
	 * Retourne la liste des panoramas déduits des métadonnées, triés par date de la face 0
	 * (ou à défaut la première face présente), du plus récent au plus ancien.
	 */
	public static List<Panorama> getPanoramas() {
		synchronized (lock) {
			if (!loaded) return Collections.emptyList();

			Map<UUID, Screenshot[]> groups = new LinkedHashMap<>();

			for (Screenshot screenshot : screenshots) {
				String rawId = screenshot.getMetadata().getPanoramaId();
				Integer face = screenshot.getMetadata().getPanoramaFace();
				if (rawId == null || face == null) continue;

				UUID id;
				try {
					id = UUID.fromString(rawId);
				} catch (IllegalArgumentException e) {
					continue; // métadonnée corrompue
				}

				if (face < 0 || face > 5) continue;

				groups.computeIfAbsent(id, k -> new Screenshot[6])[face] = screenshot;
			}

			return groups.entrySet().stream()
					.filter(e -> firstFace(e.getValue()).isPresent())
					.map(e -> {
						Screenshot[] faces = e.getValue();
						File folder = firstFace(faces)
								.map(s -> s.file().getParentFile())
								.orElse(null);
						return new Panorama(e.getKey(), folder, faces);
					})
					.sorted(Comparator.comparing(
							p -> firstFace(p.faces()).map(s -> s.file().lastModified()).orElse(0L),
							Comparator.reverseOrder()
					))
					.collect(Collectors.toCollection(ArrayList::new));
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

	public static void changeDirectory(Path newRoot) {
		synchronized (lock) {
			if (!loaded) return;
			loaded = false;
			stopWatchService();
		}

		runAsync("screenshot-loader", ScreenshotList::startWatchService);
	}

	private static void stopWatchService() {
		if (currentWatchThread != null) {
			currentWatchThread.interrupt();
			currentWatchThread = null;
		}
		try {
			if (currentWatcher != null) {
				currentWatcher.close();
				currentWatcher = null;
			}
		} catch (IOException ignored) {}
		watchedDirs.clear();
	}

	private static void startWatchService() {
		try {
			synchronized (lock) {
				currentWatcher = FileSystems.getDefault().newWatchService();
			}

			Path root = Config.getInstance().getAbsoluteScreenshotsDir();

			Files.walk(root)
					.filter(Files::isDirectory)
					.forEach(dir -> {
						try {
							registerDirectory(currentWatcher, dir);
						} catch (IOException e) {
							ScreenshotLogger.warn("Could not watch directory {}: {}", dir, e.getMessage());
						}
					});

			Thread watchThread = new Thread(() -> {
				try {
					while (true) {
						WatchKey key = currentWatcher.take();
						Path dir;
						synchronized (lock) {
							dir = watchedDirs.get(key);
						}
						if (dir == null) {
							key.reset();
							continue;
						}

						for (WatchEvent<?> event : key.pollEvents()) {
							Path changed = (Path) event.context();
							Path fullPath = dir.resolve(changed);

							if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
								if (Files.isDirectory(fullPath)) {
									try {
										registerDirectory(currentWatcher, fullPath);
									} catch (IOException e) {
										ScreenshotLogger.warn("Could not watch directory {}: {}", fullPath, e.getMessage());
										continue;
									}
								}

								if (!isScreenshot(fullPath)) continue;

								File file = fullPath.toFile();
								ScreenshotMetadata metadata;
								metadata = readMetadataWithRetry(file);
								Screenshot screenshot = new Screenshot(file, metadata);
								synchronized (lock) {
									screenshots.add(screenshot);
								}
							} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
								synchronized (lock) {
									watchedDirs.entrySet().removeIf(e -> e.getValue().startsWith(fullPath));
									screenshots.removeIf(s -> s.file().toPath().startsWith(fullPath));
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
						currentWatcher.close();
					} catch (IOException ignored) {
					}
				}
			}, "screenshot-watcher");
			watchThread.setDaemon(true);
			synchronized (lock) {
				currentWatchThread = watchThread;
			}
			watchThread.start();

		} catch (IOException e) {
			ScreenshotLogger.warn("Could not initialize watch service: {}", e.getMessage());
		}
	}

	private static void registerDirectory(WatchService watcher, Path dir) throws IOException {
		WatchKey key = dir.register(watcher,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE
		);
		watchedDirs.put(key, dir);
	}

	private static ScreenshotMetadata readMetadataWithRetry(File file) {
		for (int i = 0; i < MAX_READ_ATTEMPTS; i++) {
			try {
				// Check if the file is fully written by comparing its size on two read
				long sizeBefore = file.length();
				Thread.sleep(READ_DELAY_MS);
				long sizeAfter = file.length();

				if (sizeAfter == 0 || sizeBefore != sizeAfter) continue;

				return ScreenshotMetadataUtils.read(file);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				// File not readable yet, retry
			}
		}

		return ScreenshotMetadata.empty();
	}

	private static boolean isScreenshot(Path path) {
		String name = path.getFileName().toString().toLowerCase();
		return ACCEPTED_SCREENSHOT_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
	}
}
