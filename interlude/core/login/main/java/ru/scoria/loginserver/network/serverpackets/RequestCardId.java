package ru.scoria.loginserver.network.serverpackets;


import ru.scoria.loginserver.L2LoginClient;

public class RequestCardId extends L2LoginServerPacket {

	@Override
	protected void write(L2LoginClient client) {
		writeC(0xa);
//		writeD(10001);
		writeC(client._CardNo);
//		writeC(0xfe);
	}

}
