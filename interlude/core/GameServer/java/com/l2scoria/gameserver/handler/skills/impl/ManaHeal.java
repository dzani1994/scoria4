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
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.StatusUpdate;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Stats;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/02 15:38:36 $
 */

public class ManaHeal extends SkillAbst
{
	public ManaHeal()
	{
		_types = new SkillType[]{SkillType.MANAHEAL, SkillType.MANARECHARGE, SkillType.MANAHEAL_PERCENT};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		for (L2Character target : (L2Character[]) targets)
		{
			double mp = skill.getPower();
			if (skill.getSkillType() == SkillType.MANAHEAL_PERCENT)
			{
				mp = target.getMaxMp() * mp / 100.0;
			}
			else
			{
				mp = (skill.getSkillType() == SkillType.MANARECHARGE) ? target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null) : mp;
			}


            L2PcInstance player = activeChar.getPlayer();
            if (player != null)
            {
                L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
                if (weaponInst != null)
                {
                    if (skill.isMagic())
                    {
                        if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
                        {
                            weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
                        }
                        else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                        {
                            weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
                        }
                    }
                }
            }

			target.setLastHealAmount((int) mp);
			target.setCurrentMp(mp + target.getCurrentMp());

			StatusUpdate sump = new StatusUpdate(target.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
			target.sendPacket(sump);

			if (activeChar.isPlayer && activeChar != target)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1);
				sm.addString(activeChar.getName());
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_MP_RESTORED);
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
		}

		return true;
	}
}
