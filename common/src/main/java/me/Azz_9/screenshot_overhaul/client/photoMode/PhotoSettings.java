package me.Azz_9.screenshot_overhaul.client.photoMode;

import net.minecraft.util.Mth;

public class PhotoSettings {
	private int exposure;
	private int contrast;
	private int saturation;
	private int temperature;
	private int brightness;
	private int vignette;

	public PhotoSettings(int exposure, int contrast, int saturation, int temperature, int brightness, int vignette) {
		this.exposure = Mth.clamp(exposure, 0, 100);
		this.contrast = Mth.clamp(contrast, 0, 100);
		this.saturation = Mth.clamp(saturation, 0, 100);
		this.temperature = Mth.clamp(temperature, 0, 100);
		this.brightness = Mth.clamp(brightness, 0, 100);
		this.vignette = Mth.clamp(vignette, 0, 100);
	}

	public PhotoSettings copy() {
		return new PhotoSettings(
				this.exposure,
				this.contrast,
				this.saturation,
				this.temperature,
				this.brightness,
				this.vignette
		);
	}

	public void copyFrom(PhotoSettings other) {
		this.exposure = other.exposure;
		this.contrast = other.contrast;
		this.saturation = other.saturation;
		this.temperature = other.temperature;
		this.brightness = other.brightness;
		this.vignette = other.vignette;
	}

	public int exposure() {
		return this.exposure;
	}

	public int contrast() {
		return this.contrast;
	}

	public int saturation() {
		return this.saturation;
	}

	public int temperature() {
		return this.temperature;
	}

	public int brightness() {
		return this.brightness;
	}

	public int vignette() {
		return this.vignette;
	}
}
