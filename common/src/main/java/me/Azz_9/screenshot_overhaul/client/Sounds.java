package me.Azz_9.screenshot_overhaul.client;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class Sounds {
	public static final Identifier SHUTTER_ID = Identifier.fromNamespaceAndPath(MOD_ID, "shutter");
	public static final SoundEvent SHUTTER = SoundEvent.createVariableRangeEvent(SHUTTER_ID);

	public static void register() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, SHUTTER_ID, SHUTTER);
	}
}
