package com.banktaglabels;

import com.google.inject.Provides;
import java.util.function.Consumer;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Bank Tag Labels",
	description = "Shows custom labels on bank tag tabs",
	tags = {"bank", "tag", "label", "name"}
)
@PluginDependency(BankTagsPlugin.class)
public class BankTagLabelsPlugin extends Plugin
{
	static final String CONFIG_GROUP = "banktaglabels";
	private static final String LABEL_PREFIX = "label_";

	// these are the exact menu texts the core Bank Tags plugin uses for its tag tabs
	static final String VIEW_TAB = "View tag tab";
	private static final String DELETE_TAB = "Delete tag tab";

	private static final String SET_LABEL = "Set label";
	private static final String CLEAR_LABEL = "Clear label";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private BankTagLabelsOverlay overlay;

	@Provides
	BankTagLabelsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankTagLabelsConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	/** Returns the saved label for a tag, or null if there is none. */
	String getLabel(String tag)
	{
		String label = configManager.getConfiguration(CONFIG_GROUP, key(tag));
		return label == null || label.isEmpty() ? null : label;
	}

	private void setLabel(String tag, String label)
	{
		if (label == null || label.trim().isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, key(tag));
		}
		else
		{
			configManager.setConfiguration(CONFIG_GROUP, key(tag), label.trim());
		}
	}

	private static String key(String tag)
	{
		return LABEL_PREFIX + Text.standardize(tag).replaceAll("[^a-z0-9_-]", "_");
	}

	/** Finds the tab widget a menu entry belongs to and returns its tag name, or null. */
	private String tagOf(int param0, int param1)
	{
		if (param1 != InterfaceID.Bankmain.ITEMS_CONTAINER)
		{
			return null;
		}
		Widget container = client.getWidget(param1);
		Widget tab = container == null ? null : container.getChild(param0);
		return tab == null || tab.getName() == null ? null : Text.removeTags(tab.getName());
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		// piggyback on the last tab option so we add our entries once per tab
		if (!DELETE_TAB.equals(event.getOption()))
		{
			return;
		}
		String tag = tagOf(event.getActionParam0(), event.getActionParam1());
		if (tag == null)
		{
			return;
		}

		addEntry(event, SET_LABEL);
		if (getLabel(tag) != null)
		{
			addEntry(event, CLEAR_LABEL);
		}
	}

	private void addEntry(MenuEntryAdded event, String option)
	{
		client.createMenuEntry(-1)
			.setOption(option)
			.setTarget(event.getTarget())
			.setParam0(event.getActionParam0())
			.setParam1(event.getActionParam1())
			.setType(MenuAction.RUNELITE)
			.setIdentifier(event.getIdentifier());
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() != MenuAction.RUNELITE)
		{
			return;
		}
		String option = event.getMenuOption();
		if (!SET_LABEL.equals(option) && !CLEAR_LABEL.equals(option))
		{
			return;
		}
		String tag = tagOf(event.getParam0(), event.getParam1());
		if (tag == null)
		{
			return;
		}

		if (CLEAR_LABEL.equals(option))
		{
			setLabel(tag, null);
			return;
		}

		String current = getLabel(tag);
		chatboxPanelManager.openTextInput("Label for tag \"" + tag + "\":")
			.value(current == null ? "" : current)
			.onDone((Consumer<String>) text -> clientThread.invoke(() -> setLabel(tag, text)))
			.build();
	}
}
