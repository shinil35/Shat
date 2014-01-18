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

import java.util.Arrays;

import shinil35.shat.Main;
import shinil35.shat.network.NetworkConnectionData;
import shinil35.shat.util.RSA;

public class P2_KeyVerifier implements IPacket
{
	private byte[] encryptedRandomBytes;

	public boolean checkEncryption(NetworkConnectionData remoteData)
	{
		byte[] realData = remoteData.getRandomBytes();
		byte[] decData = RSA.decrypt(encryptedRandomBytes, remoteData.getPublicKey(), true);

		return Arrays.equals(realData, decData);
	}

	@Override
	public void dispose()
	{
		encryptedRandomBytes = null;
	}

	@Override
	public void writePacket(NetworkConnectionData connectionData, IPacket oldPacket, Object packetData)
	{
		if (!oldPacket.getClass().equals(P1_KeyVerifier.class))
			return;

		P1_KeyVerifier old = (P1_KeyVerifier) oldPacket;

		encryptedRandomBytes = RSA.encrypt(old.getToEncryptRandomBytes(), Main.getPrivateKey(), true);
	}
}
