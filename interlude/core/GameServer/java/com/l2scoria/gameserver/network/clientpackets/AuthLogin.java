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
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.thread.LoginServerThread;
import com.l2scoria.gameserver.thread.LoginServerThread.SessionKey;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.3.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AuthLogin extends L2GameClientPacket
{
	private static final String _C__08_AUTHLOGIN = "[C] 08 AuthLogin";
	private static Logger _log = Logger.getLogger(AuthLogin.class.getName());

	// loginName + keys must match what the loginserver used.
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;

	/**
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}

	@Override
	protected void runImpl()
	{
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);

		if(Config.DEBUG)
		{
			_log.info("user:" + _loginName);
			_log.info("key:" + key);
            _log.info("We have account name. Add to game server:" + _loginName);
		}

		L2GameClient client = getClient();

		// avoid potential exploits
		if(client.getAccountName() == null)
		{
			client.setAccountName(_loginName);
			LoginServerThread.getInstance().addGameServerLogin(_loginName, client);
			LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client, key);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__08_AUTHLOGIN;
	}
}
