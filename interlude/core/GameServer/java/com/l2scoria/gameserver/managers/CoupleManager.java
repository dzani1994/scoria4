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
package com.l2scoria.gameserver.managers;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Wedding;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author evill33t
 */
public class CoupleManager
{
	private static final Logger _log = Logger.getLogger(CoupleManager.class.getName());

	// =========================================================
	private static CoupleManager _instance;

	public static final CoupleManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Initializing CoupleManager");
			_instance = new CoupleManager();
			_instance.load();
		}

		return _instance;
	}

	// =========================================================
	// Data Field
	private FastList<Wedding> _couples;

	// =========================================================
	// Method - Public
	public void reload()
	{
		getCouples().clear();
		load();
	}

	// =========================================================
	// Method - Private
	private final void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("Select id from mods_wedding order by id");
			rs = statement.executeQuery();

			while(rs.next())
			{
				getCouples().add(new Wedding(rs.getInt("id")));
			}

			statement.close();
			statement = null;
			rs.close();
			rs = null;

			_log.info("Loaded: " + getCouples().size() + " couples(s)");
		}
		catch(Exception e)
		{
			_log.warn("Exception: CoupleManager.load(): " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	// =========================================================
	// Property - Public
	public final Wedding getCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		if(index >= 0)
			return getCouples().get(index);
		return null;
	}

	public void createCouple(L2PcInstance player1, L2PcInstance player2)
	{
		if(player1 != null && player2 != null)
		{
			if(player1.getPartnerId() == 0 && player2.getPartnerId() == 0)
			{
				int _player1id = player1.getObjectId();
				int _player2id = player2.getObjectId();

				Wedding _new = new Wedding(player1, player2);
				getCouples().add(_new);
				player1.setPartnerId(_player2id);
				player2.setPartnerId(_player1id);
				player1.setCoupleId(_new.getId());
				player2.setCoupleId(_new.getId());

				_new = null;
			}
		}
	}

	public void deleteCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		Wedding wedding = getCouples().get(index);

		if(wedding != null)
		{
			L2PcInstance player1 = (L2PcInstance) L2World.getInstance().findObject(wedding.getPlayer1Id());
			L2PcInstance player2 = (L2PcInstance) L2World.getInstance().findObject(wedding.getPlayer2Id());
			if(player1 != null)
			{
				player1.setPartnerId(0);
				player1.setMarried(false);
				player1.setCoupleId(0);
				player1.getAppearance().setNameColor(0xFFFFFF);

			}
			if(player2 != null)
			{
				player2.setPartnerId(0);
				player2.setMarried(false);
				player2.setCoupleId(0);
				player2.getAppearance().setNameColor(0xFFFFFF);
			}

			wedding.divorce();
			getCouples().remove(index);

			player1 = null;
			player2 = null;
			wedding = null;
		}
	}

	public final int getCoupleIndex(int coupleId)
	{
		int i = 0;
		for(Wedding temp : getCouples())
		{
			if(temp != null && temp.getId() == coupleId)
			{
				temp = null;
				return i;
			}
			i++;
		}
		return -1;
	}

	public final FastList<Wedding> getCouples()
	{
		if(_couples == null)
		{
			_couples = new FastList<Wedding>();
		}

		return _couples;
	}
}
