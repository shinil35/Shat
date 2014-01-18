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

import java.io.IOException;
import java.net.InetAddress;

import shinil35.shat.network.packet.IPacket;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;

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

		if (clientListener != null || clientListener.getConnectionData() != null)
			clientListener.getConnectionData().close();

		if (client != null)
			client.stop();

		NetworkManager.removeClient(id);

		id = 0;
	}

	public void connect(String IP, int port) throws IOException
	{
		connect(IP, port, 5000);
	}

	public void connect(String IP, int port, int timeout) throws IOException
	{
		if (closed || client == null)
			return;

		client.connect(timeout, InetAddress.getByName(IP), port);
		return;
	}

	public Client getClient()
	{
		return client;
	}

	public NetworkConnectionData getConnectionData()
	{
		return clientListener.getConnectionData();
	}

	public void requestPeerList()
	{
		if (closed)
			return;

		NetworkConnectionData data = clientListener.getConnectionData();

		if (data == null)
			return;

		data.requestPeerList();
	}

	public void sendPacket(IPacket packet)
	{
		if (closed)
			return;

		NetworkConnectionData data = clientListener.getConnectionData();

		if (data == null)
			return;

		data.sendPacket(packet);

		packet.dispose();
	}

}
