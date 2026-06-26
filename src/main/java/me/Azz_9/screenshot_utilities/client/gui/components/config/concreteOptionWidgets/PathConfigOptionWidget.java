package me.Azz_9.screenshot_utilities.client.gui.components.config.concreteOptionWidgets;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.function.Consumer;

import javax.swing.*;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOptionWidget;
import me.Azz_9.screenshot_utilities.client.config.option.options.PathConfigOption;
import me.Azz_9.screenshot_utilities.client.gui.TooltipRenderer;
import me.Azz_9.screenshot_utilities.mixin.AbstractWidgetAccessor;
import me.Azz_9.screenshot_utilities.utils.PathUtils;

/**
 * Row widget for {@link PathConfigOption}.
 *
 * <p>Shows a read-only {@link EditBox} with the path string and a "…" button
 * that opens a {@link JFileChooser} on the AWT Event Dispatch Thread.</p>
 */
@Environment(EnvType.CLIENT)
public final class PathConfigOptionWidget extends ConfigOptionWidget<Path> {

	private static final int BROWSE_BUTTON_WIDTH = 24;
	private static final int INNER_GAP = 4;

	private final @NonNull PathConfigOption pathOption;

	private EditBox pathField;
	private Button browseButton;

	public PathConfigOptionWidget(int x, int y, int width, @NonNull PathConfigOption option, @NonNull Consumer<GuiEventListener> onFocusRequested) {
		super(x, y, width, option, onFocusRequested);
		this.pathOption = option;
	}

	@Override
	protected @NonNull AbstractWidget createControlWidget(int x, int y, int width) {
		int fieldWidth = width - BROWSE_BUTTON_WIDTH - INNER_GAP;

		pathField = new EditBox(MINECRAFT.font, x, y, fieldWidth, ROW_HEIGHT, Component.empty());
		pathField.setMaxLength(256);
		pathField.setEditable(false);
		pathField.setValue(option.getWorkingValue().toString());
		pathField.setTextColor(Colors.LIGHT_GRAY);

		browseButton = Button.builder(Component.literal("…"), btn -> openFileChooser())
				.pos(x + fieldWidth + INNER_GAP, y)
				.size(BROWSE_BUTTON_WIDTH, ROW_HEIGHT)
				.build();

		return new CompositeControlWidget(x, y, width, pathField, browseButton);
	}

	@Override
	protected void onValueReset() {
		pathField.setValue(option.getWorkingValue().toString());
	}

	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

		// Show the resolved absolute path while hovering the text field
		if (pathField != null && ((AbstractWidgetAccessor) pathField).invokeAreCoordinatesInRectangle(mouseX, mouseY)) {
			String absoluteText = PathUtils.toAbsolutePath(option.getWorkingValue()).toString();
			ScreenRectangle rectangle = graphics.scissorStack.peek() != null
					? graphics.scissorStack.peek()
					: new ScreenRectangle(0, 0, MINECRAFT.getWindow().getGuiScaledWidth(), MINECRAFT.getWindow().getGuiScaledHeight());
			TooltipRenderer.render(graphics, pathField, absoluteText, rectangle);
		}
	}

	private void openFileChooser() {
		Path currentAbsolute = PathUtils.toAbsolutePath(option.getWorkingValue());

		String selectedPath = switch (pathOption.getSelectionMode()) {
			case FILES_ONLY -> {
				try (MemoryStack stack = MemoryStack.stackPush()) {

					PointerBuffer filters = null;

					// Extension filter
					if (pathOption.getFileExtensionFilter() != null) {
						String ext = pathOption.getFileExtensionFilter();

						// TinyFD expects patterns like "*.json"
						filters = stack.mallocPointer(1);
						filters.put(stack.UTF8("*." + ext));
						filters.flip();
					}

					String description = pathOption.getFileExtensionDescription() != null
							? pathOption.getFileExtensionDescription()
							: pathOption.getFileExtensionFilter();

					yield TinyFileDialogs.tinyfd_openFileDialog(
							pathOption.getDialogTitle(),
							currentAbsolute.toString(),
							filters,
							description,
							false
					);
				}
			}

			case DIRECTORIES_ONLY -> TinyFileDialogs.tinyfd_selectFolderDialog(
					pathOption.getDialogTitle(),
					currentAbsolute.toString()
			);
		};
		if (selectedPath == null) return;

		Path selected = PathUtils.toStoredPath(Path.of(selectedPath));

		option.setWorkingValue(selected);
		pathField.setValue(selected.toString());

		refreshValidation();
	}

	// -------------------------------------------------------------------------
	// Composite helper — wraps two widgets as one for the parent contract
	// -------------------------------------------------------------------------

	private static final class CompositeControlWidget extends AbstractWidget {

		private final @NonNull AbstractWidget first;
		private final @NonNull AbstractWidget second;

		CompositeControlWidget(int x, int y, int width, @NonNull AbstractWidget first, @NonNull AbstractWidget second) {
			super(x, y, width, ROW_HEIGHT, Component.empty());
			this.first = first;
			this.second = second;
		}

		@Override
		protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTick) {
			first.extractRenderState(graphics, mouseX, mouseY, deltaTick);
			second.extractRenderState(graphics, mouseX, mouseY, deltaTick);
		}

		@Override
		public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
			return first.mouseClicked(event, doubleClick) || second.mouseClicked(event, doubleClick);
		}

		@Override
		public boolean mouseReleased(@NonNull MouseButtonEvent event) {
			return first.mouseReleased(event) || second.mouseReleased(event);
		}

		@Override
		public boolean keyPressed(@NonNull KeyEvent event) {
			return first.keyPressed(event) || second.keyPressed(event);
		}

		@Override
		public boolean charTyped(@NonNull CharacterEvent event) {
			return first.charTyped(event) || second.charTyped(event);
		}

		@Override
		public void setX(int x) {
			int delta = x - getX();
			super.setX(x);
			first.setX(first.getX() + delta);
			second.setX(second.getX() + delta);
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			first.setY(y);
			second.setY(y);
		}

		@Override
		public void setWidth(int width) {
			int browseW = second.getWidth();
			int fieldW = width - browseW - 4;
			super.setWidth(width);
			first.setWidth(fieldW);
			second.setX(getX() + fieldW + 4);
		}

		@Override
		public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
			//first.updateWidgetNarration(output);
		}
	}
}