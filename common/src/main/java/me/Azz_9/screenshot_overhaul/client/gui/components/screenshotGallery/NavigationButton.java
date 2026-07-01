package me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery;

import static me.Azz_9.screenshot_overhaul.Constants.MOD_ID;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

public class NavigationButton extends Button {

	private static final @NonNull Identifier BACK_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/arrow_left");
	private static final @NonNull Identifier NEXT_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "icon/arrow_right");

	private final @NonNull NavigationType navigation;

	public NavigationButton(int x, int y, int width, int height, @NonNull NavigationType navigation, @NonNull OnPress onPress) {
		super(x, y, width, height, navigation == NavigationType.BACK
						? Component.translatable("screenshot_overhaul.navigation.back")
						: Component.translatable("screenshot_overhaul.navigation.next"),
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
