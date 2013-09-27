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

package shinil35.shat.network.packet;

import java.util.ArrayList;

import shinil35.shat.network.NetworkConnectionData;
import shinil35.shat.peer.PeerData;
import shinil35.shat.peer.PeerManager;

public class P4_PeerListResponse implements IPacket
{
	private static final int peerResponseSize = 50;

	private ArrayList<PeerData> peerList;

	public ArrayList<PeerData> getPeerList()
	{
		return peerList;
	}

	public int getQuantity()
	{
		return peerList.size();
	}

	public void writePacket(NetworkConnectionData connectionData, IPacket oldPacket, Object packetData)
	{
		peerList = PeerManager.getPeerDataList(peerResponseSize);
	}
}