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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import shinil35.shat.Configuration;
import shinil35.shat.Language;
import shinil35.shat.Main;
import shinil35.shat.network.packet.IPacket;
import shinil35.shat.network.packet.P0_PublicKey;
import shinil35.shat.network.packet.P1_KeyVerifier;
import shinil35.shat.network.packet.P2_KeyVerifier;
import shinil35.shat.network.packet.P3_PeerListRequest;
import shinil35.shat.network.packet.P4_PeerListResponse;
import shinil35.shat.network.packet.P5_Message;
import shinil35.shat.peer.PeerData;
import shinil35.shat.util.Utility;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;

public class NetworkManager
{
	private static boolean listeningEnabled;
	private static int listeningPort;

	private static boolean bootstrapEnabled;
	private static String bootstrapIP;
	private static int bootstrapPort;

	private static ConcurrentHashMap<Integer, NetworkServer> servers;
	private static ConcurrentHashMap<Integer, NetworkClient> clients;

	private static boolean initialized = false;

	public static void close()
	{
		if (!initialized)
			return;

		initialized = false;

		if (servers != null)
		{
			for (NetworkServer s : servers.values())
				s.close();

			servers.clear();
		}

		if (clients != null)
		{
			for (NetworkClient c : clients.values())
				c.close();

			clients.clear();
		}
	}

	public static void delayedConnection(String IP, int port)
	{
		if (!Utility.isValidAddress(IP, port))
			return;

		NetworkConnector con = new NetworkConnector(IP, port);
		Main.executeTask(con);
	}

	public static void init()
	{
		servers = new ConcurrentHashMap<Integer, NetworkServer>();
		clients = new ConcurrentHashMap<Integer, NetworkClient>();

		initialized = true;

		listeningEnabled = Configuration.getProperty("network.listening.enabled").equals("true")
				|| Configuration.getProperty("network.listening.enabled").equals("1");
		listeningPort = Integer.parseInt(Configuration.getProperty("network.listening.port"));

		bootstrapEnabled = Configuration.getProperty("network.bootstrap.enabled").equals("true")
				|| Configuration.getProperty("network.bootstrap.enabled").equals("1");
		bootstrapIP = Configuration.getProperty("network.bootstrap.host");
		bootstrapPort = Integer.parseInt(Configuration.getProperty("network.bootstrap.port"));

		if (listeningEnabled && (listeningPort < 0 || listeningPort > 65535))
		{
			Log.localizedWarn("[NETWORK_PORT_INVALID]");
			listeningEnabled = false;
		}

		if (bootstrapEnabled && (bootstrapPort < 0 || bootstrapPort > 65535 || bootstrapIP.equals("")))
		{
			Log.localizedWarn("[NETWORK_BOOTSTRAP_INVALID]");
			System.out.println(Language.getLocalizedText("[NETWORK_BOOTSTRAP_INVALID]"));
			bootstrapEnabled = false;
		}

		if (listeningEnabled)
			newServer(listeningPort);

		if (bootstrapEnabled)
			delayedConnection(bootstrapIP, bootstrapPort);
	}

	public static void newServer(int bindPort)
	{
		if (!initialized)
			return;

		int id = 0;

		NetworkServer s;

		synchronized (servers)
		{
			boolean exists = true;

			while (exists)
			{
				exists = false;

				for (int i : servers.keySet())
				{
					if (i == id)
					{
						exists = true;
						id++;
						break;
					}
				}
			}

			s = new NetworkServer(id);
			servers.put(id, s);
		}

		s.bind(bindPort);
	}

	public static void registerPackets(Kryo packets)
	{
		packets.register(byte[].class);

		packets.register(PublicKey.class);

		packets.register(String.class);
		packets.register(ArrayList.class);
		packets.register(PeerData.class);

		packets.register(P0_PublicKey.class);
		packets.register(P1_KeyVerifier.class);
		packets.register(P2_KeyVerifier.class);
		packets.register(P3_PeerListRequest.class);
		packets.register(P4_PeerListResponse.class);
		packets.register(P5_Message.class);
	}

	public static void removeClient(int id)
	{
		if (!initialized)
			return;

		if (clients.containsKey(id))
		{
			NetworkClient c = clients.get(id);

			if (c != null)
				c.close();

			clients.remove(id);
		}
	}

	public static void removeClient(NetworkClient c)
	{
		if (!initialized || c == null)
			return;

		c.close();

		if (clients.contains(c))
			clients.remove(c);
	}

	public static void removeServer(int id)
	{
		if (!initialized)
			return;

		if (servers.containsKey(id))
		{
			NetworkServer s = servers.get(id);

			if (s != null)
				s.close();

			servers.remove(id);
		}
	}

	public static void removeServer(NetworkServer s)
	{
		if (!initialized || s == null)
			return;

		s.close();

		if (servers.contains(s))
			servers.remove(s);
	}

	public static void requestPeerList()
	{
		if (!initialized)
			return;

		for (NetworkServer s : servers.values())
			s.requestPeerList();

		for (NetworkClient c : clients.values())
			c.requestPeerList();
	}

	public static void sendToAll(IPacket packet)
	{
		if (!initialized)
			return;

		for (NetworkServer s : servers.values())
			s.sendToAll(packet);

		for (NetworkClient c : clients.values())
			c.sendPacket(packet);
	}

	public static void waitForConnection(String IP, int port) throws IOException // ATTENTION: Thread will stop for connection! (Max 5 seconds)
	{
		if (!initialized || !Utility.isValidAddress(IP, port))
			return;

		int id = 0;

		NetworkClient c;

		synchronized (clients)
		{
			boolean exists = true;

			while (exists)
			{
				exists = false;

				for (int i : clients.keySet())
				{
					if (i == id)
					{
						exists = true;
						id++;
						break;
					}
				}
			}

			c = new NetworkClient(id);
			clients.put(id, c);
		}

		c.connect(IP, port);
	}
}
