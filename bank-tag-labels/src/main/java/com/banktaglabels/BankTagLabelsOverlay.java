package com.banktaglabels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.Text;

public class BankTagLabelsOverlay extends Overlay
{
	private final Client client;
	private final BankTagLabelsPlugin plugin;
	private final BankTagLabelsConfig config;

	@Inject
	private BankTagLabelsOverlay(Client client, BankTagLabelsPlugin plugin, BankTagLabelsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		Widget container = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
		if (container == null || container.isHidden())
		{
			return null;
		}
		Widget[] children = container.getChildren();
		if (children == null)
		{
			return null;
		}

		g.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics fm = g.getFontMetrics();
		Point mouse = new Point(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY());
		Rectangle hoverBox = null;
		String hoverText = null;

		for (Widget w : children)
		{
			// the tab background is the only child with the "View tag tab" action;
			// tabs scrolled out of view are hidden by the core plugin
			if (w == null || w.isHidden() || !isTagTab(w))
			{
				continue;
			}

			String tag = Text.removeTags(w.getName());
			String label = plugin.getLabel(tag);
			if (label == null)
			{
				if (!config.showTagName())
				{
					continue;
				}
				label = tag;
			}

			Rectangle b = w.getBounds();
			String shown = label;
			int max = config.maxChars();
			if (shown.length() > max)
			{
				shown = shown.substring(0, Math.max(1, max - 2)) + "..";
			}

			int x = b.x + (b.width - fm.stringWidth(shown)) / 2;
			int y = b.y + b.height - 2;
			g.setColor(Color.BLACK);
			g.drawString(shown, x + 1, y + 1);
			g.setColor(config.textColor());
			g.drawString(shown, x, y);

			if (config.showFullOnHover() && b.contains(mouse) && !shown.equals(label))
			{
				hoverBox = b;
				hoverText = label;
			}
		}

		if (hoverBox != null)
		{
			int w = fm.stringWidth(hoverText) + 8;
			int h = fm.getHeight() + 2;
			int x = hoverBox.x + hoverBox.width + 4;
			int y = hoverBox.y + (hoverBox.height - h) / 2;
			g.setColor(new Color(0, 0, 0, 200));
			g.fillRect(x, y, w, h);
			g.setColor(config.textColor());
			g.drawRect(x, y, w, h);
			g.drawString(hoverText, x + 4, y + fm.getAscent() + 1);
		}

		return null;
	}

	private static boolean isTagTab(Widget w)
	{
		String[] actions = w.getActions();
		if (actions == null)
		{
			return false;
		}
		for (String a : actions)
		{
			if (BankTagLabelsPlugin.VIEW_TAB.equals(a))
			{
				return true;
			}
		}
		return false;
	}
}
