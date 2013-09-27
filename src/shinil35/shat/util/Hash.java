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

import java.util.Arrays;

public class Hash
{
	private byte[] bytes;
	private String algorithm;

	public Hash(byte[] bytes, String algorithm)
	{
		this.bytes = bytes;
		this.algorithm = algorithm;
	}

	public boolean equals(Object other)
	{
		if (!(other instanceof Hash))
			return false;

		Hash hash = (Hash) other;

		if (hash.getAlgorithm() != this.getAlgorithm())
			return false;

		if (!Arrays.equals(this.getBytes(), hash.getBytes()))
			return false;

		return true;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	public byte[] getBytes()
	{
		return bytes;
	}

	public String getReadableHash()
	{
		return Hashing.getHexString(bytes);
	}

	public int hashCode()
	{
		return getReadableHash().hashCode();
	}

}
