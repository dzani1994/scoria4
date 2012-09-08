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
package com.l2scoria.gameserver.handler.skills.impl;

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Fishing;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2Weapon;

public class FishingSkill extends SkillAbst
{
	public FishingSkill()
	{
		_types = new SkillType[]{SkillType.PUMPING, SkillType.REELING};

		_playerUseOnly = true;
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) activeChar;

		L2Fishing fish = player.GetFishCombat();
		if (fish == null)
		{
			if (skill.getSkillType() == SkillType.PUMPING)
			{
				//Pumping skill is available only while fishing
				player.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING));
			}
			else if (skill.getSkillType() == SkillType.REELING)
			{
				//Reeling skill is available only while fishing
				player.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING));
			}

			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		L2Weapon weaponItem = player.getActiveWeaponItem();
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst == null || weaponItem == null)
		{
			return false;
		}

		int SS = 1;
		int pen = 0;

		if (weaponInst.getChargedFishshot())
		{
			SS = 2;
		}

		double gradebonus = 1 + weaponItem.getCrystalType() * 0.1;
		int dmg = (int) (skill.getPower() * gradebonus * SS);

		if (player.getSkillLevel(1315) <= skill.getLevel() - 2) //1315 - Fish Expertise
		{
			//Penalty
			player.sendPacket(new SystemMessage(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY));
			pen = 50;
			int penatlydmg = dmg - pen;
			if (player.isGM())
			{
				player.sendMessage("Dmg w/o penalty = " + dmg);
			}
			dmg = penatlydmg;
		}

		if (SS > 1)
		{
			weaponInst.setChargedFishshot(false);
		}

		if (skill.getSkillType() == SkillType.REELING)//Realing
		{
			fish.useRealing(dmg, pen);
		}
		else//Pumping
		{
			fish.usePomping(dmg, pen);
		}

		return true;
	}
}