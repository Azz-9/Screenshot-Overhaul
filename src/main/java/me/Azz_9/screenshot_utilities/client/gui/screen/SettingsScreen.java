package me.Azz_9.screenshot_utilities.client.gui.screen;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import me.Azz_9.screenshot_utilities.client.Colors;
import me.Azz_9.screenshot_utilities.client.config.Config;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.options.BooleanConfigOption;
import me.Azz_9.screenshot_utilities.client.config.option.options.PathConfigOption;
import me.Azz_9.screenshot_utilities.client.config.widget.ConfigOptionListWidget;
import me.Azz_9.screenshot_utilities.client.config.widget.ConfigTabContent;
import me.Azz_9.screenshot_utilities.client.config.widget.UnsavedChangesOverlay;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

/**
 * Settings screen with tabs, scrollable option lists, validation, and an
 * "unsaved changes" overlay.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * SettingsScreen screen = new SettingsScreen(currentScreen);
 *
 * ConfigTabContent general = ConfigTabContent.builder()
 *     .section(Component.literal("Appearance"))
 *     .option(BooleanConfigOption.builder(myToggle).build())
 *     .section(Component.literal("Paths"))
 *     .option(PathConfigOption.builder(myPath).selectionMode(DIRECTORIES_ONLY).build())
 *     .build();
 *
 * screen.addConfigTab(Component.literal("General"), general);
 * Minecraft.getInstance().setScreen(screen);
 * }</pre>
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends TabsScreen implements FocusableScreen {

	// -------------------------------------------------------------------------
	// Layout
	// -------------------------------------------------------------------------

	private static final int BOTTOM_BAR_HEIGHT = 36;
	private static final int BUTTON_WIDTH = 120;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 8;
	private static final int LIST_TOP_PADDING = 8;

	// -------------------------------------------------------------------------
	// State
	// -------------------------------------------------------------------------

	private final @NonNull FocusManager focusManager = new FocusManager();

	/**
	 * Maps each Tab to its content definition.
	 */
	private final @NonNull Map<Tab, ConfigTabContent> tabContents = new LinkedHashMap<>();

	/**
	 * The currently displayed list widget.
	 */
	private @Nullable ConfigOptionListWidget listWidget;

	/**
	 * Bottom-bar buttons.
	 */
	private @Nullable Button saveButton;
	private @Nullable Button cancelButton;

	/**
	 * Overlay shown when closing with unsaved changes; {@code null} when inactive.
	 */
	private @Nullable UnsavedChangesOverlay unsavedOverlay;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	public SettingsScreen(@Nullable Screen parent) {
		super(Component.translatable("screenshot_utilities.settings"), parent);
	}

	// -------------------------------------------------------------------------
	// Tab registration (call before the screen is opened)
	// -------------------------------------------------------------------------

	/**
	 * Registers a tab with its content. Tabs are displayed in registration order.
	 *
	 * @param tabLabel human-readable tab title
	 * @param content  the option layout for this tab
	 */
	public void addConfigTab(@NonNull Component tabLabel, @NonNull ConfigTabContent content) {
		Tab tab = new SettingsScreen.ConfigTab(tabLabel, content);
		tabContents.put(tab, content);
	}

	// -------------------------------------------------------------------------
	// Screen lifecycle
	// -------------------------------------------------------------------------

	@Override
	protected void init() {

		// General
		BooleanConfigOption enableWholeMod = BooleanConfigOption.builder(Config.getInstance().enableWholeMod).build();
		BooleanConfigOption showScreenshotsOnXaerosWorldMap = BooleanConfigOption.builder(Config.getInstance().showScreenshotsOnXaerosWorldMap).build();

		ConfigTabContent generalContent = ConfigTabContent.builder()
				.option(enableWholeMod)
				.option(showScreenshotsOnXaerosWorldMap)
				.build();

		addConfigTab(Component.literal("General"), generalContent);

		// Screenshot
		PathConfigOption screenshotsDir = PathConfigOption.builder(Config.getInstance().screenshotsDir).build();
		BooleanConfigOption showChatMessage = BooleanConfigOption.builder(Config.getInstance().showChatMessage).build();

		ConfigTabContent screenshotContent = ConfigTabContent.builder()
				.option(screenshotsDir)
				.option(showChatMessage)
				.build();

		addConfigTab(Component.literal("Screenshot"), screenshotContent);

		// Photo mode
		BooleanConfigOption freezeInPhotoMode = BooleanConfigOption.builder(Config.getInstance().freezeInPhotoMode).build();
		BooleanConfigOption showPlayer = BooleanConfigOption.builder(Config.getInstance().showPlayer).build();
		BooleanConfigOption showNametags = BooleanConfigOption.builder(Config.getInstance().showNametags).build();

		ConfigTabContent photoModeContent = ConfigTabContent.builder()
				.option(freezeInPhotoMode)
				.option(showPlayer)
				.option(showNametags)
				.build();

		addConfigTab(Component.literal("Photo mode"), photoModeContent);

		super.init();

		// Register tabs
		tabContents.forEach((tab, content) -> addTab(tab));

		// List widget — fills space between tabs and bottom bar
		int tabsBottom = getTabsBottom();
		int listTop = tabsBottom + LIST_TOP_PADDING;
		int listHeight = height - listTop - BOTTOM_BAR_HEIGHT;

		listWidget = new ConfigOptionListWidget(0, listTop, width, listHeight);
		addRenderableWidget(listWidget);

		// Bottom-bar buttons
		int buttonsY = height - BOTTOM_BAR_HEIGHT + (BOTTOM_BAR_HEIGHT - BUTTON_HEIGHT) / 2;
		int totalW = BUTTON_WIDTH * 2 + BUTTON_GAP;
		int startX = (width - totalW) / 2;

		cancelButton = Button.builder(
						Component.translatable("screenshot_utilities.settings.cancel"),
						btn -> onCancelPressed())
				.pos(startX, buttonsY)
				.size(BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();

		saveButton = Button.builder(
						Component.translatable("screenshot_utilities.settings.save"),
						btn -> onSavePressed())
				.pos(startX + BUTTON_WIDTH + BUTTON_GAP, buttonsY)
				.size(BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();

		addRenderableWidget(cancelButton);
		addRenderableWidget(saveButton);

		// Select the first tab
		if (!tabContents.isEmpty()) {
			Tab firstTab = tabContents.keySet().iterator().next();
			selectTab(firstTab);
		}

		// Re-attach overlay if it was active before resize
		if (unsavedOverlay != null) {
			unsavedOverlay.resize(width, height);
		}
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
		// Suppress mouse coords for everything below the overlay
		int effectiveMouseX = unsavedOverlay != null ? -1 : mouseX;
		int effectiveMouseY = unsavedOverlay != null ? -1 : mouseY;

		super.extractRenderState(graphics, effectiveMouseX, effectiveMouseY, deltaTicks);

		// Bottom bar separator
		graphics.fill(0, height - BOTTOM_BAR_HEIGHT, width, height - BOTTOM_BAR_HEIGHT + 1, Colors.GRAY);

		// Update save button state
		if (saveButton != null && listWidget != null) {
			saveButton.active = listWidget.hasAnyChanged() && listWidget.isAllValid();
		}

		// Overlay (rendered last so it sits on top)
		if (unsavedOverlay != null) {
			unsavedOverlay.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
		}
	}

	// -------------------------------------------------------------------------
	// Tab switching
	// -------------------------------------------------------------------------

	@Override
	public void selectTab(@NonNull Tab tab) {
		super.selectTab(tab);

		ConfigTabContent content = tabContents.get(tab);
		if (content != null && listWidget != null) {
			listWidget.loadContent(content);
		}
	}

	// -------------------------------------------------------------------------
	// Close / save logic
	// -------------------------------------------------------------------------

	@Override
	public void onClose() {
		if (hasUnsavedChanges()) {
			showUnsavedOverlay();
		} else {
			super.onClose();
		}
	}

	/**
	 * ESC key triggers the same guard.
	 */
	@Override
	public boolean keyPressed(@NonNull KeyEvent event) {
		// Let the overlay consume keys first
		if (unsavedOverlay != null) {
			return unsavedOverlay.keyPressed(event);
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		if (unsavedOverlay != null) {
			return unsavedOverlay.mouseClicked(event, doubleClick);
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(@NonNull MouseButtonEvent event) {
		if (unsavedOverlay != null) {
			return unsavedOverlay.mouseReleased(event);
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
		if (unsavedOverlay != null) {
			return unsavedOverlay.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
		}
		return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
	}

	// -------------------------------------------------------------------------
	// Button handlers
	// -------------------------------------------------------------------------

	private void onCancelPressed() {
		if (hasUnsavedChanges()) {
			showUnsavedOverlay();
		} else {
			super.onClose();
		}
	}

	private void onSavePressed() {
		commitAllChanges();
		super.onClose();
	}

	// -------------------------------------------------------------------------
	// Overlay helpers
	// -------------------------------------------------------------------------

	private void showUnsavedOverlay() {
		unsavedOverlay = new UnsavedChangesOverlay(
				width, height,
				/* onCancel   */ this::dismissOverlay,
				/* onDiscard  */ () -> {
			revertAllChanges();
			dismissOverlay();
			super.onClose();
		}
		);
	}

	private void dismissOverlay() {
		unsavedOverlay = null;
	}

	// -------------------------------------------------------------------------
	// Change helpers
	// -------------------------------------------------------------------------

	private boolean hasUnsavedChanges() {
		return tabContents.values().stream()
				.flatMap(c -> c.getAllOptions().stream())
				.anyMatch(ConfigOption::hasChanged);
	}

	private void commitAllChanges() {
		tabContents.values().stream()
				.flatMap(c -> c.getAllOptions().stream())
				.forEach(ConfigOption::commitChanges);
	}

	private void revertAllChanges() {
		tabContents.values().stream()
				.flatMap(c -> c.getAllOptions().stream())
				.forEach(ConfigOption::revertChanges);
	}

	// -------------------------------------------------------------------------
	// Layout helpers
	// -------------------------------------------------------------------------

	/**
	 * Returns the Y coordinate of the bottom edge of the tallest tab button.
	 * Used to position the list widget below all tabs.
	 */
	private int getTabsBottom() {
		// Tabs are at y=10 and their height is font.lineHeight + padding*2
		return 10 + MINECRAFT.font.lineHeight + Tab.PADDING_VERTICAL * 2;
	}

	// -------------------------------------------------------------------------
	// FocusableScreen
	// -------------------------------------------------------------------------

	@Override
	public @NonNull FocusManager getFocusManager() {
		return focusManager;
	}

	// -------------------------------------------------------------------------
	// ConfigTab — Tab subtype carrying content metadata
	// -------------------------------------------------------------------------

	/**
	 * Extension of {@link Tab} that carries a reference to the tab's content,
	 * so {@link TabsScreen} stays generic while {@link SettingsScreen} can
	 * access the content on tab switch.
	 */
	private final class ConfigTab extends Tab {

		private final @NonNull ConfigTabContent content;

		ConfigTab(@NonNull Component label, @NonNull ConfigTabContent content) {
			super(0 /* x set by addTab */, TabsScreen.TABS_Y, label, SettingsScreen.this);
			this.content = content;
		}

		@Override
		public void onPress(@NonNull InputWithModifiers input) {
			// Block tab switching while overlay is active
			if (unsavedOverlay != null) return;
			super.onPress(input);
		}
	}
}
