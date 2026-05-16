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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.*;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.gui.widget.PlaceholderEditBox;
import me.Azz_9.screenshot_utilities.client.gui.widget.SimpleParentWidget;
import me.Azz_9.screenshot_utilities.client.gui.widget.SmoothScrollableWidget;
import me.Azz_9.screenshot_utilities.client.screenshot.Screenshot;
import me.Azz_9.screenshot_utilities.client.screenshot.ScreenshotMetadata;

@Environment(EnvType.CLIENT)
public class MetadataEditorPanel extends SmoothScrollableWidget {

	// layout
	private static final int PANEL_PADDING = 10;
	private static final int FIELD_HEIGHT = 20;
	private static final int LABEL_HEIGHT = 10;
	private static final int FIELD_GAP = 6;
	private static final int BUTTON_WIDTH = 20;
	private static final int HEADER_HEIGHT = PANEL_PADDING + LABEL_HEIGHT + PANEL_PADDING;

	private static final Component FIELD_PLACEHOLDER = Component.literal("empty");

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
				Component.translatable("screenshot_utilities.edit_metadata"), MINECRAFT.font
		);
		addFixedChild(title);

		addFixedChild(createCloseButton());

		// scrollable fields

		ScreenshotMetadata meta = screenshot.getMetadata();

		new FieldListBuilder(this)
				.longField(meta::getX, meta::setX, "screenshot_utilities.metadata.x")
				.longField(meta::getY, meta::setY, "screenshot_utilities.metadata.y")
				.longField(meta::getZ, meta::setZ, "screenshot_utilities.metadata.z")
				.stringField(() -> meta.getDimension() == null ? null : meta.getDimension().toString(),
						v -> meta.setDimension(v == null ? null : Identifier.tryParse(v)),
						"screenshot_utilities.metadata.dimension")
				.stringField(() -> meta.getBiome() == null ? null : meta.getBiome().toString(),
						v -> meta.setBiome(v == null ? null : Identifier.tryParse(v)),
						"screenshot_utilities.metadata.biome")
				.longField(meta::getSeed, meta::setSeed, "screenshot_utilities.metadata.seed")
				.stringField(meta::getWorldName, meta::setWorldName, "screenshot_utilities.metadata.world_name")
				.stringField(meta::getServerIp, meta::setServerIp, "screenshot_utilities.metadata.server")
				.stringField(meta::getVersion, meta::setVersion, "screenshot_utilities.metadata.version")
				.stringField(() -> String.join(", ", meta.getResourcePacks()),
						v -> meta.setResourcePacks(v == null
								? new ArrayList<>()
								: Arrays.stream(v.split(",")).map(String::trim).toList()),
						"screenshot_utilities.metadata.resource_packs",
						Component.translatable("screenshot_utilities.metadata.resource_packs.placeholder"))
				.stringField(meta::getShader, meta::setShader, "screenshot_utilities.metadata.shader")
				.positiveLongField(meta::getTimestamp, meta::setTimestamp, "screenshot_utilities.metadata.timestamp")
				.stringField(() -> String.join(", ", meta.getTags()),
						v -> meta.setTags(v == null
								? new ArrayList<>()
								: Arrays.stream(v.split(",")).map(String::trim).toList()),
						"screenshot_utilities.metadata.tags",
						Component.translatable("screenshot_utilities.metadata.tags.placeholder"))
				.build();
	}

	private Button createCloseButton() {
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

	public void setOnVisibilityChange(@Nullable Consumer<Boolean> cb) {
		this.onVisibilityChange = cb;
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

		FieldListBuilder(@NonNull MetadataEditorPanel panel) {
			this.panel = panel;
		}

		@NonNull FieldListBuilder longField(
				@NonNull Supplier<@Nullable Long> getter,
				@NonNull Consumer<@Nullable Long> setter,
				@NonNull String translationKey
		) {
			return addField(new NumberField<>(
					Component.translatable(translationKey),
					fieldX(), cursorY, fieldWidth(), LABEL_HEIGHT + FIELD_HEIGHT,
					getter.get(),
					v -> Long.parseLong(v.trim()),
					v -> true,
					v -> v == Long.MAX_VALUE ? v : v + 1,
					v -> v - 1,
					setter));
		}

		@NonNull FieldListBuilder positiveLongField(
				@NonNull Supplier<@Nullable Long> getter,
				@NonNull Consumer<@Nullable Long> setter,
				@NonNull String translationKey
		) {
			return addField(new NumberField<>(
					Component.translatable(translationKey),
					fieldX(), cursorY, fieldWidth(), LABEL_HEIGHT + FIELD_HEIGHT,
					getter.get(),
					v -> Long.parseLong(v.trim()),
					v -> v >= 0,
					v -> v == Long.MAX_VALUE ? v : v + 1,
					v -> v - 1,
					setter));
		}

		@NonNull FieldListBuilder stringField(
				@NonNull Supplier<@Nullable String> getter,
				@NonNull Consumer<@Nullable String> setter,
				@NonNull String translationKey
		) {
			return stringField(getter, setter, translationKey, null);
		}

		@NonNull FieldListBuilder stringField(
				@NonNull Supplier<@Nullable String> getter,
				@NonNull Consumer<@Nullable String> setter,
				@NonNull String translationKey,
				@Nullable Component placeholder
		) {
			return addField(new StringField(
					Component.translatable(translationKey),
					fieldX(), cursorY, fieldWidth(), LABEL_HEIGHT + FIELD_HEIGHT,
					getter.get(),
					setter,
					placeholder != null ? placeholder : FIELD_PLACEHOLDER));
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

		private final @NonNull PlaceholderEditBox editBox;

		@SuppressWarnings("unchecked")
		NumberField(
				@NonNull Component label,
				int x, int y, int width, int height,
				@Nullable N initialValue,
				@NonNull Function<String, N> parser,
				@NonNull Predicate<N> validator,
				@NonNull UnaryOperator<N> increment,
				@NonNull UnaryOperator<N> decrement,
				@NonNull Consumer<@Nullable N> callback
		) {
			super(x, y, width, height);

			// Mutable box — lambdas need a stable reference to the current value
			N[] current = (N[]) new Number[]{initialValue};

			addRenderableChild(new StringWidget(
					x, y, width, LABEL_HEIGHT, label, MINECRAFT.font));

			editBox = new PlaceholderEditBox(
					MINECRAFT.font,
					x + BUTTON_WIDTH, y + LABEL_HEIGHT,
					width - BUTTON_WIDTH * 3, FIELD_HEIGHT,
					Component.empty());
			editBox.setPlaceholder(FIELD_PLACEHOLDER);
			editBox.setValue(initialValue == null ? "" : initialValue.toString());

			Button resetBtn = SpriteIconButton.CenteredIcon.builder(
							Component.translatable("screenshot_utilities.reset"),
							btn -> {
								current[0] = initialValue;
								editBox.setValue(initialValue == null ? "" : initialValue.toString());
								editBox.setTextColor(Colors.WHITE);
								callback.accept(initialValue);
								btn.active = false;
							}, true)
					.withTootip()
					.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/reset"), 15, 15)
					.size(BUTTON_WIDTH, FIELD_HEIGHT)
					.build();
			resetBtn.setPosition(x + width - BUTTON_WIDTH, y + LABEL_HEIGHT);
			resetBtn.active = false;

			Button decrementBtn = Button.builder(Component.literal("-"), btn -> {
						if (current[0] == null) return;
						N next = decrement.apply(current[0]);
						if (!validator.test(next)) return;
						current[0] = next;
						editBox.setValue(next.toString());
						callback.accept(next);
						resetBtn.active = !Objects.equals(next, initialValue);
					})
					.tooltip(Tooltip.create(
							Component.translatable("screenshot_utilities.int_field.decrement")))
					.bounds(x, y + LABEL_HEIGHT, BUTTON_WIDTH, FIELD_HEIGHT)
					.build();

			Button incrementBtn = Button.builder(Component.literal("+"), btn -> {
						if (current[0] == null) return;
						N next = increment.apply(current[0]);
						if (!validator.test(next)) return;
						current[0] = next;
						editBox.setValue(next.toString());
						callback.accept(next);
						resetBtn.active = !Objects.equals(next, initialValue);
					})
					.tooltip(Tooltip.create(
							Component.translatable("screenshot_utilities.int_field.increment")))
					.bounds(x + width - BUTTON_WIDTH * 2, y + LABEL_HEIGHT, BUTTON_WIDTH, FIELD_HEIGHT)
					.build();

			editBox.setResponder(text -> {
				if (text.isEmpty()) {
					current[0] = null;
					callback.accept(null);
					editBox.setTextColor(Colors.WHITE);
					resetBtn.active = initialValue != null;
					return;
				}
				try {
					N parsed = parser.apply(text);
					if (!validator.test(parsed)) {
						editBox.setTextColor(Colors.RED);
						return;
					}
					current[0] = parsed;
					editBox.setTextColor(Colors.WHITE);
					callback.accept(parsed);
					resetBtn.active = !Objects.equals(parsed, initialValue);
				} catch (NumberFormatException ignored) {
					editBox.setTextColor(Colors.RED);
				}
			});

			addRenderableChild(decrementBtn);
			addRenderableChild(editBox);
			addRenderableChild(incrementBtn);
			addRenderableChild(resetBtn);
		}

		@Override
		public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		}
	}

	private static final class StringField extends SimpleParentWidget {

		private final @NonNull PlaceholderEditBox editBox;

		StringField(
				@NonNull Component label,
				int x, int y, int width, int height,
				@Nullable String initialValue,
				@NonNull Consumer<@Nullable String> setter,
				@NonNull Component placeholder
		) {
			super(x, y, width, height);

			addRenderableChild(new StringWidget(
					x, y, width, LABEL_HEIGHT, label, MINECRAFT.font));

			editBox = new PlaceholderEditBox(
					MINECRAFT.font,
					x, y + LABEL_HEIGHT,
					width - BUTTON_WIDTH, FIELD_HEIGHT,
					Component.empty());
			editBox.setMaxLength(200);
			editBox.setValue(initialValue == null ? "" : initialValue);
			editBox.setPlaceholder(placeholder);

			Button resetBtn = SpriteIconButton.CenteredIcon.builder(
							Component.translatable("screenshot_utilities.reset"),
							btn -> {
								editBox.setValue(initialValue == null ? "" : initialValue);
								editBox.setTextColor(Colors.WHITE);
								setter.accept(initialValue);
								btn.active = false;
							}, true)
					.withTootip()
					.sprite(Identifier.fromNamespaceAndPath(MOD_ID, "icon/reset"), 15, 15)
					.size(BUTTON_WIDTH, FIELD_HEIGHT)
					.build();
			resetBtn.setPosition(x + width - BUTTON_WIDTH, y + LABEL_HEIGHT);
			resetBtn.active = false;

			editBox.setResponder(text -> {
				String value = text.isEmpty() ? null : text.trim();
				resetBtn.active = !Objects.equals(value, initialValue);
				try {
					setter.accept(value);
					editBox.setTextColor(Colors.WHITE);
				} catch (Exception ignored) {
					editBox.setTextColor(Colors.RED);
				}
			});

			addRenderableChild(editBox);
			addRenderableChild(resetBtn);
		}

		@Override
		public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
		}
	}
}
