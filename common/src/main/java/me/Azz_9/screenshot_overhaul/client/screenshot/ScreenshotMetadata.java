package me.Azz_9.screenshot_overhaul.client.screenshot;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScreenshotMetadata {
	private @Nullable Integer x;
	private @Nullable Integer y;
	private @Nullable Integer z;
	private @Nullable Identifier dimension;
	private @Nullable Identifier biome;
	private @Nullable Long seed;
	private @Nullable String worldName;
	private @Nullable String serverIp;
	private @Nullable String version;
	private @NonNull List<String> resourcePacks;
	private @Nullable String shader;
	private @Nullable Long timestamp;
	private @Nullable String panoramaId;
	private @Nullable Integer panoramaFace;

	public ScreenshotMetadata(@Nullable Integer x, @Nullable Integer y, @Nullable Integer z,
	                          @Nullable Identifier dimension, @Nullable Identifier biome,
	                          @Nullable Long seed, @Nullable String worldName,
	                          @Nullable String serverIp, @Nullable String version,
	                          @NonNull List<String> resourcePacks, @Nullable String shader,
	                          @Nullable Long timestamp,
							  @Nullable String panoramaId, @Nullable Integer panoramaFace) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dimension;
		this.biome = biome;
		this.seed = seed;
		this.worldName = worldName;
		this.serverIp = serverIp;
		this.version = version;
		this.resourcePacks = resourcePacks;
		this.shader = shader;
		this.timestamp = timestamp;
		this.panoramaId = panoramaId;
		this.panoramaFace = panoramaFace;
	}

	public static @NonNull ScreenshotMetadata empty() {
		return new ScreenshotMetadata(null, null, null, null, null, null,
				null, null, null, new ArrayList<>(), null, null,
				null, null);
	}

	public static @NonNull ScreenshotMetadata copyOf(@NonNull ScreenshotMetadata from) {
		return new ScreenshotMetadata(from.x, from.y, from.z, from.dimension, from.biome, from.seed, from.worldName,
				from.serverIp, from.version, from.resourcePacks, from.shader, from.timestamp, from.panoramaId, from.panoramaFace);
	}

	public @Nullable Integer getX() {
		return x;
	}

	public void setX(@Nullable Integer x) {
		this.x = x;
	}

	public @Nullable Integer getY() {
		return y;
	}

	public void setY(@Nullable Integer y) {
		this.y = y;
	}

	public @Nullable Integer getZ() {
		return z;
	}

	public void setZ(@Nullable Integer z) {
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

	public @Nullable Long getSeed() {
		return seed;
	}

	public void setSeed(@Nullable Long seed) {
		this.seed = seed;
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

	public @Nullable String getShader() {
		return shader;
	}

	public void setShader(@Nullable String shader) {
		this.shader = shader;
	}

	public @Nullable Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(@Nullable Long timestamp) {
		this.timestamp = timestamp;
	}

	public @Nullable String getPanoramaId() {
		return panoramaId;
	}

	public void setPanoramaId(@Nullable String panoramaId) {
		this.panoramaId = panoramaId;
	}

	public @Nullable Integer getPanoramaFace() {
		return panoramaFace;
	}

	public void setPanoramaFace(@Nullable Integer panoramaFace) {
		this.panoramaFace = panoramaFace;
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
				Objects.equals(getSeed(), that.getSeed()) &&
				Objects.equals(getWorldName(), that.getWorldName()) &&
				Objects.equals(getServerIp(), that.getServerIp()) &&
				Objects.equals(getVersion(), that.getVersion()) &&
				Objects.equals(getResourcePacks(), that.getResourcePacks()) &&
				Objects.equals(getShader(), that.getShader()) &&
				Objects.equals(getTimestamp(), that.getTimestamp()) &&
				Objects.equals(getPanoramaId(), that.getPanoramaId()) &&
				Objects.equals(getPanoramaFace(), that.getPanoramaFace());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getX(), getY(), getZ(), getDimension(), getBiome(), getSeed(), getWorldName(),
				getServerIp(), getVersion(), getResourcePacks(), getShader(), getTimestamp(), getPanoramaId(), getPanoramaFace());
	}

	@Override
	public @NonNull String toString() {
		return "ScreenshotMetadata{" +
				"x=" + x +
				", y=" + y +
				", z=" + z +
				", dimension=" + dimension +
				", biome=" + biome +
				", seed=" + seed +
				", worldName='" + worldName + '\'' +
				", server='" + serverIp + '\'' +
				", version='" + version + '\'' +
				", resourcePacks='" + resourcePacks + '\'' +
				", shader='" + shader + '\'' +
				", timestamp=" + timestamp +
				", panoramaId=" + panoramaId +
				", panoramaFace=" + panoramaFace +
				'}';
	}
}