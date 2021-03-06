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
package com.l2scoria.telnet;

import com.l2scoria.ServerType;
import com.l2scoria.gameserver.services.FService;
import com.l2scoria.util.random.Rnd;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

public class Status extends Thread
{
	protected static final Logger _log = Logger.getLogger(Status.class.getName());

	private ServerSocket statusServerSocket;

	private int _uptime;
	private int _statusPort;
	private String _statusPw;
	private int _mode;
	private boolean _superPass;
	private int _lengthPass;

	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Socket connection = statusServerSocket.accept();

				if(_mode == ServerType.MODE_GAMESERVER)
				{
					new GameTelnetThread(connection, _uptime, _statusPw);
				}
				if(isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch(IOException io)
					{
						io.printStackTrace();
					}
					break;
				}

				connection = null;
			}
			catch(IOException e)
			{
				if(isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch(IOException io)
					{
						io.printStackTrace();
					}
					break;
				}
			}
		}
	}

	public Status(int mode) throws IOException
	{
		super("Status");
		_mode = mode;
		Properties telnetSettings = new Properties();
		InputStream is = new FileInputStream(new File(FService.TELNET_FILE));
		telnetSettings.load(is);
		is.close();

		_statusPort = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
		_statusPw = telnetSettings.getProperty("StatusPW");

		_lengthPass = Integer.parseInt(telnetSettings.getProperty("LengthPass", "10"));

		if(_mode == ServerType.MODE_GAMESERVER)
		{
			if(_statusPw == null)
			{
				_log.info("Server's Telnet Function Has No Password Defined!");
				_log.info("A Password Has Been Automaticly Created!");
				_statusPw = rndPW(_lengthPass);
				_log.info("Password Has Been Set To: " + _statusPw);
			}
			_log.info("StatusServer Started! - Listening on Port: " + _statusPort);
			_log.info("Password Has Been Set To: " + _statusPw);
		}
		else
		{
			_log.info("StatusServer Started! - Listening on Port: " + _statusPort);
			_log.info("Password Has Been Set To: " + _statusPw);
		}
		statusServerSocket = new ServerSocket(_statusPort);

		_superPass = Boolean.parseBoolean(telnetSettings.getProperty("SuperPassword", "False"));

		_uptime = (int) System.currentTimeMillis();
	}

	private String rndPW(int length)
	{
		TextBuilder password = new TextBuilder();

		String lowerChar = "qwertyuiopasdfghjklzxcvbnm";
		String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
		String digits = "1234567890";
		String symvols = "!@#$%^&*()";

		int charSet;

		for(int i = 0; i < length; i++)
		{
			if(_superPass)
			{
				charSet = Rnd.nextInt(4);
			}
			else
			{
				charSet = Rnd.nextInt(3);
			}

			switch(charSet)
			{
				case 0:
					password.append(lowerChar.charAt(Rnd.nextInt(lowerChar.length() - 1)));
					break;
				case 1:
					password.append(upperChar.charAt(Rnd.nextInt(upperChar.length() - 1)));
					break;
				case 2:
					password.append(digits.charAt(Rnd.nextInt(digits.length() - 1)));
					break;
				case 3:
					password.append(symvols.charAt(Rnd.nextInt(symvols.length() - 1)));
					break;
			}
		}

		lowerChar = null;
		upperChar = null;
		digits = null;
		symvols = null;

		return password.toString();
	}

}
