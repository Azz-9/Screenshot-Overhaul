package me.Azz_9.screenshot_utilities.client.gui.components.screenshotGallery.content;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

@Environment(EnvType.CLIENT)
public class ScreenshotNameWidget extends AbstractWidget {
	public static final int DEFAULT_EDITABLE_COLOR = 0xffe0e0e0;
	public static final Style PLACEHOLDER_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
	private static final WidgetSprites SPRITES = new WidgetSprites(
			Identifier.withDefaultNamespace("widget/text_field"), Identifier.withDefaultNamespace("widget/text_field_highlighted")
	);
	private static final float UNDERLINE_SPEED = 18f;
	private final Font font;
	private final List<EditBox.TextFormatter> formatters = new ArrayList<>();
	// screenshot
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
	private int uneditableColor = 0xff707070;
	@Nullable
	private String suggestion;
	@Nullable
	private Consumer<String> changedListener;
	private Predicate<String> textPredicate = Objects::nonNull;
	@Nullable
	private Component placeholder;
	private long lastSwitchFocusTime = Util.getMillis();
	private int textX;
	private int textY;
	private int renderOffsetX = 0;
	private int fullTextWidth = 0;
	// underline
	private float underlineProgress = 0.0f;
	private final @NonNull List<ChatFormatting> chatFormattings = new ArrayList<>();

	public ScreenshotNameWidget(int width, int height, @NonNull String initialText) {
		this(0, 0, width, height, initialText);
	}

	public ScreenshotNameWidget(int x, int y, int width, int height, @NonNull String initialText) {
		super(x, y, width, height, Component.empty());
		this.font = MINECRAFT.font;
		setMaxLength(255); // max file name length

		setText(initialText);
		setDrawsBackground(false);

		this.updateTextPosition();
	}

	public void setChangedListener(@Nullable Consumer<String> changedListener) {
		this.changedListener = changedListener;
	}

	public void addFormatter(EditBox.TextFormatter formatter) {
		this.formatters.add(formatter);
	}

