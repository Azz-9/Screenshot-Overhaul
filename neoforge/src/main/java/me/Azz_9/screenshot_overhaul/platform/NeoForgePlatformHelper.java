package me.Azz_9.screenshot_overhaul.platform;

import static me.Azz_9.screenshot_overhaul.Constants.NEOFORGE;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

import me.Azz_9.screenshot_overhaul.compat.IrisCompat;
import me.Azz_9.screenshot_overhaul.platform.services.IPlatformHelper;

public class NeoForgePlatformHelper implements IPlatformHelper {

	public static IEventBus eventBus;

	@Override
	public String getPlatformName() {
		return NEOFORGE;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.getCurrent().isProduction();
	}

	@Override
	public @NonNull Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public @NonNull String getShaderName() {
		return IrisCompat.getShaderName();
	}

	@Override
	public void registerOnJoinWorldEvent(@NonNull Runnable runnable) {
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> runnable.run());
	}

	@Override
	public void registerOnStartTickEvent(@NonNull Runnable runnable) {
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> runnable.run());
	}

	@Override
	public @NonNull KeyMapping registerKeyMapping(@NonNull KeyMapping keyMapping) {
		eventBus.addListener((RegisterKeyMappingsEvent event) -> event.register(keyMapping));
		return keyMapping;
	}

	@Override
	public @Nullable ScreenRectangle scissorStackPeek(GuiGraphicsExtractor graphics) {
		return graphics.peekScissorStack();
	}
}