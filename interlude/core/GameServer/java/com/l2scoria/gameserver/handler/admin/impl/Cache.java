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
import com.l2scoria.gameserver.cache.CrestCache;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

import java.io.File;

/**
 * @author Akumu, ProGramMoS
 * @version $Revision: 1.1 $
 */
public class Cache extends AdminAbst
{
	public Cache()
	{
		_commands = new String[]{"admin_cache_htm_rebuild", "admin_cache_htm_reload", "admin_cache_reload_path", "admin_cache_reload_file", "admin_cache_crest_rebuild", "admin_cache_crest_reload", "admin_cache_crest_fix"};
	}

	private enum CommandEnum
	{
		admin_cache_htm_rebuild,
		admin_cache_htm_reload,
		admin_cache_reload_path,
		admin_cache_reload_file,
		admin_cache_crest_rebuild,
		admin_cache_crest_reload,
		admin_cache_crest_fix
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!super.useAdminCommand(command, activeChar))
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
			case admin_cache_htm_reload:
				HtmCache.getInstance().reload(Config.DATAPACK_ROOT);
				activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB on " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
				break;

			case admin_cache_reload_path:
				try
				{
					String path = command.split(" ")[1];
					HtmCache.getInstance().reloadPath(new File(Config.DATAPACK_ROOT, path));
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB in " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
					path = null;
				} catch (Exception e)
				{
					activeChar.sendMessage("Usage: //cache_reload_path <path>");
				}
				break;

			case admin_cache_reload_file:
				try
				{
					String path = command.split(" ")[1];
					if (HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT, path)) != null)
					{
						activeChar.sendMessage("Cache[HTML]: file was loaded");
						path = null;
					}
					else
					{
						activeChar.sendMessage("Cache[HTML]: file can't be loaded");
						path = null;
					}
				} catch (Exception e)
				{
					activeChar.sendMessage("Usage: //cache_reload_file <relative_path/file>");
				}
				break;

			case admin_cache_crest_rebuild:
				CrestCache.getInstance().reload();
				activeChar.sendMessage("Cache[Crest]: " + String.format("%.3f", CrestCache.getInstance().getMemoryUsage()) + " megabytes on " + CrestCache.getInstance().getLoadedFiles() + " files loaded");
				break;

			case admin_cache_crest_fix:
				CrestCache.getInstance().convertOldPedgeFiles();
				activeChar.sendMessage("Cache[Crest]: crests fixed");
				break;
		}

		wordList = null;
		comm = null;
		commandEnum = null;

		return true;
	}
}
