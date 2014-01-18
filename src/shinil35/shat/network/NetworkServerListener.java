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

import java.util.concurrent.ConcurrentHashMap;

import shinil35.shat.log.Log;
import shinil35.shat.log.LogTraceType;
import shinil35.shat.network.packet.IPacket;
import shinil35.shat.network.packet.P0_PublicKey;
import shinil35.shat.network.packet.P1_KeyVerifier;
import shinil35.shat.network.packet.P2_KeyVerifier;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.ThreadedListener;

public class NetworkServerListener extends ThreadedListener
{
	private ConcurrentHashMap<Integer, NetworkConnectionData> connectionDatas;

	public NetworkServerListener(Listener listener, NetworkServer s)
	{
		super(listener);

		connectionDatas = new ConcurrentHashMap<Integer, NetworkConnectionData>();
	}

	@Override
	public void connected(Connection conn)
	{
		Log.trace("Nuova connessione in entrata, id: " + conn.getID(), LogTraceType.INCOMING_CONNECTION);
	}

	@Override
	public void disconnected(Connection conn)
	{
		Log.trace("Connessione in entrata chiusa, id: " + conn.getID(), LogTraceType.DISCONNECTED);
	}

	private NetworkConnectionData getConnectionData(Connection conn)
	{
		int id = conn.getID();

		if (id == -1)
			return null;

		if (connectionDatas.containsKey(id))
			return connectionDatas.get(id);

		NetworkConnectionData newData = new NetworkConnectionData(conn, NetworkConnectionType.INCOMING);
		connectionDatas.put(id, newData);
		return newData;
	}

	public ConcurrentHashMap<Integer, NetworkConnectionData> getConnectionDatas()
	{
		return connectionDatas;
	}

	@Override
	public void received(Connection conn, Object inPacket)
	{
		if (inPacket instanceof FrameworkMessage)
			return;

		NetworkConnectionData connectionData = getConnectionData(conn);

		if (inPacket instanceof P0_PublicKey)
		{
			P0_PublicKey packet = (P0_PublicKey) inPacket;
			connectionData.setPublicKey(packet.getPublicKey());
			connectionData.setListeningPort(packet.getListeningPort());

			P1_KeyVerifier outPacket = new P1_KeyVerifier();
			outPacket.writePacket(connectionData, packet, null);
			conn.sendTCP(outPacket);
		}
		else if (inPacket instanceof P2_KeyVerifier)
		{
			P2_KeyVerifier packet = (P2_KeyVerifier) inPacket;

			if (!packet.checkEncryption(connectionData))
			{
				conn.close();
				return;
			}

			connectionData.handshakeCompleted();

			Log.trace("Handshake completo, id: " + conn.getID(), LogTraceType.HANDSHAKE_COMPLETED);
		}
		else if (inPacket instanceof IPacket)
			connectionData.elaboratePacket((IPacket) inPacket);
		else
			Log.localizedWarn("[UNKNOW_PACKET]", inPacket.getClass().getName());
	}
}
