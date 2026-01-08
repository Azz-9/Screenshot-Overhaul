package me.Azz_9.screenshot_utilities.client.gui.widget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

public class NavigationButton extends ButtonWidget {

	private static final ButtonTextures TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("widget/button"),
			Identifier.ofVanilla("widget/button_disabled"),
			Identifier.ofVanilla("widget/button_highlighted")
	);

	private static final Identifier BACK_TEXTURE = Identifier.of(MOD_ID, "icon/arrow_left");
	private static final Identifier NEXT_TEXTURE = Identifier.of(MOD_ID, "icon/arrow_right");
	@NotNull
	private final NavigationButton.NavigationTypes navigation;

	public NavigationButton(int x, int y, int width, int height, @NotNull NavigationButton.NavigationTypes navigation, PressAction onPress) {
		super(x, y, width, height, navigation == NavigationTypes.BACK
						? net.minecraft.text.Text.translatable("screenshot_utilities.navigation.back")
						: net.minecraft.text.Text.translatable("screenshot_utilities.navigation.next"),
				onPress, DEFAULT_NARRATION_SUPPLIER);
		this.navigation = navigation;
	}

	@Override
	protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		this.drawButton(context);

		if (navigation == NavigationTypes.BACK) {
			context.drawGuiTexture(
					RenderPipelines.GUI_TEXTURED,
					BACK_TEXTURE,
					getX(), getY(),
					getWidth(), getHeight()
			);
		} else {
			context.drawGuiTexture(
					RenderPipelines.GUI_TEXTURED,
					NEXT_TEXTURE,
					getX(), getY(),
					getWidth(), getHeight()
			);
		}
	}

	public enum NavigationTypes {
		BACK,
		NEXT,
	}
}
