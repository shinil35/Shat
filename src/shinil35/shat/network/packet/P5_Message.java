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

import java.nio.ByteBuffer;
import java.security.PublicKey;

import shinil35.shat.Main;
import shinil35.shat.network.NetworkConnectionData;
import shinil35.shat.util.Encoding;
import shinil35.shat.util.Hash;
import shinil35.shat.util.Hashing;
import shinil35.shat.util.RSA;
import shinil35.shat.util.Utility;

public class P5_Message implements IPacket
{
	private byte[] encryptedSignedMessage;
	private byte[] encryptedSourcePublicKey;
	private byte[] randomBytes;

	public Hash getMessageHash()
	{
		ByteBuffer hashB = ByteBuffer.allocate(encryptedSignedMessage.length + randomBytes.length);
		hashB.put(encryptedSignedMessage);
		hashB.put(randomBytes);
		Hash hash = Hashing.getHash(hashB.array());
		hashB.clear();

		return hash;
	}

	public String testDecrypt()
	{
		try
		{
			byte[] decryptedSource = RSA.decrypt(encryptedSourcePublicKey, Main.getPrivateKey(), false); // Decrypt source's encoded public key
			if (decryptedSource == null)
				return null;
			byte[] decryptedSignedMessage = RSA.decrypt(encryptedSignedMessage, Main.getPrivateKey(), false); // Decrypt signed messagge
			if (decryptedSignedMessage == null)
				return null;
			PublicKey source = Encoding.decodePublicKey(decryptedSource); // Decode source's publickey
			if (source == null)
				return null;
			String message = new String(RSA.decrypt(decryptedSignedMessage, source, true), Main.getCharset()); // Decrypt message
			if (message == null || message.equals(""))
				return null;

			return message;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public void writePacket(NetworkConnectionData connectionData, IPacket oldPacket, Object packetData)
	{
		if (packetData == null || !(packetData instanceof String))
			return;

		String message = (String) packetData;

		if (message.equals(""))
			return;

		PublicKey destPublicKey = connectionData.getPublicKey();

		byte[] encodedPublicKey = Encoding.encodePublicKey(Main.getPublicKey()); // Encode my public key

		byte[] messageBytes = message.getBytes(Main.getCharset()); // Get message bytes
		byte[] signedMessage = RSA.encrypt(messageBytes, Main.getPrivateKey(), true); // Sign message

		encryptedSignedMessage = RSA.encrypt(signedMessage, destPublicKey, true); // Encrypt signed message
		encryptedSourcePublicKey = RSA.encrypt(encodedPublicKey, destPublicKey, true); // Encrypt my public key

		randomBytes = Utility.randomBytes(16);
	}
}
