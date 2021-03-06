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

import com.l2scoria.Config;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.managers.FishingZoneManager;
import com.l2scoria.gameserver.model.Inventory;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.zone.type.L2FishingZone;
import com.l2scoria.gameserver.model.zone.type.L2WaterZone;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.InventoryUpdate;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2Weapon;
import com.l2scoria.gameserver.templates.L2WeaponType;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;

public class Fishing extends SkillAbst
{
	public Fishing()
	{
		_types = new SkillType[]{SkillType.FISHING};

		_playerUseOnly = true;
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		L2PcInstance player = activeChar.getPlayer();

		if (!Config.ALLOWFISHING && !player.isGM())
		{
			player.sendMessage("Fishing server is currently ofline");
			return false;
		}

		if (player.isFishing())
		{
			if (player.GetFishCombat() != null)
			{
				player.GetFishCombat().doDie(false);
			}
			else
			{
				player.EndFishing(false);
			}

			// Cancels fishing
			player.sendPacket(new SystemMessage(SystemMessageId.FISHING_ATTEMPT_CANCELLED));
			return false;
		}

		L2Weapon weaponItem = player.getActiveWeaponItem();
		if ((weaponItem == null || weaponItem.getItemType() != L2WeaponType.ROD))
		{
			// Fishing poles are not installed
			player.sendPacket(new SystemMessage(SystemMessageId.FISHING_POLE_NOT_EQUIPPED));
			return false;
		}

		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			// Bait not equiped.
			player.sendPacket(new SystemMessage(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING));
			return false;
		}

		player.SetLure(lure);
		L2ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if (lure2 == null || lure2.getCount() < 1) // Not enough bait.
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_BAIT));
			return false;
		}

		if (player.isInBoat())
		{
			// You can't fish while you are on boat
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_ON_BOAT));
			if (!player.isGM())
			{
				return false;
			}
		}

		if (player.isInCraftMode() || player.isInStoreMode())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK));
			if (!player.isGM())
			{
				return false;
			}
		}

		/*
		 * If fishing is enabled, here is the code that was striped from
		 * startFishing() in L2PcInstance. Decide now where will the hook be
		 * cast...
		 */
		int rnd = Rnd.get(200) + 200;
		double angle = Util.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x1 = (int) (cos * rnd);
		int y1 = (int) (sin * rnd);
		int x = player.getX() + x1;
		int y = player.getY() + y1;
		int z = player.getZ() - 30;

		/*
		 * ...and if the spot is in a fishing zone. If it is, it will then
		 * position the hook on the water surface. If not, you have to be GM to
		 * proceed past here... in that case, the hook will be positioned using
		 * the old Z lookup method.
		 */
		L2FishingZone aimingTo = FishingZoneManager.getInstance().isInsideFishingZone(x, y, z);
		L2WaterZone water = FishingZoneManager.getInstance().isInsideWaterZone(x, y, z);
		if (aimingTo != null && water != null && (GeoEngine.canSeeCoord(player, x, y, water.getWaterZ() - 50, false)))
		{
			z = water.getWaterZ() + 10;
			// player.sendMessage("Hook x,y: " + x + "," + y + " - Water Z,
			// Player Z:" + z + ", " + player.getZ()); //debug line, shows hook
			// landing related coordinates. Uncoment if needed.
		}
		else if (aimingTo != null && GeoEngine.canSeeCoord(player, x, y, water.getWaterZ() - 50, false))
		{
			z = aimingTo.getWaterZ() + 10;
		}
		else
		{
			//You can't fish here
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_HERE));
			if (!player.isGM())
			{
				return false;
			}
		}

		/* Of course since you can define fishing water volumes of any height, the function needs to be
						 * changed to cope with that. Still, this is assuming that fishing zones water surfaces, are
						 * always above "sea level".
						 */
		if (player.getZ() <= -3800 || player.getZ() < (z - 32))
		{
			//You can't fish in water
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_UNDER_WATER));
			if (!player.isGM())
			{
				return false;
			}
		}

		// Has enough bait, consume 1 and update inventory. Start fishing
		// follows.
		lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(lure2);
		player.sendPacket(iu);

		// If everything else checks out, actually cast the hook and start
		// fishing... :P
		player.startFishing(x, y, z);

		return false;
	}
}