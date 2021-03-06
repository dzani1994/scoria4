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

package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;

public class L2SkillZone extends L2ZoneDefault
{
	private int _skillId;
	private int _skillLvl;
	private boolean _onSiege;

	public L2SkillZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("skillId"))
		{
			_skillId = Integer.parseInt(value);
		}
		else if(name.equals("skillLvl"))
		{
			_skillLvl = Integer.parseInt(value);
		}
		else if(name.equals("onSiege"))
		{
			_onSiege = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		startSkill(character);

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		stopSkill(character);

		super.onExit(character);
	}

	@Override
	protected void onDieInside(L2Character character)
	{
		stopSkill(character);

		super.onDieInside(character);
	}

	@Override
	protected void onReviveInside(L2Character character)
	{
		startSkill(character);

		super.onReviveInside(character);
	}

	private void startSkill(L2Character character)
	{
		if((character.isPlayer || character.isSummonInstance) && (!_onSiege || character.isInsideZone(L2Character.ZONE_SIEGE)))
		{
			if(character.isPlayer)
			{
				((L2PcInstance) character).enterDangerArea();
			}

			SkillTable.getInstance().getInfo(_skillId, _skillLvl).getEffects(character, character);
		}
	}

	private void stopSkill(L2Character character)
	{
		if(character.isPlayer || character.isSummonInstance)
		{
			character.stopSkillEffects(_skillId);

			if(character.isPlayer)
			{
				((L2PcInstance) character).exitDangerArea();
			}
		}
	}
}
