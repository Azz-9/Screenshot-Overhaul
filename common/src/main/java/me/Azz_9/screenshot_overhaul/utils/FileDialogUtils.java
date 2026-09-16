package me.Azz_9.screenshot_overhaul.utils;

import static org.lwjgl.sdl.SDLDialog.*;
import static org.lwjgl.sdl.SDLProperties.*;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDL_DialogFileCallbackI;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class FileDialogUtils {

	private FileDialogUtils() {
	}

	public static CompletableFuture<Optional<Path>> openFile(String title,
															 @Nullable Path origin,
															 @Nullable String extension,
															 @Nullable String description) {
		return showDialog(SDL_FILEDIALOG_OPENFILE, title, origin, extension, description);
	}

	public static CompletableFuture<Optional<Path>> selectFolder(String title, @Nullable Path origin) {
		return showDialog(SDL_FILEDIALOG_OPENFOLDER, title, origin, null, null);
	}

	private static CompletableFuture<Optional<Path>> showDialog(int type,
																String title,
																@Nullable Path origin,
																@Nullable String extension,
																@Nullable String description) {
		CompletableFuture<Optional<Path>> future = new CompletableFuture<>();

		SDL_DialogFileFilter.Buffer filters;

		if (extension != null) {
			filters = SDL_DialogFileFilter.calloc(1);

			String pattern = extension.startsWith(".")
					? extension.substring(1)
					: extension;

			filters.get(0)
					.name(MemoryUtil.memUTF8(description != null
							? description
							: extension))
					.pattern(MemoryUtil.memUTF8(pattern));
		} else {
			filters = null;
		}

		int properties = SDL_CreateProperties();

		if (properties == 0) {
			freeFilters(filters);

			future.completeExceptionally(new IllegalStateException("Failed to create SDL dialog properties"));

			return future;
		}

		SDL_SetStringProperty(
				properties,
				SDL_PROP_FILE_DIALOG_TITLE_STRING,
				title
		);

		if (origin != null) {
			SDL_SetStringProperty(
					properties,
					SDL_PROP_FILE_DIALOG_LOCATION_STRING,
					origin.toAbsolutePath().toString()
			);
		}

		if (filters != null) {
			SDL_SetPointerProperty(
					properties,
					SDL_PROP_FILE_DIALOG_FILTERS_POINTER,
					filters.address()
			);

			SDL_SetNumberProperty(
					properties,
					SDL_PROP_FILE_DIALOG_NFILTERS_NUMBER,
					filters.capacity()
			);
		}

		SDL_DialogFileCallbackI callback =
				(_, fileList, _) -> {
					try {
						if (fileList == MemoryUtil.NULL) {
							future.completeExceptionally(new IllegalStateException("SDL file dialog failed"));
							return;
						}

						PointerBuffer files = MemoryUtil.memPointerBuffer(fileList, 1);

						long firstFile = files.get(0);

						if (firstFile == MemoryUtil.NULL) {
							future.complete(Optional.empty());
						} else {
							String path = MemoryUtil.memUTF8(firstFile);

							future.complete(Optional.of(Path.of(path)));
						}
					} catch (Throwable t) {
						future.completeExceptionally(t);
					} finally {
						SDL_DestroyProperties(properties);
						freeFilters(filters);
					}
				};

		SDL_ShowFileDialogWithProperties(type, callback, MemoryUtil.NULL, properties);

		return future;
	}

	private static void freeFilters(@Nullable SDL_DialogFileFilter.Buffer filters) {
		if (filters == null) {
			return;
		}

		for (int i = 0; i < filters.capacity(); i++) {
			SDL_DialogFileFilter filter = filters.get(i);

			MemoryUtil.memFree(filter.name());
			MemoryUtil.memFree(filter.pattern());
		}

		filters.free();
	}
}
