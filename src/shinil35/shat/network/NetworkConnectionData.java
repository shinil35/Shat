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

import shinil35.shat.network.packet.IPacket;
import shinil35.shat.network.packet.P3_PeerListRequest;
import shinil35.shat.network.packet.P4_PeerListResponse;
import shinil35.shat.network.packet.P5_Message;
import shinil35.shat.peer.PeerManager;
import shinil35.shat.util.Hash;
import shinil35.shat.util.Utility;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

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

	public NetworkConnectionData(Connection conn)
	{
		this.conn = conn;
		this.messageHashes = new ArrayList<Hash>();
	}

	public void elaboratePacket(IPacket inPacket)
	{
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
			PeerManager.addPeerDataList(resp.getPeerList());

			Log.trace("Received peer list!");
		}
		else if (inPacket instanceof P5_Message)
		{
			P5_Message messagePacket = (P5_Message) inPacket;
			Hash messageHash = messagePacket.getPacketHash();

			if (messageHashes.contains(messageHash))
				return;

			messageHashes.add(messageHash);

		}
		else
			Log.warn("Unknow packet: " + inPacket.getClass().getName());
	}

	public boolean getHandshakeStatus()
	{
		return handshakeCompleted;
	}

	public String getIP()
	{
		return conn.getRemoteAddressTCP().getHostString();
	}

	public long getPeersRequestingTime()
	{
		return lastPeersRequestingTime;
	}

	public long getPeersSendingTime()
	{
		return lastPeersSendingTime;
	}

	public int getPort()
	{
		return conn.getRemoteAddressTCP().getPort();
	}

	public PublicKey getPublicKey()
	{
		return publicKey;
	}

	public byte[] getRandomBytes()
	{
		return randomBytes;
	}

	public void handshakeWasCompleted()
	{
		handshakeCompleted = true;

		PeerManager.generatePeer(this);
	}

	public boolean messageWasSended(Hash hash)
	{
		return messageHashes.contains(hash);
	}

	public void requestPeerList()
	{
		if (!getHandshakeStatus() || Utility.getElapsedFromTime(getPeersRequestingTime()) < 30000)
			return;

		lastPeersRequestingTime = Utility.getTimeNow();
		peerListRequested = true;

		P3_PeerListRequest outPacket = new P3_PeerListRequest();
		outPacket.writePacket(this, null, null);
		conn.sendTCP(outPacket);

		Log.trace("Requesting peer list");
	}

	public void sendPacket(IPacket outPacket)
	{
		if (!getHandshakeStatus())
			return;

		if (outPacket instanceof P5_Message)
		{
			P5_Message outMessage = (P5_Message) outPacket;

			if (messageWasSended(outMessage.getPacketHash()))
				return;

			messageHashes.add(outMessage.getPacketHash());

			conn.sendTCP(outMessage);
		}
	}

	public void sendPeerList()
	{
		if (!getHandshakeStatus() || Utility.getElapsedFromTime(getPeersSendingTime()) < 20000)
			return;

		P4_PeerListResponse outPacket = new P4_PeerListResponse();

		if (lastPeersSendingTime > PeerManager.getLastPeerListUpdate())
			outPacket.writePacket(this, null, true);
		else
			outPacket.writePacket(this, null, null);

		lastPeersSendingTime = Utility.getTimeNow();

		int b = conn.sendTCP(outPacket);

		Log.trace("Sending peer list, bytes: " + b + ", count: " + outPacket.getQuantity());
	}

	public void setPublicKey(PublicKey pk)
	{
		publicKey = pk;
	}

	public void setRandomBytes(byte[] rb)
	{
		randomBytes = rb;
	}
}
