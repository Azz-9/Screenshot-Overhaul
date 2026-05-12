package me.Azz_9.screenshot_utilities.client.gui.widget.scrollableScreenshotGallery;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;
import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MOD_ID;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.widget.PlaceholderEditBox;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;

@Environment(EnvType.CLIENT)
public class MetadataEditorPanel extends SimpleParentWidget {
	private static final int PANEL_PADDING = 10;
	private static final int FIELD_HEIGHT = 20;
	private static final int LABEL_HEIGHT = 10;
	private static final int BUTTON_GAP = 6;
	private static final int BUTTON_WIDTH = 20;
	private static final Component FIELD_PLACEHOLDER = Component.literal("null");

	private @Nullable Consumer<Boolean> onVisibilityChange;

	// animation
	private static final int ANIMATION_DURATION = 400;
	private long animationStart;
	private float progress;
	private boolean slidingIn = false;

	public MetadataEditorPanel(int width, int height) {
		super(MINECRAFT.getWindow().getGuiScaledWidth() - width, (MINECRAFT.getWindow().getGuiScaledHeight() - height) / 2, width, height);
	}

	public void init(@NonNull Screenshot screenshot) {
		clearChildren();

		StringWidget title = new StringWidget(getX() + PANEL_PADDING, getY() + PANEL_PADDING, getWidth() - PANEL_PADDING * 2, LABEL_HEIGHT, Component.translatable("screenshot_utilities.edit_metadata"), MINECRAFT.font);
		addRenderableChild(title);

		Button closeButton = SpriteIconButton.CenteredIcon.builder(
						Component.translatable("screenshot_utilities.close"),
						button -> {
							setVisible(false);
						}, true)
				.withTootip()
				.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/close"), 15, 15)
				.size(BUTTON_WIDTH, FIELD_HEIGHT)
				.build();
		closeButton.setPosition(getRight() - BUTTON_WIDTH - PANEL_PADDING, PANEL_PADDING);
		addRenderableChild(closeButton);

		int y = PANEL_PADDING + LABEL_HEIGHT + PANEL_PADDING;

		addRenderableChild(longField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getX(), val -> screenshot.metadata().setX(val), Component.translatable("screenshot_utilities.metadata.x")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(longField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getY(), val -> screenshot.metadata().setY(val), Component.translatable("screenshot_utilities.metadata.y")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(longField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getZ(), val -> screenshot.metadata().setZ(val), Component.translatable("screenshot_utilities.metadata.z")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(stringField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getDimension() == null ? null : screenshot.metadata().getDimension().toString(),
				val -> screenshot.metadata().setDimension(val == null ? null : Identifier.tryParse(val)), Component.translatable("screenshot_utilities.metadata.dimension")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(stringField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getBiome() == null ? null : screenshot.metadata().getBiome().toString(),
				val -> screenshot.metadata().setBiome(val == null ? null : Identifier.tryParse(val)), Component.translatable("screenshot_utilities.metadata.biome")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(stringField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getWorldName(), val -> screenshot.metadata().setWorldName(val), Component.translatable("screenshot_utilities.metadata.world_name")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(stringField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getServerIp(), val -> screenshot.metadata().setServerIp(val), Component.translatable("screenshot_utilities.metadata.server")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(positiveLongField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				screenshot.metadata().getTimestamp(), val -> screenshot.metadata().setTimestamp(val), Component.translatable("screenshot_utilities.metadata.timestamp")));
		y += LABEL_HEIGHT + FIELD_HEIGHT + BUTTON_GAP;
		addRenderableChild(stringField(getX() + PANEL_PADDING, getY() + y, getWidth() - PANEL_PADDING * 2,
				String.join(", ", screenshot.metadata().getTags()),
				val -> screenshot.metadata().setTags(val == null ? new ArrayList<>() : Arrays.stream(val.split(",")).map(String::trim).toList()),
				Component.translatable("screenshot_utilities.metadata.tags"), Component.translatable("screenshot_utilities.metadata.tags.placeholder")));
	}

	@Override
	protected void renderWidget(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		updateAnimationProgress();

		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(getWidth() * (1.0f - progress), 0);

		graphics.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK_SEMI_TRANSPARENT);

		if (isMouseOver(mouseX, mouseY)) {
			graphics.requestCursor(CursorTypes.ARROW);
		}

		super.renderWidget(graphics, mouseX, mouseY, deltaTicks);

		matrices.popMatrix();
	}

	private void updateAnimationProgress() {
		float t = Math.min(1.0f, (float) (System.currentTimeMillis() - animationStart) / ANIMATION_DURATION);
		float eased = Ease.outQuad(t);
		progress = slidingIn ? eased : 1.0f - eased;

		if (!slidingIn && t >= 1.0f) {
			super.setVisible(false);
		}
	}

	public void setOnVisibilityChange(@Nullable Consumer<Boolean> onVisibilityChange) {
		this.onVisibilityChange = onVisibilityChange;
	}

	@Override
	public void setVisible(boolean visible) {
		slidingIn = visible;
		animationStart = System.currentTimeMillis();
		if (onVisibilityChange != null) onVisibilityChange.accept(visible);
		if (visible) {
			super.setVisible(true);
		}
	}

	@Override
	public boolean isVisible() {
		return slidingIn;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		super.mouseClicked(click, doubled);
		return isMouseOver(click.x(), click.y());
	}

	@Override
	public void updateNarration(@NonNull NarrationElementOutput output) {
	}

	private static NumberField<Integer> integerField(int x, int y, int width,
	                                                 @Nullable Integer initial, @NonNull Consumer<@Nullable Integer> callback,
	                                                 Component label) {
		return new NumberField<>(label, x, y, width, LABEL_HEIGHT + FIELD_HEIGHT, initial,
				v -> Integer.parseInt(v.trim()),
				v -> true,
				v -> v == Integer.MAX_VALUE ? v : v + 1,
				v -> v == Integer.MIN_VALUE ? v : v - 1,
				callback);
	}

	private static NumberField<Long> positiveLongField(int x, int y, int width,
	                                                   @Nullable Long initial, @NonNull Consumer<@Nullable Long> callback,
	                                                   Component label) {
		return new NumberField<>(label, x, y, width, LABEL_HEIGHT + FIELD_HEIGHT, initial,
				v -> Long.parseLong(v.trim()),
				v -> v >= 0,
				v -> v == Long.MAX_VALUE ? v : v + 1,
				v -> v - 1,
				callback);
	}

	private static NumberField<Long> longField(int x, int y, int width,
	                                           @Nullable Long initial, @NonNull Consumer<@Nullable Long> callback,
	                                           @NonNull Component label) {
		return new NumberField<>(label, x, y, width, LABEL_HEIGHT + FIELD_HEIGHT, initial,
				v -> Long.parseLong(v.trim()),
				v -> true,
				v -> v == Long.MAX_VALUE ? v : v + 1,
				v -> v - 1,
				callback);
	}

	private static StringField stringField(int x, int y, int width,
	                                       @Nullable String initial, @NonNull Consumer<@Nullable String> callback,
	                                       @NonNull Component label) {
		return stringField(x, y, width, initial, callback, label, FIELD_PLACEHOLDER);
	}

	private static StringField stringField(int x, int y, int width,
	                                       @Nullable String initial, @NonNull Consumer<@Nullable String> callback,
	                                       @NonNull Component label, @NonNull Component placeholder) {
		return new StringField(label, x, y, width, LABEL_HEIGHT + FIELD_HEIGHT, initial, callback, placeholder);
	}

	private static class NumberField<N extends Number> extends SimpleParentWidget {
		private final @NonNull StringWidget labelWidget;
		private final @NonNull PlaceholderEditBox editBox;

		private @Nullable N value;

		public NumberField(
				Component label,
				int x,
				int y,
				int width,
				int height,
				@Nullable N initialValue,
				@NonNull Function<String, N> parser,
				@NonNull Predicate<N> validator,
				@NonNull UnaryOperator<N> increment,
				@NonNull UnaryOperator<N> decrement,
				@NonNull Consumer<@Nullable N> callback
		) {
			super(x, y, width, height);
			this.value = initialValue;

			this.labelWidget = new StringWidget(x, y, width, LABEL_HEIGHT, label, MINECRAFT.font);

			this.editBox = new PlaceholderEditBox(
					MINECRAFT.font,
					x + BUTTON_WIDTH, y + LABEL_HEIGHT,
					width - BUTTON_WIDTH * 3, FIELD_HEIGHT,
					Component.empty()
			);
			editBox.setPlaceholder(FIELD_PLACEHOLDER);
			editBox.setValue(initialValue == null ? "" : initialValue.toString());

			Button decrementButton = Button.builder(
							Component.literal("-"),
							button -> {
								if (value == null) return;

								N next = decrement.apply(value);
								if (!validator.test(next)) return;

								value = next;
								editBox.setValue(value.toString());
								callback.accept(value);
							})
					.tooltip(Tooltip.create(Component.translatable("screenshot_utilities.int_field.decrement")))
					.bounds(x, y + LABEL_HEIGHT, BUTTON_WIDTH, FIELD_HEIGHT)
					.build();

			Button incrementButton = Button.builder(
							Component.literal("+"),
							button -> {
								if (value == null) return;

								N next = increment.apply(value);
								if (!validator.test(next)) return;

								value = next;
								editBox.setValue(value.toString());
								callback.accept(value);
							})
					.tooltip(Tooltip.create(Component.translatable("screenshot_utilities.int_field.increment")))
					.bounds(x + width - BUTTON_WIDTH * 2, y + LABEL_HEIGHT, BUTTON_WIDTH, FIELD_HEIGHT)
					.build();

			Button resetButton = SpriteIconButton.CenteredIcon.builder(
							Component.translatable("screenshot_utilities.reset"),
							button -> {
								value = initialValue;
								editBox.setValue(initialValue == null ? "" : initialValue.toString());
								callback.accept(initialValue);
							}, true)
					.withTootip()
					.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/reset"), 15, 15)
					.size(BUTTON_WIDTH, FIELD_HEIGHT)
					.build();
			resetButton.setPosition(x + width - BUTTON_WIDTH, y + LABEL_HEIGHT);
			resetButton.active = false;

			editBox.setResponder(text -> {
				if (text.isEmpty()) {
					value = null;
					callback.accept(null);
					return;
				}
				try {
					N parsed = parser.apply(text);
					if (!validator.test(parsed)) {
						editBox.setTextColor(Colors.RED);
						return;
					}
					value = parsed;
					editBox.setTextColor(Colors.WHITE);
					callback.accept(value);
				} catch (NumberFormatException e) {
					editBox.setTextColor(Colors.RED);
				}

				resetButton.active = !Objects.equals(value, initialValue);
			});

			addRenderableChild(labelWidget);
			addRenderableChild(decrementButton);
			addRenderableChild(editBox);
			addRenderableChild(incrementButton);
			addRenderableChild(resetButton);
		}

		@Override
		public void updateNarration(@NonNull NarrationElementOutput output) {
		}
	}

	private static class StringField extends SimpleParentWidget {
		private final @NonNull StringWidget labelWidget;
		private final @NonNull PlaceholderEditBox editBox;

		private @Nullable String value;

		public StringField(
				Component label,
				int x,
				int y,
				int width,
				int height,
				@Nullable String initialValue,
				@NonNull Consumer<@Nullable String> callback,
				@NonNull Component placeholder
		) {
			super(x, y, width, height);
			this.value = initialValue;

			labelWidget = new StringWidget(x, y, width, LABEL_HEIGHT, label, MINECRAFT.font);

			editBox = new PlaceholderEditBox(MINECRAFT.font, x, y + LABEL_HEIGHT, width - BUTTON_WIDTH, FIELD_HEIGHT, Component.empty());
			editBox.setMaxLength(100);
			editBox.setValue(initialValue == null ? "" : initialValue);
			editBox.setPlaceholder(placeholder);

			Button resetButton = SpriteIconButton.CenteredIcon.builder(
							Component.translatable("screenshot_utilities.reset"),
							button -> {
								value = initialValue;
								editBox.setValue(initialValue == null ? "" : initialValue);
								callback.accept(initialValue);
							}, true)
					.withTootip()
					.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/reset"), 15, 15)
					.size(BUTTON_WIDTH, FIELD_HEIGHT)
					.build();
			resetButton.setPosition(x + width - BUTTON_WIDTH, y + LABEL_HEIGHT);
			resetButton.active = false;

			editBox.setResponder(string -> {
				if (string.isEmpty()) {
					value = null;
				} else {
					value = string.trim();
				}

				resetButton.active = !Objects.equals(value, initialValue);

				try {
					callback.accept(value);
					editBox.setTextColor(Colors.WHITE);
				} catch (Exception e) {
					editBox.setTextColor(Colors.RED);
				}
			});

			addRenderableChild(labelWidget);
			addRenderableChild(editBox);
			addRenderableChild(resetButton);
		}

		@Override
		public void updateNarration(@NonNull NarrationElementOutput output) {
		}
	}
}
