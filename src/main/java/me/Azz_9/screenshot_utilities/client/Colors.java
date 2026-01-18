package me.Azz_9.screenshot_utilities.client;

import net.minecraft.util.math.ColorHelper;

public final class Colors {

	public static final int WHITE = 0xFFFFFFFF;
	public static final int BLACK = 0xFF000000;
	public static final int BLACK_SEMI_TRANSPARENT = ColorHelper.withAlpha(0x7F, BLACK);
	public static final int BLACK_TRANSPARENT = ColorHelper.withAlpha(0xA0, BLACK);
	public static final int GRAY = 0xFF9C9C9C;

	private Colors() {
	}
}
