package me.Azz_9.screenshot_overhaul;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.platform.NeoForgePlatformHelper;

@Mod(Constants.MOD_ID)
public class ScreenshotOverhaul {

    public ScreenshotOverhaul(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.

		NeoForgePlatformHelper.eventBus = eventBus;

		CommonClass.initKeyMappings();
		eventBus.addListener(FMLClientSetupEvent.class, (event) -> CommonClass.init());

		ModLoadingContext.get().registerExtensionPoint(
				IConfigScreenFactory.class,
				() -> (container, parent) -> Config.getInstance().getSettingsScreen(parent)
		);
    }
}