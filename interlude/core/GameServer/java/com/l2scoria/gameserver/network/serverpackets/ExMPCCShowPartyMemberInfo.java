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
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.gameserver.model.L2Party;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: ch d[Sdd]
 * 
 * @author KenM
 */
public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private static final String _S__FE_4A_EXMPCCSHOWPARTYMEMBERINFO = "[S] FE:4A ExMPCCShowPartyMemberInfo";
	private L2Party _party;

	public ExMPCCShowPartyMemberInfo(L2Party party)
	{
		_party = party;
	}
	/**
	 * @see com.l2scoria.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4A);
		writeD(_party.getMemberCount()); // Number of Members
		for(L2PcInstance pc : _party.getPartyMembers())
		{
			writeS(pc.getName()); // Membername
			writeD(pc.getObjectId()); // ObjId
			writeD(pc.getClassId().getId()); // Classid
		}
	}

	/**
	 * @see com.l2scoria.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_4A_EXMPCCSHOWPARTYMEMBERINFO;
	}
}
