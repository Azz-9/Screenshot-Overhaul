package me.Azz_9.screenshot_overhaul.client.screenshot;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractLruTextureCache<K, T extends AutoCloseable> {

	private final @NonNull List<Map<K, T>> caches = new ArrayList<>();

	protected @NonNull Map<K, T> createCache(int maxEntries) {
		LinkedHashMap<K, T> cache = new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, T> eldest) {
				if (size() > maxEntries) {
					try {
						eldest.getValue().close();
					} catch (Exception ignored) {
					}
					return true;
				}
				return false;
			}
		};

		caches.add(cache);
		return cache;
	}

	protected @NonNull T get(@NonNull Map<K, T> cache, @NonNull K key, @NonNull Function<K, T> loader) {
		return cache.computeIfAbsent(key, loader);
	}

	public synchronized void clear() {
		for (Map<K, T> cache : caches) {
			clearMap(cache);
		}
	}

	private void clearMap(@NonNull Map<K, T> map) {
		map.values().forEach(this::safeClose);
		map.clear();
	}

	private void safeClose(@NonNull T value) {
		try {
			value.close();
		} catch (Exception ignored) {
		}
	}
}
