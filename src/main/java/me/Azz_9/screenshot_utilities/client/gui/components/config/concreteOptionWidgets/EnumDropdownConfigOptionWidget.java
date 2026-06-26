package me.Azz_9.screenshot_utilities.client.gui.components.config.concreteOptionWidgets;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.EnumConfigOption;

/**
 * Row widget for {@link EnumConfigOption} with {@link EnumConfigOption.Style#DROPDOWN}.
 *
 * <p>Renders a button that shows the current value. On click, a small dropdown
 * list appears below, rendered above all other widgets via a late render pass
 * registered on the parent screen. Selecting an entry commits the choice and
 * closes the dropdown.</p>
 */
@Environment(EnvType.CLIENT)
public final class EnumDropdownConfigOptionWidget<E extends Enum<E>> extends ConfigOptionWidget<E> {

	private static final int ENTRY_HEIGHT = 12;
	private static final int DROPDOWN_PAD = 2;

	private final @NonNull EnumConfigOption<E> enumOption;
	private Button mainButton;
	private boolean expanded = false;

	public EnumDropdownConfigOptionWidget(int x, int y, int width, @NonNull EnumConfigOption<E> option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, option, onFocusRequested);
		this.enumOption = option;
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		mainButton = Button.builder(getCurrentLabel(), btn -> expanded = !expanded)
				.pos(x, y)
				.size(width, ROW_HEIGHT)
				.build();
		return mainButton;
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

		if (!expanded) return;

		E[] constants = enumOption.getEnumConstants();
		int dropX = mainButton.getX();
		int dropY = mainButton.getBottom();
		int dropW = mainButton.getWidth();
		int dropH = DROPDOWN_PAD * 2 + constants.length * ENTRY_HEIGHT;

		// Background
		graphics.fill(dropX, dropY, dropX + dropW, dropY + dropH, Colors.DARK_GRAY);
		graphics.outline(dropX, dropY, dropW, dropH, Colors.GRAY);

		// Entries
		for (int i = 0; i < constants.length; i++) {
			E entry = constants[i];
			int entryY = dropY + DROPDOWN_PAD + i * ENTRY_HEIGHT;
			boolean hovered = mouseX >= dropX && mouseX < dropX + dropW
					&& mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;
			boolean selected = entry == option.getWorkingValue();

			if (hovered || selected) {
				graphics.fill(dropX, entryY, dropX + dropW, entryY + ENTRY_HEIGHT,
						selected ? Colors.DARK_GRAY : ARGB.color(0x40, Colors.WHITE));
			}

			int textColor = selected ? Colors.WHITE : (hovered ? Colors.LIGHT_GRAY : Colors.GRAY);
			graphics.text(MINECRAFT.font, enumOption.getValueName(entry),
					dropX + DROPDOWN_PAD + 2, entryY + (ENTRY_HEIGHT - MINECRAFT.font.lineHeight) / 2,
					textColor, false);
		}
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		if (!option.isDependencySatisfied()) return false;

		if (expanded) {
			E[] constants = enumOption.getEnumConstants();
			int dropX = mainButton.getX();
			int dropY = mainButton.getBottom();
			int dropW = mainButton.getWidth();

			for (int i = 0; i < constants.length; i++) {
				int entryY = dropY + DROPDOWN_PAD + i * ENTRY_HEIGHT;
				if (event.x() >= dropX && event.x() < dropX + dropW
						&& event.y() >= entryY && event.y() < entryY + ENTRY_HEIGHT) {
					option.setWorkingValue(constants[i]);
					mainButton.setMessage(getCurrentLabel());
					refreshValidation();
					expanded = false;
					return true;
				}
			}
			// Click outside → close
			expanded = false;
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	protected void onValueReset() {
		mainButton.setMessage(getCurrentLabel());
		expanded = false;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void collapse() {
		expanded = false;
	}

	private @NonNull Component getCurrentLabel() {
		return enumOption.getValueName(option.getWorkingValue());
	}
}