	@Override
	protected @NonNull MutableComponent createNarrationMessage() {
		Component text = this.getMessage();
		return Component.translatable("gui.narrate.editBox", text, this.text);
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

	public void clearChatFormattings() {
		this.chatFormattings.clear();
	}

	public void addChatFormatting(@NonNull ChatFormatting formatting) {
		this.chatFormattings.add(formatting);
	}

	public void write(String input) {
		int start = Math.min(this.selectionStart, this.selectionEnd);
		int end = Math.max(this.selectionStart, this.selectionEnd);
		int maxInsertionLength = this.maxLength - this.text.length() - (start - end);
		if (maxInsertionLength > 0) {
			String text = StringUtil.filterText(input);
			int insertionLength = text.length();
			if (maxInsertionLength < insertionLength) {
				if (Character.isHighSurrogate(text.charAt(maxInsertionLength - 1))) {
					maxInsertionLength--;
				}

				text = text.substring(0, maxInsertionLength);
				insertionLength = maxInsertionLength;
			}

			String text2 = new StringBuilder(this.text).replace(start, end, text).toString();
			if (this.textPredicate.test(text2)) {
				this.text = text2;
				this.setSelectionStart(start + insertionLength);
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
		return Util.offsetByCodepoints(this.text, this.selectionStart, offset);
	}

	public void setCursor(int cursor, boolean select) {
		this.setSelectionStart(cursor);
		if (!select) {
			this.setSelectionEnd(this.selectionStart);
		}

		this.onChanged(this.text);
	}

	public void setSelectionStart(int cursor) {
		this.selectionStart = Mth.clamp(cursor, 0, this.text.length());
		this.updateFirstCharacterIndex(this.selectionStart);
	}

	public void setCursorToStart(boolean shiftKeyPressed) {
		this.setCursor(0, shiftKeyPressed);
	}

	public void setCursorToEnd(boolean shiftKeyPressed) {
		this.setCursor(this.text.length(), shiftKeyPressed);
	}

	@Override
	public boolean keyPressed(@NonNull KeyEvent input) {
		if (this.shouldTakeFocusAfterInteraction() && this.isFocused()) {
			switch (input.key()) {
				case 259:
					if (this.editable) {
						this.erase(-1, input.hasControlDown());
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
						MINECRAFT.keyboardHandler.setClipboard(this.getSelectedText());
						return true;
					} else if (input.isPaste()) {
						if (this.isEditable()) {
							this.write(MINECRAFT.keyboardHandler.getClipboard());
						}

						return true;
					} else {
						if (input.isCut()) {
							MINECRAFT.keyboardHandler.setClipboard(this.getSelectedText());
							if (this.isEditable()) {
								this.write("");
							}

							return true;
						}

						return false;
					}
				case 261:
					if (this.editable) {
						this.erase(1, input.hasControlDown());
					}

					return true;
				case 262:
					if (input.hasControlDown()) {
						this.setCursor(this.getWordSkipPosition(1), input.hasShiftDown());
					} else {
						this.moveCursor(1, input.hasShiftDown());
					}

					return true;
				case 263:
					if (input.hasControlDown()) {
						this.setCursor(this.getWordSkipPosition(-1), input.hasShiftDown());
					} else {
						this.moveCursor(-1, input.hasShiftDown());
					}

					return true;
				case 268:
					this.setCursorToStart(input.hasShiftDown());
					return true;
				case 269:
					this.setCursorToEnd(input.hasShiftDown());
					return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean isActive() {
		return this.shouldTakeFocusAfterInteraction() && this.isEditable() && active;
	}

	@Override
	public boolean charTyped(@NonNull CharacterEvent input) {
		if (!this.isActive() || !this.isFocused()) {
			return false;
		} else if (input.isAllowedChatCharacter()) {
			if (this.editable) {
				this.write(input.codepointAsString());
			}

			return true;
		} else {
			return false;
		}
	}

	private int calculateCursorPos(MouseButtonEvent click) {
		int baseX = getX() + (drawsBackground ? 4 : 0);
		int mouseX = (int) click.x();

		int localX = mouseX - baseX - renderOffsetX;
		localX = Mth.clamp(localX, 0, fullTextWidth);

		return font.plainSubstrByWidth(text, localX).length();
	}

	private void selectWord(MouseButtonEvent click) {
		int i = this.calculateCursorPos(click);
		int j = this.getWordSkipPosition(-1, i);
		int k = this.getWordSkipPosition(1, i);
		this.setCursor(j, false);
		this.setCursor(k, true);
	}

	@Override
	public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
		if (MINECRAFT.gui.screen() instanceof FocusableScreen screen) {
			screen.requestFocus(this);
		}

		if (doubled) {
			this.selectWord(click);
		} else {
			this.setCursor(this.calculateCursorPos(click), click.hasShiftDown());
		}
	}

	@Override
	protected void onDrag(@NonNull MouseButtonEvent click, double offsetX, double offsetY) {
		this.setCursor(this.calculateCursorPos(click), true);
	}

	@Override
	public void playDownSound(@NonNull SoundManager soundManager) {
	}

	@Override
	public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		if (!this.isVisible()) {
			return;
		}

		// === Background ===
		if (this.drawsBackground()) {
			Identifier identifier = SPRITES.get(this.shouldTakeFocusAfterInteraction(), this.isFocused());
			graphics.blitSprite(
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
		graphics.enableScissor(
				innerX,
				this.getY(),
				innerX + innerWidth,
				getBottom()
		);

		// === Texte complet ===
		if (!this.text.isEmpty()) {
			graphics.text(
					this.font,
					Component.literal(this.text).withStyle(chatFormattings.toArray(ChatFormatting[]::new)),
					drawX,
					textY,
					textColor,
					this.textShadow
			);
		} else if (this.placeholder != null && !this.isFocused()) {
			graphics.text(
					this.font,
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

			int selX1 = drawX + this.font.width(this.text.substring(0, selStart));
			int selX2 = drawX + this.font.width(this.text.substring(0, selEnd));

			graphics.textHighlight(
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
						&& (Util.getMillis() - this.lastSwitchFocusTime) / 300L % 2L == 0L;

		if (cursorVisible) {
			int cursorX = drawX + this.font.width(
					this.text.substring(0, this.selectionStart)
			);

			graphics.fill(
					cursorX,
					textY - 1,
					cursorX + 1,
					textY + 9,
					textColor
			);
		}

		int cursorX = updateUnderlineProgress(deltaTicks / 20f);
		if (underlineProgress > 0.001f) {
			renderUnderline(graphics, cursorX);
		}

		graphics.disableScissor();

		// === Curseur souris ===
		if (this.isHovered()) {
			graphics.requestCursor(this.isEditable() ? CursorTypes.IBEAM : CursorTypes.NOT_ALLOWED);
		}
	}

	private void renderUnderline(@NonNull GuiGraphicsExtractor graphics, int cursorTextX) {
		Font font = MINECRAFT.font;

		if (underlineProgress <= 0.001f) {
			return;
		}

		int innerX = this.getX() + (this.drawsBackground ? 4 : 0);
		int textBaseX = innerX + renderOffsetX;

		int fullTextWidth = font.width(this.getText());
		int underlineHeight = 1;

		int y = textY + font.lineHeight + 1;

		int left = textBaseX;
		int right = textBaseX + fullTextWidth;

		// Position finale du curseur à l'écran
		int cursorScreenX = textBaseX + cursorTextX;

		// Interpolation depuis le curseur
		int animLeft = Mth.lerpDiscrete(underlineProgress, cursorScreenX, left);
		int animRight = Mth.lerpDiscrete(underlineProgress, cursorScreenX, right);

		// Clip pour éviter les débordements
		graphics.enableScissor(innerX, this.getY(), innerX + this.getInnerWidth(), this.getY() + this.height);

		graphics.fill(animLeft, y, animRight, y + underlineHeight, Colors.WHITE);

		graphics.disableScissor();
	}


	private int updateUnderlineProgress(float dt) {
		float target = this.isFocused() ? 1.0f : 0.0f;

		underlineProgress += (float) ((target - underlineProgress) * (1f - Math.exp(-UNDERLINE_SPEED * dt)));
		underlineProgress = Mth.clamp(underlineProgress, 0.0f, 1.0f);

		return MINECRAFT.font.width(this.text.substring(0, this.getCursor()));
	}


	private FormattedCharSequence format(String string) {
		for (EditBox.TextFormatter formatter : this.formatters) {
			FormattedCharSequence formattedCharSequence = formatter.format(string, 0);
			if (formattedCharSequence != null) {
				return formattedCharSequence;
			}
		}

		return FormattedCharSequence.forward(string, Style.EMPTY);
	}

	private void updateTextPosition() {
		if (this.font != null) {
			String string = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
			this.textX = this.getX() + (this.getWidth() - this.font.width(string)) / 2;
			this.textY = this.getY() + (this.height - font.lineHeight) / 2;
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
				this.lastSwitchFocusTime = Util.getMillis();
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
		this.selectionEnd = Mth.clamp(index, 0, this.text.length());
		this.updateFirstCharacterIndex(this.selectionEnd);
	}

	private void updateFirstCharacterIndex(int cursor) {
		if (this.font != null) {
			this.firstCharacterIndex = Math.min(this.firstCharacterIndex, this.text.length());
			int i = this.getInnerWidth();
			String string = this.font.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), i);
			int j = string.length() + this.firstCharacterIndex;
			if (cursor == this.firstCharacterIndex) {
				this.firstCharacterIndex = this.firstCharacterIndex - this.font.plainSubstrByWidth(this.text, i, true).length();
			}

			if (cursor > j) {
				this.firstCharacterIndex += cursor - j;
			} else if (cursor <= this.firstCharacterIndex) {
				this.firstCharacterIndex = this.firstCharacterIndex - (this.firstCharacterIndex - cursor);
			}

			this.firstCharacterIndex = Mth.clamp(this.firstCharacterIndex, 0, this.text.length());
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
		return index > this.text.length() ? this.getX() : this.getX() + this.font.width(this.text.substring(0, index));
	}

	@Override
	public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, this.createNarrationMessage());
	}

	public void setPlaceholder(Component placeholder) {
		boolean bl = placeholder.getStyle().equals(Style.EMPTY);
		this.placeholder = bl ? placeholder.copy().setStyle(PLACEHOLDER_STYLE) : placeholder;
	}

	private void updateRenderOffset() {
		if (font == null) {
			renderOffsetX = 0;
			return;
		}

		fullTextWidth = font.width(text);
		int innerWidth = getInnerWidth();

		int cursorPixelX = font.width(text.substring(0, selectionStart));

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
		FormattedCharSequence format(String string, int firstCharacterIndex);
	}

}
