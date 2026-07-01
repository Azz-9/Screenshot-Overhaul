package me.Azz_9.screenshot_overhaul.client.gui.trackableChanges;

public interface TrackableChanges {

	/**
	 * Returns {@code true} if the working value differs from the persisted value.
	 */
	boolean hasChanged();

	/**
	 * Discards the working value and resets it to the persisted value.
	 */
	void revertChanges();

	/**
	 * Commits the working value to the persisted value.
	 */
	void commitChanges();

	/**
	 * Returns {@code true} if the current working value is valid and can be committed.
	 * Defaults to {@code true} -- override to add validation logic.
	 */
	default boolean isValid() {
		return true;
	}
}
