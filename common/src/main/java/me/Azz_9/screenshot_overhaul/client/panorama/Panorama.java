package me.Azz_9.screenshot_overhaul.client.panorama;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.*;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;

/**
 * Représente un panorama complet ou partiel : 6 faces identifiées par le même UUID.
 * Les faces sont indexées 0-5 ; une face absente est null.
 */
public record Panorama(@NonNull UUID id, @NonNull File folder, @Nullable Screenshot[] faces,
                       @NonNull String pathRelativeToScreenshotDir) {
	public Panorama(@NonNull UUID id, @NonNull File folder, @Nullable Screenshot[] faces,
					@NonNull String pathRelativeToScreenshotDir) {
		this.id = id;
		this.folder = folder;
		this.faces = faces;
		this.pathRelativeToScreenshotDir = pathRelativeToScreenshotDir;
	}

	public Panorama(@NonNull UUID id, @NonNull File folder, @Nullable Screenshot[] faces) {
		this(id, folder, faces, Config.getInstance().getAbsoluteScreenshotsDir().relativize(folder.toPath()).toString());
	}


	/**
	 * @return true si les 6 faces sont présentes
	 */
	public boolean isComplete() {
		for (Screenshot face : faces) {
			if (face == null) return false;
		}
		return true;
	}

	/**
	 * @return les faces non-null, dans l'ordre
	 */
	public @NonNull List<Screenshot> presentFaces() {
		List<Screenshot> result = new ArrayList<>();
		for (Screenshot face : faces) {
			if (face != null) result.add(face);
		}
		return result;
	}

	public @NonNull String folderName() {
		return folder.getName();
	}

	public static @NonNull Optional<Screenshot> firstFace(Screenshot[] faces) {
		for (Screenshot face : faces) {
			if (face != null) return Optional.of(face);
		}
		return Optional.empty();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Panorama panorama)) return false;
		return Objects.equals(id, panorama.id) && Objects.equals(folder, panorama.folder) && Objects.deepEquals(faces, panorama.faces);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, folder, Arrays.hashCode(faces));
	}
}