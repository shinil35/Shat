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

package shinil35.shat.log;

import java.util.ArrayList;

public class LogTraceManager
{
	private static ArrayList<LogTraceType> toTrace = new ArrayList<LogTraceType>();
	
	public static void loadTraceManager()
	{
		toTrace = new ArrayList<LogTraceType>();
		
		// Register what we need to view, comment for disable or uncomment to enable
		
		// toTrace.add(LogTraceType.ATTEMPTING_CONNECTION);
		// toTrace.add(LogTraceType.CONNECTION_FAILED);
		toTrace.add(LogTraceType.DISCONNECTED);
		toTrace.add(LogTraceType.HANDSHAKE_COMPLETED);
		// toTrace.add(LogTraceType.INCOMING_CONNECTION);
		// toTrace.add(LogTraceType.OUTGOING_CONNECTION);
		// toTrace.add(LogTraceType.PEER_DISCOVERED);
		toTrace.add(LogTraceType.PEERLIST_RECEIVED);
		// toTrace.add(LogTraceType.PEERLIST_REQUESTING);
		// toTrace.add(LogTraceType.PEERLIST_SENDING);
		// toTrace.add(LogTraceType.THREAD_ALIVE);
	}
	
	public static boolean isToTrace(LogTraceType type)
	{
		return toTrace.contains(type);
	}
}
