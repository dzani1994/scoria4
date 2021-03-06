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
import com.l2scoria.gameserver.instancemanager.InstanceManager;
import com.l2scoria.gameserver.managers.QuestManager;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Instance;
import com.l2scoria.gameserver.model.quest.Quest;
import com.l2scoria.gameserver.model.quest.QuestState;
import com.l2scoria.gameserver.network.serverpackets.QuestList;
import org.apache.log4j.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestQuestAbort extends L2GameClientPacket
{
	private static final String _C__64_REQUESTQUESTABORT = "[C] 64 RequestQuestAbort";
	private static Logger _log = Logger.getLogger(RequestQuestAbort.class.getName());

	private int _questId;

	@Override
	protected void readImpl()
	{
		_questId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		Quest qe;
		QuestState qs;
		switch (_questId)
		{
			case 247:
				qe = QuestManager.getInstance().getQuest(246);
				if (qe != null)
				{
					qs = activeChar.getQuestState(qe.getName());
					if (qs != null)
					{
						Quest.deleteQuestInDb(qs);
					}
				}
			case 246:
				qe = QuestManager.getInstance().getQuest(242);
				if (qe != null)
				{
					qs = activeChar.getQuestState(qe.getName());
					if (qs != null)
					{
						Quest.deleteQuestInDb(qs);
					}
				}
			case 242:
				qe = QuestManager.getInstance().getQuest(241);
				if (qe != null)
				{
					qs = activeChar.getQuestState(qe.getName());
					if (qs != null)
					{
						Quest.deleteQuestInDb(qs);
					}
				}
		}

		qe = QuestManager.getInstance().getQuest(_questId);
		if (qe != null)
		{
			qs = activeChar.getQuestState(qe.getName());
			if (qs != null)
			{
				if (qs.getPlayer().getInstanceId() > 0)
				{
					Instance plInst = InstanceManager.getInstance().getInstance(qs.getPlayer().getInstanceId());
					if (plInst.isQuestStarter(qs.getQuest().getQuestIntId()))
					{
						plInst.ejectPlayer(qs.getPlayer().getObjectId());
					}
				}

				qs.exitQuest(true);
				activeChar.sendMessage("Квест отменен.");
				activeChar.sendPacket(new QuestList());
			}
			else
			{
				if (Config.DEBUG)
				{
					_log.info("Player '" + activeChar.getName() + "' try to abort quest " + qe.getName() + " but he didn't have it started.");
				}
			}
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.warn("Quest (id='" + _questId + "') not found.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__64_REQUESTQUESTABORT;
	}
}
