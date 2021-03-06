package com.l2scoria.gameserver.mode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import java.sql.Connection;
import com.l2scoria.util.database.L2DatabaseFactory;

public class EventPoint
{
	private final L2PcInstance _activeChar;
	private Integer _points = 0;

	public EventPoint(L2PcInstance player)
	{
		_activeChar = player;
		loadFromDB();
	}

	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}

	public void savePoints()
	{
		saveToDb();
	}

	private void loadFromDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("Select * From char_points where charId = ?");
			st.setInt(1, getActiveChar().getObjectId());
			ResultSet rst = st.executeQuery();

			while(rst.next())
			{
				_points = rst.getInt("points");
			}

			rst.close();
			st.close();
		}
		catch(Exception ex)
		{
			//null
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	private void saveToDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("Update char_points Set points = ? Where charId = ?");
			st.setInt(1, _points);
			st.setInt(2, getActiveChar().getObjectId());
			st.execute();
			st.close();
		}
		catch(Exception ex)
		{
			//null
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	public Integer getPoints()
	{
		return _points;
	}

	public void setPoints(Integer points)
	{
		_points = points;
	}

	public void addPoints(Integer points)
	{
		_points += points;
	}

	public void removePoints(Integer points)
	{
		//Don't know , do the calc or return. it's up to you
		if(_points - points < 0)
			return;

		_points -= points;
	}

	public boolean canSpend(Integer value)
	{
		return _points - value >= 0;
	}

}
