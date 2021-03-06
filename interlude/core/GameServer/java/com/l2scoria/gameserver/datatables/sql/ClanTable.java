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
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.managers.FortManager;
import com.l2scoria.gameserver.managers.FortSiegeManager;
import com.l2scoria.gameserver.managers.SiegeManager;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.L2ClanMember;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.Fort;
import com.l2scoria.gameserver.model.entity.siege.FortSiege;
import com.l2scoria.gameserver.model.entity.siege.Siege;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.5.2.5 $ $Date: 2005/03/27 15:29:18 $
 */
public class ClanTable
{
	private static Logger _log = Logger.getLogger(ClanTable.class.getName());

	private static ClanTable _instance;

	private Map<Integer, L2Clan> _clans;

	public static ClanTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new ClanTable();
		}

		return _instance;
	}

	public L2Clan[] getClans()
	{
		return _clans.values().toArray(new L2Clan[_clans.size()]);
	}

	public int getTopRate(int clan_id)
	{
		L2Clan clan = getClan(clan_id);
		if (clan.getLevel() < 3)
		{
			return 0;
		}
		int i = 1;
		for (L2Clan clans : getClans())
		{
			if (clan != clans)
			{
				if (clan.getLevel() < clans.getLevel())
				{
					i++;
				}
				else if (clan.getLevel() == clans.getLevel())
				{
					if (clan.getReputationScore() <= clans.getReputationScore())
					{
						i++;
					}
				}
			}
		}
		clan = null;
		return i;
	}

	private ClanTable()
	{
		_clans = new FastMap<Integer, L2Clan>();
		L2Clan clan;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data");
			ResultSet result = statement.executeQuery();

			// Count the clans
			int clanCount = 0;

			while(result.next())
			{
				_clans.put(Integer.parseInt(result.getString("clan_id")), new L2Clan(Integer.parseInt(result.getString("clan_id"))));
				clan = getClan(Integer.parseInt(result.getString("clan_id")));
				if(clan.getDissolvingExpiryTime() != 0)
				{
					scheduleRemoveClan(clan.getClanId());
				}
				clanCount++;
			}
			result.close();
			statement.close();
			statement = null;
			result = null;

			_log.info("Restored " + clanCount + " clans from the database.");
		}
		catch(Exception e)
		{
			_log.warn("data error on ClanTable: " + e);
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}

		restorewars();
	}

	/**
	 * @param clanId
	 * @return
	 */
	public L2Clan getClan(int clanId)
	{
		L2Clan clan = _clans.get(new Integer(clanId));

		return clan;
	}

	public L2Clan getClanByName(String clanName)
	{
		for(L2Clan clan : getClans())
		{
			if(clan.getName().equalsIgnoreCase(clanName))
				return clan;
		}

		return null;
	}

	/**
	 * Creates a new clan and store clan info to database
	 * 
	 * @param player
	 * @return NULL if clan with same name already exists
	 */
	public L2Clan createClan(L2PcInstance player, String clanName)
	{
		if(null == player)
			return null;

		if(Config.DEBUG)
		{
			_log.info(player.getObjectId() + "(" + player.getName() + ") requested a clan creation.");
		}

		if(10 > player.getLevel())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN));
			return null;
		}

		if(0 != player.getClanId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_CREATE_CLAN));
			return null;
		}

		if(System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN));
			return null;
		}

		if(!isValidCalnName(player, clanName))
			return null;

		L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		L2ClanMember leader = new L2ClanMember(clan, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());

		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(leader.calculatePledgeClass(player));
		player.setClanPrivileges(L2Clan.CP_ALL);

		if(Config.DEBUG)
		{
			_log.info("New clan created: " + clan.getClanId() + " " + clan.getName());
		}

		_clans.put(new Integer(clan.getClanId()), clan);

		//should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, player));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new SystemMessage(SystemMessageId.CLAN_CREATED));

		leader = null;

		return clan;
	}

	public boolean isValidCalnName(L2PcInstance player, String clanName)
	{
		if(!Util.isAlphaNumeric(clanName) || clanName.length() < 2)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return false;
		}

		if(clanName.length() > 16)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
			return false;
		}

		if(getClanByName(clanName) != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(clanName);
			player.sendPacket(sm);
			sm = null;
			return false;
		}

		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
		}
		catch(PatternSyntaxException e) // case of illegal pattern
		{
			_log.warn("ERROR : Clan name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}

		Matcher match = pattern.matcher(clanName);

		if(!match.matches())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return false;
		}

		return true;
	}

	public synchronized void destroyClan(int clanId, Connection con)
	{
		L2Clan clan = getClan(clanId);

		if(clan == null)
			return;

		L2PcInstance leader = null;
		if(clan.getLeader()!=null)
			leader = clan.getLeader().getPlayerInstance();
		if(leader!=null)
		{
			if(Config.CLAN_LEADER_COLOR_ENABLED && clan.getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
			{
				if(Config.CLAN_LEADER_COLORED == 1)
				{
					leader.getAppearance().setNameColor(0xFFFFFF);
				}
				else
				{
					leader.getAppearance().setTitleColor(0xFFFF77);
				}
			}
		}

		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));

		int castleId = clan.getHasCastle();

		if(castleId == 0)
		{
			for(Siege siege : SiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}

		int fortId = clan.getHasFort();

		if(fortId == 0)
		{
			for(FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}

		L2ClanMember leaderMember = clan.getLeader();

		if(leaderMember == null)
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
		}
		else
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayerInstance(), null);
		}

		leaderMember = null;

		for(L2ClanMember member : clan.getMembers())
		{
			clan.removeClanMember(member.getObjectId(), 0);
		}

		int leaderId = clan.getLeaderId();
		int clanLvl = clan.getLevel();

		clan = null;

		_clans.remove(clanId);
		IdFactory.getInstance().releaseId(clanId);

		try
		{
			if(con == null)
				con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();

			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();

			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();

			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();

			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
			statement.setInt(1, clanId);
			statement.setInt(2, clanId);
			statement.execute();

			statement = con.prepareStatement("DELETE FROM clan_notices WHERE clanID=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			if(leader == null && leaderId != 0 && Config.CLAN_LEADER_COLOR_ENABLED && clanLvl >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
			{
				if(Config.CLAN_LEADER_COLORED == 1)
				{
					statement = con.prepareStatement("UPDATE characters SET name_color = '000000' WHERE odj_Id = ?");
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET title_color = 'FFFF77' WHERE odj_Id = ?");
				}
				statement.setInt(1, leaderId);
				statement.execute();
			}

			if(castleId != 0)
			{
				statement = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?");
				statement.setInt(1, castleId);
				statement.execute();
			}

			if(fortId != 0)
			{
				Fort fort = FortManager.getInstance().getFortById(fortId);
				if(fort != null)
				{
					L2Clan owner = fort.getOwnerClan();
					if(clan == owner)
					{
						fort.removeOwner(clan);
					}

					owner = null;
				}
				fort = null;
			}

			if(Config.DEBUG)
			{
				_log.info("clan removed in db: " + clanId);
			}
			
			statement.close();
		}
		catch(Exception e)
		{
			_log.warn("error while removing clan in db " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	public void scheduleRemoveClan(final int clanId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
			public void run()
			{
				if(getClan(clanId) == null)
					return;

				if(getClan(clanId).getDissolvingExpiryTime() != 0)
				{
					destroyClan(clanId, null);
				}
			}
		}, Math.max(getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis(), 120000));
	}

	public boolean isAllyExists(String allyName)
	{
		for(L2Clan clan : getClans())
		{
			if(clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
				return true;
		}
		return false;
	}

	public void storeclanswars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);

		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2) VALUES(?,?,?,?)");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.warn("could not store clans wars data:" + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		//SystemMessage msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);

		SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		msg = null;

		//msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
		//msg.addString(clan1.getName());
		//clan2.broadcastToOnlineMembers(msg);
		// clan1 declared clan war.

		msg = new SystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);

		clan1 = null;
		clan2 = null;
		msg = null;
	}

	public void deleteclanswars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);

		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();

		//for(L2ClanMember player: clan1.getMembersCount())
		//{
		//    if(player.getPlayerInstance()!=null)
		//			player.getPlayerInstance().setWantsPeace(0);
		//}
		//for(L2ClanMember player: clan2.getMembersCount())
		//{
		//    if(player.getPlayerInstance()!=null)
		//			player.getPlayerInstance().setWantsPeace(0);
		//}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.execute();

			//statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			//statement.setInt(1,clanId2);
			//statement.setInt(2,clanId1);
			//statement.execute();

			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.warn("could not restore clans wars data:" + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		//SystemMessage msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_ENDED);

		SystemMessage msg = new SystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		msg = null;

		msg = new SystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
		msg = null;

		//msg = new SystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
		//msg.addString(clan1.getName());
		//clan2.broadcastToOnlineMembers(msg);
		//msg = null;

		clan1 = null;
		clan2 = null;
	}

	public void checkSurrender(L2Clan clan1, L2Clan clan2)
	{
		int count = 0;

		for(L2ClanMember player : clan1.getMembers())
		{
			if(player != null && player.getPlayerInstance().getWantsPeace() == 1)
			{
				count++;
			}
		}

		if(count == clan1.getMembers().length - 1)
		{
			clan1.deleteEnemyClan(clan2);
			clan2.deleteEnemyClan(clan1);
			deleteclanswars(clan1.getClanId(), clan2.getClanId());
		}
	}

	private void restorewars()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan1, clan2, wantspeace1, wantspeace2 FROM clan_wars");
			ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				getClan(rset.getInt("clan1")).setEnemyClan(rset.getInt("clan2"));
				getClan(rset.getInt("clan2")).setAttackerClan(rset.getInt("clan1"));
			}
			rset.close();
			statement.close();
			statement = null;
			rset = null;
		}
		catch(Exception e)
		{
			_log.warn("could not restore clan wars data:" + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}
}
