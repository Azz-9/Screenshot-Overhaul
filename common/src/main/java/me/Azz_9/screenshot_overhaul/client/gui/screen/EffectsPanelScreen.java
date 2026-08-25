package me.Azz_9.screenshot_overhaul.client.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_overhaul.CommonClass;
import me.Azz_9.screenshot_overhaul.client.gui.components.EffectsPanel;

public class EffectsPanelScreen extends BaseScreen {

	private static final int PANEL_WIDTH = 200;
	private static final int MARGIN = 30;

	public EffectsPanelScreen() {
		super(Component.empty());
	}

	@Override
	public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
	}

	@Override
	protected void init() {
		EffectsPanel effectsPanel = new EffectsPanel(
				width - MARGIN - PANEL_WIDTH, MARGIN,
				PANEL_WIDTH, height - MARGIN * 2
		);

		addRenderableWidget(effectsPanel);
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		if (CommonClass.getOpenEffectsPanelKeybind().matches(event)) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}
}
