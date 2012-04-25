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
package com.l2scoria.gameserver.handler.itemhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.GameTimeController;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.handler.IItemHandler;
import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.managers.ClanHallManager;
import com.l2scoria.gameserver.managers.FortManager;
import com.l2scoria.gameserver.managers.GrandBossManager;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.model.entity.event.TvTEvent;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.SetupGauge;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.3 $ $Date: 2009/04/29 14:01:12 $
 */

public class ScrollOfEscape implements IItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] ITEM_IDS =
	{
			736,
			1830,
			1829,
			1538,
			3958,
			5858,
			5859,
			7117,
			7118,
			7119,
			7120,
			7121,
			7122,
			7123,
			7124,
			7125,
			7126,
			7127,
			7128,
			7129,
			7130,
			7131,
			7132,
			7133,
			7134,
			7135,
			7554,
			7555,
			7556,
			7557,
			7558,
			7559,
			7618,
			7619
	};

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IItemHandler#useItem(com.l2scoria.gameserver.model.L2PcInstance, com.l2scoria.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		if(checkConditions(activeChar))
			return;

		//Check to see if player is sitting
		if(activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		if(activeChar.atEvent && !Config.EVENT_ALLOW_SUMMON)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(!TvTEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if ((activeChar.isFightingInEvent()) &&
			((activeChar.getEventName().equals("CTF") && !Config.CTF_ALLOW_SCROLL) ||
			(activeChar.getEventName().equals("BW") && !Config.BW_ALLOW_SCROLL) ||
			(activeChar.getEventName().equals("DM") && !Config.DM_ALLOW_SCROLL)))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
		{
			activeChar.sendMessage("You Can't Use SOE In Grand boss zone!");
			return;
		}

		// Check to see if player is on olympiad
		if(activeChar.isInOlympiadMode() || activeChar.inObserverMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if(!Config.ALLOW_SOE_IN_PVP && activeChar.getPvpFlag() != 0)
		{
			activeChar.sendMessage("You Can't Use SOE In PvP!");
			return;
		}

		// Check to see if the player is in a festival.
		if(activeChar.isFestivalParticipant())
		{
			activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
			return;
		}

		// Check to see if player is in jail
		if(activeChar.isInJail())
		{
			activeChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
			return;
		}

		// Check to see if player is in a duel
		if(activeChar.isInDuel())
		{
			activeChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
			return;
		}

		if(activeChar.isParalyzed())
		{
			activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a paralyzed."));
			return;
		}

		//activeChar.abortCast();
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		//SoE Animation section
		activeChar.setTarget(activeChar);
		// Check if this is a blessed scroll, if it is then shorten the cast time.
		int itemId = item.getItemId();
		int escapeSkill = itemId == 1538 || itemId == 5858 || itemId == 5859 || itemId == 3958 ? 2036 : 2013;

		if(!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return;

		activeChar.disableAllSkills();

		//fix soe
		L2Object oldtarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);

		L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);
		MagicSkillUser msu = new MagicSkillUser(activeChar, escapeSkill, 1, skill.getHitTime(), 0);
		activeChar.broadcastPacket(msu);
		activeChar.setTarget(oldtarget);
		SetupGauge sg = new SetupGauge(0, skill.getHitTime());
		activeChar.sendPacket(sg);
		oldtarget = null;
		sg = null;
		//End SoE Animation section

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(itemId);
		activeChar.sendPacket(sm);
		sm = null;

		EscapeFinalizer ef = new EscapeFinalizer(activeChar, itemId);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(ef, skill.getHitTime()));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + skill.getHitTime() / GameTimeController.MILLIS_IN_TICK);

		ef = null;
		activeChar = null;
	}

	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private int _itemId;

		EscapeFinalizer(L2PcInstance activeChar, int itemId)
		{
			_activeChar = activeChar;
			_itemId = itemId;
		}

		public void run()
		{
			if(_activeChar.isDead())
				return;

			_activeChar.enableAllSkills();

			_activeChar.setIsIn7sDungeon(false);

			try
			{
				// escape to castle if own's one
				if((_itemId == 1830 || _itemId == 5859) && CastleManager.getInstance().getCastleByOwner(_activeChar.getClan()) != null)
				{
					_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Castle);
				}
				// escape to fortress if own's one if own's one
				else if((_itemId == 1830 || _itemId == 5859) && FortManager.getInstance().getFortByOwner(_activeChar.getClan()) != null)
				{
					_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Fortress);
				}
				else if((_itemId == 1829 || _itemId == 5858) && _activeChar.getClan() != null && ClanHallManager.getInstance().getClanHallByOwner(_activeChar.getClan()) != null) // escape to clan hall if own's one
				{
					_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.ClanHall);
				}
				else if(_itemId == 5858) // do nothing
				{
					_activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_HAS_NO_CLAN_HALL));
					return;
				}
				else if(_itemId == 5859) // do nothing
				{
					_activeChar.sendPacket(SystemMessage.sendString("Your clan does not own castle or fortress."));
					return;
				}
				else
				{
					if(_itemId < 7117)
					{
						_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
					else
					{
						switch(_itemId)
						{
							case 7117:
								_activeChar.teleToLocation(-84318, 244579, -3730, true); // Talking Island
								break;
							case 7554:
								_activeChar.teleToLocation(-84318, 244579, -3730, true); // Talking Island quest scroll
								break;
							case 7118:
								_activeChar.teleToLocation(46934, 51467, -2977, true); // Elven Village
								break;
							case 7555:
								_activeChar.teleToLocation(46934, 51467, -2977, true); // Elven Village quest scroll
								break;
							case 7119:
								_activeChar.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village
								break;
							case 7556:
								_activeChar.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village quest scroll
								break;
							case 7120:
								_activeChar.teleToLocation(-44836, -112524, -235, true); // Orc Village
								break;
							case 7557:
								_activeChar.teleToLocation(-44836, -112524, -235, true); // Orc Village quest scroll
								break;
							case 7121:
								_activeChar.teleToLocation(115113, -178212, -901, true); // Dwarven Village
								break;
							case 7558:
								_activeChar.teleToLocation(115113, -178212, -901, true); // Dwarven Village quest scroll
								break;
							case 7122:
								_activeChar.teleToLocation(-80826, 149775, -3043, true); // Gludin Village
								break;
							case 7123:
								_activeChar.teleToLocation(-12678, 122776, -3116, true); // Gludio Castle Town
								break;
							case 7124:
								_activeChar.teleToLocation(15670, 142983, -2705, true); // Dion Castle Town
								break;
							case 7125:
								_activeChar.teleToLocation(17836, 170178, -3507, true); // Floran
								break;
							case 7126:
								_activeChar.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town
								break;
							case 7559:
								_activeChar.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town quest scroll
								break;
							case 7127:
								_activeChar.teleToLocation(105918, 109759, -3207, true); // Hardin's Private Academy
								break;
							case 7128:
								_activeChar.teleToLocation(111409, 219364, -3545, true); // Heine
								break;
							case 7129:
								_activeChar.teleToLocation(82956, 53162, -1495, true); // Oren Castle Town
								break;
							case 7130:
								_activeChar.teleToLocation(85348, 16142, -3699, true); // Ivory Tower
								break;
							case 7131:
								_activeChar.teleToLocation(116819, 76994, -2714, true); // Hunters Village
								break;
							case 7132:
								_activeChar.teleToLocation(146331, 25762, -2018, true); // Aden Castle Town
								break;
							case 7133:
								_activeChar.teleToLocation(147928, -55273, -2734, true); // Goddard Castle Town
								break;
							case 7134:
								_activeChar.teleToLocation(43799, -47727, -798, true); // Rune Castle Town
								break;
							case 7135:
								_activeChar.teleToLocation(87331, -142842, -1317, true); // Schuttgart Castle Town
								break;
							case 7618:
								_activeChar.teleToLocation(149864, -81062, -5618, true); // Ketra Orc Village
								break;
							case 7619:
								_activeChar.teleToLocation(108275, -53785, -2524, true); // Varka Silenos Village
								break;
							default:
								_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
								break;
						}
					}
				}
			}
			catch(Throwable e)
			{
				if(Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private static boolean checkConditions(L2PcInstance actor)
	{
		return actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isFakeDeath() || actor.isTeleporting() || actor.isMuted() || actor.isAlikeDead() || actor.isAllSkillsDisabled();
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
