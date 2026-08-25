package me.Azz_9.screenshot_overhaul.client.gui.components.screenshotGallery;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.CLOSE_SPRITE;
import static me.Azz_9.screenshot_overhaul.client.CommonSprites.RESET_SPRITE;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.*;

import me.Azz_9.screenshot_overhaul.client.Colors;
import me.Azz_9.screenshot_overhaul.client.gui.components.CustomEditBox;
import me.Azz_9.screenshot_overhaul.client.gui.components.SimpleParentWidget;
import me.Azz_9.screenshot_overhaul.client.gui.components.SmoothScrollableWidget;
import me.Azz_9.screenshot_overhaul.client.metadata.Metadata;
import me.Azz_9.screenshot_overhaul.client.screenshot.Screenshot;

public class MetadataEditorPanel extends SmoothScrollableWidget {

	// layout
	private static final int PANEL_PADDING = 10;
	private static final int FIELD_HEIGHT = 20;
	private static final int LABEL_HEIGHT = 10;
	private static final int FIELD_GAP = 6;
	private static final int BUTTON_WIDTH = 20;
	private static final int HEADER_HEIGHT = PANEL_PADDING + LABEL_HEIGHT + PANEL_PADDING;

	private static final @NonNull Component FIELD_PLACEHOLDER = Component.translatable("screenshot_overhaul.empty");
	private static final @NonNull DateTimeFormatter TIMESTAMP_TOOLTIP_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

	// animation
	private static final int ANIMATION_DURATION = 400;
	private long animationStart;
	private float progress;
	private boolean slidingIn = false;

	// scroll
	private int totalFieldsHeight = 0;

	// callback
	private @Nullable Consumer<Boolean> onVisibilityChange;

	public MetadataEditorPanel(int width, int height) {
		super(MINECRAFT.getWindow().getGuiScaledWidth() - width,
				(MINECRAFT.getWindow().getGuiScaledHeight() - height) / 2,
				width, height);
	}

	public void init(@NonNull Screenshot screenshot) {
		clearChildren();
		clearScrollableChildren();

		// fixed header

		StringWidget title = new StringWidget(
				getX() + PANEL_PADDING, getY() + PANEL_PADDING,
				getWidth() - PANEL_PADDING * 2, LABEL_HEIGHT,
				Component.translatable("screenshot_overhaul.edit_metadata"), MINECRAFT.font
		);
		addFixedChild(title);

		addFixedChild(createCloseButton());

		// scrollable fields

		Metadata meta = screenshot.getMetadata();

		new FieldListBuilder(this)
				.intField(meta::getX, meta::setX, Component.translatable("screenshot_overhaul.metadata.x"))
				.intField(meta::getY, meta::setY, Component.translatable("screenshot_overhaul.metadata.y"))
				.intField(meta::getZ, meta::setZ, Component.translatable("screenshot_overhaul.metadata.z"))
				.stringField(() -> meta.getDimension() == null ? null : meta.getDimension().toString(),
						v -> meta.setDimension(v == null ? null : Identifier.tryParse(v)),
						Component.translatable("screenshot_overhaul.metadata.dimension"))
				.stringField(() -> meta.getBiome() == null ? null : meta.getBiome().toString(),
						v -> meta.setBiome(v == null ? null : Identifier.tryParse(v)),
						Component.translatable("screenshot_overhaul.metadata.biome"))
				.longField(meta::getSeed, meta::setSeed, Component.translatable("screenshot_overhaul.metadata.seed"))
				.stringField(meta::getWorldName, meta::setWorldName, Component.translatable("screenshot_overhaul.metadata.world_name"))
				.stringField(meta::getServerIp, meta::setServerIp, Component.translatable("screenshot_overhaul.metadata.server"))
				.stringField(meta::getVersion, meta::setVersion, Component.translatable("screenshot_overhaul.metadata.version"))
				.stringField(() -> String.join(", ", meta.getResourcePacks()),
						v -> meta.setResourcePacks(v == null
								? new ArrayList<>()
								: Arrays.stream(v.split(",")).map(String::trim).toList()),
						Component.translatable("screenshot_overhaul.metadata.resource_packs"),
						Component.translatable("screenshot_overhaul.metadata.resource_packs.placeholder"))
				.stringField(meta::getShader, meta::setShader, Component.translatable("screenshot_overhaul.metadata.shader"))
				.positiveLongField(
						meta::getTimestamp, meta::setTimestamp,
						Component.translatable("screenshot_overhaul.metadata.timestamp"),
						MetadataEditorPanel::timestampTooltip)
				.build();
	}

