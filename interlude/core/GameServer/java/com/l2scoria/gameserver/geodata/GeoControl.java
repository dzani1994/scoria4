package com.l2scoria.gameserver.geodata;

import com.l2scoria.gameserver.model.L2Territory;

import java.util.HashMap;

public interface GeoControl
{
	public abstract L2Territory getGeoPos();

	public abstract void setGeoPos(L2Territory value);

	public abstract HashMap<Long, Byte> getGeoAround();

	public abstract void setGeoAround(HashMap<Long, Byte> value);

	public abstract boolean isGeoCloser();
}
