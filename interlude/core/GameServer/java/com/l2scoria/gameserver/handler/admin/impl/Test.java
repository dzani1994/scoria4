/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
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

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class Test extends AdminAbst
{
	public Test()
	{
		_commands = new String[]{"admin_test", "admin_stats", "admin_skill_test", "admin_st", "admin_mp", "admin_known"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.equals("admin_stats"))
		{
			for(String line : ThreadPoolManager.getInstance().getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		else if(command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();

				int id = Integer.parseInt(st.nextToken());

				adminTestSkill(activeChar, id);

				st = null;
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
			catch(NoSuchElementException nsee)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		else if(command.equals("admin_mp on"))
		{
			//.startPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if(command.equals("admin_mp off"))
		{
			//.stopPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if(command.equals("admin_mp dump"))
		{
			//.dumpPacketHistory();
			activeChar.sendMessage("command not working");
		}
		else if(command.equals("admin_known on"))
		{
			Config.CHECK_KNOWN = true;
		}
		else if(command.equals("admin_known off"))
		{
			Config.CHECK_KNOWN = false;
		}
		return true;
	}

	/**
	 * @param activeChar
	 * @param id
	 */
	private void adminTestSkill(L2PcInstance activeChar, int id)
	{
		L2Character player;
		L2Object target = activeChar.getTarget();

		if(target == null || !(target.isCharacter))
		{
			player = activeChar;
		}
		else
		{
			player = (L2Character) target;
		}

		player.broadcastPacket(new MagicSkillUser(activeChar, player, id, 1, 1, 1));

		target = null;
		player = null;
	}
}
