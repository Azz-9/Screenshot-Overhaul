package me.Azz_9.screenshot_utilities.client.screenshot;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotMetadata {
	private @Nullable Long x;
	private @Nullable Long y;
	private @Nullable Long z;
	private @Nullable Identifier dimension;
	private @Nullable Identifier biome;
	private @Nullable String worldName;
	private @Nullable String serverIp;
	private @Nullable String version;
	private @Nullable Long timestamp;
	private @NonNull List<String> tags;

	private @Nullable Runnable onUpdate;

	public ScreenshotMetadata(@Nullable Long x, @Nullable Long y, @Nullable Long z,
	                          @Nullable Identifier dimension, @Nullable Identifier biome,
	                          @Nullable String worldName, @Nullable String serverIp,
	                          @Nullable String version,
	                          @Nullable Long timestamp, @NonNull List<String> tags) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dimension;
		this.biome = biome;
		this.worldName = worldName;
		this.serverIp = serverIp;
		this.version = version;
		this.timestamp = timestamp;
		this.tags = tags;
	}

	public static ScreenshotMetadata empty() {
		return new ScreenshotMetadata(null, null, null, null, null, null, null, null, null, new ArrayList<>());
	}

	public @Nullable Long getX() {
		return x;
	}

	public void setX(@Nullable Long x) {
		this.x = x;
	}

	public @Nullable Long getY() {
		return y;
	}

	public void setY(@Nullable Long y) {
		this.y = y;
		if (onUpdate != null) onUpdate.run();
	}

	public @Nullable Long getZ() {
		return z;
	}

	public void setZ(@Nullable Long z) {
		this.z = z;
		if (onUpdate != null) onUpdate.run();
	}

	public @Nullable Identifier getDimension() {
		return dimension;
	}

	public void setDimension(@Nullable Identifier dimension) {
		this.dimension = dimension;
		if (onUpdate != null) onUpdate.run();
	}

	public @Nullable Identifier getBiome() {
		return biome;
	}

	public void setBiome(@Nullable Identifier biome) {
		this.biome = biome;
		if (onUpdate != null) onUpdate.run();
	}

	public @Nullable String getWorldName() {
		return worldName;
	}

	public void setWorldName(@Nullable String worldName) {
		this.worldName = worldName;
		if (onUpdate != null) onUpdate.run();
	}

	public @Nullable String getServerIp() {
		return serverIp;
	}

	public void setServerIp(@Nullable String serverIp) {
		this.serverIp = serverIp;
		if (onUpdate != null) onUpdate.run();
	}

	public @Nullable String getVersion() {
		return version;
	}

	public void setVersion(@Nullable String version) {
		this.version = version;
		if (onUpdate != null) onUpdate.run();
	}

	public @Nullable Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(@Nullable Long timestamp) {
		this.timestamp = timestamp;
		if (onUpdate != null) onUpdate.run();
	}

	public @NonNull List<String> getTags() {
		return List.copyOf(tags);
	}

	public void setTags(@NonNull List<String> tags) {
		this.tags = tags;
		if (onUpdate != null) onUpdate.run();
	}

	void setOnUpdate(@Nullable Runnable onUpdate) {
		this.onUpdate = onUpdate;
	}

	@Override
	public @NonNull String toString() {
		return "ScreenshotMetadata{" +
				"x=" + x +
				", y=" + y +
				", z=" + z +
				", dimension=" + dimension +
				", biome=" + biome +
				", worldName='" + worldName + '\'' +
				", server='" + serverIp + '\'' +
				", timestamp=" + timestamp +
				", tags=" + tags +
				'}';
	}
}