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
import com.l2scoria.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Party;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.network.L2GameClient.GameClientState;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.CharSelectInfo;
import com.l2scoria.gameserver.network.serverpackets.RestartResponse;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.taskmanager.AttackStanceTaskManager;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestart extends L2GameClientPacket
{
	private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
	private static Logger _log = Logger.getLogger(RequestRestart.class.getName());

	@Override
	protected void readImpl()
	{
	// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			_log.warn("[RequestRestart] activeChar null!?");
			return;
		}

		if(player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player))
		{
			player.sendMessage("You cant logout in olympiad mode");
			return;
		}

		if (player.isLocked())
		{
			_log.warn("Player " + player.getName() + " tried to restart during class change.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(player.isTeleporting())
		{
			player.abortCast();
			player.setIsTeleporting(false);
		}

		player.getInventory().updateDatabase();

		if(player.getPrivateStoreType() != 0)
		{
			player.sendMessage("Cannot restart while trading");
			return;
		}

		if(player.getActiveRequester() != null)
		{
			player.getActiveRequester().onTradeCancel(player);
			player.onTradeCancel(player.getActiveRequester());
		}

		if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
		{
			if(Config.DEBUG)
			{
				_log.info("Player " + player.getName() + " tried to logout while fighting.");
			}

			player.sendPacket(new SystemMessage(SystemMessageId.CANT_RESTART_WHILE_FIGHTING));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(player.getOlympiadGameId() > 0 || player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player))
		{
			player.sendMessage("Cannot restart while in Olympiad.");
			return;
		}

		if(player.isAway())
		{
			player.sendMessage("You can't restart in Away mode.");
			return;
		}

		if (player.isInFunEvent())
		{
			if(!player._event.canLogout(player)) {
				player.sendMessage("Нельзя покинуть игру.");
				return;
			}
		}

		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(player.isFestivalParticipant())
		{
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			L2Party playerParty = player.getParty();
			if(playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
			}
		}

		if(player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}

		L2GameClient client = getClient();

		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);
		player._inWorld=false;

		RegionBBSManager.getInstance().changeCommunityBoard();

		// removing player from the world
		player.deleteMe();
		try
		{
			player.store();
		}
		catch(Exception e){}

		getClient().setActiveChar(null);

		// return the client to the authed status
		client.setState(GameClientState.AUTHED);

		RestartResponse response = new RestartResponse();
		sendPacket(response);

		// send char list
		CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__46_REQUESTRESTART;
	}
}
