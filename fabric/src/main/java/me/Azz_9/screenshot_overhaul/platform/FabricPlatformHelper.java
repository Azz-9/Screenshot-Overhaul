package me.Azz_9.screenshot_overhaul.platform;

import static me.Azz_9.screenshot_overhaul.Constants.FABRIC;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

import me.Azz_9.screenshot_overhaul.compat.IrisCompat;
import me.Azz_9.screenshot_overhaul.platform.services.IPlatformHelper;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
		return FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

	@Override
	public @NonNull Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	public @NonNull String getShaderName() {
		return IrisCompat.getShaderName();
	}

	@Override
	public void registerOnJoinWorldEvent(@NonNull Runnable runnable) {
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> runnable.run());
	}

	@Override
	public void registerOnStartTickEvent(@NonNull Runnable runnable) {
		ClientTickEvents.START_CLIENT_TICK.register(_ -> runnable.run());
	}

	@Override
	public @NonNull KeyMapping registerKeyMapping(@NonNull KeyMapping keyMapping) {
		return KeyMappingHelper.registerKeyMapping(keyMapping);
	}

	@Override
	public @Nullable ScreenRectangle scissorStackPeek(GuiGraphicsExtractor graphics) {
		return graphics.scissorStack.peek();
	}
}
