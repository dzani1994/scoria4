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
package com.l2scoria.gameserver.datatables.sql;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.AccessLevel;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author FBIagent<br>
 */
public class AccessLevels
{
	/** The logger<br> */
	private final static Logger _log = Logger.getLogger(AccessLevels.class.getName());
	/** The one and only instance of this class, retriveable by getInstance()<br> */
	private static AccessLevels _instance = null;
	/** Reserved master access level<br> */
	public static final int _masterAccessLevelNum = Config.MASTERACCESS_LEVEL;
	/** The master access level which can use everything<br> */
	public static AccessLevel _masterAccessLevel = new AccessLevel(_masterAccessLevelNum, "Master Access", Config.MASTERACCESS_NAME_COLOR, Config.MASTERACCESS_TITLE_COLOR, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
	/** Reserved user access level<br> */
	public static final int _userAccessLevelNum = 0;
	/** The user access level which can do no administrative tasks<br> */
	public static AccessLevel _userAccessLevel = new AccessLevel(_userAccessLevelNum, "User", Integer.decode("0xFFFFFF"), Integer.decode("0xFFFFFF"), false, false, false, true, false, true, true, true, true, true, false, false, false, false);
	/** FastMap of access levels defined in database<br> */
	private FastMap<Integer, AccessLevel> _accessLevels = new FastMap<Integer, AccessLevel>();

	/**
	 * Loads the access levels from database<br>
	 */
	private AccessLevels()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement stmt = con.prepareStatement("SELECT * FROM `access_levels` ORDER BY `accessLevel` DESC");
			ResultSet rset = stmt.executeQuery();
			int accessLevel = 0;
			String name = null;
			int nameColor = 0;
			int titleColor = 0;
			boolean isGm = false;
			boolean allowPeaceAttack = false;
			boolean allowFixedRes = false;
			boolean allowTransaction = false;
			boolean allowAltG = false;
			boolean giveDamage = false;
			boolean takeAggro = false;
			boolean gainExp = false;
			boolean useNameColor = true;
			boolean useTitleColor = false;
			boolean canDisableGmStatus = true;
			boolean HeroVoice = false;
			boolean SeeAllChat = false;
                        boolean FullClassMaster = false;

			while(rset.next())
			{
				accessLevel = rset.getInt("accessLevel");
				name = rset.getString("name");

				if(accessLevel == _userAccessLevelNum)
				{
					_log.warn("AccessLevels: Access level with name " + name + " is using reserved user access level " + _userAccessLevelNum + ". Ignoring it!");
					continue;
				}
				else if(accessLevel == _masterAccessLevelNum)
				{
					_log.warn("AccessLevels: Access level with name " + name + " is using reserved master access level " + _masterAccessLevelNum + ". Ignoring it!");
					continue;
				}
				else if(accessLevel < 0)
				{
					_log.warn("AccessLevels: Access level with name " + name + " is using banned access level state(below 0). Ignoring it!");
					continue;
				}

				try
				{
					nameColor = Integer.decode("0x" + rset.getString("nameColor"));
				}
				catch(NumberFormatException nfe)
				{
					try
					{
						nameColor = Integer.decode("0xFFFFFF");
					}
					catch(NumberFormatException nfe2)
					{}
				}

				try
				{
					titleColor = Integer.decode("0x" + rset.getString("titleColor"));

				}
				catch(NumberFormatException nfe)
				{
					try
					{
						titleColor = Integer.decode("0x77FFFF");
					}
					catch(NumberFormatException nfe2)
					{}
				}

				isGm = rset.getBoolean("isGm");
				allowPeaceAttack = rset.getBoolean("allowPeaceAttack");
				allowFixedRes = rset.getBoolean("allowFixedRes");
				allowTransaction = rset.getBoolean("allowTransaction");
				allowAltG = rset.getBoolean("allowAltg");
				giveDamage = rset.getBoolean("giveDamage");
				takeAggro = rset.getBoolean("takeAggro");
				gainExp = rset.getBoolean("gainExp");
				useNameColor = rset.getBoolean("useNameColor");
				useTitleColor = rset.getBoolean("useTitleColor");
				canDisableGmStatus = rset.getBoolean("canDisableGmStatus");
                                FullClassMaster = rset.getBoolean("FullClassMaster");
				_accessLevels.put(accessLevel, new AccessLevel(accessLevel, name, nameColor, titleColor, isGm, allowPeaceAttack, allowFixedRes, allowTransaction, allowAltG, giveDamage, takeAggro, gainExp, useNameColor, useTitleColor, canDisableGmStatus, HeroVoice, SeeAllChat, FullClassMaster));
			}

			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			_log.warn("AccessLevels: Error loading from database:" + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		_log.info("AccessLevels: Loaded " + _accessLevels.size() + " Access Levels from database.");
	}

	/**
	 * Returns the one and only instance of this class<br>
	 * <br>
	 * 
	 * @return AccessLevels: the one and only instance of this class<br>
	 */
	public static AccessLevels getInstance()
	{
		return _instance == null ? (_instance = new AccessLevels()) : _instance;
	}

	/**
	 * Returns the access level by characterAccessLevel<br>
	 * <br>
	 * 
	 * @param accessLevelNum as int<br>
	 * <br>
	 * @return AccessLevel: AccessLevel instance by char access level<br>
	 */
	public AccessLevel getAccessLevel(int accessLevelNum)
	{
		AccessLevel accessLevel = null;

		synchronized (_accessLevels)
		{
			accessLevel = _accessLevels.get(accessLevelNum);
		}
		return accessLevel;
	}

	public void addBanAccessLevel(int accessLevel)
	{
		synchronized (_accessLevels)
		{
			if(accessLevel > -1)
				return;

			_accessLevels.put(accessLevel, new AccessLevel(accessLevel, "Banned", Integer.decode("0x000000"), Integer.decode("0x000000"), false, false, false, false, false, false, false, false, false, false, false, false, false, false));
		}
	}
}
