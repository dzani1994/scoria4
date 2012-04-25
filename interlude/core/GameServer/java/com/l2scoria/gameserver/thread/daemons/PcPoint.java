package com.l2scoria.gameserver.thread.daemons;

import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.random.Rnd;

/**
 * @author ProGramMoS
 */

public class PcPoint implements Runnable
{
	Logger _log = Logger.getLogger(PcPoint.class.getName());
	private static PcPoint _instance;

	public static PcPoint getInstance()
	{
		if(_instance == null)
		{
			_instance = new PcPoint();
		}

		return _instance;
	}

	private PcPoint()
	{
		_log.info("PcBang point event started.");
	}

	@Override
	public void run()
	{
		int score = 0;
		for(L2PcInstance activeChar: L2World.getInstance().getAllPlayers())
		{

			if(activeChar.getLevel() >= Config.PCB_MIN_LEVEL && !activeChar.isOffline() && !activeChar.isInStoreMode() && !activeChar.isInCraftMode())
			{
				score = Rnd.get(Config.PCB_POINT_MIN, Config.PCB_POINT_MAX);

				if(Rnd.get(100) <= Config.PCB_CHANCE_DUAL_POINT)
				{
					score *= 2;

					activeChar.addPcBangScore(score);

					SystemMessage sm = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_GOT_S1_GLASSES_PC);
					sm.addNumber(score);
					activeChar.sendPacket(sm);
					sm = null;

					activeChar.updatePcBangWnd(score, true, true);
				}
				else
				{
					activeChar.addPcBangScore(score);

					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_RECEVIED_S1_GLASSES_PC);
					sm.addNumber(score);
					activeChar.sendPacket(sm);
					sm = null;

					activeChar.updatePcBangWnd(score, true, false);
				}
			}

			activeChar = null;
		} 
	}
}
