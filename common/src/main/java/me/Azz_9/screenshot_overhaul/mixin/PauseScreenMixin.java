package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.SCREENSHOT_SPRITE;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.gui.screen.ScreenshotGalleryScreen;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

	@Unique
	private static final int SIZE = 20;

	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Definition(id = "integratedServer", local = @Local(type = IntegratedServer.class, name = "integratedServer"))
	@Expression("integratedServer = ?")
	@Inject(method = "createPauseMenu", at = @At("MIXINEXTRAS:EXPRESSION"))
	private void insertIconButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
		iconButtonRow.addChild(SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_overhaul.options.screenshots"),
						(btn) -> MINECRAFT.gui.setScreen(new ScreenshotGalleryScreen(this)),
								true
						)
						.withTootip()
				.size(SIZE, SIZE)
				.sprite(SCREENSHOT_SPRITE, 15, 15)
				.build());
	}
}
