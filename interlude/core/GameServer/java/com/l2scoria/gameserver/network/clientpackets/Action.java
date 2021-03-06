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

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 18:46:19 $
 */
public final class Action extends L2GameClientPacket
{
	private static final String ACTION__C__04 = "[C] 04 Action";
	private static Logger _log = Logger.getLogger(Action.class.getName());

	// cddddc
	private int _objectId;
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		/*_originX = */readD();
		/*_originY = */readD();
		/*_originZ = */readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}

	@Override
	protected void runImpl()
	{
		if(Config.DEBUG)
		{
			_log.info("Action:" + _actionId);
			_log.info("oid:" + _objectId);
		}

		// Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();


		if(activeChar == null)
			return;

        if(activeChar._noAction)
            return;

        L2Object obj;

		if(activeChar.getTargetId() == _objectId)
		{
			obj = activeChar.getTarget();
		}
		else
		{
            obj = L2World.getInstance().findObject(_objectId);
		}

		// If object requested does not exist, add warn msg into logs
		if(obj == null)
		{
			// pressing e.g. pickup many times quickly would get you here
			// _log.warn("Character: " + activeChar.getName() + " request action with non existent ObjectID:" + _objectId);
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if(activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null)
		{
			switch(_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					if(obj.isCharacter && ((L2Character) obj).isAlikeDead())
					{
						obj.onAction(activeChar);
					}
					else
					{
						obj.onActionShift(getClient());
					}
					break;
				default:
					// Ivalid action detected (probably client cheating), log this
					_log.warn("Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
					getClient().sendPacket(ActionFailed.STATIC_PACKET);
					break;
			}
		}
		else
		{
			// Actions prohibited when in trade
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return ACTION__C__04;
	}
}
