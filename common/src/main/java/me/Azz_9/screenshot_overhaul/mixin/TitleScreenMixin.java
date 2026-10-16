package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.SCREENSHOT_SPRITE;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.gui.screen.PanoramaGalleryScreen;
import me.Azz_9.screenshot_overhaul.client.gui.screen.ScreenshotGalleryScreen;
import me.Azz_9.screenshot_overhaul.compat.CompatManager;

@Mixin(value = TitleScreen.class, priority = 1100)
public abstract class TitleScreenMixin extends Screen {

	@Unique
	private static final int screenshot_overhaul$MARGIN = 4;
	@Unique
	private static final int screenshot_overhaul$SIZE = 20;
	@Unique
	private static final int screenshot_overhaul$BUTTONS_WIDTH = 200;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	// add buttons on the title screen
	@Inject(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
					ordinal = 0
			)
	)
	public void addClassicButton(CallbackInfo info, @Local(name = "topPos") int topPos) {
		if (Config.getInstance().titleScreenButtonStyle.getValue() == Config.TitleScreenButtonStyle.CLASSIC) {
			int screenshotButtonX = (this.width - screenshot_overhaul$BUTTONS_WIDTH) / 2;

			for (GuiEventListener child : this.children()) {
				if (child instanceof AbstractWidget widget
						&& widget.getX() == screenshotButtonX
						&& widget.getWidth() == screenshot_overhaul$BUTTONS_WIDTH) {
					widget.setY(widget.getY() - 24);
				}
			}

			this.addRenderableWidget(
					Button.builder(
									Component.translatable("screenshot_overhaul.options.screenshots"),
									_ -> MINECRAFT.setScreen(new ScreenshotGalleryScreen(this))
							)
							.bounds(screenshotButtonX, topPos, screenshot_overhaul$BUTTONS_WIDTH, screenshot_overhaul$SIZE)
							.build()
			);
		}

		this.addRenderableWidget(Button.builder(
						Component.translatable("screenshot_overhaul.options.change_panorama"),
						_ -> MINECRAFT.setScreen(new PanoramaGalleryScreen(Component.empty(), this))
				)
				.bounds(width - 100 - 10, 10, 100, 20)
				.build());
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void addIconButton(CallbackInfo ci) {
		if (Config.getInstance().titleScreenButtonStyle.getValue() != Config.TitleScreenButtonStyle.ICON) return;
		int buttonsX = (this.width - screenshot_overhaul$BUTTONS_WIDTH) / 2;

		int screenshotButtonX = 10;
		int screenshotButtonY = 10;

		for (int i = children().size() - 1; i >= 0; i--) {
			GuiEventListener child = children().get(i);
			if (child instanceof AbstractWidget widget && widget.getX() == buttonsX && widget.getWidth() == screenshot_overhaul$BUTTONS_WIDTH) {
				screenshotButtonY = widget.getY();

				// right
				if (!CompatManager.modMenuPresent() || !widget.getMessage().contains(Component.translatable("menu.online"))) {
					screenshotButtonX = widget.getRight() + screenshot_overhaul$MARGIN;
					if (!screenshot_overhaul$overlapsWithAnyChildren(screenshotButtonX, screenshotButtonY, screenshot_overhaul$SIZE, screenshot_overhaul$SIZE)) {
						break;
					}
				}

				// left
				screenshotButtonX = widget.getX() - screenshot_overhaul$MARGIN - screenshot_overhaul$SIZE;
				if (!screenshot_overhaul$overlapsWithAnyChildren(screenshotButtonX, screenshotButtonY, screenshot_overhaul$SIZE, screenshot_overhaul$SIZE)) {
					break;
				}
			}
		}

		SpriteIconButton screenshotViewerButton = this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_overhaul.options.screenshots_toolitp"),
								_ -> MINECRAFT.setScreen(new ScreenshotGalleryScreen(this)),
								true
						)
						.withTootip()
						.size(screenshot_overhaul$SIZE, screenshot_overhaul$SIZE)
						.sprite(SCREENSHOT_SPRITE, 15, 15)
						.build()
		);

		screenshotViewerButton.setPosition(screenshotButtonX, screenshotButtonY);
	}

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(II)V"), index = 1)
	private int adjustRealmsHeight(int height) {
		if (Config.getInstance().titleScreenButtonStyle.getValue() == Config.TitleScreenButtonStyle.CLASSIC) {
			return height - 51;
		} else {
			return height;
		}
	}

	@Unique
	private boolean screenshot_overhaul$overlapsWithAnyChildren(int x, int y, int width, int height) {
		for (GuiEventListener child : children()) {
			if (child instanceof AbstractWidget widget && screenshot_overhaul$overlaps(x, y, width, height, widget)) {
				return true;
			}
		}

		return false;
	}

	@Unique
	private boolean screenshot_overhaul$overlaps(int x, int y, int width, int height, @NotNull AbstractWidget widget) {
		return !(x + width < widget.getX() || x > widget.getRight() || y + height < widget.getY() || y > widget.getBottom());
	}
}
