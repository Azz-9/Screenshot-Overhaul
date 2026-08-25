package me.Azz_9.screenshot_overhaul.client;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

public class CommonSprites {
	public static final @NonNull Identifier SCREENSHOT_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/screenshot");
	public static final @NonNull Identifier FOLDER_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/folder");
	public static final @NonNull Identifier ARROW_LEFT_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/arrow_left");
	public static final @NonNull Identifier ARROW_RIGHT_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/arrow_right");
	public static final @NonNull Identifier ARROW_BACK_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/back_arrow");
	public static final @NonNull Identifier COPY_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/copy");
	public static final @NonNull Identifier DELETE_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/delete");
	public static final @NonNull Identifier SETTINGS_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/cogwheel");
	public static final @NonNull Identifier CLOSE_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/close");
	public static final @NonNull Identifier RESET_SPRITE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/reset");
}
