/*	Copyright (C) 2013 Emilio Cafe' Nunes
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

import shinil35.shat.network.packet.P5_Message;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class NetworkClient
{
	private int id;

	private Client client;
	private NetworkClientListener clientListener;

	private boolean closed = false;

	public NetworkClient(int ID)
	{
		id = ID;

		client = new Client();
		clientListener = new NetworkClientListener(new Listener(), this);
		NetworkManager.registerPackets(client.getKryo());
		client.addListener(clientListener);
		client.start();
	}

	public void close()
	{
		if (closed)
			return;

		closed = true;

		if (client != null)
			client.close();

		NetworkManager.removeClient(id);

		id = 0;
	}

	public void connect(String IP, int port)
	{
		connect(IP, port, 5000);
	}

	public void connect(String IP, int port, int timeout)
	{
		if (closed || client == null)
			return;

		try
		{
			client.connect(timeout, IP, port);
			return;
		}
		catch (Exception e)
		{
			Log.localizedWarn("[NETWORK_CONNECTION_FAILED]", e.getMessage());
			close();
		}
	}

	public Client getClient()
	{
		return client;
	}

	public void sendMessage(P5_Message packet)
	{
		if (closed || !clientListener.getConnectionData().getHandshakeStatus()
				|| clientListener.getConnectionData().messageWasSended(packet.getMessageHash()))
			return;

		client.sendTCP(packet);
	}

}
