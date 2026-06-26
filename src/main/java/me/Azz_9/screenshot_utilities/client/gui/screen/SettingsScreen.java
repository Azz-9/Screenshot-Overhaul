package me.Azz_9.screenshot_utilities.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.Azz_9.screenshot_utilities.client.config.ConfigLoader;
import me.Azz_9.screenshot_utilities.client.config.option.ConfigOption;
import me.Azz_9.screenshot_utilities.client.gui.components.config.ConfigOptionListWidget;
import me.Azz_9.screenshot_utilities.client.gui.components.config.ConfigTabContent;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusManager;
import me.Azz_9.screenshot_utilities.client.gui.focusSystem.FocusableScreen;

/**
 * Settings screen with tabs, scrollable option lists, validation, and an
 * "unsaved changes" overlay.
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends TabsScreen implements FocusableScreen {

	// -------------------------------------------------------------------------
	// Layout
	// -------------------------------------------------------------------------

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
		Tab tab = new ConfigTab(tabLabel);
		tabContents.put(tab, content);
	}

	// -------------------------------------------------------------------------
	// Screen lifecycle
	// -------------------------------------------------------------------------

	@Override
	protected void initContent() {
		super.initContent(); // clears tab list

		// Register tabs
		tabContents.keySet().forEach(this::addTab);

		// List widget — fills space between tabs and bottom bar
		int listTop = getTabsBottom() + LIST_TOP_PADDING;
		int listHeight = getBottomBarTop() - listTop;

		listWidget = new ConfigOptionListWidget(0, listTop, width, listHeight);
		addRenderableWidget(listWidget);

		// Register all options across all tabs for dirty / valid tracking.
		// setTrackedItems replaces any previous registration so resize is safe.
		List<ConfigOption<?>> allOptions = tabContents.values().stream()
				.flatMap(c -> c.getAllOptions().stream())
				.collect(java.util.stream.Collectors.toList());
		setTrackedItems(allOptions);

		// Select the first tab
		if (!tabContents.isEmpty()) {
			selectTab(tabContents.keySet().iterator().next());
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

		ConfigTab(@NonNull Component label) {
			// x=0 — repositioned by addTab() via setX()
			super(0, TabsScreen.TABS_Y, label, SettingsScreen.this);
		}
	}

	@Override
	protected void onSave() {
		super.onSave();
		ConfigLoader.trySave();
	}
}
