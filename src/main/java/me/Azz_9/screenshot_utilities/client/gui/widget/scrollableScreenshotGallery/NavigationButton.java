package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class NavigationButton extends Button {

	private static final @NonNull Identifier BACK_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/arrow_left");
	private static final @NonNull Identifier NEXT_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/arrow_right");

	private final @NonNull NavigationButton.NavigationType navigation;

	public NavigationButton(int x, int y, int width, int height, @NonNull NavigationButton.NavigationType navigation, OnPress onPress) {
		super(x, y, width, height, navigation == NavigationType.BACK
						? Component.translatable("screenshot_utilities.navigation.back")
						: Component.translatable("screenshot_utilities.navigation.next"),
				onPress, DEFAULT_NARRATION);
		this.navigation = navigation;
	}

	@Override
	protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.extractDefaultSprite(graphics);

		if (navigation == NavigationType.BACK) {
			graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					BACK_TEXTURE,
					getX(), getY(),
					getWidth(), getHeight()
			);
		} else {
			graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					NEXT_TEXTURE,
					getX(), getY(),
					getWidth(), getHeight()
			);
		}
	}

	public enum NavigationType {
		BACK,
		NEXT,
	}
}
