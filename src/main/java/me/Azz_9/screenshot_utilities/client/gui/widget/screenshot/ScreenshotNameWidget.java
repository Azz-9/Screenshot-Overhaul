package me.Azz_9.screenshot_utilities.client.gui.widget.screenshot;

import me.Azz_9.screenshot_utilities.api.widget.TextFieldAccessor;
import me.Azz_9.screenshot_utilities.client.gui.FocusManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.io.File;

@Environment(EnvType.CLIENT)
public class ScreenshotNameWidget extends TextFieldWidget {

	private static final float UNDERLINE_SPEED = 0.4f;
	private final FocusManager focusManager;
	private final File screenshot;
	private final String extension;
	private float underlineProgress = 0.0f;
	private String baseName;

	public ScreenshotNameWidget(int x, int y, int width, int height, File screenshot, FocusManager focusManager) {
		super(MinecraftClient.getInstance().textRenderer, x, y, width, height, Text.empty());
		this.focusManager = focusManager;

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

		float target = this.isFocused() ? 1.0f : 0.0f;
		underlineProgress += (target - underlineProgress) * UNDERLINE_SPEED;

		if (underlineProgress > 0.001f) {
			renderUnderline(context);
		}

		context.disableScissor();
	}

	private void renderUnderline(DrawContext context) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		int textX = ((TextFieldAccessor) this).screenshotUtilities$getTextX();
		int textY = ((TextFieldAccessor) this).screenshotUtilities$getTextY();

		int fullWidth = textRenderer.getWidth(getText());
		int underlineHeight = 1;

		float centerX = textX + fullWidth / 2f;
		float y = textY + textRenderer.fontHeight + 1;

		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(centerX, y);
		matrices.scale(underlineProgress, 1.0f);
		matrices.translate(-fullWidth / 2f, 0);

		context.fill(0, 0, fullWidth, underlineHeight, 0xFFFFFFFF);

		matrices.popMatrix();
	}


	@Override
	public void onClick(Click click, boolean doubled) {
		focusManager.requestFocus(this);
		super.onClick(click, doubled);
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.setSelectionEnd(getCursor());
		}
	}

	public void commitRename(String newBaseName) {
		if (newBaseName.isBlank()) return;

		File renamed = new File(screenshot.getParentFile(), newBaseName + extension);
		if (screenshot.renameTo(renamed)) {
			baseName = newBaseName;
		}
	}
}
