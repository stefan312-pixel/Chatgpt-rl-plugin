package com.banktaglabels;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(BankTagLabelsPlugin.CONFIG_GROUP)
public interface BankTagLabelsConfig extends Config
{
	@ConfigItem(
		keyName = "showTagName",
		name = "Show tag name if no label",
		description = "Tabs without a custom label show the name of their tag",
		position = 1
	)
	default boolean showTagName()
	{
		return true;
	}

	@Range(min = 2, max = 12)
	@ConfigItem(
		keyName = "maxChars",
		name = "Max characters on tab",
		description = "Longer text is cut off with '..' (full text shows on hover)",
		position = 2
	)
	default int maxChars()
	{
		return 7;
	}

	@ConfigItem(
		keyName = "showFullOnHover",
		name = "Show full text on hover",
		description = "Shows the complete label in a box next to the tab while hovering it",
		position = 3
	)
	default boolean showFullOnHover()
	{
		return true;
	}

	@ConfigItem(
		keyName = "textColor",
		name = "Text color",
		description = "Color of the label text",
		position = 4
	)
	default Color textColor()
	{
		return Color.YELLOW;
	}
}
