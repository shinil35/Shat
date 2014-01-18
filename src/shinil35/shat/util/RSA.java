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

package shinil35.shat.util;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import shinil35.shat.log.Log;

public class RSA
{
	public static byte[] decrypt(byte[] toDecrypt, Key key, boolean logException)
	{
		byte[] finals = null;

		try
		{
			ByteBuffer byteBuffer = ByteBuffer.wrap(toDecrypt);
			byteBuffer.position(0);
			ByteBuffer builder = ByteBuffer.allocate((toDecrypt.length / 256) * 245);

			int totBytes = 0;

			while (byteBuffer.hasRemaining())
			{
				int toGet = byteBuffer.remaining() >= 256 ? 256 : byteBuffer.remaining();
				byte[] encode = new byte[toGet];
				byteBuffer.get(encode);

				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] toPut = cipher.doFinal(encode);
				builder.put(toPut);
				totBytes += toPut.length;
			}

			finals = new byte[totBytes];

			builder.position(0);
			builder.get(finals);
		}
		catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex)
		{
			if (logException)
				Log.localizedWarn("[RSA_DECRYPTION_ERROR]", ex.getMessage());
		}

		return finals;
	}

	public static byte[] encrypt(byte[] toEncrypt, Key key, boolean logException)
	{
		byte[] finals = null;
		try
		{
			ByteBuffer byteBuffer = ByteBuffer.wrap(toEncrypt);
			byteBuffer.position(0);
			ByteBuffer builder = ByteBuffer.allocate((toEncrypt.length / 245 + 1) * 256);

			while (byteBuffer.hasRemaining())
			{
				int toGet = byteBuffer.remaining() >= 245 ? 245 : byteBuffer.remaining();
				byte[] encode = new byte[toGet];
				byteBuffer.get(encode);

				Cipher cipher;
				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				byte[] encrypted;
				encrypted = cipher.doFinal(encode);
				builder.put(encrypted);
			}

			finals = builder.array();
		}
		catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			if (logException)
				Log.localizedWarn("[RSA_ENCRYPTION_ERROR]", e.getMessage());
		}

		return finals;
	}

	public static KeyPair generatePair()
	{
		try
		{
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			return keyGen.generateKeyPair();
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.localizedWarn("[RSA_GENERATION_ERROR]", e.getMessage());
			return null;
		}
	}

}
