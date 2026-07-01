package me.Azz_9.screenshot_overhaul.platform.services;

import static me.Azz_9.screenshot_overhaul.Constants.FABRIC;
import static me.Azz_9.screenshot_overhaul.Constants.NEOFORGE;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
	@NonNull String getPlatformName();

	default boolean isFabric() {
		return getPlatformName().equals(FABRIC);
	}

	default boolean isNeoForge() {
		return getPlatformName().equals(NEOFORGE);
	}

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

	@NonNull Path getConfigDir();

	@NonNull String getShaderName();

	void registerOnJoinWorldEvent(@NonNull Runnable runnable);

	void registerOnStartTickEvent(@NonNull Runnable runnable);

	@NonNull KeyMapping registerKeyMapping(@NonNull KeyMapping keyMapping);

	@Nullable ScreenRectangle scissorStackPeek(GuiGraphicsExtractor graphics);
}