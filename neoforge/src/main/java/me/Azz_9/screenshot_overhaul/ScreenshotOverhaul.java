package me.Azz_9.screenshot_overhaul;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.ArrayList;
import java.util.List;

@Mod(Constants.MOD_ID)
public class ScreenshotOverhaul {

	public static final List<KeyMapping> KEY_MAPPINGS = new ArrayList<>();

    public ScreenshotOverhaul(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.

		CommonClass.initKeyMappings();
		eventBus.addListener(FMLClientSetupEvent.class, (event) -> CommonClass.init());
		eventBus.addListener(RegisterKeyMappingsEvent.class, (event) -> KEY_MAPPINGS.forEach(event::register));
    }
}