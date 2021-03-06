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
package mmo;

import java.nio.ByteOrder;


/**
 * @author ProGramMoS
 */

public class SelectorConfig
{
    private int READ_BUFFER_SIZE = Config.getInstance().NETWORK_READ_BUFFER_SIZE * 1024;
    private int WRITE_BUFFER_SIZE = Config.getInstance().NETWORK_WRITE_BUFFER_SIZE * 1024;
    private int HELPER_BUFFER_COUNT = Config.getInstance().NETWORK_HELPER_BUFFER_COUNT;

    private ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private HeaderSize HEADER_TYPE = HeaderSize.SHORT_HEADER;

    /**
     * BYTE_HEADER: unsigned byte, max size: 255 <BR>
     * SHORT_HEADER: unsigned short, max size: 65535<BR>
     * INT_HEADER: signed integer, max size: Integer.MAX_SIZE<BR>
     */
    public static enum HeaderSize
    { 
        BYTE_HEADER,
        SHORT_HEADER,
        INT_HEADER,
    }

    public SelectorConfig()
    { }

    public int getReadBufferSize()
    {
        return READ_BUFFER_SIZE;
    }

    public int getWriteBufferSize()
    {
        return WRITE_BUFFER_SIZE;
    }

    public int getHelperBufferSize()
    {
        return Math.max(READ_BUFFER_SIZE, WRITE_BUFFER_SIZE);
    }

    public int getHelperBufferCount()
    {
        return HELPER_BUFFER_COUNT;
    }

    public ByteOrder getByteOrder()
    {
        return BYTE_ORDER;
    }

    public HeaderSize getHeaderType()
    {
        return HEADER_TYPE;
    }
}