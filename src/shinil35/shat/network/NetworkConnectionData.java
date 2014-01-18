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

import java.security.PublicKey;
import java.util.ArrayList;

import shinil35.shat.log.Log;
import shinil35.shat.log.LogTraceType;
import shinil35.shat.network.packet.IPacket;
import shinil35.shat.network.packet.P3_PeerListRequest;
import shinil35.shat.network.packet.P4_PeerListResponse;
import shinil35.shat.network.packet.P5_Message;
import shinil35.shat.peer.PeerData;
import shinil35.shat.peer.PeerManager;
import shinil35.shat.util.Hash;
import shinil35.shat.util.Utility;

import com.esotericsoftware.kryonet.Connection;

public class NetworkConnectionData
{
	private boolean handshakeCompleted = false;

	private byte[] randomBytes = null;

	private PublicKey publicKey = null;

	private Connection conn = null;

	private boolean peerListRequested = false;
	private long lastPeersSendingTime = 0;
	private long lastPeersRequestingTime = 0;

	private ArrayList<Hash> messageHashes;
	private ArrayList<Hash> alreadySendedPeers;

	private NetworkConnectionType connectionType;
	private int listeningPort = -1;

	private boolean closed = false;

	public NetworkConnectionData(Connection conn, NetworkConnectionType type)
	{
		this.conn = conn;
		this.connectionType = type;
		this.messageHashes = new ArrayList<Hash>();
		this.alreadySendedPeers = new ArrayList<Hash>();

		if (connectionType.equals(NetworkConnectionType.OUTGOING))
			this.listeningPort = conn.getRemoteAddressTCP().getPort();
	}

	public void close()
	{
		if (closed)
			return;

		closed = true;

		if (conn != null)
			conn.close();

		if (messageHashes != null)
			messageHashes.clear();

		if (alreadySendedPeers != null)
			alreadySendedPeers.clear();

		lastPeersSendingTime = 0;
		lastPeersRequestingTime = 0;
		listeningPort = 0;

		randomBytes = null;
		publicKey = null;
		conn = null;
		messageHashes = null;
		alreadySendedPeers = null;
	}

	public void elaboratePacket(IPacket inPacket)
	{
		if (closed)
			return;

		if (!getHandshakeStatus())
		{
			conn.close();
			return;
		}

		if (inPacket instanceof P3_PeerListRequest)
		{
			sendPeerList();
		}
		else if (inPacket instanceof P4_PeerListResponse)
		{
			if (!peerListRequested)
				return;

			peerListRequested = false;

			P4_PeerListResponse resp = (P4_PeerListResponse) inPacket;
			Log.trace("Received peer list with " + resp.getQuantity() + " peers", LogTraceType.PEERLIST_RECEIVED);

			PeerManager.addPeerDataList(resp.getPeerList());
		}
		else if (inPacket instanceof P5_Message)
		{
			P5_Message messagePacket = (P5_Message) inPacket;
			Hash messageHash = messagePacket.getPacketHash();

			if (messageHashes.contains(messageHash))
				return;

			messageHashes.add(messageHash);

			String message = messagePacket.testDecrypt();

			NetworkManager.sendToAll(messagePacket);

			if (message != null)
				System.out.println("Ricevuto messaggio: \"" + message + "\"");
		}
		else
			Log.localizedWarn("[UNKNOW_PACKET]", inPacket.getClass().getName());

		inPacket.dispose();
	}

	public ArrayList<Hash> getAlreadySendedPeers()
	{
		if (closed)
			return null;

		return alreadySendedPeers;
	}

	public boolean getHandshakeStatus()
	{
		if (closed)
			return false;

		return handshakeCompleted;
	}

	public String getIP()
	{
		if (closed)
			return null;

		return conn.getRemoteAddressTCP().getHostString();
	}

	public long getPeersRequestingTime()
	{
		if (closed)
			return -1;

		return lastPeersRequestingTime;
	}

	public long getPeersSendingTime()
	{
		if (closed)
			return -1;

		return lastPeersSendingTime;
	}

	public int getPort()
	{
		if (closed)
			return -1;

		return listeningPort;
	}

	public PublicKey getPublicKey()
	{
		if (closed)
			return null;

		return publicKey;
	}

	public byte[] getRandomBytes()
	{
		if (closed)
			return null;

		return randomBytes;
	}

	public void handshakeCompleted()
	{
		if (closed)
			return;

		handshakeCompleted = true;

		PeerManager.generatePeer(this);
	}

	public boolean isConnected()
	{
		if (closed)
			return false;
		
		return conn.isConnected();
	}

	public void requestPeerList()
	{
		if (closed || !getHandshakeStatus() || Utility.getElapsedFromTime(getPeersRequestingTime()) < 30000)
			return;

		lastPeersRequestingTime = Utility.getTimeNow();
		peerListRequested = true;

		P3_PeerListRequest outPacket = new P3_PeerListRequest();
		outPacket.writePacket(this, null, null);
		conn.sendTCP(outPacket);

		outPacket.dispose();
		
		Log.trace("Requesting peer list",LogTraceType.PEERLIST_REQUESTING);
	}

	public void sendPacket(IPacket outPacket)
	{
		if (closed || !getHandshakeStatus())
			return;

		// TODO: Send packet here
	}

	public void sendPeerList()
	{
		if (closed || !getHandshakeStatus() || Utility.getElapsedFromTime(getPeersSendingTime()) < 20000)
			return;

		P4_PeerListResponse outPacket = new P4_PeerListResponse();

		if (lastPeersSendingTime > PeerManager.getLastPeerListUpdate())
			outPacket.writePacket(this, null, true);
		else
			outPacket.writePacket(this, null, false);

		if (outPacket.getQuantity() > 0)
		{
			for (PeerData peerD : outPacket.getPeerList())
			{
				if (!alreadySendedPeers.contains(peerD.getHash()))
					alreadySendedPeers.add(peerD.getHash());
			}
		}

		lastPeersSendingTime = Utility.getTimeNow();

		conn.sendTCP(outPacket);

		outPacket.dispose();
		
		Log.trace("Sending peer list with " + outPacket.getQuantity() + " peers.", LogTraceType.PEERLIST_SENDING);
	}

	public void setListeningPort(int port)
	{
		if (closed)
			return;

		listeningPort = port;
	}

	public void setPublicKey(PublicKey pk)
	{
		if (closed)
			return;
		
		publicKey = pk;
	}

	public void setRandomBytes(byte[] rb)
	{
		if (closed)
			return;
		
		randomBytes = rb;
	}
}
