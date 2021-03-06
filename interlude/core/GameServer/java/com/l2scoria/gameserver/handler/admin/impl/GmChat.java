/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.gameserver.datatables.GmListTable;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.CreatureSay;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands: - gmchat text = sends text to all online GM's - gmchat_menu text = same
 * as gmchat, displays the admin panel after chat
 * 
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class GmChat extends AdminAbst
{
	public GmChat()
	{
		_commands = new String[]{"admin_gmchat", "admin_snoop", "admin_gmchat_menu"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_gmchat"))
		{
			handleGmChat(command, activeChar);
		}
		else if(command.startsWith("admin_snoop"))
		{
			snoop(command, activeChar);
		}

		if(command.startsWith("admin_gmchat_menu"))
		{
			HelpPage.showHelpPage(activeChar, "main_menu.htm");
		}

		return true;
	}

	/**
	 * @param command
	 * @param activeChar
	 */
	private void snoop(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (!st.hasMoreTokens())
		{
			activeChar.sendMessage("Usage: //snoop <player_name>");
			return;
		}

		L2PcInstance target = L2World.getInstance().getPlayer(st.nextToken());
		if(target == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
			return;
		}
		if (target.getAccessLevel().getLevel() < activeChar.getAccessLevel().getLevel() && target.getAccessLevel().getLevel() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			target.sendMessage(activeChar.getName() + " tried to snoop your conversations. Blocked.");
			return;
		}

		target.addSnooper(activeChar);
		activeChar.addSnooped(target);
	}

	/**
	 * @param command
	 * @param activeChar
	 */
	private void handleGmChat(String command, L2PcInstance activeChar)
	{
		try
		{
			int offset = 0;

			String text;

			if(command.contains("menu"))
			{
				offset = 17;
			}
			else
			{
				offset = 13;
			}

			text = command.substring(offset);
			CreatureSay cs = new CreatureSay(0, 9, activeChar.getName(), text);
			GmListTable.broadcastToGMs(cs);

			text = null;
			cs = null;
		}
		catch(StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}
}
