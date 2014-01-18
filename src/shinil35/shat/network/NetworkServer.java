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
import java.net.InetSocketAddress;
import java.util.Collection;

import shinil35.shat.log.Log;
import shinil35.shat.network.packet.IPacket;

import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class NetworkServer
{
	private int id;

	private NetworkServerListener serverListener;
	private Server server;

	private int port;

	private boolean closed = false;

	public NetworkServer(int ID)
	{
		id = ID;

		server = new Server();
		serverListener = new NetworkServerListener(new Listener(), this);
		NetworkManager.registerPackets(server.getKryo());

		server.start();
		server.addListener(serverListener);
	}

	public void bind(int p)
	{
		port = p;

		try
		{
			server.bind(new InetSocketAddress(InetAddress.getByName("::0"), p), null);
		}
		catch (IOException e)
		{
			Log.localizedWarn("[NETWORK_LISTENING_FAILED]", e.getMessage());
			close();
		}
	}

	public void close()
	{
		if (closed)
			return;

		closed = true;

		if (serverListener != null && serverListener.getConnectionDatas() != null)
		{
			for (NetworkConnectionData cd : serverListener.getConnectionDatas().values())
				cd.close();
		}

		if (server != null)
			server.stop();

		NetworkManager.removeServer(id);

		id = 0;
	}

	public Collection<NetworkConnectionData> getConnectionDatas()
	{
		return serverListener.getConnectionDatas().values();
	}

	public int getPort()
	{
		return port;
	}

	public Server getServer()
	{
		return server;
	}

	public void requestPeerList()
	{
		if (closed)
			return;

		for (NetworkConnectionData data : serverListener.getConnectionDatas().values())
		{
			if (data == null)
				continue;

			data.requestPeerList();
		}
	}

	public void sendToAll(IPacket packet)
	{
		if (closed)
			return;

		for (NetworkConnectionData data : serverListener.getConnectionDatas().values())
		{
			if (data == null)
				continue;

			data.sendPacket(packet);
		}

		packet.dispose();
	}
}
