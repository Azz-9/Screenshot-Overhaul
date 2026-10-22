package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.SCREENSHOT_SPRITE;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.config.Config;
import me.Azz_9.screenshot_overhaul.client.gui.screen.ScreenshotGalleryScreen;

@Mixin(value = TitleScreen.class, priority = 1100)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

	@Shadow
	private @Nullable RealmsNotificationsScreen realmsNotificationsScreen;
	@Unique
	private static final int screenshot_overhaul$SIZE = 20;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;getHorizontalPosition(III)I"))
	private int replaceInlinedConstant(TitleScreen instance, int currentButton, int numberOfButtons, int buttonWidth, Operation<Integer> original, @Local(name = "numberOfButtons") int actualNumberOfButtons) {
		return original.call(instance, currentButton, actualNumberOfButtons, buttonWidth);
	}

	// Réserve un slot dans la rangée d'icônes uniquement en mode ICON
	@Definition(id = "numberOfButtons", local = @Local(type = int.class, name = "numberOfButtons"))
	@Expression("numberOfButtons = ?")
	@Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
	private void reserveSlot(CallbackInfo ci, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons) {
		if (Config.getInstance().titleScreenButtonStyle.getValue() == Config.ScreenButtonStyle.ICON) {
			numberOfButtons.set(numberOfButtons.get() + 1);
		}
	}

	// Ajoute les boutons sur le title screen
	@Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/TitleScreen;width:I")
	@Expression("this.width / 2 - 100")
	@Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	public void addButton(
			CallbackInfo ci,
			@Local(name = "currentButton") LocalIntRef currentButton,
			@Local(name = "topPos") int topPos,
			@Local(name = "numberOfButtons") int numberOfButtons
	) {
		switch (Config.getInstance().titleScreenButtonStyle.getValue()) {
			case ICON -> {
				currentButton.set(currentButton.get() + 1);

				this.addRenderableWidget(
						SpriteIconButton.TextAndIcon.builder(
										Component.translatable("screenshot_overhaul.options.screenshots_toolitp"),
										_ -> {
											if (MINECRAFT.hasShiftDown()) {
												MINECRAFT.gui.setScreen();
											} else {
												MINECRAFT.gui.setScreen(new ScreenshotGalleryScreen(this));
											}
										},
										true
								)
								.withTootip()
								.size(screenshot_overhaul$SIZE, screenshot_overhaul$SIZE)
								.sprite(SCREENSHOT_SPRITE, 15, 15)
								.build()
				).setPosition(
						this.getHorizontalPosition(currentButton.get(), numberOfButtons, screenshot_overhaul$SIZE),
						topPos
				);
			}
			case CLASSIC -> {
				int mainX = this.width / 2 - 100;
				for (GuiEventListener child : this.children()) {
					if (child instanceof AbstractWidget widget
							&& widget.getX() == mainX
							&& widget.getWidth() == 200) {
						widget.setY(widget.getY() - 24);
					}
				}

				this.addRenderableWidget(Button.builder(
								Component.translatable("screenshot_overhaul.options.screenshots"),
								_ -> MINECRAFT.gui.setScreen(new ScreenshotGalleryScreen(this))
						)
						.bounds(mainX, topPos - 24, 200, 20)
						.build());
			}
		}

	}

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(II)V"), index = 1)
	private int adjustRealmsHeight(int height) {
		if (Config.getInstance().titleScreenButtonStyle.getValue() == Config.ScreenButtonStyle.CLASSIC) {
			return height - 51;
		} else {
			return height;
		}
	}
}
