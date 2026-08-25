package me.Azz_9.screenshot_overhaul.client.gui.screen;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_overhaul.client.gui.focusSystem.FocusableScreen;
import me.Azz_9.screenshot_overhaul.client.gui.rightClickMenu.RightClickMenu;
import me.Azz_9.screenshot_overhaul.client.gui.rightClickMenu.RightClickMenuScreen;

public abstract class BaseScreen extends Screen implements FocusableScreen, RightClickMenuScreen {
	protected final @Nullable Screen PARENT;

	private final @NonNull RightClickMenu RIGHT_CLICK_MENU;
	private final @NonNull FocusManager FOCUS_MANAGER;

	protected BaseScreen(@NonNull final Component title, @Nullable final Screen parent) {
		super(title);
		this.PARENT = parent;

		RIGHT_CLICK_MENU = new RightClickMenu();
		FOCUS_MANAGER = new FocusManager();
	}

	public BaseScreen(@NonNull final Component title) {
		this(title, null);
	}

	@Override
	public void onClose() {
		if (PARENT != null) {
			minecraft.setScreen(PARENT);
		} else {
			super.onClose();
		}
	}

	public boolean overlayHovered(int mouseX, int mouseY) {
		return getRightClickMenu().isMouseOver(mouseX, mouseY);
	}

	@Override
	public final void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		extractBeforeChildren(graphics, mouseX, mouseY, deltaTicks);
		super.extractRenderState(graphics, overlayHovered(mouseX, mouseY) ? -1 : mouseX, overlayHovered(mouseX, mouseY) ? -1 : mouseY, deltaTicks);
		extractAfterChildren(graphics, mouseX, mouseY, deltaTicks);

		getRightClickMenu().extractRenderState(graphics, mouseX, mouseY, deltaTicks);
	}

	public void extractBeforeChildren(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
	}

	public void extractAfterChildren(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		hideRightClickMenu();
		if (getRightClickMenu().mouseClicked(event, doubleClick)) {
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public @NonNull FocusManager getFocusManager() {
		return FOCUS_MANAGER;
	}

	@Override
	public RightClickMenu getRightClickMenu() {
		return RIGHT_CLICK_MENU;
	}
}
