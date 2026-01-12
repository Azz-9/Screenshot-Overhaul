package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.api.widget.TextFieldAccessor;
import me.Azz_9.screenshot_utilities.client.gui.widget.screenshot.ScreenshotNameWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget implements TextFieldAccessor {

	@Shadow
	private int textX;
	@Shadow
	private int textY;
	@Shadow
	private int firstCharacterIndex;

	public TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
	}

	@Override
	public int screenshotUtilities$getTextX() {
		return textX;
	}

	@Override
	public int screenshotUtilities$getTextY() {
		return textY;
	}

	@Shadow
	public abstract String getText();

	@Unique
	public void recenter() {
		TextRenderer textRenderer = CLIENT.textRenderer;
		this.textX = getX() + (getWidth() - textRenderer.getWidth(getText())) / 2;
		this.textY = getY() + (getHeight() - textRenderer.fontHeight) / 2;
	}

	@Inject(method = "updateTextPosition", at = @At("TAIL"))
	private void autoRecenter(CallbackInfo ci) {
		Object self = this;

		if (self instanceof ScreenshotNameWidget) {
			this.recenter();
		}
	}

	@Redirect(
			method = "renderWidget",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/font/TextRenderer;trimToWidth(Ljava/lang/String;I)Ljava/lang/String;"
			)
	)
	private String disableTrimOnRender(TextRenderer renderer, String text, int maxWidth) {
		Object self = this;

		if (self instanceof ScreenshotNameWidget) {
			return text;
		}

		return renderer.trimToWidth(text, maxWidth);
	}

	@Redirect(
			method = "updateTextPosition",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/font/TextRenderer;trimToWidth(Ljava/lang/String;I)Ljava/lang/String;"
			)
	)
	private String disableTrimOnUpdateTextPosition(TextRenderer renderer, String text, int maxWidth) {
		Object self = this;

		if (self instanceof ScreenshotNameWidget) {
			return text;
		}

		return renderer.trimToWidth(text, maxWidth);
	}
}