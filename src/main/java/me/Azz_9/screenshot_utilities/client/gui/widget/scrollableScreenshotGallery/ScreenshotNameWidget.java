package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import me.Azz_9.screenshot_utilities.accessors.widget.TextFieldAccessor;
import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.Cursor;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.io.File;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Environment(EnvType.CLIENT)
public class ScreenshotNameWidget extends TextFieldWidget {

	private static final float UNDERLINE_SPEED = 0.4f;

	private final @NonNull File screenshot;
	private final @NonNull String extension;
	private @NonNull String baseName;
	private float underlineProgress = 0.0f;
	private int onClickX = -1;

	public ScreenshotNameWidget(int x, int y, int width, int height, @NonNull File screenshot) {
		super(CLIENT.textRenderer, x, y, width, height, Text.empty());

		setText(screenshot.getName());
		setDrawsBackground(false);
		setCentered(true);

		this.screenshot = screenshot;

		String name = screenshot.getName();
		int dot = name.lastIndexOf('.');
		this.baseName = dot == -1 ? name : name.substring(0, dot);
		this.extension = dot == -1 ? "" : name.substring(dot);

		setTextPredicate((text) -> text.endsWith(extension));
		setMaxLength(50);
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.enableScissor(getX(), getY(), getRight(), getBottom());

		super.renderWidget(context, mouseX, mouseY, deltaTicks);

		float target;
		int cursorX;
		if (isFocused()) {
			target = 1.0f;
			cursorX = MathHelper.clamp(
					onClickX - ((TextFieldAccessor) this).screenshotUtilities$getTextX(),
					0,
					CLIENT.textRenderer.getWidth(getText())
			);
		} else {
			target = 0.0f;
			cursorX = CLIENT.textRenderer.getWidth(getText().substring(0, getCursor()));
		}
		underlineProgress += (target - underlineProgress) * UNDERLINE_SPEED;

		if (underlineProgress > 0.001f) {
			renderUnderline(context, cursorX);
		}

		context.disableScissor();

		if (this.isHovered()) {
			context.setCursor(this.isInteractable() ? StandardCursors.IBEAM : Cursor.DEFAULT);
		}
	}

	private void renderUnderline(@NonNull DrawContext context, int cursorX) {
		TextRenderer textRenderer = CLIENT.textRenderer;

		int textX = ((TextFieldAccessor) this).screenshotUtilities$getTextX();
		int textY = ((TextFieldAccessor) this).screenshotUtilities$getTextY();

		int fullWidth = textRenderer.getWidth(getText());
		int underlineHeight = 1;

		float y = textY + textRenderer.fontHeight + 1;

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(textX + cursorX, y);
		matrices.scale(underlineProgress, 1.0f);

		context.fill(-cursorX, 0, fullWidth - cursorX, underlineHeight, Colors.WHITE);

		matrices.popMatrix();
	}


	@Override
	public void onClick(Click click, boolean doubled) {
		this.onClickX = (int) click.x();
		if (CLIENT.currentScreen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}
		super.onClick(click, doubled);
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.setSelectionEnd(getCursor());
		}
	}

	public void commitRename(@NonNull String newBaseName) {
		if (newBaseName.isBlank()) return;

		File renamed = new File(screenshot.getParentFile(), newBaseName + extension);
		if (screenshot.renameTo(renamed)) {
			baseName = newBaseName;
		}
	}
}
