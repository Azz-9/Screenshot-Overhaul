package me.Azz_9.screenshot_utilities.compat.xaeroWorldmap.mixin;

import net.minecraft.client.input.MouseButtonEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import xaero.map.element.HoveredMapElementHolder;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin {

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (event.button() != 0) return;

		GuiMap self = (GuiMap) (Object) this;
		HoveredMapElementHolder<?, ?> viewed = ((GuiMapAccessor) self).getViewed();
		if (viewed == null) return;

		Object element = viewed.getElement();
		if (!(element instanceof Screenshot screenshot)) return;

		// Vérifier que c'était bien un clic et pas un drag
		// (GuiMap le vérifie déjà avec Math.abs(pressedAt - mousePos) < 5)
		/*MINECRAFT.setScreen(
				new ScreenshotViewerScreen(self, screenshot.meta())
		);*/
		System.out.println("screenshot clicked " + screenshot.pathRelativeToScreenshotDir());
		cir.setReturnValue(false);
	}
}