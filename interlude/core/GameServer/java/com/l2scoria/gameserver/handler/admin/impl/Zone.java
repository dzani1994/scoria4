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
import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.Location;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

/**
 * @author Akumu, luisantonioa
 */

public class Zone extends AdminAbst
{
	public Zone()
	{
		_commands = new String[]{"admin_zone_check", "admin_zone_reload"};
	}


	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		//String val = "";
		//if (st.countTokens() >= 1) {val = st.nextToken();}

		if(actualCommand.equalsIgnoreCase("admin_zone_check"))
		{
			if(activeChar.isInsideZone(L2Character.ZONE_PVP))
			{
				activeChar.sendMessage("This is a PvP zone.");
			}
			else
			{
				activeChar.sendMessage("This is NOT a PvP zone.");
			}

			if(activeChar.isInsideZone(L2Character.ZONE_NOLANDING))
			{
				activeChar.sendMessage("This is a no landing zone.");
			}
			else
			{
				activeChar.sendMessage("This is NOT a no landing zone.");
			}

			activeChar.sendMessage("MapRegion: x:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getX()) + " y:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getY()));

			activeChar.sendMessage("Closest Town: " + MapRegionTable.getInstance().getClosestTownName(activeChar));

			Location loc;

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
			activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
			activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
			activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = null;
		}
		else if(actualCommand.equalsIgnoreCase("admin_zone_reload"))
		{
			//TODO: ZONETODO ZoneManager.getInstance().reload();
			GmListTable.broadcastMessageToGMs("Zones can not be reloaded in this version.");
		}

		actualCommand = null;
		st = null;

		return true;
	}
}
