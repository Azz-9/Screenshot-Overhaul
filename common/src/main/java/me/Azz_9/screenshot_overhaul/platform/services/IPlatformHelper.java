package me.Azz_9.screenshot_overhaul.platform.services;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.ClientPacketListener;

import org.apache.logging.log4j.util.TriConsumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

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