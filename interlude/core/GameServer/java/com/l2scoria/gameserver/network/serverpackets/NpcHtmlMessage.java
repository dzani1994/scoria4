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
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.Config;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.clientpackets.RequestBypassToServer;
import org.apache.log4j.Logger;

/**
 * The HTML parser in the client knowns these standard and non-standard tags and attributes<br>
 * <li>VOLUMN<br> <li>UNKNOWN<br> <li>UL<br> <li>U<br> <li>TT<br> <li>TR<br> <li>TITLE<br> <li>TEXTCODE<br> <li>TEXTAREA
 * <br> <li>TD<br> <li>TABLE<br> <li>SUP<br> <li>SUB<br> <li>STRIKE<br> <li>SPIN<br> <li>SELECT<br> <li>RIGHT<br> <li>
 * PRE<br> <li>P<br> <li>OPTION<br> <li>OL<br> <li>MULTIEDIT<br> <li>LI<br> <li>LEFT<br> <li>INPUT<br> <li>IMG<br> <li>I
 * <br> <li>HTML<br> <li>H7<br> <li>H6<br> <li>H5<br> <li>H4<br> <li>H3<br> <li>H2<br> <li>H1<br> <li>FONT<br> <li>
 * EXTEND<br> <li>EDIT<br> <li>COMMENT<br> <li>COMBOBOX<br> <li>CENTER<br> <li>BUTTON<br> <li>BR<br> <li>BODY<br> <li>
 * BAR<br> <li>ADDRESS<br> <li>A<br> <li>SEL<br> <li>LIST<br> <li>VAR<br> <li>FORE<br> <li>READONL<br> <li>ROWS<br> <li>
 * VALIGN<br> <li>FIXWIDTH<br> <li>BORDERCOLORLI<br> <li>BORDERCOLORDA<br> <li>BORDERCOLOR<br> <li>BORDER<br> <li>
 * BGCOLOR<br> <li>BACKGROUND<br> <li>ALIGN<br> <li>VALU<br> <li>READONLY<br> <li>MULTIPLE<br> <li>SELECTED<br> <li>TYP
 * <br> <li>TYPE<br> <li>MAXLENGTH<br> <li>CHECKED<br> <li>SRC<br> <li>Y<br> <li>X<br> <li>QUERYDELAY<br> <li>
 * NOSCROLLBAR<br> <li>IMGSRC<br> <li>B<br> <li>FG<br> <li>SIZE<br> <li>FACE<br> <li>COLOR<br> <li>DEFFON<br> <li>
 * DEFFIXEDFONT<br> <li>WIDTH<br> <li>VALUE<br> <li>TOOLTIP<br> <li>NAME<br> <li>MIN<br> <li>MAX<br> <li>HEIGHT<br> <li>
 * DISABLED<br> <li>ALIGN<br> <li>MSG<br> <li>LINK<br> <li>HREF<br> <li>ACTION<br>
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class NpcHtmlMessage extends L2GameServerPacket
{
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	//
	private static final String _S__1B_NPCHTMLMESSAGE = "[S] 0f NpcHtmlMessage";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	private int _npcObjId;
	private String _html;

	/**
	 * @param _characters
	 */
	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		setHtml(text);
	}

	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}

	@Override
	public void runImpl()
	{
		if(Config.BYPASS_VALIDATION)
		{
			buildBypassCache(getClient().getActiveChar());
		}
	}

	public void setHtml(String text)
	{
		_html = text;
	}

	public boolean setFile(String path)
	{
		String content = HtmCache.getInstance().getHtm(path);

		if(content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			_log.warn("missing html page " + path);
			return false;
		}

		setHtml(content);
		return true;
	}

	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}

	public void replace(String pattern, int value)
	{
		_html = _html.replaceAll(pattern, Integer.toString(value));
	}

	private void buildBypassCache(L2PcInstance activeChar)
	{
		if(activeChar == null)
			return;

		activeChar.clearBypass();
		int len = _html.length();
		for(int i = 0; i < len; i++)
		{
			int start = _html.indexOf("bypass -h", i);
			int finish = _html.indexOf("\"", start);

			if(start < 0 || finish < 0)
			{
				break;
			}

			start += 10;
			i = start;
			int finish2 = _html.indexOf("$", start);
			if(finish2 < finish && finish2 > 0)
			{
				activeChar.addBypass2(_html.substring(start, finish2));
			}
			else
			{
				activeChar.addBypass(_html.substring(start, finish));
				//System.err.println("["+_html.substring(start, finish)+"]");
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x0f);

		writeD(_npcObjId);
		if(_html.length() > 8192)
		{
			_log.warn("Html is too long! this will crash the client!");
			_html = "<html><body>Html was too long,<br>Try another time.</body></html>";
		}
		else
		{
			writeS(_html);
		}
		writeD(0x00);
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__1B_NPCHTMLMESSAGE;
	}

}
