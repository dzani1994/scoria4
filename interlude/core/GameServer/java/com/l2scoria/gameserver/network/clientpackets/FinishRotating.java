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

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.StopRotation;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class FinishRotating extends L2GameClientPacket
{
	private static final String _C__4B_FINISHROTATING = "[C] 4B FinishRotating";

	private int _degree;

	@Override
	protected void readImpl()
	{
		_degree = readD();
		/*_unknown = */readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		StopRotation sr = new StopRotation(player, _degree, 0);
		player.broadcastPacket(sr);
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__4B_FINISHROTATING;
	}
}
