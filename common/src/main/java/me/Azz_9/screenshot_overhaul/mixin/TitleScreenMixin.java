package me.Azz_9.screenshot_overhaul.mixin;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.Azz_9.screenshot_overhaul.client.gui.screen.PanoramaGalleryScreen;
import me.Azz_9.screenshot_overhaul.client.gui.screen.ScreenshotGalleryScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

	@Unique
	private static final int SIZE = 20;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;getHorizontalPosition(III)I"))
	private int replaceInlinedConstant(TitleScreen instance, int currentButton, int numberOfButtons, int buttonWidth, Operation<Integer> original, @Local(name = "numberOfButtons") int actualNumberOfButtons) {
		return original.call(instance, currentButton, actualNumberOfButtons, buttonWidth);
	}

	@Definition(id = "numberOfButtons", local = @Local(type = int.class, name = "numberOfButtons"))
	@Expression("numberOfButtons = ?")
	@Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
	private void reserveSlot(CallbackInfo ci, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons) {
		numberOfButtons.set(numberOfButtons.get() + 1);
	}

	// add buttons on the title screen
	@Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/TitleScreen;width:I")
	@Expression("this.width / 2 - 100")
	@Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	public void addButton(CallbackInfo ci,
	                      @Local(name = "currentButton") LocalIntRef currentButton,
	                      @Local(name = "topPos") int topPos,
	                      @Local(name = "numberOfButtons") int numberOfButtons) {

		currentButton.set(currentButton.get() + 1);

		this.addRenderableWidget(
				SpriteIconButton.TextAndIcon.builder(
								Component.translatable("screenshot_overhaul.options.screenshots"),
								(btn) -> MINECRAFT.gui.setScreen(new ScreenshotGalleryScreen(this)),
								true
						)
						.withTootip()
						.size(SIZE, SIZE)
						.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/screenshot"), 15, 15)
						.build()
		).setPosition(
				this.getHorizontalPosition(currentButton.get(), numberOfButtons, SIZE),
				topPos
		);

		this.addRenderableWidget(Button.builder(
						Component.translatable("screenshot_overhaul.options.change_panorama"),
						(btn) -> MINECRAFT.gui.setScreen(new PanoramaGalleryScreen(Component.empty(), this))
				)
				.bounds(width - 100 - 10, 10, 100, 20)
				.build());
	}
}
