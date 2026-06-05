package me.Azz_9.screenshot_utilities.client.screenshot.panorama;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;

/**
 * Représente un panorama complet ou partiel : 6 faces identifiées par le même UUID.
 * Les faces sont indexées 0-5 ; une face absente est null.
 */
public record Panorama(@NonNull UUID id, @NonNull String folderName, Screenshot[] faces) {

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
	public List<Screenshot> presentFaces() {
		List<Screenshot> result = new ArrayList<>();
		for (Screenshot face : faces) {
			if (face != null) result.add(face);
		}
		return result;
	}
}