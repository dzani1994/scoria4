/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.ItemContainer;
import com.l2scoria.gameserver.model.PcFreight;
import com.l2scoria.gameserver.model.actor.instance.L2FolkInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.templates.L2EtcItemType;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author -Wooden-
 */
public final class RequestPackageSend extends L2GameClientPacket
{
	private static final String _C_9F_REQUESTPACKAGESEND = "[C] 9F RequestPackageSend";
	private static Logger _log = Logger.getLogger(RequestPackageSend.class.getName());
	private List<Item> _items = new FastList<Item>();
	private int _objectID;
	private int _count;

	@Override
	protected void readImpl()
	{
		_objectID = readD();
		_count = readD();

		if(_count < 0 || _count > 500)
		{
			_count = -1;
			return;
		}

		for(int i = 0; i < _count; i++)
		{
			int id = readD(); //this is some id sent in PackageSendableList
			int count = readD();
			_items.add(new Item(id, count));
		}
	}

	/**
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if(_count == -1 || _items == null)
			return;

		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
			return;

		if(player.getObjectId() == _objectID)
			return;
		if(player.getAccountChars().size() < 1)
			return;
		if(!player.getAccountChars().containsKey(_objectID))
			return;

		if(player.getActiveEnchantItem() != null)
		{
			_log.info("Player " + player.getName() + " trying to use enchant exploit, ban this player!");
			player.closeNetConnection();
			return;
		}

		if(player.isInStoreMode() || player.isInCraftMode())
		{
			return;
		}

		L2PcInstance target = L2PcInstance.load(_objectID);
		PcFreight freight = target.getFreight();
		player.setActiveWarehouse(freight);
		target.deleteMe();
		target = null;
		ItemContainer warehouse = player.getActiveWarehouse();

		if(warehouse == null)
			return;

		L2FolkInstance manager = player.getLastFolkNPC();

		if((manager == null || !player.isInsideRadius(manager, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !player.isGM())
			return;

		if(warehouse instanceof PcFreight && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Alt game - Karma punishment
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
			return;

		// Freight price from config or normal price per item slot (30)
		int fee = _count * Config.ALT_GAME_FREIGHT_PRICE;
		int currentAdena = player.getAdena();
		int slots = 0;

		for(Item i : _items)
		{
			int objectId = i.id;
			int count = i.count;

			// Check validity of requested item
			L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");

			if(item == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				i.id = 0;
				i.count = 0;
				continue;
			}

			if(item.isTimeLimitedItem())
			{
				player.sendMessage("Time-limited items can`t be send.");
				i.id = 0;
				i.count = 0;
				continue;
			}

			if(!item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
				return;

			// Calculate needed adena and slots
			if(item.getItemId() == 57)
			{
				currentAdena -= count;
			}

			if(!item.isStackable())
			{
				slots += count;
			}
			else if(warehouse.getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		// Item Max Limit Check
		if(!warehouse.validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		// Check if enough adena and charge the fee
		if(currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for(Item i : _items)
		{
			int objectId = i.id;
			int count = i.count;

			// check for an invalid item
			if(objectId == 0 && count == 0)
			{
				continue;
			}

			L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);

			if(oldItem == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}

			int itemId = oldItem.getItemId();

			if(itemId >= 6611 && itemId <= 6621 || itemId == 6842)
			{
				continue;
			}

			L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());

			if(newItem == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}

			if(playerIU != null)
			{
				if(oldItem.getCount() > 0 && oldItem != newItem)
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
		}

		// Send updated item list to the player
		if(playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		player.setActiveWarehouse(null);
	}

	/**
	 * @see com.l2scoria.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C_9F_REQUESTPACKAGESEND;
	}

	private class Item
	{
		public int id;
		public int count;

		public Item(int i, int c)
		{
			id = i;
			count = c;
		}
	}
}
