/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2scoria.gameserver.network;

/**
 * @author l2scoria
 */
public class GameCrypt
{
	private final byte[] _inKey = new byte[16];
	private final byte[] _outKey = new byte[16];
	private boolean _isEnabled;

	private com.lameguard.crypt.GameCrypt _lameCrypt;
	public static boolean _ISLAME;

	public GameCrypt()
	{
		if(_ISLAME)
			_lameCrypt = new com.lameguard.crypt.GameCrypt();
	}

	public static void decrypt(final byte[] raw, final int offset, final int size, final GameCrypt gcrypt)
	{
		if(!gcrypt._isEnabled)
			return;

		int temp = 0;

		for(int i = 0; i < size; i++)
		{
			final int temp2 = raw[offset + i] & 0xFF;

			raw[offset + i] = (byte) (temp2 ^ gcrypt._inKey[i & 15] ^ temp);
			temp = temp2;
		}

		int old = gcrypt._inKey[8] & 0xff;
		old |= gcrypt._inKey[9] << 8 & 0xff00;
		old |= gcrypt._inKey[10] << 0x10 & 0xff0000;
		old |= gcrypt._inKey[11] << 0x18 & 0xff000000;

		old += size;

		gcrypt._inKey[8] = (byte) (old & 0xff);
		gcrypt._inKey[9] = (byte) (old >> 0x08 & 0xff);
		gcrypt._inKey[10] = (byte) (old >> 0x10 & 0xff);
		gcrypt._inKey[11] = (byte) (old >> 0x18 & 0xff);
	}

	public void decrypt(final byte[] raw, final int offset, final int size)
	{
		if(_ISLAME)
		{
			_lameCrypt.decrypt(raw, offset, size);
			return;
		}

		if(!_isEnabled)
			return;

		int temp = 0;

		for(int i = 0; i < size; i++)
		{
			final int temp2 = raw[offset + i] & 0xFF;

			raw[offset + i] = (byte) (temp2 ^ _inKey[i & 15] ^ temp);
			temp = temp2;
		}

		int old = _inKey[8] & 0xff;
		old |= _inKey[9] << 8 & 0xff00;
		old |= _inKey[10] << 0x10 & 0xff0000;
		old |= _inKey[11] << 0x18 & 0xff000000;

		old += size;

		_inKey[8] = (byte) (old & 0xff);
		_inKey[9] = (byte) (old >> 0x08 & 0xff);
		_inKey[10] = (byte) (old >> 0x10 & 0xff);
		_inKey[11] = (byte) (old >> 0x18 & 0xff);
	}

	public static void encrypt(final byte[] raw, final int offset, final int size, final GameCrypt gcrypt)
	{
		if(!gcrypt._isEnabled)
		{
			gcrypt._isEnabled = true;
			return;
		}

		int temp = 0;

		for(int i = 0; i < size; i++)
		{
			final int temp2 = raw[offset + i] & 0xFF;

			temp = temp2 ^ gcrypt._outKey[i & 15] ^ temp;
			raw[offset + i] = (byte) temp;
		}

		int old = gcrypt._outKey[8] & 0xff;

		old |= gcrypt._outKey[9] << 8 & 0xff00;
		old |= gcrypt._outKey[10] << 0x10 & 0xff0000;
		old |= gcrypt._outKey[11] << 0x18 & 0xff000000;

		old += size;

		gcrypt._outKey[8] = (byte) (old & 0xff);
		gcrypt._outKey[9] = (byte) (old >> 0x08 & 0xff);
		gcrypt._outKey[10] = (byte) (old >> 0x10 & 0xff);
		gcrypt._outKey[11] = (byte) (old >> 0x18 & 0xff);
	}

	public void encrypt(final byte[] raw, final int offset, final int size)
	{
		if(_ISLAME)
		{
			_lameCrypt.encrypt(raw, offset, size);
			return;
		}

		if(!_isEnabled)
		{
			_isEnabled = true;
			return;
		}

		int temp = 0;

		for(int i = 0; i < size; i++)
		{
			final int temp2 = raw[offset + i] & 0xFF;

			temp = temp2 ^ _outKey[i & 15] ^ temp;
			raw[offset + i] = (byte) temp;
		}

		int old = _outKey[8] & 0xff;

		old |= _outKey[9] << 8 & 0xff00;
		old |= _outKey[10] << 0x10 & 0xff0000;
		old |= _outKey[11] << 0x18 & 0xff000000;

		old += size;

		_outKey[8] = (byte) (old & 0xff);
		_outKey[9] = (byte) (old >> 0x08 & 0xff);
		_outKey[10] = (byte) (old >> 0x10 & 0xff);
		_outKey[11] = (byte) (old >> 0x18 & 0xff);
	}

	public static void setKey(final byte[] key, final GameCrypt gcrypt)
	{
		System.arraycopy(key, 0, gcrypt._inKey, 0, 16);
		System.arraycopy(key, 0, gcrypt._outKey, 0, 16);
	}

	public void setKey(final byte[] key)
	{
		if(_ISLAME)
		{
			_lameCrypt.setKey(key);
			return;
		}
		System.arraycopy(key, 0, _inKey, 0, 16);
		System.arraycopy(key, 0, _outKey, 0, 16);
	}
}
