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

public class PeerData
{
	private byte[] encodedPublicKey;
	private String ip;
	private int port;

	@SuppressWarnings("unused")
	private PeerData()
	{
	} // No-arguments constructor for kryo serialization

	public PeerData(PublicKey pk, String ip, int port)
	{
		this.encodedPublicKey = Encoding.encodePublicKey(pk);
		this.ip = ip;
		this.port = port;
	}

	public Peer getPeer()
	{
		Hash h = Hashing.getHash(encodedPublicKey);
		Peer p = PeerManager.getPeerIfExists(h);

		if (p == null)
		{
			PublicKey pk = Encoding.decodePublicKey(encodedPublicKey);
			p = new Peer(pk, ip, port, 0);
		}

		return p;
	}
}
