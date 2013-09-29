/*	Copyright (C) 2013 Emilio Cafè Nunes
 *
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package shinil35.shat.network;

import java.io.IOException;

import shinil35.shat.util.Utility;

import com.esotericsoftware.minlog.Log;

public class NetworkConnector implements Runnable
{
	private String ip;
	private int port;

	public NetworkConnector(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}

	@Override
	public void run()
	{
		if (!Utility.isValidAddress(ip, port))
			return;

		try
		{
			NetworkManager.waitForConnection(ip, port);
		}
		catch (IOException e)
		{
			Log.trace("Connessione a \"[" + ip + "]:" + port + "\" fallita");
		}
	}
}
