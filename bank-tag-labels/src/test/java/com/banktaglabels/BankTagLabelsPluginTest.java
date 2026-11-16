package com.banktaglabels;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankTagLabelsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankTagLabelsPlugin.class);
		RuneLite.main(args);
	}
}
