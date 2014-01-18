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

public class NetworkClientListener extends ThreadedListener
{
	private NetworkClient networkClient;
	private NetworkConnectionData connectionData;

	public NetworkClientListener(Listener listener, NetworkClient c)
	{
		super(listener);

		networkClient = c;
	}

	@Override
	public void connected(Connection conn)
	{
		Log.trace("Nuova connessione in uscita, id: " + conn.getID(), LogTraceType.OUTGOING_CONNECTION);

		connectionData = new NetworkConnectionData(conn, NetworkConnectionType.OUTGOING);

		P0_PublicKey outPacket = new P0_PublicKey();
		outPacket.writePacket(connectionData, null, null);
		conn.sendTCP(outPacket);

		connectionData.setRandomBytes(outPacket.getRandomBytes());
	}

	@Override
	public void disconnected(Connection conn)
	{
		Log.trace("Connessione in uscita chiusa, id: " + conn.getID(), LogTraceType.DISCONNECTED);

		if (networkClient != null)
			networkClient.close();
	}

	public NetworkConnectionData getConnectionData()
	{
		return connectionData;
	}

	@Override
	public void received(Connection conn, Object inPacket)
	{
		if (inPacket instanceof FrameworkMessage)
			return;

		if (inPacket instanceof P1_KeyVerifier)
		{
			P1_KeyVerifier packet = (P1_KeyVerifier) inPacket;

			connectionData.setPublicKey(packet.getPublicKey());

			if (!packet.checkEncryption(connectionData))
			{
				conn.close();
				return;
			}

			connectionData.handshakeCompleted();

			Log.trace("Handshake completo, id: " + conn.getID(), LogTraceType.HANDSHAKE_COMPLETED);

			P2_KeyVerifier outPacket = new P2_KeyVerifier();
			outPacket.writePacket(connectionData, packet, null);
			conn.sendTCP(outPacket);
		}
		else if (inPacket instanceof IPacket)
			connectionData.elaboratePacket((IPacket) inPacket);
		else
			Log.localizedWarn("[UNKNOW_PACKET]", inPacket.getClass().getName());
	}
}
