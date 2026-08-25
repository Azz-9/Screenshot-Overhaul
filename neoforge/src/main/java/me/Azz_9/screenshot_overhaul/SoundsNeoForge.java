package me.Azz_9.screenshot_overhaul;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import me.Azz_9.screenshot_overhaul.client.Sounds;

public final class SoundsNeoForge {
	public static final DeferredRegister<SoundEvent> SOUNDS =
			DeferredRegister.create(
					BuiltInRegistries.SOUND_EVENT,
					MOD_ID
			);

	public static final DeferredHolder<SoundEvent, SoundEvent> SHUTTER =
			SOUNDS.register(
					"shutter",
					() -> Sounds.SHUTTER
			);

	public static void register(IEventBus eventBus) {
		SOUNDS.register(eventBus);
	}
}
