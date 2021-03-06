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

package com.l2scoria.gameserver.datatables.sql;

import com.l2scoria.gameserver.model.L2Territory;
import com.l2scoria.util.database.L2DatabaseFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class TerritoryTable
{
	private static Logger _log = Logger.getLogger(TerritoryTable.class.getName());
	private static final TerritoryTable _instance = new TerritoryTable();
	private static Map<String, L2Territory> _territory;

	public static TerritoryTable getInstance()
	{
		return _instance;
	}

	private TerritoryTable()
	{
		_territory = new HashMap<String, L2Territory>();
		// load all data at server start
		reload_data();
	}

	public int[] getRandomPoint(int terr)
	{
		return _territory.get(terr).getRandomPoint();
	}

	public int getProcMax(int terr)
	{
		return _territory.get(terr).getProcMax();
	}

	public void reload_data()
	{
		_territory.clear();
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT loc_id, loc_x, loc_y, loc_zmin, loc_zmax, proc FROM `locations`");
			ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				String terr = "sql_terr_" + rset.getString("loc_id");

				if(_territory.get(terr) == null)
				{
					L2Territory t = new L2Territory(/*terr*/);
					_territory.put(terr, t);
				}
				_territory.get(terr).add(rset.getInt("loc_x"), rset.getInt("loc_y"), rset.getInt("loc_zmin"), rset.getInt("loc_zmax"), rset.getInt("proc"));

				terr = null;
			}
			
			rset.close();
			statement.close();
			statement = null;
			rset = null;
		}
		catch(Exception e1)
		{
			//problem with initializing spawn, go to next one
			_log.warn("locations couldnt be initialized:" + e1);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}

		_log.info("TerritoryTable: Loaded " + _territory.size() + " locations");
	}
}
