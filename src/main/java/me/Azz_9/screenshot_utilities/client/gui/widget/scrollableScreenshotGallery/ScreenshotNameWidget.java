package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Environment(EnvType.CLIENT)
public class ScreenshotNameWidget extends ClickableWidget {
	public static final int DEFAULT_EDITABLE_COLOR = -2039584;
	public static final Style PLACEHOLDER_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY);
	private static final ButtonTextures TEXTURES = new ButtonTextures(
			Identifier.ofVanilla("widget/text_field"), Identifier.ofVanilla("widget/text_field_highlighted")
	);
	private static final float UNDERLINE_SPEED = 18f;
	private final TextRenderer textRenderer;
	private final List<TextFieldWidget.Formatter> formatters = new ArrayList<>();
	// screenshot
	private final @NonNull File screenshot;
	private final @NonNull String extension;
	private final @NonNull String baseName;
	private String text = "";
	private int maxLength = 32;
	private boolean drawsBackground = true;
	private boolean focusUnlocked = true;
	private boolean editable = true;
	private boolean textShadow = true;
	private boolean invertSelectionBackground = true;
	/**
	 * The index of the leftmost character that is rendered on a screen.
	 */
	private int firstCharacterIndex;
	private int selectionStart;
	private int selectionEnd;
	private int editableColor = DEFAULT_EDITABLE_COLOR;
	private int uneditableColor = -9408400;
	@Nullable
	private String suggestion;
	@Nullable
	private Consumer<String> changedListener;
	private Predicate<String> textPredicate = Objects::nonNull;
	@Nullable
	private Text placeholder;
	private long lastSwitchFocusTime = Util.getMeasuringTimeMs();
	private int textX;
	private int textY;
	private int renderOffsetX = 0;
	private int fullTextWidth = 0;
	// underline
	private float underlineProgress = 0.0f;
	private int onClickX = -1;

	public ScreenshotNameWidget(int width, int height, File screenshotFile) {
		this(0, 0, width, height, screenshotFile);
	}

	public ScreenshotNameWidget(int x, int y, int width, int height, File screenshotFile) {
		this(x, y, width, height, null, screenshotFile);
	}

	public ScreenshotNameWidget(int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, File screenshotFile) {
		super(x, y, width, height, Text.empty());
		this.textRenderer = CLIENT.textRenderer;
		if (copyFrom != null) {
			this.setText(copyFrom.getText());
		}

		setText(screenshotFile.getName());
		setDrawsBackground(false);

		this.screenshot = screenshotFile;

		String name = screenshot.getName();
		int dot = name.lastIndexOf('.');
		this.baseName = dot == -1 ? name : name.substring(0, dot);
		this.extension = dot == -1 ? "" : name.substring(dot);

		setTextPredicate((text) -> text.endsWith(extension));
		setMaxLength(50);

		this.updateTextPosition();
	}

	public void setChangedListener(@Nullable Consumer<String> changedListener) {
		this.changedListener = changedListener;
	}

	public void addFormatter(TextFieldWidget.Formatter formatter) {
		this.formatters.add(formatter);
	}

	@Override
	protected MutableText getNarrationMessage() {
		Text text = this.getMessage();
		return Text.translatable("gui.narrate.editBox", text, this.text);
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		if (this.textPredicate.test(text)) {
			if (text.length() > this.maxLength) {
				this.text = text.substring(0, this.maxLength);
			} else {
				this.text = text;
			}

			this.setCursorToEnd(false);
			this.setSelectionEnd(this.selectionStart);
			this.onChanged(text);
		}
	}

	public String getSelectedText() {
		int i = Math.min(this.selectionStart, this.selectionEnd);
		int j = Math.max(this.selectionStart, this.selectionEnd);
		return this.text.substring(i, j);
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		this.updateTextPosition();
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		this.updateTextPosition();
	}

	public void setTextPredicate(Predicate<String> textPredicate) {
		this.textPredicate = textPredicate;
	}

	public void write(String text) {
		int i = Math.min(this.selectionStart, this.selectionEnd);
		int j = Math.max(this.selectionStart, this.selectionEnd);
		int k = this.maxLength - this.text.length() - (i - j);
		if (k > 0) {
			String string = StringHelper.stripInvalidChars(text);
			int l = string.length();
			if (k < l) {
				if (Character.isHighSurrogate(string.charAt(k - 1))) {
					k--;
				}

				string = string.substring(0, k);
				l = k;
			}

			String string2 = new StringBuilder(this.text).replace(i, j, string).toString();
			if (this.textPredicate.test(string2)) {
				this.text = string2;
				this.setSelectionStart(i + l);
				this.setSelectionEnd(this.selectionStart);
				this.onChanged(this.text);
			}
		}
	}

	private void onChanged(String newText) {
		if (this.changedListener != null) {
			this.changedListener.accept(newText);
		}

		this.updateTextPosition();
	}

	private void erase(int offset, boolean words) {
		if (words) {
			this.eraseWords(offset);
		} else {
			this.eraseCharacters(offset);
		}
	}

	public void eraseWords(int wordOffset) {
		if (!this.text.isEmpty()) {
			if (this.selectionEnd != this.selectionStart) {
				this.write("");
			} else {
				this.eraseCharactersTo(this.getWordSkipPosition(wordOffset));
			}
		}
	}

	public void eraseCharacters(int characterOffset) {
		this.eraseCharactersTo(this.getCursorPosWithOffset(characterOffset));
	}

	public void eraseCharactersTo(int position) {
		if (!this.text.isEmpty()) {
			if (this.selectionEnd != this.selectionStart) {
				this.write("");
			} else {
				int i = Math.min(position, this.selectionStart);
				int j = Math.max(position, this.selectionStart);
				if (i != j) {
					String string = new StringBuilder(this.text).delete(i, j).toString();
					if (this.textPredicate.test(string)) {
						this.text = string;
						this.setCursor(i, false);
					}
				}
			}
		}
	}

	public int getWordSkipPosition(int wordOffset) {
		return this.getWordSkipPosition(wordOffset, this.getCursor());
	}

	private int getWordSkipPosition(int wordOffset, int cursorPosition) {
		return this.getWordSkipPosition(wordOffset, cursorPosition, true);
	}

	private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
		int i = cursorPosition;
		boolean bl = wordOffset < 0;
		int j = Math.abs(wordOffset);

		for (int k = 0; k < j; k++) {
			if (!bl) {
				int l = this.text.length();
				i = this.text.indexOf(32, i);
				if (i == -1) {
					i = l;
				} else {
					while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
						i++;
					}
				}
			} else {
				while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
					i--;
				}

				while (i > 0 && this.text.charAt(i - 1) != ' ') {
					i--;
				}
			}
		}

		return i;
	}

	public void moveCursor(int offset, boolean shiftKeyPressed) {
		this.setCursor(this.getCursorPosWithOffset(offset), shiftKeyPressed);
	}

	private int getCursorPosWithOffset(int offset) {
		return Util.moveCursor(this.text, this.selectionStart, offset);
	}

	public void setCursor(int cursor, boolean select) {
		this.setSelectionStart(cursor);
		if (!select) {
			this.setSelectionEnd(this.selectionStart);
		}

		this.onChanged(this.text);
	}

	public void setSelectionStart(int cursor) {
		this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
		this.updateFirstCharacterIndex(this.selectionStart);
	}

	public void setCursorToStart(boolean shiftKeyPressed) {
		this.setCursor(0, shiftKeyPressed);
	}

	public void setCursorToEnd(boolean shiftKeyPressed) {
		this.setCursor(this.text.length(), shiftKeyPressed);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (this.isInteractable() && this.isFocused()) {
			switch (input.key()) {
				case 259:
					if (this.editable) {
						this.erase(-1, input.hasCtrlOrCmd());
					}

					return true;
				case 260:
				case 264:
				case 265:
				case 266:
				case 267:
				default:
					if (input.isSelectAll()) {
						this.setCursorToEnd(false);
						this.setSelectionEnd(0);
						return true;
					} else if (input.isCopy()) {
						MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
						return true;
					} else if (input.isPaste()) {
						if (this.isEditable()) {
							this.write(MinecraftClient.getInstance().keyboard.getClipboard());
						}

						return true;
					} else {
						if (input.isCut()) {
							MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
							if (this.isEditable()) {
								this.write("");
							}

							return true;
						}

						return false;
					}
				case 261:
					if (this.editable) {
						this.erase(1, input.hasCtrlOrCmd());
					}

					return true;
				case 262:
					if (input.hasCtrlOrCmd()) {
						this.setCursor(this.getWordSkipPosition(1), input.hasShift());
					} else {
						this.moveCursor(1, input.hasShift());
					}

					return true;
				case 263:
					if (input.hasCtrlOrCmd()) {
						this.setCursor(this.getWordSkipPosition(-1), input.hasShift());
					} else {
						this.moveCursor(-1, input.hasShift());
					}

					return true;
				case 268:
					this.setCursorToStart(input.hasShift());
					return true;
				case 269:
					this.setCursorToEnd(input.hasShift());
					return true;
			}
		} else {
			return false;
		}
	}

	public boolean isActive() {
		return this.isInteractable() && this.isFocused() && this.isEditable();
	}

	@Override
	public boolean charTyped(CharInput input) {
		if (!this.isActive()) {
			return false;
		} else if (input.isValidChar()) {
			if (this.editable) {
				this.write(input.asString());
			}

			return true;
		} else {
			return false;
		}
	}

	private int calculateCursorPos(Click click) {
		int baseX = getX() + (drawsBackground ? 4 : 0);
		int mouseX = (int) click.x();

		int localX = mouseX - baseX - renderOffsetX;
		localX = MathHelper.clamp(localX, 0, fullTextWidth);

		return textRenderer.trimToWidth(text, localX).length();
	}

	private void selectWord(Click click) {
		int i = this.calculateCursorPos(click);
		int j = this.getWordSkipPosition(-1, i);
		int k = this.getWordSkipPosition(1, i);
		this.setCursor(j, false);
		this.setCursor(k, true);
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		this.onClickX = (int) click.x();
		if (CLIENT.currentScreen instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}

		if (doubled) {
			this.selectWord(click);
		} else {
			this.setCursor(this.calculateCursorPos(click), click.hasShift());
		}
	}

	@Override
	protected void onDrag(Click click, double offsetX, double offsetY) {
		this.setCursor(this.calculateCursorPos(click), true);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (!this.isVisible()) {
			return;
		}

		// === Background ===
		if (this.drawsBackground()) {
			Identifier identifier = TEXTURES.get(this.isInteractable(), this.isFocused());
			context.drawGuiTexture(
					RenderPipelines.GUI_TEXTURED,
					identifier,
					this.getX(),
					this.getY(),
					this.getWidth(),
					this.getHeight()
			);
		}

		int textColor = this.editable ? this.editableColor : this.uneditableColor;

		// === Positions de base ===
		int innerX = this.getX() + (this.drawsBackground ? 4 : 0);
		int innerWidth = this.getInnerWidth();
		//int textY = this.getY() + (this.height - 8) / 2;

		// === MODE CENTRÉ ===

		// Met à jour le scroll visuel
		updateRenderOffset();

		int drawX = innerX + renderOffsetX;

		// Clip pour éviter le débordement
		context.enableScissor(
				innerX,
				this.getY(),
				innerX + innerWidth,
				getBottom()
		);

		// === Texte complet ===
		if (!this.text.isEmpty()) {
			context.drawText(
					this.textRenderer,
					this.format(this.text),
					drawX,
					textY,
					textColor,
					this.textShadow
			);
		} else if (this.placeholder != null && !this.isFocused()) {
			context.drawTextWithShadow(
					this.textRenderer,
					this.placeholder,
					drawX,
					textY,
					textColor
			);
		}

		// === Sélection ===
		if (this.selectionStart != this.selectionEnd) {
			int selStart = Math.min(this.selectionStart, this.selectionEnd);
			int selEnd = Math.max(this.selectionStart, this.selectionEnd);

			int selX1 = drawX + this.textRenderer.getWidth(this.text.substring(0, selStart));
			int selX2 = drawX + this.textRenderer.getWidth(this.text.substring(0, selEnd));

			context.drawSelection(
					selX1,
					textY - 1,
					selX2,
					textY + 9,
					this.invertSelectionBackground
			);
		}

		// === Curseur ===
		boolean cursorVisible =
				this.isFocused()
						&& this.isEditable()
						&& (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L;

		if (cursorVisible) {
			int cursorX = drawX + this.textRenderer.getWidth(
					this.text.substring(0, this.selectionStart)
			);

			context.fill(
					cursorX,
					textY - 1,
					cursorX + 1,
					textY + 9,
					textColor
			);
		}

		int cursorX = updateUnderlineProgress(deltaTicks / 20f);
		if (underlineProgress > 0.001f) {
			renderUnderline(context, cursorX);
		}

		context.disableScissor();

		// === Curseur souris ===
		if (this.isHovered()) {
			context.setCursor(this.isEditable() ? StandardCursors.IBEAM : StandardCursors.NOT_ALLOWED);
		}
	}

	private void renderUnderline(@NonNull DrawContext context, int cursorTextX) {
		TextRenderer textRenderer = CLIENT.textRenderer;

		if (underlineProgress <= 0.001f) {
			return;
		}

		int innerX = this.getX() + (this.drawsBackground ? 4 : 0);
		int textBaseX = innerX + renderOffsetX;

		int fullTextWidth = textRenderer.getWidth(this.getText());
		int underlineHeight = 1;

		int y = textY + textRenderer.fontHeight + 1;

		int left = textBaseX;
		int right = textBaseX + fullTextWidth;

		// Position finale du curseur à l'écran
		int cursorScreenX = textBaseX + cursorTextX;

		// Interpolation depuis le curseur
		int animLeft = MathHelper.lerp(underlineProgress, cursorScreenX, left);
		int animRight = MathHelper.lerp(underlineProgress, cursorScreenX, right);

		// Clip pour éviter les débordements
		context.enableScissor(innerX, this.getY(), innerX + this.getInnerWidth(), this.getY() + this.height);

		context.fill(animLeft, y, animRight, y + underlineHeight, Colors.WHITE);

		context.disableScissor();
	}


	private int updateUnderlineProgress(float dt) {
		float target = this.isFocused() ? 1.0f : 0.0f;

		underlineProgress += (float) ((target - underlineProgress) * (1f - Math.exp(-UNDERLINE_SPEED * dt)));
		underlineProgress = MathHelper.clamp(underlineProgress, 0.0f, 1.0f);

		return CLIENT.textRenderer.getWidth(this.text.substring(0, this.getCursor()));
	}


	private OrderedText format(String string) {
		for (TextFieldWidget.Formatter formatter : this.formatters) {
			OrderedText orderedText = formatter.format(string, 0);
			if (orderedText != null) {
				return orderedText;
			}
		}

		return OrderedText.styledForwardsVisitedString(string, Style.EMPTY);
	}

	private void updateTextPosition() {
		if (this.textRenderer != null) {
			String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
			this.textX = this.getX() + (this.getWidth() - this.textRenderer.getWidth(string)) / 2;
			this.textY = this.getY() + (this.height - textRenderer.fontHeight) / 2;
		}
	}

	private int getMaxLength() {
		return this.maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
		if (this.text.length() > maxLength) {
			this.text = this.text.substring(0, maxLength);
			this.onChanged(this.text);
		}
	}

	public int getCursor() {
		return this.selectionStart;
	}

	public boolean drawsBackground() {
		return this.drawsBackground;
	}

	public void setDrawsBackground(boolean drawsBackground) {
		this.drawsBackground = drawsBackground;
		this.updateTextPosition();
	}

	public void setEditableColor(int editableColor) {
		this.editableColor = editableColor;
	}

	public void setUneditableColor(int uneditableColor) {
		this.uneditableColor = uneditableColor;
	}

	@Override
	public void setFocused(boolean focused) {
		if (this.focusUnlocked || focused) {
			super.setFocused(focused);
			if (focused) {
				this.lastSwitchFocusTime = Util.getMeasuringTimeMs();
			}
		}

		if (!focused) {
			this.setSelectionEnd(getCursor());
		}
	}

	private boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setTextShadow(boolean textShadow) {
		this.textShadow = textShadow;
	}

	public void setInvertSelectionBackground(boolean invertSelectionBackground) {
		this.invertSelectionBackground = invertSelectionBackground;
	}

	public int getInnerWidth() {
		return this.drawsBackground() ? this.width - 8 : this.width;
	}

	public void setSelectionEnd(int index) {
		this.selectionEnd = MathHelper.clamp(index, 0, this.text.length());
		this.updateFirstCharacterIndex(this.selectionEnd);
	}

	private void updateFirstCharacterIndex(int cursor) {
		if (this.textRenderer != null) {
			this.firstCharacterIndex = Math.min(this.firstCharacterIndex, this.text.length());
			int i = this.getInnerWidth();
			String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), i);
			int j = string.length() + this.firstCharacterIndex;
			if (cursor == this.firstCharacterIndex) {
				this.firstCharacterIndex = this.firstCharacterIndex - this.textRenderer.trimToWidth(this.text, i, true).length();
			}

			if (cursor > j) {
				this.firstCharacterIndex += cursor - j;
			} else if (cursor <= this.firstCharacterIndex) {
				this.firstCharacterIndex = this.firstCharacterIndex - (this.firstCharacterIndex - cursor);
			}

			this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, this.text.length());
		}
	}

	public void setFocusUnlocked(boolean focusUnlocked) {
		this.focusUnlocked = focusUnlocked;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setSuggestion(@Nullable String suggestion) {
		this.suggestion = suggestion;
	}

	public int getCharacterX(int index) {
		return index > this.text.length() ? this.getX() : this.getX() + this.textRenderer.getWidth(this.text.substring(0, index));
	}

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, this.getNarrationMessage());
	}

	public void setPlaceholder(Text placeholder) {
		boolean bl = placeholder.getStyle().equals(Style.EMPTY);
		this.placeholder = bl ? placeholder.copy().fillStyle(PLACEHOLDER_STYLE) : placeholder;
	}

	private void updateRenderOffset() {
		if (textRenderer == null) {
			renderOffsetX = 0;
			return;
		}

		fullTextWidth = textRenderer.getWidth(text);
		int innerWidth = getInnerWidth();

		int cursorPixelX = textRenderer.getWidth(text.substring(0, selectionStart));

		// Centrage idéal
		int idealCenterOffset = (innerWidth - fullTextWidth) / 2;

		// Position réelle du curseur à l'écran
		int cursorScreenX = idealCenterOffset + cursorPixelX;

		// Si le curseur sort à droite
		if (cursorScreenX > innerWidth - 2) {
			renderOffsetX = innerWidth - 2 - cursorPixelX;
		}
		// Si le curseur sort à gauche
		else if (cursorScreenX < 2) {
			renderOffsetX = 2 - cursorPixelX;
		}
		// Sinon centré
		else {
			renderOffsetX = idealCenterOffset;
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Formatter {
		@Nullable
		OrderedText format(String string, int firstCharacterIndex);
	}

}
