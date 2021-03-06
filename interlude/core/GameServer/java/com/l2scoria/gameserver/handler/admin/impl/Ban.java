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
package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.thread.LoginServerThread;
import com.l2scoria.util.database.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * This class handles following admin commands: - ban account_name = changes account access level to -100 and logs him
 * off. If no account is specified, target's account is used. - unban account_name = changes account access level to 0.
 * - jail charname [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified. -
 * unjail charname = Unjails player, teleport him to Floran.
 * 
 * @version $Revision: 1.2 $
 * @author Akumu, ProGramMoS
 */
public class Ban extends AdminAbst
{
	public Ban()
	{
		_commands = new String[] {"admin_ban", "admin_unban", "admin_jail", "admin_unjail"};
	}

	private enum CommandEnum
	{
		admin_ban,
		admin_unban,
		admin_jail,
		admin_unjail
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String account_name = "";
		String player = "";
		L2PcInstance plyr = null;

		String[] wordList = command.split(" ");
		CommandEnum comm;

		try
		{
			comm = CommandEnum.valueOf(wordList[0]);
		}
		catch(Exception e)
		{
			return false;
		}

		CommandEnum commandEnum = comm;

		switch(commandEnum)
		{
			case admin_ban:
				try
				{
					player = st.nextToken();
					plyr = L2World.getInstance().getPlayer(player);
				}
				catch(Exception e)
				{
					L2Object target = activeChar.getTarget();

					if(target != null && target.isPlayer)
					{
						plyr = (L2PcInstance) target;
					}
					else
					{
						activeChar.sendMessage("Usage: //ban [account_name] (if none, target char's account gets banned)");
					}

					target = null;
				}

				if(plyr != null && plyr.equals(activeChar))
				{
					plyr.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
				}
				else if(plyr == null)
				{
					account_name = player;
					LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
					activeChar.sendMessage("Ban request sent for account " + account_name + ". If you need a playername based commmand, see //ban_menu");
				}
				else
				{
					if (plyr._event != null)
						plyr._event.remove(plyr);

					plyr.setAccountAccesslevel(-1);
					account_name = plyr.getAccountName();
					RegionBBSManager.getInstance().changeCommunityBoard();
					//plyr.logout();
					plyr.closeNetConnection();
					activeChar.sendMessage("Account " + account_name + " banned.");
					if(Config.ANNOUNCE_BAN)
					{
						if(wordList.length > 2)
							Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " забанил акаунт " + account_name + ". Причина: " + command.substring(wordList[0].length()+wordList[1].length()+2));
						else
							Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " забанил акаунт " + account_name + ".");
					}
				}
				break;

			case admin_unban:
				try
				{
					account_name = st.nextToken();
					LoginServerThread.getInstance().sendAccessLevel(account_name, 0);
					activeChar.sendMessage("Unban request sent for account " + account_name + ". If you need a playername based commmand, see //unban_menu");
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Usage: //unban <account_name>");

					if(Config.DEBUG)
					{
						e.printStackTrace();
					}
				}
				break;

			case admin_jail:
				try
				{
					player = st.nextToken();

					int delay = 0;

					try
					{
						delay = Integer.parseInt(st.nextToken());
					}
					catch(NumberFormatException nfe)
					{
						activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes]");
					}
					catch(NoSuchElementException nsee)
					{
						//ignore
					}

					L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

					if(playerObj != null)
					{
						playerObj.setInJail(true, delay);
						if(Config.ANNOUNCE_BAN)
						{
							if(wordList.length > 3)
								Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " отправил в тюрьму игрока " + player + " на " + (delay > 0 ? delay + " минут." : "всегда.") + " Причина: " + command.substring(wordList[0].length()+wordList[1].length()+wordList[2].length()+3));
							else
								Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " отправил в тюрьму игрока " + player + " на " + (delay > 0 ? delay + " минут." : "всегда."));
						}
						activeChar.sendMessage("Character " + player + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
					}
					else
					{
						if(Config.ANNOUNCE_BAN)
						{
							if(wordList.length > 3)
								Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " отправил в тюрьму игрока " + player + " на " + (delay > 0 ? delay + " минут." : "всегда.") + " Причина: " + command.substring(wordList[0].length()+wordList[1].length()+wordList[2].length()+3));
							else
								Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " отправил в тюрьму игрока " + player + " на " + (delay > 0 ? delay + " минут." : "всегда."));
						}
						jailOfflinePlayer(activeChar, player, delay);
					}

					playerObj = null;
				}
				catch(NoSuchElementException nsee)
				{
					activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes]");
				}
				catch(Exception e)
				{
					if(Config.DEBUG)
					{
						e.printStackTrace();
					}
				}
				break;

			case admin_unjail:
				try
				{
					player = st.nextToken();
					L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

					if(playerObj != null)
					{
						playerObj.setInJail(false, 0);
						activeChar.sendMessage("Character " + player + " removed from jail");
					}
					else
					{
						unjailOfflinePlayer(activeChar, player);
					}

					playerObj = null;
				}
				catch(NoSuchElementException nsee)
				{
					activeChar.sendMessage("Specify a character name.");
				}
				catch(Exception e)
				{
					if(Config.DEBUG)
					{
						e.printStackTrace();
					}
				}
				break;
		}

		st = null;
		account_name = null;
		player = null;
		plyr = null;
		wordList = null;
		comm = null;
		commandEnum = null;

		return true;
	}

	private void jailOfflinePlayer(L2PcInstance activeChar, String name, int delay)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, 1);
			statement.setLong(5, delay * 60000L);
			statement.setString(6, name);

			statement.execute();

			int count = statement.getUpdateCount();

			statement.close();
			statement = null;

			if(count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
		}
		catch(SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");

			if(Config.DEBUG)
			{
				se.printStackTrace();
			}
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	private void unjailOfflinePlayer(L2PcInstance activeChar, String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.execute();

			int count = statement.getUpdateCount();

			statement.close();
			statement = null;

			if(count == 0)
			{
				activeChar.sendMessage("Character not found!");
			}
			else
			{
				activeChar.sendMessage("Character " + name + " removed from jail");
			}
		}
		catch(SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");

			if(Config.DEBUG)
			{
				se.printStackTrace();
			}
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}
}
