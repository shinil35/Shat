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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import com.esotericsoftware.minlog.Log;

public class Encoding
{
	/**
	 * Decode one byte array to the original key pair
	 */
	public static KeyPair decodeKeyPair(byte[] pairE)
	{
		ByteBuffer reader = ByteBuffer.wrap(pairE);
		reader.position(0);

		int header = reader.getInt();

		if (header != 3)
			return null;

		byte[] modA = new byte[257];
		byte[] expA = new byte[257];
		byte[] expPubA = new byte[3];

		reader.get(modA);
		reader.get(expA);
		reader.get(expPubA);

		BigInteger mod = new BigInteger(modA);
		BigInteger exp = new BigInteger(expA);
		BigInteger expPub = new BigInteger(expPubA);

		try
		{
			KeyFactory factory = KeyFactory.getInstance("RSA");

			RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(mod, exp);
			RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(mod, expPub);

			PrivateKey privateKey = factory.generatePrivate(privSpec);
			PublicKey publicKey = factory.generatePublic(pubSpec);

			return new KeyPair(publicKey, privateKey);
		}
		catch (Exception e)
		{
			Log.warn("[DECODING_ERROR]", e.getMessage());
			return null;
		}
	}

	/**
	 * Decode an byte array to the original PublicKey
	 */
	public static PublicKey decodePublicKey(byte[] pub)
	{
		if (pub.length != 264)
			return null;

		ByteBuffer reader = ByteBuffer.wrap(pub);
		reader.position(0);

		int header = reader.getInt();

		if (header != 2)
			return null;

		byte[] modA = new byte[257];
		byte[] expA = new byte[3];

		reader.get(modA);
		reader.get(expA);

		BigInteger mod = new BigInteger(modA);
		BigInteger exp = new BigInteger(expA);

		try
		{
			KeyFactory factory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(mod, exp);
			PublicKey publicKey = factory.generatePublic(pubSpec);

			return publicKey;
		}
		catch (Exception e)
		{
			Log.warn("[DECODING_ERROR]", e.getMessage());
			return null;
		}
	}

	/**
	 * Get an byte array that can be decoded to the original keypair
	 */
	public static byte[] encodeKeyPair(KeyPair rsaPair)
	{
		RSAPrivateKey priv = (RSAPrivateKey) rsaPair.getPrivate();
		RSAPublicKey pub = (RSAPublicKey) rsaPair.getPublic();

		ByteBuffer buffer = ByteBuffer.allocate(521);

		byte[] modA = priv.getModulus().toByteArray();
		byte[] expPrivA = priv.getPrivateExponent().toByteArray();
		byte[] expPubA = pub.getPublicExponent().toByteArray();

		ByteBuffer modB = ByteBuffer.allocate(257);
		ByteBuffer expPrivB = ByteBuffer.allocate(257);
		ByteBuffer expPubB = ByteBuffer.allocate(3);

		int modI = 257 - modA.length;
		int expPrivI = 257 - expPrivA.length;
		int expPubI = 3 - expPubA.length;

		while (modI > 0)
		{
			modB.put((byte) 0);
			modI--;
		}

		while (expPrivI > 0)
		{
			expPrivB.put((byte) 0);
			expPrivI--;
		}

		while (expPubI > 0)
		{
			expPubB.put((byte) 0);
			expPubI--;
		}

		modB.put(modA);
		expPrivB.put(expPrivA);
		expPubB.put(expPubA);

		buffer.putInt(3);
		buffer.put(modB.array());
		buffer.put(expPrivB.array());
		buffer.put(expPubB.array());

		return buffer.array();
	}

	/**
	 * Get an byte array that can be decoded to the original public key
	 */
	public static byte[] encodePublicKey(PublicKey pub1)
	{
		ByteBuffer buffer = ByteBuffer.allocate(264);
		RSAPublicKey pub = (RSAPublicKey) pub1;

		byte[] modA = pub.getModulus().toByteArray();
		byte[] expA = pub.getPublicExponent().toByteArray();

		ByteBuffer modB = ByteBuffer.allocate(257);
		ByteBuffer expB = ByteBuffer.allocate(3);

		int modI = 257 - modA.length;
		int expI = 3 - expA.length;

		while (modI > 0)
		{
			modB.put((byte) 0);
			modI--;
		}

		while (expI > 0)
		{
			expB.put((byte) 0);
			expI--;
		}

		modB.put(modA);
		expB.put(expA);

		buffer.putInt(2);
		buffer.put(modB.array());
		buffer.put(expB.array());

		return buffer.array();
	}

}
