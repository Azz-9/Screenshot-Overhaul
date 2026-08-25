package me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery;

import static me.Azz_9.screenshot_overhaul.client.CommonSprites.ARROW_LEFT_SPRITE;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.ARROW_RIGHT_SPRITE;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

public class NavigationButton extends Button {
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
					ARROW_LEFT_SPRITE,
					getX(), getY(),
					getWidth(), getHeight()
			);
		} else {
			graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					ARROW_RIGHT_SPRITE,
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