	private static @Nullable Component timestampTooltip(@Nullable Long timestamp) {
		if (timestamp == null) return null;
		return Component.literal(TIMESTAMP_TOOLTIP_FORMATTER.format(Instant.ofEpochMilli(timestamp)));
	}

	private @NonNull Button createCloseButton() {
		Button closeButton = SpriteIconButton.CenteredIcon.builder(
						Component.translatable("screenshot_overhaul.close"),
						_ -> setVisible(false),
						true
				)
				.withTootip()
				.sprite(CLOSE_SPRITE, 15, 15)
				.size(BUTTON_WIDTH, FIELD_HEIGHT)
				.build();
		closeButton.setPosition(getRight() - BUTTON_WIDTH - PANEL_PADDING, PANEL_PADDING);
		return closeButton;
	}

	// SmoothScrollableWidget contract

	@Override
	protected int getTotalScrollableHeight() {
		return totalFieldsHeight;
	}

	@Override
	protected @NonNull ScrollArea getScrollArea() {
		return new ScrollArea(getX(), getY() + HEADER_HEIGHT, getRight(), getBottom());
	}

	// render
	@Override
	protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		updateAnimationProgress();

		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(getWidth() * (1.0f - progress), 0);

		graphics.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK_SEMI_TRANSPARENT);

		if (isMouseOver(mouseX, mouseY)) graphics.requestCursor(CursorTypes.ARROW);

		super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

		matrices.popMatrix();
	}

	// animation

	private void updateAnimationProgress() {
		float t = Math.min(1.0f,
				(float) (System.currentTimeMillis() - animationStart) / ANIMATION_DURATION);
		progress = slidingIn ? Ease.outQuad(t) : 1.0f - Ease.outQuad(t);
		if (!slidingIn && t >= 1.0f) super.setVisible(false);
	}

	@Override
	public void setVisible(boolean visible) {
		slidingIn = visible;
		animationStart = System.currentTimeMillis();
		if (onVisibilityChange != null) onVisibilityChange.accept(visible);
		if (visible) super.setVisible(true);
	}

	@Override
	public boolean isVisible() {
		return slidingIn;
	}

	public void setOnVisibilityChange(@Nullable Consumer<Boolean> onVisibilityChange) {
		this.onVisibilityChange = onVisibilityChange;
	}


	void registerField(@NonNull SimpleParentWidget widget) {
		addScrollableChild(widget);
	}

	void setTotalFieldsHeight(int height) {
		this.totalFieldsHeight = height;
	}

	@Override
	protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
	}

	/**
	 * Fluent builder that creates field widgets and registers them with the panel.
	 * Tracks a running Y cursor so callers never deal with layout arithmetic.
	 */
	private static final class FieldListBuilder {

		private final @NonNull MetadataEditorPanel panel;
		private int cursorY = 0;

		@NonNull FieldListBuilder intField(
				@NonNull Supplier<@Nullable Integer> getter,
				@NonNull Consumer<@Nullable Integer> setter,
				@NonNull Component label
		) {
			return intField(getter, setter, label, null);
		}

		@NonNull FieldListBuilder intField(
				@NonNull Supplier<@Nullable Integer> getter,
				@NonNull Consumer<@Nullable Integer> setter,
				@NonNull Component label,
				@Nullable Function<@Nullable Integer, @Nullable Component> tooltip
		) {
			return addField(new NumberField<>(
					label,
					fieldX(), cursorY, fieldWidth(), LABEL_HEIGHT + FIELD_HEIGHT,
					getter.get(),
					v -> Integer.parseInt(v.trim()),
					v -> true,
					v -> v == Integer.MAX_VALUE ? v : v + 1,
					v -> v - 1,
					setter,
					tooltip));
		}

		FieldListBuilder(@NonNull MetadataEditorPanel panel) {
			this.panel = panel;
		}

		@NonNull FieldListBuilder longField(
				@NonNull Supplier<@Nullable Long> getter,
				@NonNull Consumer<@Nullable Long> setter,
				@NonNull Component label
		) {
			return longField(getter, setter, label, null);
		}

		@NonNull FieldListBuilder longField(
				@NonNull Supplier<@Nullable Long> getter,
				@NonNull Consumer<@Nullable Long> setter,
				@NonNull Component label,
				@Nullable Function<@Nullable Long, @Nullable Component> tooltip
		) {
			return addField(new NumberField<>(
					label,
					fieldX(), cursorY, fieldWidth(), LABEL_HEIGHT + FIELD_HEIGHT,
					getter.get(),
					v -> Long.parseLong(v.trim()),
					v -> true,
					v -> v == Long.MAX_VALUE ? v : v + 1,
					v -> v - 1,
					setter,
					tooltip));
		}

		@NonNull FieldListBuilder positiveLongField(
				@NonNull Supplier<@Nullable Long> getter,
				@NonNull Consumer<@Nullable Long> setter,
				@NonNull Component label
		) {
			return positiveLongField(getter, setter, label, null);
		}

		@NonNull FieldListBuilder positiveLongField(
				@NonNull Supplier<@Nullable Long> getter,
				@NonNull Consumer<@Nullable Long> setter,
				@NonNull Component label,
				@Nullable Function<@Nullable Long, @Nullable Component> tooltip
		) {
			return addField(new NumberField<>(
					label,
					fieldX(), cursorY, fieldWidth(), LABEL_HEIGHT + FIELD_HEIGHT,
					getter.get(),
					v -> Long.parseLong(v.trim()),
					v -> v >= 0,
					v -> v == Long.MAX_VALUE ? v : v + 1,
					v -> v - 1,
					setter,
					tooltip));
		}

		@NonNull FieldListBuilder stringField(
				@NonNull Supplier<@Nullable String> getter,
				@NonNull Consumer<@Nullable String> setter,
				@NonNull Component label
		) {
			return stringField(getter, setter, label, null);
		}

		@NonNull FieldListBuilder stringField(
				@NonNull Supplier<@Nullable String> getter,
				@NonNull Consumer<@Nullable String> setter,
				@NonNull Component label,
				@Nullable Component placeholder
		) {
			return stringField(getter, setter, label, placeholder, null);
		}

		@NonNull FieldListBuilder stringField(
				@NonNull Supplier<@Nullable String> getter,
				@NonNull Consumer<@Nullable String> setter,
				@NonNull Component label,
				@Nullable Component placeholder,
				@Nullable Function<@Nullable String, @Nullable Component> tooltip
		) {
			return addField(new StringField(
					label,
					fieldX(), cursorY, fieldWidth(), LABEL_HEIGHT + FIELD_HEIGHT,
					getter.get(),
					setter,
					placeholder != null ? placeholder : FIELD_PLACEHOLDER,
					tooltip));
		}

		void build() {
			panel.setTotalFieldsHeight(cursorY);
		}

		private @NonNull FieldListBuilder addField(@NonNull SimpleParentWidget widget) {
			// Store content-space Y inside widget.y — the render loop uses and
			// restores this value so hit-testing stays consistent between frames.
			widget.setY(cursorY);
			panel.registerField(widget);
			cursorY += widget.getHeight() + FIELD_GAP;
			return this;
		}

		private int fieldX() {
			return panel.getX() + PANEL_PADDING;
		}

		private int fieldWidth() {
			return panel.getWidth() - PANEL_PADDING * 2;
		}
	}

	private static final class NumberField<N extends Number> extends SimpleParentWidget {

		private final @NonNull CustomEditBox editBox;
		private final @Nullable Function<@Nullable N, @Nullable Component> tooltip;
		private final N[] current;
		private boolean valid = true;

		private final @NonNull Predicate<N> validator;
		private final @NonNull Consumer<@Nullable N> callback;
		private final @Nullable N initialValue;
		private final @NonNull Button resetBtn;

		@SuppressWarnings("unchecked")
		NumberField(
				@NonNull Component label,
				int x, int y, int width, int height,
				@Nullable N initialValue,
				@NonNull Function<String, N> parser,
				@NonNull Predicate<N> validator,
				@NonNull UnaryOperator<N> increment,
				@NonNull UnaryOperator<N> decrement,
				@NonNull Consumer<@Nullable N> callback,
				@Nullable Function<@Nullable N, @Nullable Component> tooltip
		) {
			super(x, y, width, height);
			this.tooltip = tooltip;
			this.validator = validator;
			this.callback = callback;
			this.initialValue = initialValue;

			// Mutable box — lambdas need a stable reference to the current value
			this.current = (N[]) new Number[]{initialValue};

			addRenderableChild(new StringWidget(
					x, y, width, LABEL_HEIGHT, label, MINECRAFT.font));

			editBox = new CustomEditBox(
					x + BUTTON_WIDTH, y + LABEL_HEIGHT,
					width - BUTTON_WIDTH * 3, FIELD_HEIGHT,
					Component.empty());
			editBox.setPlaceholder(FIELD_PLACEHOLDER);
			editBox.setValue(initialValue == null ? "" : initialValue.toString());

			resetBtn = SpriteIconButton.CenteredIcon.builder(
							Component.translatable("screenshot_overhaul.reset"),
							btn -> {
								current[0] = initialValue;
								valid = true;
								editBox.setValue(initialValue == null ? "" : initialValue.toString());
								editBox.setTextColor(Colors.WHITE);
								callback.accept(initialValue);
								btn.active = false;
							}, true)
					.withTootip()
					.sprite(RESET_SPRITE, 15, 15)
					.size(BUTTON_WIDTH, FIELD_HEIGHT)
					.build();
			resetBtn.setPosition(x + width - BUTTON_WIDTH, y + LABEL_HEIGHT);
			resetBtn.active = false;

			Button decrementBtn = Button.builder(Component.literal("-"), _ -> applyOperator(decrement))
					.tooltip(Tooltip.create(
							Component.translatable("screenshot_overhaul.int_field.decrement")))
					.bounds(x, y + LABEL_HEIGHT, BUTTON_WIDTH, FIELD_HEIGHT)
					.build();

			Button incrementBtn = Button.builder(Component.literal("+"), _ -> applyOperator(increment))
					.tooltip(Tooltip.create(
							Component.translatable("screenshot_overhaul.int_field.increment")))
					.bounds(x + width - BUTTON_WIDTH * 2, y + LABEL_HEIGHT, BUTTON_WIDTH, FIELD_HEIGHT)
					.build();

			editBox.setResponder(text -> {
				if (text.isEmpty()) {
					current[0] = null;
					valid = true;
					callback.accept(null);
					editBox.setTextColor(Colors.WHITE);
					resetBtn.active = initialValue != null;
					return;
				}
				try {
					N parsed = parser.apply(text);
					if (!validator.test(parsed)) {
						valid = false;
						editBox.setTextColor(Colors.RED);
						return;
					}
					current[0] = parsed;
					valid = true;
					editBox.setTextColor(Colors.WHITE);
					callback.accept(parsed);
					resetBtn.active = !Objects.equals(parsed, initialValue);
				} catch (NumberFormatException ignored) {
					valid = false;
					editBox.setTextColor(Colors.RED);
				}
			});

			addRenderableChild(decrementBtn);
			addRenderableChild(editBox);
			addRenderableChild(incrementBtn);
			addRenderableChild(resetBtn);
		}

		private void applyOperator(@NonNull UnaryOperator<N> operator) {
			if (current[0] == null) return;
			N next = operator.apply(current[0]);
			if (!validator.test(next)) return;
			current[0] = next;
			editBox.setValue(next.toString());
			callback.accept(next);
			resetBtn.active = !Objects.equals(next, initialValue);
		}

		@Override
		protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
			renderFieldTooltip(graphics, editBox, tooltip, valid ? current[0] : null, mouseX, mouseY);
		}

		@Override
		public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		}
	}

	private static final class StringField extends SimpleParentWidget {

		private final @NonNull CustomEditBox editBox;
		private final @Nullable Function<@Nullable String, @Nullable Component> tooltip;
		private @Nullable String currentValue;
		private boolean valid = true;

		StringField(
				@NonNull Component label,
				int x, int y, int width, int height,
				@Nullable String initialValue,
				@NonNull Consumer<@Nullable String> setter,
				@NonNull Component placeholder,
				@Nullable Function<@Nullable String, @Nullable Component> tooltip
		) {
			super(x, y, width, height);
			this.tooltip = tooltip;
			currentValue = initialValue;

			addRenderableChild(new StringWidget(
					x, y, width, LABEL_HEIGHT, label, MINECRAFT.font));

			editBox = new CustomEditBox(
					x, y + LABEL_HEIGHT,
					width - BUTTON_WIDTH, FIELD_HEIGHT,
					Component.empty());
			editBox.setMaxLength(200);
			editBox.setValue(initialValue == null ? "" : initialValue);
			editBox.setPlaceholder(placeholder);

			Button resetBtn = SpriteIconButton.CenteredIcon.builder(
							Component.translatable("screenshot_overhaul.reset"),
							btn -> {
								currentValue = initialValue;
								valid = true;
								editBox.setValue(initialValue == null ? "" : initialValue);
								editBox.setTextColor(Colors.WHITE);
								setter.accept(initialValue);
								btn.active = false;
							}, true)
					.withTootip()
					.sprite(RESET_SPRITE, 15, 15)
					.size(BUTTON_WIDTH, FIELD_HEIGHT)
					.build();
			resetBtn.setPosition(x + width - BUTTON_WIDTH, y + LABEL_HEIGHT);
			resetBtn.active = false;

			editBox.setResponder(text -> {
				String value = text.isEmpty() ? null : text.trim();
				resetBtn.active = !Objects.equals(value, initialValue);
				try {
					setter.accept(value);
					currentValue = value;
					valid = true;
					editBox.setTextColor(Colors.WHITE);
				} catch (Exception ignored) {
					valid = false;
					editBox.setTextColor(Colors.RED);
				}
			});

			addRenderableChild(editBox);
			addRenderableChild(resetBtn);
		}

		@Override
		protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
			renderFieldTooltip(graphics, editBox, tooltip, valid ? currentValue : null, mouseX, mouseY);
		}

		@Override
		public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		}
	}

	private static <T> void renderFieldTooltip(
			@NonNull GuiGraphicsExtractor graphics,
			@NonNull CustomEditBox editBox,
			@Nullable Function<@Nullable T, @Nullable Component> tooltip,
			@Nullable T value,
			int mouseX, int mouseY
	) {
		if (tooltip == null || !editBox.isHovered()) return;
		Component component = tooltip.apply(value);
		if (component != null) {
			graphics.setTooltipForNextFrame(MINECRAFT.font, component, mouseX, mouseY);
		}
	}
}
