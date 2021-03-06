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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.datatables.sql.SpawnTable;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2scoria.gameserver.model.spawn.AutoSpawn;
import com.l2scoria.gameserver.model.spawn.AutoSpawn.AutoSpawnInstance;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Admin Command Handler for Mammon NPCs
 * 
 * @author Akumu, Tempy
 */
public class Mammon extends AdminAbst
{
	public Mammon()
	{
		_commands = new String[]{"admin_mammon_find", "admin_mammon_respawn", "admin_list_spawns", "admin_msg"};
	}

	private boolean _isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();


	@SuppressWarnings("deprecation")
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}
		int npcId = 0;
		int teleportIndex = -1;

		AutoSpawnInstance blackSpawnInst = AutoSpawn.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
		AutoSpawnInstance merchSpawnInst = AutoSpawn.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);

		if(command.startsWith("admin_mammon_find"))
		{
			try
			{
				if(command.length() > 17)
				{
					teleportIndex = Integer.parseInt(command.substring(18));
				}
			}
			catch(Exception NumberFormatException)
			{
				activeChar.sendMessage("Usage: //mammon_find [teleportIndex] (where 1 = Blacksmith, 2 = Merchant)");
			}

			if(!_isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}

			if(blackSpawnInst != null)
			{
				L2NpcInstance[] blackInst = blackSpawnInst.getNPCInstanceList();
				if(blackInst.length > 0)
				{
					int x1 = blackInst[0].getX(), y1 = blackInst[0].getY(), z1 = blackInst[0].getZ();
					activeChar.sendMessage("Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);

					if(teleportIndex == 1)
					{
						activeChar.teleToLocation(x1, y1, z1, true);
					}
				}

				blackInst = null;
			}
			else
			{
				activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
			}

			if(merchSpawnInst != null)
			{
				L2NpcInstance[] merchInst = merchSpawnInst.getNPCInstanceList();

				if(merchInst.length > 0)
				{
					int x2 = merchInst[0].getX(), y2 = merchInst[0].getY(), z2 = merchInst[0].getZ();

					activeChar.sendMessage("Merchant of Mammon: " + x2 + " " + y2 + " " + z2);

					if(teleportIndex == 2)
					{
						activeChar.teleToLocation(x2, y2, z2, true);
					}
				}

				merchInst = null;
			}
			else
			{
				activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
			}
		}

		else if(command.startsWith("admin_mammon_respawn"))
		{
			if(!_isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}

			if(merchSpawnInst != null)
			{
				long merchRespawn = AutoSpawn.getInstance().getTimeToNextSpawn(merchSpawnInst);
				activeChar.sendMessage("The Merchant of Mammon will respawn in " + merchRespawn / 60000 + " minute(s).");
			}
			else
			{
				activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
			}

			if(blackSpawnInst != null)
			{
				long blackRespawn = AutoSpawn.getInstance().getTimeToNextSpawn(blackSpawnInst);
				activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + blackRespawn / 60000 + " minute(s).");
			}
			else
			{
				activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
			}
		}

		else if(command.startsWith("admin_list_spawns"))
		{
			try
			{
				// admin_list_spawns x[xxxx] x[xx]
				String[] params = command.split(" ");
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher regexp = pattern.matcher(params[1]);

				if(regexp.matches())
				{
					npcId = Integer.parseInt(params[1]);
				}
				else
				{
					params[1] = params[1].replace('_', ' ');
					npcId = NpcTable.getInstance().getTemplateByName(params[1]).npcId;
				}

				if(params.length > 2)
				{
					teleportIndex = Integer.parseInt(params[2]);
				}

				regexp = null;
				pattern = null;
				params = null;
			}
			catch(Exception e)
			{
				activeChar.sendPacket(SystemMessage.sendString("Command format is //list_spawns <npcId|npc_name> [tele_index]"));
			}

			SpawnTable.getInstance().findNPCInstances(activeChar, npcId, teleportIndex);
		}

		// Used for testing SystemMessage IDs	- Use //msg <ID>
		else if(command.startsWith("admin_msg"))
		{
			int msgId = -1;

			try
			{
				msgId = Integer.parseInt(command.substring(10).trim());
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>");
				return true;
			}
			activeChar.sendPacket(new SystemMessage(msgId));
		}

		merchSpawnInst = null;
		blackSpawnInst = null;

		return true;
	}
}
