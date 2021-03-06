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

import com.l2scoria.Config;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.communitybbs.CommunityBoard;
import com.l2scoria.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2scoria.gameserver.handler.AdminCommandHandler;
import com.l2scoria.gameserver.handler.admin.impl.AdminAbst;
import com.l2scoria.gameserver.handler.custom.CustomBypassHandler;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2scoria.gameserver.model.actor.instance.L2EventNpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SymbolMakerInstance;
import com.l2scoria.gameserver.model.actor.position.L2CharPosition;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.entity.event.TvT.TvT;
import com.l2scoria.gameserver.model.entity.event.LastHero.LastHero;
import com.l2scoria.gameserver.model.entity.event.CTF.CTF;
import com.l2scoria.gameserver.model.entity.event.DeathMatch.DeathMatch;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.util.FloodProtector;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.12.4.5 $ $Date: 2009/04/13 09:41:11 $
 */
public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__21_REQUESTBYPASSTOSERVER = "[C] 21 RequestBypassToServer";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());

	// S
	private String _command;

	/**
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_BYPASS))
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}
                
                if(activeChar.isDead()) {
                    activeChar.sendPacket(new ActionFailed());
                    return;
                }

		try
		{
			if(_command.startsWith("admin_"))
			{
				String command;

				if(_command.contains(" "))
				{
					command = _command.substring(0, _command.indexOf(" "));
				}
				else
				{
					command = _command;
				}

				AdminAbst ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);

				if(ach == null)
				{
					if(activeChar.isGM())
					{
						activeChar.sendMessage("The command " + command + " does not exists!");
					}

					_log.warn("No handler registered for admin command '" + command + "'");
					return;
				}

				if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access right to use this command!");
					if(Config.DEBUG)
					{
						_log.warn("Character " + activeChar.getName() + " tried to use admin command " + command + ", but doesn't have access to it!");
					}
					return;
				}

				ach.useAdminCommand(_command, activeChar);
			}
			else if(_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if(_command.startsWith("player_help "))
			{
				playerHelp(activeChar, _command.substring(12));
			}
			else if(_command.startsWith("npc_"))
			{
				if(!activeChar.validateBypass(_command))
					return;

				int endOfId = _command.indexOf('_', 5);
				String id;

				if(endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}

				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

					if((Config.ALLOW_CLASS_MASTERS && Config.ALLOW_REMOTE_CLASS_MASTERS && object instanceof L2ClassMasterInstance)
						|| (object.isNpc && endOfId > 0 && activeChar.isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false)))
					{
						((L2NpcInstance) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}

					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch(NumberFormatException nfe)
				{
					//null
				}
			}
			//	Draw a Symbol
			else if(_command.equals("Draw"))
			{
				L2Object object = activeChar.getTarget();
				if(object.isNpc)
				{
					((L2SymbolMakerInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			else if(_command.equals("RemoveList"))
			{
				L2Object object = activeChar.getTarget();
				if(object.isNpc)
				{
					((L2SymbolMakerInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			else if(_command.equals("Remove "))
			{
				L2Object object = activeChar.getTarget();

				if(object.isNpc)
				{
					((L2SymbolMakerInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			// Navigate throught Manor windows
			else if(_command.startsWith("manor_menu_select?"))
			{
				L2Object object = activeChar.getTarget();
				if(object.isNpc)
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			else if(_command.startsWith("bbs_"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if(_command.startsWith("_bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if(_command.startsWith("Quest "))
			{
				if(!activeChar.validateBypass(_command))
					return;

				L2PcInstance player = getClient().getActiveChar();
				if(player == null)
					return;

				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');

				if(idx < 0)
				{
					player.processQuestEvent(p, "");
				}
				else
				{
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
				}
			}
                        else if(_command.startsWith("Customevent "))
                        {
                            String evName = _command.substring(12);
                            if(evName != null && evName.length() > 1)
                            {
                                L2PcInstance player = getClient().getActiveChar();
                                if(evName.equals("tvtjoin"))
                                {
                                    TvT.getInstance().register(player);
                                }
                                else if(evName.equals("tvtinfo"))
                                {
                                    TvT.getInstance().showNpcInfo(player);
                                }
                                else if(evName.equals("lhjoin"))
                                {
                                    LastHero.getInstance().register(player);
                                }
                                else if(evName.equals("lhinfo"))
                                {
                                    LastHero.getInstance().showNpcInfo(player);
                                }
                                else if(evName.equals("dmjoin"))
                                {
                                    DeathMatch.getInstance().register(player);
                                }
                                else if(evName.equals("dminfo"))
                                {
                                    DeathMatch.getInstance().showNpcInfo(player);
                                }
                                else if(evName.equals("ctfjoin"))
                                {
                                    CTF.getInstance().register(player);
                                }
                                else if(evName.equals("ctfinfo"))
                                {
                                    CTF.getInstance().showNpcInfo(player);
                                }
                                else if(evName.equals("eventleave"))
                                {
                                    TvT.getInstance().doLeave(player);
                                    CTF.getInstance().doLeave(player);
                                    DeathMatch.getInstance().doLeave(player);
                                    LastHero.getInstance().doLeave(player);
                                }
                            }
                        }
			else if (_command.startsWith("OlympiadArenaChange"))
				Olympiad.getInstance().bypassChangeArena(_command, activeChar);

			// Jstar's Custom Bypass Caller!
			else if(_command.startsWith("custom_"))
			{
				L2PcInstance player = getClient().getActiveChar();
				CustomBypassHandler.getInstance().handleBypass(player, _command);
			}
		}
		catch(Exception e)
		{
			_log.warn("Bad RequestBypassToServer: ", e);
		}
		//		finally
		//		{
		//			activeChar.clearBypass();
		//		}
	}

	/**
	 * @param client
	 */
	private void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if(obj == null)
			return;

		if(obj.isNpc)
		{
			L2NpcInstance temp = (L2NpcInstance) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
			//			temp.moveTo(player.getX(),player.getY(), player.getZ(), 0 );
		}

	}

	private void playerHelp(L2PcInstance activeChar, String path)
	{
		if(path.contains(".."))
			return;

		String filename = "data/html/help/" + path;
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__21_REQUESTBYPASSTOSERVER;
	}
}
