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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.PrivateStoreManageListBuy;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreManageBuy extends L2GameClientPacket
{
	private static final String _C__90_REQUESTPRIVATESTOREMANAGEBUY = "[C] 90 RequestPrivateStoreManageBuy";

	//private static Logger _log = Logger.getLogger(RequestPrivateStoreBuyManage.class.getName());

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

        if(player._noAction)
            return;

        if(player.getPet() != null)
        {
            player.sendMessage("Remove pet");
            return;
        }

		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if(player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(player.isInOlympiadMode())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(player.getMountType() != 0)
			return;

		if(player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY || player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY + 1)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
		}

		if(player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
		{
			if(player.isSitting())
			{
				player.standUp();
			}

            if(player.isMoving())
            {
                player.stopMove(null);
            }

			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY + 1);
			player.sendPacket(new PrivateStoreManageListBuy(player));
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__90_REQUESTPRIVATESTOREMANAGEBUY;
	}
}
