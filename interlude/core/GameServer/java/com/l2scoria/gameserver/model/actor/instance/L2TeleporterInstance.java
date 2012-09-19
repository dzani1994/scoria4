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
package com.l2scoria.gameserver.model.actor.instance;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.sql.TeleportLocationTable;
import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.managers.SiegeManager;
import com.l2scoria.gameserver.managers.TownManager;
import com.l2scoria.gameserver.model.L2TeleportLocation;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2TeleporterInstance extends L2FolkInstance
{
	//private static Logger _log = Logger.getLogger(L2TeleporterInstance.class.getName());

	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;

	/**
	 * @param template
	 */
	public L2TeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		if (Olympiad.getInstance().isRegisteredInComp(player))
		{
			player.sendMessage("You are not allowed to use a teleport while registered in olympiad game.");
			return;
		}

		int condition = validateCondition(player);

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if(actualCommand.equalsIgnoreCase("goto"))
		{
                        player.setCurrentTpTimer();
			int npcId = getTemplate().npcId;

			switch(npcId)
			{
				case 31095: //
				case 31096: //
				case 31097: //
				case 31098: // Enter Necropolises
				case 31099: //
				case 31100: //
				case 31101: //
				case 31102: //

				case 31114: //
				case 31115: //
				case 31116: // Enter Catacombs
				case 31117: //
				case 31118: //
				case 31119: //
					player.setIsIn7sDungeon(true);
					break;
				case 31103: //
				case 31104: //
				case 31105: //
				case 31106: // Exit Necropolises
				case 31107: //
				case 31108: //
				case 31109: //
				case 31110: //

				case 31120: //
				case 31121: //
				case 31122: // Exit Catacombs
				case 31123: //
				case 31124: //
				case 31125: //
					player.setIsIn7sDungeon(false);
					break;
			}

			if(st.countTokens() <= 0)
				return;

			int whereTo = Integer.parseInt(st.nextToken());
			if(condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if(condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0; // NOTE: Replace 0 with highest level when privilege level is implemented
				if(st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}

				if(10 >= minPrivilegeLevel)
				{
					doTeleport(player, whereTo);
				}
				else
				{
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				}

				return;
			}
		}

		st = null;
		actualCommand = null;
		super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if(val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "data/html/teleporter/" + pom + ".htm";
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";

		int condition = validateCondition(player);
		if(condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if(condition > COND_ALL_FALSE)
		{
			if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/teleporter/castleteleporter-busy.htm"; // Busy because of siege
			}
			else if(condition == COND_OWNER)
			{
				filename = getHtmlPath(getNpcId(), 0); // Owner message window
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);

		filename = null;
		html = null;
	}

	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if(list != null)
		{
			//you cannot teleport to village that is in siege
			if(SiegeManager.getInstance().getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null && !player.isNoble())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE));
				return;
			}
			else if(TownManager.getInstance().townHasCastleInSiege(list.getLocX(), list.getLocY()) && !player.isNoble())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE));
				return;
			}
			else if(!player.isGM() && !Config.FLAGED_PLAYER_CAN_USE_GK && player.getPvpFlag() > 0)
			{
				player.sendMessage("Don't run from PvP! You will be able to use the teleporter only after your flag is gone.");
				return;
			}
			else if(!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0) //karma
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Go away, you're not welcome here.");
				player.sendPacket(sm);
				sm = null;
				return;
			}
			else if(list.getIsForNoble() && !player.isNoble())
			{
				String filename = "data/html/teleporter/nobleteleporter-no.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				html = null;
				filename = null;
				return;
			}
			else if(player.isAlikeDead())
			{
				return;
			}
			else if(!list.getIsForNoble() && (Config.ALT_GAME_FREE_TELEPORT || player.reduceAdena("Teleport", list.getPrice(), this, true)))
			{
				if(Config.DEBUG)
				{
					_log.info("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				}
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
			}
			else if((getNpcId() == 30483 || val == 9982 || val == 9983 || val == 9984) && player.getLevel() >= Config.CRUMA_TOWER_LEVEL_RESTRICT && !player.isGM())
			{
				// Chars level XX can't enter in Cruma Tower. Retail: level 56 and above
				int maxlvl = Config.CRUMA_TOWER_LEVEL_RESTRICT;

				String filename = "data/html/teleporter/30483-biglvl.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%allowedmaxlvl%", "" + maxlvl + "");
				player.sendPacket(html);
				filename = null;
				html = null;
				return;
			}
			else if(list.getIsForNoble() && (Config.ALT_GAME_FREE_TELEPORT || player.destroyItemByItemId("Noble Teleport", 6651, list.getPrice(), this, true)))
			{
				if(Config.DEBUG)
				{
					_log.info("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				}
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
			}
		}
		else
		{
			_log.warn("No teleport destination with id:" + val);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
		list = null;
	}

	private int validateCondition(L2PcInstance player)
	{
		if(CastleManager.getInstance().getCastleIndex(this) < 0) // Teleporter isn't on castle ground
			return COND_REGULAR; // Regular access
		else if(getCastle().getSiege().getIsInProgress()) // Teleporter is on castle ground and siege is in progress
			return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
		else if(player.getClan() != null) // Teleporter is on castle ground and player is in a clan
		{
			if(getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				return COND_OWNER; // Owner
		}

		return COND_ALL_FALSE;
	}
}
