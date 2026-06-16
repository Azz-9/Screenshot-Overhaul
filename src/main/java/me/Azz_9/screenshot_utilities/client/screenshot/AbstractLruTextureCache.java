package me.Azz_9.screenshot_utilities.client.screenshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractLruTextureCache<K, T extends AutoCloseable> {

	private final List<Map<K, T>> caches = new ArrayList<>();

	protected Map<K, T> createCache(int maxEntries) {
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

	protected T get(Map<K, T> cache, K key, Function<K, T> loader) {
		return cache.computeIfAbsent(key, loader);
	}

	public synchronized void clear() {
		for (Map<K, T> cache : caches) {
			clearMap(cache);
		}
	}

	private void clearMap(Map<K, T> map) {
		map.values().forEach(this::safeClose);
		map.clear();
	}

	private void safeClose(T value) {
		try {
			value.close();
		} catch (Exception ignored) {
		}
	}
}
