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
package com.l2scoria.gameserver.templates;

import java.util.List;

import javolution.util.FastList;

import com.l2scoria.gameserver.model.base.ClassId;
import com.l2scoria.gameserver.model.base.Race;

/**
 * @author mkizub TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class L2PcTemplate extends L2CharTemplate
{

	/** The Class object of the L2PcInstance */
	public final Race race;
	public final ClassId classId;

	public final int _currentCollisionRadius;
	public final int _currentCollisionHeight;
	public final String className;

	public final int spawnX;
	public final int spawnY;
	public final int spawnZ;

	public final int classBaseLevel;
	public final float lvlHpAdd;
	public final float lvlHpMod;
	public final float lvlCpAdd;
	public final float lvlCpMod;
	public final float lvlMpAdd;
	public final float lvlMpMod;

	private final List<PcTemplateItem> _items = new FastList<PcTemplateItem>();

	public L2PcTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");
		_currentCollisionRadius = set.getInteger("collision_radius");
		_currentCollisionHeight = set.getInteger("collision_height");

		spawnX = set.getInteger("spawnX");
		spawnY = set.getInteger("spawnY");
		spawnZ = set.getInteger("spawnZ");

		classBaseLevel = set.getInteger("classBaseLevel");
		lvlHpAdd = set.getFloat("lvlHpAdd");
		lvlHpMod = set.getFloat("lvlHpMod");
		lvlCpAdd = set.getFloat("lvlCpAdd");
		lvlCpMod = set.getFloat("lvlCpMod");
		lvlMpAdd = set.getFloat("lvlMpAdd");
		lvlMpMod = set.getFloat("lvlMpMod");
	}

	/**
	 * add starter equipment
	 * 
	 * @param i
	 */
	public void addItem(int itemId, int amount, boolean equipped)
	{
		_items.add(new PcTemplateItem(itemId, amount, equipped));
	}

	/**
	 * @return itemIds of all the starter equipment
	 */
	public List<PcTemplateItem> getItems()
	{
		return _items;
	}

	/**
	 * @return
	 */
	@Override
	public int getCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	/**
	 * @return
	 */
	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public int getBaseFallSafeHeight(boolean female)
	{
		if(classId.getRace() == Race.darkelf || classId.getRace() == Race.elf)
			return classId.isMage() ? (female ? 330 : 300) : female ? 380 : 350;
		else if(classId.getRace() == Race.dwarf)
			return female ? 200 : 180;
		else if(classId.getRace() == Race.human)
			return classId.isMage() ? (female ? 220 : 200) : female ? 270 : 250;
		else if(classId.getRace() == Race.orc)
			return classId.isMage() ? (female ? 280 : 250) : female ? 220 : 200;

		return 400;

		/*
		  	Dark Elf Fighter F 380
			Dark Elf Fighter M 350
			Dark Elf Mystic F 330
			Dark Elf Mystic M 300
			Dwarf Fighter F 200
			Dwarf Fighter M 180
			Elf Fighter F 380
			Elf Fighter M 350
			Elf Mystic F 330
			Elf Mystic M 300
			Human Fighter F 270
			Human Fighter M 250
			Human Mystic F 220
			Human Mystic M 200
			Orc Fighter F 220
			Orc Fighter M 200
			Orc Mystic F 280
			Orc Mystic M 250
		*/
	}

	public static final class PcTemplateItem
	{
		private final int _itemId;
		private final int _amount;
		private final boolean _equipped;

		/**
		 * @param amount
		 * @param itemId
		 */
		public PcTemplateItem(int itemId, int amount, boolean equipped)
		{
			_itemId = itemId;
			_amount = amount;
			_equipped = equipped;
		}

		/**
		 * @return Returns the itemId.
		 */
		public int getItemId()
		{
			return _itemId;
		}

		/**
		 * @return Returns the amount.
		 */
		public int getAmount()
		{
			return _amount;
		}

		/**
		 * @return Returns the if the item should be equipped after char creation.
		 */
		public boolean isEquipped()
		{
			return _equipped;
		}
	}
}