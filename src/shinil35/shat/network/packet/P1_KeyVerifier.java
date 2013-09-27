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

import java.security.PublicKey;
import java.util.Arrays;

import shinil35.shat.Main;
import shinil35.shat.network.NetworkConnectionData;
import shinil35.shat.util.Encoding;
import shinil35.shat.util.RSA;
import shinil35.shat.util.Utility;

public class P1_KeyVerifier implements IPacket
{
	private byte[] encryptedPublicKey;
	private byte[] encryptedRandomBytes;
	private byte[] newRandomBytes;

	public boolean checkEncryption(NetworkConnectionData data)
	{
		byte[] realData = data.getRandomBytes();
		byte[] decData = RSA.decrypt(encryptedRandomBytes, data.getPublicKey(), true);

		return Arrays.equals(realData, decData);
	}

	public PublicKey getPublicKey()
	{
		return Encoding.decodePublicKey(RSA.decrypt(encryptedPublicKey, Main.getPrivateKey(), true));
	}

	public byte[] getToEncryptRandomBytes()
	{
		return newRandomBytes;
	}

	public void writePacket(NetworkConnectionData connectionData, IPacket oldPacket, Object packetData)
	{
		if (!oldPacket.getClass().equals(P0_PublicKey.class))
			return;

		P0_PublicKey old = (P0_PublicKey) oldPacket;
		encryptedPublicKey = RSA.encrypt(Encoding.encodePublicKey(Main.getPublicKey()), old.getPublicKey(), true);
		encryptedRandomBytes = RSA.encrypt(old.getRandomBytes(), Main.getPrivateKey(), true);
		newRandomBytes = Utility.randomBytes(128);
		connectionData.setRandomBytes(newRandomBytes);
	}
}
