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

import java.security.MessageDigest;

import com.esotericsoftware.minlog.Log;

public class Hashing
{
	public static Hash getHash(byte[] data)
	{
		return getHash(data, "SHA-512");
	}

	public static Hash getHash(byte[] data, String algorithm)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(data);
			return new Hash(md.digest(), algorithm);
		}
		catch (Exception e)
		{
			Log.localizedWarn("[HASHING_ERROR]", e.getMessage());

			return null;
		}
	}

	public static String getHexString(byte[] data)
	{
		String out = "";

		for (int i = 0; i < data.length; i++)
		{
			byte temp = data[i];
			String s = Integer.toHexString(new Byte(temp));
			while (s.length() < 2)
			{
				s = "0" + s;
			}
			s = s.substring(s.length() - 2);
			out += s;
		}

		return out.toUpperCase();
	}
}
