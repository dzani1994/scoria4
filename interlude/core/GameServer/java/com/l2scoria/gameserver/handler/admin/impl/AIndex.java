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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.GmListTable;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands: - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting
 * admin menus - gmliston/gmlistoff = includes/excludes active character from /gmlist results - silence = toggles
 * private messages acceptance mode - diet = toggles weight penalty mode - tradeoff = toggles trade acceptance mode -
 * reload = reloads specified component from multisell|skill|npc|htm|item|instancemanager - set/set_menu/set_mod =
 * alters specified server setting - saveolymp = saves olympiad state manually - manualhero = cycles olympiad and
 * calculate new heroes.
 *
 * @author Akumu, ProGramMoS
 * @version $Revision: 1.4 $
 */
public class AIndex extends AdminAbst
{
	public AIndex()
	{
		_commands = new String[]{"admin_admin", "admin_admin1", "admin_admin2", "admin_admin3", "admin_admin4", "admin_admin5", "admin_gmliston", "admin_gmlistoff", "admin_silence", "admin_diet", "admin_set", "admin_set_menu", "admin_set_mod", "admin_saveolymp", "admin_manualhero"};
	}

	private enum CommandEnum
	{
		admin_admin,
		admin_admin1,
		admin_admin2,
		admin_admin3,
		admin_admin4,
		admin_admin5,
		admin_gmliston,
		admin_gmlistoff,
		admin_silence,
		admin_diet,
		admin_set,
		admin_set_menu,
		admin_set_mod,
		admin_saveolymp,
		admin_manualhero
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		String[] wordList = command.split(" ");
		CommandEnum comm;

		try
		{
			comm = CommandEnum.valueOf(wordList[0]);
		} catch (Exception e)
		{
			return false;
		}

		CommandEnum commandEnum = comm;

		switch (commandEnum)
		{
			case admin_admin:
			case admin_admin1:
			case admin_admin2:
			case admin_admin3:
			case admin_admin4:
			case admin_admin5:
				showMainPage(activeChar, command);
				break;

			case admin_gmliston:
				GmListTable.getInstance().showGm(activeChar);
				activeChar.sendMessage("Registerd into gm list");
				break;

			case admin_gmlistoff:
				GmListTable.getInstance().hideGm(activeChar);
				activeChar.sendMessage("Removed from gm list");
				break;

			case admin_silence:
				if (activeChar.isSilenceMode()) // already in message refusal mode
				{
					activeChar.setSilenceMode(false);
					activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
				}
				else
				{
					activeChar.setSilenceMode(true);
					activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
				}
				break;

			case admin_saveolymp:
				try
				{
					Olympiad.getInstance().save();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				activeChar.sendMessage("Olympiad stuff saved!");
				break;

			case admin_manualhero:
				try
				{
					Olympiad.getInstance().manualSelectHeroes();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				activeChar.sendMessage("Heroes formed!");
				break;

			case admin_diet:
				try
				{
					StringTokenizer st = new StringTokenizer(command);
					st.nextToken();

					if (st.nextToken().equalsIgnoreCase("on"))
					{
						activeChar.setDietMode(true);
						activeChar.sendMessage("Diet mode on");
					}
					else if (st.nextToken().equalsIgnoreCase("off"))
					{
						activeChar.setDietMode(false);
						activeChar.sendMessage("Diet mode off");
					}

					st = null;
				} catch (Exception ex)
				{
					if (activeChar.getDietMode())
					{
						activeChar.setDietMode(false);
						activeChar.sendMessage("Diet mode off");
					}
					else
					{
						activeChar.setDietMode(true);
						activeChar.sendMessage("Diet mode on");
					}
				} finally
				{
					activeChar.refreshOverloaded();
				}
				break;

			case admin_set:
				StringTokenizer st = new StringTokenizer(command);
				String[] cmd = st.nextToken().split("_");

				try
				{
					String[] parameter = st.nextToken().split("=");
					String pName = parameter[0].trim();
					String pValue = parameter[1].trim();

					if (Config.setParameterValue(pName, pValue))
					{
						activeChar.sendMessage("parameter " + pName + " succesfully set to " + pValue);
					}
					else
					{
						activeChar.sendMessage("Invalid parameter!");
					}

					parameter = null;
					pName = null;
					pValue = null;
				} catch (Exception e)
				{
					if (cmd.length == 2)
					{
						activeChar.sendMessage("Usage: //set parameter=vaue");
					}
				} finally
				{
					st = null;

					if (cmd.length == 3)
					{
						if (cmd[2].equalsIgnoreCase("menu"))
						{
							HelpPage.showHelpPage(activeChar, "settings.htm");
						}
						else if (cmd[2].equalsIgnoreCase("mod"))
						{
							HelpPage.showHelpPage(activeChar, "mods_menu.htm");
						}
					}
				}
				break;
		}

		wordList = null;
		comm = null;
		commandEnum = null;

		return true;
	}


	private void showMainPage(L2PcInstance activeChar, String command)
	{
		int mode = 0;
		String filename = null;

		try
		{
			mode = Integer.parseInt(command.substring(11));
		} catch (Exception e)
		{
			//ignore
		}

		switch (mode)
		{
			case 1:
				filename = "main";
				break;
			case 2:
				filename = "game";
				break;
			case 3:
				filename = "effects";
				break;
			case 4:
				filename = "server";
				break;
			case 5:
				filename = "mods";
				break;
			default:
				if (Config.GM_ADMIN_MENU_STYLE.equals("modern"))
				{
					filename = "main";
				}
				else
				{
					filename = "classic";
				}
				break;
		}

		HelpPage.showHelpPage(activeChar, filename + "_menu.htm");

		filename = null;
	}
}
