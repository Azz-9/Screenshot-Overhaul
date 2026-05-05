package me.Azz_9.screenshot_utilities.client.screenshot;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.minecraft.util.Mth;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record ScreenshotMetadata(Integer x, Integer y, Integer z, String dimension, String biome, String worldName,
                                 String server, Long timestamp, @NonNull List<String> tags) {
}