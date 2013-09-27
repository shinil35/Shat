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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import shinil35.shat.Database;
import shinil35.shat.network.NetworkConnectionData;
import shinil35.shat.util.Encoding;
import shinil35.shat.util.Hash;

import com.esotericsoftware.minlog.Log;

public class PeerManager
{
	private static ConcurrentHashMap<Hash, Peer> peers;
	private static boolean initialized = false;

	private static int maxPeerLoaded = 200;
	
	public static void addPeer(Peer p)
	{
		if (!initialized || peers.containsKey(p.getHash()))
			return;

		try
		{
			PreparedStatement st = Database.getConnection().prepareStatement("INSERT INTO peers VALUES(?, ?, ?, ?, ?)");

			st.setString(1, p.getHash().getReadableHash());
			st.setBytes(2, Encoding.encodePublicKey(p.getPublicKey()));
			st.setString(3, p.getIP());
			st.setInt(4, p.getPort());
			st.setLong(5, p.getLastCommunication());
			st.executeUpdate();
			st.close();

			peers.put(p.getHash(), p);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			Log.localizedWarn("[SQL_QUERY_ERROR]", e.getMessage());
		}
	}

	public static void addPeerDataList(Collection<PeerData> peerList)
	{
		if (!initialized || peerList == null)
			return;

		for (PeerData p : peerList)
			addPeer(p.getPeer());

		removeExceedingPeers();
	}

	public static void close()
	{
		if (!initialized)
			return;

		initialized = false;

		if (peers != null)
			peers.clear();
	}

	public static void generatePeer(NetworkConnectionData data)
	{
		if (!initialized || data == null)
			return;

		PublicKey pk = data.getPublicKey();
		String ip = data.getIP();
		int port = data.getPort();
		long date = new Date().getTime();

		Peer p = new Peer(pk, ip, port, date);

		addPeer(p);
	}

	public static ArrayList<PeerData> getPeerDataList(int maxPeers)
	{
		if (!initialized)
			return null;

		ArrayList<PeerData> toReturn = new ArrayList<PeerData>();

		if (maxPeers < 0 || maxPeers >= peers.size())
		{
			for (Peer p : peers.values())
				toReturn.add(p.getPeerData());
		}
		else
		{
			ArrayList<Peer> orderedPeers = sortPeerList(new ArrayList<Peer>(peers.values()));

			for (Peer p : orderedPeers)
				toReturn.add(p.getPeerData());

			toReturn = new ArrayList<PeerData>(toReturn.subList(0, maxPeers));
		}

		return toReturn;
	}

	public static Peer getPeerIfExists(Hash hash)
	{
		if (!initialized || !peers.containsKey(hash))
			return null;

		return peers.get(hash);
	}

	public static int getPeerQuantity()
	{
		if (!initialized)
			return 0;

		return peers.size();
	}

	public static void init()
	{
		peers = new ConcurrentHashMap<Hash, Peer>();

		initialized = true;

		loadPeerList();
	}

	public static void loadPeerList()
	{
		if (!Database.isInitialized())
			return;

		try
		{
			Statement sta = Database.getConnection().createStatement();

			ResultSet rs = sta.executeQuery("SELECT * FROM peers");

			while (rs.next())
			{
				PublicKey pk = Encoding.decodePublicKey(rs.getBytes("public_key"));
				String ip = rs.getString("ip");
				int port = rs.getInt("port");
				long last = rs.getLong("last_access");

				Peer p = new Peer(pk, ip, port, last);

				peers.put(p.getHash(), p);
			}

			sta.close();
		}
		catch (SQLException e)
		{
			Log.localizedWarn("[SQL_QUERY_ERROR]", e.getMessage());
		}
	}

	public static void removeExceedingPeers()
	{
		if (!initialized)
			return;

		synchronized (peers)
		{
			if (peers.size() <= maxPeerLoaded)
				return;

			ArrayList<Peer> exceedingPeers = new ArrayList<Peer>(peers.values());

			exceedingPeers = sortPeerList(exceedingPeers);
			exceedingPeers.subList(exceedingPeers.size() - maxPeerLoaded, exceedingPeers.size());

			for (Peer p : exceedingPeers)
				removePeer(p.getHash());
		}
	}

	public static void removePeer(Hash peerHash)
	{
		if (!initialized || !peers.containsKey(peerHash))
			return;

		try
		{
			PreparedStatement st = Database.getConnection().prepareStatement("DELETE FROM peers WHERE hash=?");

			st.setString(1, peerHash.getReadableHash());
			st.executeUpdate();
			st.close();

			peers.remove(peerHash);
		}
		catch (SQLException e)
		{
			Log.localizedWarn("[SQL_QUERY_ERROR]", e.getMessage());
		}
	}

	public static ArrayList<Peer> sortPeerList(ArrayList<Peer> p)
	{
		Collections.sort(p, new Comparator<Peer>()
		{

			public int compare(Peer a, Peer b)
			{
				long aLast = 0;
				long bLast = 0;

				if (a != null)
					aLast = a.getLastCommunication();

				if (b != null)
					bLast = b.getLastCommunication();

				return (int) (bLast - aLast);
			}
		});

		return p;
	}
}
