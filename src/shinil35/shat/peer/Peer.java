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

package shinil35.shat.peer;

import java.security.PublicKey;

import shinil35.shat.util.Encoding;
import shinil35.shat.util.Hash;
import shinil35.shat.util.Hashing;

public class Peer
{
	private PublicKey publicKey;
	private Hash publicKeyHash;
	private String ip;
	private int port;
	private int status;
	private long lastCommunication;

	public static final int PEER_UNKNOW = 0;
	public static final int AVAILABLE = 1;
	public static final int CONNECTED = 2;

	public Peer(PublicKey publicKey, String ip, int port, long last)
	{
		setPublicKey(publicKey);
		this.ip = ip;
		this.port = port;
		this.lastCommunication = last;
	}

	public Hash getHash()
	{
		return publicKeyHash;
	}

	public String getIP()
	{
		return ip;
	}

	public long getLastCommunication()
	{
		return lastCommunication;
	}

	public PeerData getPeerData()
	{
		return new PeerData(publicKey, ip, port);
	}

	public int getPort()
	{
		return port;
	}

	public PublicKey getPublicKey()
	{
		return publicKey;
	}

	public int getStatus()
	{
		return status;
	}

	public void setLastCommunication(long last)
	{
		lastCommunication = last;
	}

	public void setPublicKey(PublicKey pk)
	{
		publicKey = pk;
		publicKeyHash = Hashing.getHash(Encoding.encodePublicKey(publicKey), "SHA-512");
	}

	public void setStatus(int status)
	{
		this.status = status;
	}
}
