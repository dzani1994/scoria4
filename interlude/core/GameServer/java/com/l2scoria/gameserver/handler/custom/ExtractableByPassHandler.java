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
package com.l2scoria.gameserver.handler.custom;

import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.items.IItemHandler;
import com.l2scoria.gameserver.handler.ItemHandler;
import com.l2scoria.gameserver.handler.items.impl.ExtractableItems;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.util.FloodProtector;
import org.apache.log4j.Logger;

/**
 * @author Nick
 */
public class ExtractableByPassHandler implements ICustomByPassHandler
{
	private static Logger _log = Logger.getLogger(ExtractableByPassHandler.class.getName());
	private static String[] _IDS =
	{
			"extractOne", "extractAll"
	};

	@Override
	public String[] getByPassCommands()
	{
		return _IDS;
	}

	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		try
		{
			int objId = Integer.parseInt(parameters);
			L2ItemInstance item = player.getInventory().getItemByObjectId(objId);
			if(item == null)
				return;
			IItemHandler ih = ItemHandler.getInstance().getItemHandler(item.getItemId());
			if(ih == null || !(ih instanceof ExtractableItems))
				return;
			if(command.compareTo(_IDS[0]) == 0)
			{
				((ExtractableItems) ih).doExtract(player, item, 1);
			}
			else if(command.compareTo(_IDS[1]) == 0)
			{
				if(!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_UNPACK_ITEM))
				{
					player.sendMessage("You cannot unpack items so fast!");
					return;
				}

				if(item.getCount() <= 100)
				{
					((ExtractableItems) ih).doExtract(player, item, item.getCount());
				}
				else
				{
					player.sendMessage("Items count is to big! Extract first 100.");
					((ExtractableItems) ih).doExtract(player, item, 100);
				}
			}
		}
		catch(Exception e)
		{
			_log.warn("ExtractableByPassHandler: Error while running " + e);
		}

	}

}
