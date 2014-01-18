/*	Copyright (C) 2013 Emilio Cafè Nunes
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

import java.util.ArrayList;

import shinil35.shat.log.Log;
import shinil35.shat.log.LogTraceType;
import shinil35.shat.network.NetworkManager;
import shinil35.shat.util.Utility;

public class PeerConnector implements Runnable
{
	public PeerConnector()
	{

	}

	@Override
	public void run()
	{
			ArrayList<Peer> peerList = PeerManager.getPeerList();

			for (Peer p : peerList)
			{
				String IP = p.getIP();
				int port = p.getPort();

				if (NetworkManager.isConnected(IP, port) || Utility.getElapsedFromTime(p.getLastConnectionAttempt()) < 350000
						|| !Utility.isValidAddress(IP, port))
					continue;

				p.setLastConnectionAttempt(Utility.getTimeNow());

				Log.trace("Tentativo di connessione a " + IP + ":" + port, LogTraceType.ATTEMPTING_CONNECTION);

				NetworkManager.delayedConnection(IP, port);

				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{

				}
			}

			peerList.clear();
			
			Log.trace("PeerConnector thread succefull completed", LogTraceType.THREAD_ALIVE);
	}
}
