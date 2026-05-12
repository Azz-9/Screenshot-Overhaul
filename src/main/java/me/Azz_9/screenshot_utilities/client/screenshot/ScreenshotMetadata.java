package me.Azz_9.screenshot_utilities.client.screenshot;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScreenshotMetadata {
	private @Nullable Long x;
	private @Nullable Long y;
	private @Nullable Long z;
	private @Nullable Identifier dimension;
	private @Nullable Identifier biome;
	private @Nullable String worldName;
	private @Nullable String serverIp;
	private @Nullable String version;
	private @NonNull List<String> resourcePacks;
	private @Nullable Long timestamp;
	private @NonNull List<String> tags;

	public ScreenshotMetadata(@Nullable Long x, @Nullable Long y, @Nullable Long z,
	                          @Nullable Identifier dimension, @Nullable Identifier biome,
	                          @Nullable String worldName, @Nullable String serverIp,
	                          @Nullable String version, @NonNull List<String> resourcePacks,
	                          @Nullable Long timestamp, @NonNull List<String> tags) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dimension;
		this.biome = biome;
		this.worldName = worldName;
		this.serverIp = serverIp;
		this.version = version;
		this.resourcePacks = resourcePacks;
		this.timestamp = timestamp;
		this.tags = tags;
	}

	public static ScreenshotMetadata empty() {
		return new ScreenshotMetadata(null, null, null, null, null, null, null, null, new ArrayList<>(), null, new ArrayList<>());
	}

	public static ScreenshotMetadata copyOf(@NonNull ScreenshotMetadata from) {
		return new ScreenshotMetadata(from.x, from.y, from.z, from.dimension, from.biome, from.worldName, from.serverIp,
				from.version, from.resourcePacks, from.timestamp, from.tags);
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
	}

	public @Nullable Long getZ() {
		return z;
	}

	public void setZ(@Nullable Long z) {
		this.z = z;
	}

	public @Nullable Identifier getDimension() {
		return dimension;
	}

	public void setDimension(@Nullable Identifier dimension) {
		this.dimension = dimension;
	}

	public @Nullable Identifier getBiome() {
		return biome;
	}

	public void setBiome(@Nullable Identifier biome) {
		this.biome = biome;
	}

	public @Nullable String getWorldName() {
		return worldName;
	}

	public void setWorldName(@Nullable String worldName) {
		this.worldName = worldName;
	}

	public @Nullable String getServerIp() {
		return serverIp;
	}

	public void setServerIp(@Nullable String serverIp) {
		this.serverIp = serverIp;
	}

	public @Nullable String getVersion() {
		return version;
	}

	public void setVersion(@Nullable String version) {
		this.version = version;
	}

	public @NonNull List<String> getResourcePacks() {
		return resourcePacks;
	}

	public void setResourcePacks(@NonNull List<String> resourcePacks) {
		this.resourcePacks = resourcePacks;
	}

	public @Nullable Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(@Nullable Long timestamp) {
		this.timestamp = timestamp;
	}

	public @NonNull List<String> getTags() {
		return List.copyOf(tags);
	}

	public void setTags(@NonNull List<String> tags) {
		this.tags = tags;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ScreenshotMetadata that = (ScreenshotMetadata) o;
		return Objects.equals(getX(), that.getX()) &&
				Objects.equals(getY(), that.getY()) &&
				Objects.equals(getZ(), that.getZ()) &&
				Objects.equals(getDimension(), that.getDimension()) &&
				Objects.equals(getBiome(), that.getBiome()) &&
				Objects.equals(getWorldName(), that.getWorldName()) &&
				Objects.equals(getServerIp(), that.getServerIp()) &&
				Objects.equals(getVersion(), that.getVersion()) &&
				Objects.equals(getResourcePacks(), that.getResourcePacks()) &&
				Objects.equals(getTimestamp(), that.getTimestamp()) &&
				Objects.equals(getTags(), that.getTags());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getX(), getY(), getZ(), getDimension(), getBiome(), getWorldName(), getServerIp(), getVersion(), getResourcePacks(), getTimestamp(), getTags());
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
				", version='" + version + '\'' +
				", resourcePacks='" + resourcePacks + '\'' +
				", timestamp=" + timestamp +
				", tags=" + tags +
				'}';
	}
}