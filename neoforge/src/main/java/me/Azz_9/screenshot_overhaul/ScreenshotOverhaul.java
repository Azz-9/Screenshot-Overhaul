package me.Azz_9.screenshot_overhaul;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.List;

import me.Azz_9.screenshot_overhaul.client.config.Config;

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

		ModLoadingContext.get().registerExtensionPoint(
				IConfigScreenFactory.class,
				() -> (container, parent) -> Config.getInstance().getSettingsScreen(parent)
		);
    }
}