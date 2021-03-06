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
package com.l2scoria.gameserver.model.actor.stat;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PetInstance;
import com.l2scoria.gameserver.model.base.ClassLevel;
import com.l2scoria.gameserver.model.base.Experience;
import com.l2scoria.gameserver.model.base.PlayerClass;
import com.l2scoria.gameserver.model.quest.QuestState;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.*;
import org.apache.log4j.Logger;

public class PcStat extends PlayableStat
{
	private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());

	// =========================================================
	// Data Field

	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch

	// =========================================================
	// Constructor
	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();

		//Player is Gm and access level is below or equal to canGainExp and is in party, don't give Xp
		if(!getActiveChar().getAccessLevel().canGainExp() && getActiveChar().isInParty())
			return false;

		if(!super.addExp(value))
			return false;

		// Set new karma
		if(!activeChar.isCursedWeaponEquiped() && activeChar.getKarma() > 0 && (activeChar.isGM() || !activeChar.isInsideZone(L2Character.ZONE_PVP)))
		{
			int karmaLost = activeChar.calculateKarmaLost(value);

			if(karmaLost > 0)
			{
				activeChar.setKarma(activeChar.getKarma() - karmaLost);
			}
		}

		/* Micht : Use of UserInfo for C5
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.EXP, getExp());
		activeChar.sendPacket(su);
		*/
		activeChar.sendPacket(new UserInfo(activeChar));

		activeChar = null;

		return true;
	}

	/**
	 * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level
	 * task.<BR>
	 * <BR>
	 * <B><U> Actions </U> :</B><BR>
	 * <BR>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li> <li>Send a Server->Client packet StatusUpdate to
	 * the L2PcInstance</li> <li>Send a Server->Client System Message to the L2PcInstance</li> <li>If the L2PcInstance
	 * increases it's level, send a Server->Client packet SocialAction (broadcast)</li> <li>If the L2PcInstance
	 * increases it's level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner
	 * skills...)</li> <li>If the L2PcInstance increases it's level, send a Server->Client packet UserInfo to the
	 * L2PcInstance</li><BR>
	 * <BR>
	 * 
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		float ratioTakenByPet = 0;

		//Player is Gm and acces level is below or equal to GM_DONT_TAKE_EXPSP and is in party, don't give Xp/Sp
		L2PcInstance activeChar = getActiveChar();
		if(!activeChar.getAccessLevel().canGainExp() && activeChar.isInParty())
			return false;

		// if this player has a pet that takes from the owner's Exp, give the pet Exp now

		if(activeChar.getPet() != null && activeChar.getPet().isPet)
		{
			L2PetInstance pet = (L2PetInstance) activeChar.getPet();
			ratioTakenByPet = pet.getPetData().getOwnerExpTaken();

			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if(ratioTakenByPet > 0 && !pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * ratioTakenByPet), (int) (addToSp * ratioTakenByPet));
			}

			// now adjust the max ratio to avoid the owner earning negative exp/sp
			if(ratioTakenByPet > 1)
			{
				ratioTakenByPet = 1;
			}

			addToExp = (long) (addToExp * (1 - ratioTakenByPet));
			addToSp = (int) (addToSp * (1 - ratioTakenByPet));
		}

		if(!super.addExpAndSp(addToExp, addToSp))
			return false;

		// Send a Server->Client System Message to the L2PcInstance
		SystemMessage sm = new SystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
		sm.addNumber((int) addToExp);
		sm.addNumber(addToSp);
		getActiveChar().sendPacket(sm);

		activeChar = null;

		return true;
	}

	@Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
	{
		if(!super.removeExpAndSp(addToExp, addToSp))
			return false;

		// Send a Server->Client System Message to the L2PcInstance
		SystemMessage sm = new SystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
		sm.addNumber((int) addToExp);
		getActiveChar().sendPacket(sm);
		sm = null;

		sm = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
		sm.addNumber(addToSp);
		getActiveChar().sendPacket(sm);
		sm = null;

		return true;
	}

	@Override
	public final boolean addLevel(byte value)
	{
		if(getLevel() + value > Experience.MAX_LEVEL - 1)
			return false;

		boolean levelIncreased = super.addLevel(value);

		if(Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			ClassLevel lvlnow = PlayerClass.values()[getActiveChar().getClassId().getId()].getLevel();
			if(getLevel() >= 20 && lvlnow == ClassLevel.First)
			{
				L2ClassMasterInstance.ClassMaster.onAction(getActiveChar());
			}
			else if(getLevel() >= 40 && lvlnow == ClassLevel.Second)
			{
				L2ClassMasterInstance.ClassMaster.onAction(getActiveChar());
			}
			else if(getLevel() >= 76 && lvlnow == ClassLevel.Third)
			{
				L2ClassMasterInstance.ClassMaster.onAction(getActiveChar());
			}

			lvlnow = null;
		}

		if(levelIncreased)
		{
			if(getActiveChar().getLevel() >= 25 && getActiveChar().isNewbie())
			{
				getActiveChar().setNewbie(false);

				if(Config.DEBUG)
				{
					_log.info("Newbie character ended: " + getActiveChar().getCharId());
				}
			}

			QuestState qs = getActiveChar().getQuestState("255_Tutorial");

			if(qs != null && qs.getQuest() != null)
			{
				qs.getQuest().notifyEvent("CE40", null, getActiveChar());
			}

			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
			getActiveChar().sendPacket(new SystemMessage(SystemMessageId.YOU_INCREASED_YOUR_LEVEL));

			qs = null;
		}

		getActiveChar().rewardSkills(); // Give Expertise skill of this level

		if(getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}

		if(getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		}

		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);
		su = null;

		// Update the overloaded status of the L2PcInstance
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the L2PcInstance
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));

		return levelIncreased;
	}

	@Override
	public boolean addSp(int value)
	{
		if(!super.addSp(value))
			return false;

		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
		su = null;

		return true;
	}

	@Override
	public final long getExpForLevel(int level)
	{
		return Experience.getExp(level);
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}

	@Override
	public final long getExp()
	{
		if(getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();

		return super.getExp();
	}

	@Override
	public final void setExp(long value)
	{
		if(getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}

	@Override
	public final int getLevel()
	{
		if(getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();

		return super.getLevel();
	}

	@Override
	public final void setLevel(int value)
	{
		if(value > Experience.MAX_LEVEL - 1)
		{
			value = Experience.MAX_LEVEL - 1;
		}

		if(getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(value);
		}
	}

	@Override
	public final int getMaxCp()
	{
		int val = super.getMaxCp();

		if(val != _oldMaxCp)
		{
			_oldMaxCp = val;

			if(getActiveChar().getStatus().getCurrentCp() != val)
			{
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp());
			}
		}
		return val;
	}

	@Override
	public final int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the L2PcInstance
		int val = super.getMaxHp();

		if(val != _oldMaxHp)
		{
			_oldMaxHp = val;

			// Launch a regen task if the new Max HP is higher than the old one
			if(getActiveChar().getStatus().getCurrentHp() != val)
			{
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public final int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = super.getMaxMp();

		if(val != _oldMaxMp)
		{
			_oldMaxMp = val;

			// Launch a regen task if the new Max MP is higher than the old one
			if(getActiveChar().getStatus().getCurrentMp() != val)
			{
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public final int getSp()
	{
		if(getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();

		return super.getSp();
	}

	@Override
	public final void setSp(int value)
	{
		if(getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		}
		else
		{
			super.setSp(value);
		}
	}
}
