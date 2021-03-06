package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import javolution.text.TextBuilder;

import java.util.StringTokenizer;

/**
 * @author Akumu, ProGramMoS
 */

public class Buffs extends AdminAbst
{
	public Buffs()
	{
		_commands = new String[]{"admin_getbuffs", "admin_stopbuff", "admin_stopallbuffs", "admin_areacancel"};
	}

	private enum CommandEnum
	{
		admin_getbuffs,
		admin_stopbuff,
		admin_stopallbuffs,
		admin_areacancel
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command, " ");

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
			case admin_getbuffs:
				st = new StringTokenizer(command, " ");
				command = st.nextToken();

				if(st.hasMoreTokens())
				{
					L2PcInstance player = null;
					String playername = st.nextToken();
					st = null;

					try
					{
						player = L2World.getInstance().getPlayer(playername);
					}
					catch(Exception e)
					{
						//ignore
					}

					if(player != null)
					{
						showBuffs(player, activeChar);
						playername = null;
						player = null;
						return true;
					}
					else
					{
						activeChar.sendMessage("The player " + playername + " is not online");
						playername = null;
						player = null;
						return false;
					}
				}
				else if(activeChar.getTarget() != null && activeChar.getTarget().isPlayer)
				{
					showBuffs((L2PcInstance) activeChar.getTarget(), activeChar);
					return true;
				}
				else
					return true;

			case admin_stopbuff:
				try
				{
					st = new StringTokenizer(command, " ");

					st.nextToken();
					String playername = st.nextToken();

					int SkillId = Integer.parseInt(st.nextToken());

					removeBuff(activeChar, playername, SkillId);

					st = null;
					playername = null;

					return true;
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Failed removing effect: " + e.getMessage());
					activeChar.sendMessage("Usage: //stopbuff <playername> [skillId]");
					return false;
				}

			case admin_stopallbuffs:
				st = new StringTokenizer(command, " ");
				st.nextToken();
				String playername = null;
				try
				{
					playername = st.nextToken();
				}
				catch(Exception e)
				{
					//I like big (. )( .)
				}

				if(playername != null)
				{
					removeAllBuffs(activeChar, playername);
					playername = null;
					st = null;

					return true;
				}
				else
				{
					if(activeChar.getTarget() != null && activeChar.getTarget().isPlayer)
					{
						removeAllBuffs(activeChar, activeChar.getTarget().getName());
						st = null;
						return true;
					}
					return false;
				}

			case admin_areacancel:
				st = new StringTokenizer(command, " ");
				st.nextToken();
				String val = st.nextToken();

				try
				{
					int radius = Integer.parseInt(val);

					for(L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
					{
						if(knownChar.isPlayer && !knownChar.equals(activeChar))
						{
							knownChar.stopAllEffects();
						}
					}

					activeChar.sendMessage("All effects canceled within raidus " + radius);
					st = null;
					val = null;

					return true;
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("Usage: //areacancel <radius>");
					st = null;
					val = null;

					return false;
				}
		}

		wordList = null;
		comm = null;
		commandEnum = null;
		st = null;

		return true;
	}

	public void showBuffs(L2PcInstance player, L2PcInstance activeChar)
	{
		TextBuilder html = new TextBuilder();

		html.append("<html><center><font color=\"LEVEL\">Effects of ").append(player.getName()).append("</font><center><br>");
		html.append("<table>");
		html.append("<tr><td width=200>Skill</td><td width=70>Action</td></tr>");

		L2Effect[] effects = player.getAllEffects();

		for(L2Effect e : effects)
		{
			if(e != null)
			{
				html.append("<tr><td>").append(e.getSkill().getName()).append("</td><td><button value=\"Remove\" action=\"bypass -h admin_stopbuff ").append(player.getName()).append(" ").append(String.valueOf(e.getSkill().getId())).append("\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			}
		}

		html.append("</table><br>");
		html.append("<button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs ").append(player.getName()).append("\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		html.append("</html>");

		NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setHtml(html.toString());

		activeChar.sendPacket(ms);

		html = null;
		ms = null;
		effects = null;
	}

	private void removeBuff(L2PcInstance remover, String playername, int SkillId)
	{
		L2PcInstance player = null;

		try
		{
			player = L2World.getInstance().getPlayer(playername);
		}
		catch(Exception e)
		{
			//ignore
		}

		if(player != null && SkillId > 0)
		{
			L2Effect[] effects = player.getAllEffects();

			for(L2Effect e : effects)
			{
				if(e != null && e.getSkill().getId() == SkillId)
				{
					e.exit();
					remover.sendMessage("Removed " + e.getSkill().getName() + " level " + e.getSkill().getLevel() + " from " + playername);
				}
			}
			showBuffs(player, remover);

			player = null;
			effects = null;
		}
	}

	private void removeAllBuffs(L2PcInstance remover, String playername)
	{
		L2PcInstance player = null;

		try
		{
			player = L2World.getInstance().getPlayer(playername);
		}
		catch(Exception e)
		{
			//ignore
		}

		if(player != null)
		{
			player.stopAllEffects();
			remover.sendMessage("Removed all effects from " + playername);
			showBuffs(player, remover);

			player = null;
		}
		else
		{
			remover.sendMessage("Can not remove effects from " + playername + ". Player appears offline.");
			showBuffs(player, remover);

			player = null;
		}
	}

}
