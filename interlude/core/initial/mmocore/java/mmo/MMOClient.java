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

import java.nio.ByteBuffer;

/**
 * @author ProGramMoS
 */

public abstract class MMOClient<T extends MMOConnection>
{
    private T _connection;
    public boolean _enableAdvCrypt = false;
    private int _key = 0;
    public int _newKey = 0;
    @SuppressWarnings("unchecked")
    public MMOClient(T con)
    {
        this.setConnection(con);
        if(con != null)
            con.setClient(this);
    }
    
    public int getKey() {
    	return _key;
    }
    public void setKey(int val ){
    	if(_enableAdvCrypt && val!=_key) {
    		_key =val;
    	}
    }
    protected void setConnection(T con)
    {
        _connection = con;
    }
    
    public T getConnection()
    {
        return _connection;
    }
    
    public void closeNow()
    {
        if(this.getConnection() != null)
            this.getConnection().closeNow();
    }
    
    public void closeLater()
    {
        if(this.getConnection() != null)
            this.getConnection().closeLater();
    }
    
    public abstract boolean decrypt(ByteBuffer buf, int size);
    
    public abstract boolean encrypt(ByteBuffer buf, int size);
    
    protected void onDisconection()
    {
    	//null
    }
    
    protected void onForcedDisconnection()
    {
    	//null
    }
}
