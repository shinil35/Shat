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

package shinil35.shat;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import shinil35.shat.network.NetworkManager;
import shinil35.shat.peer.PeerManager;
import shinil35.shat.util.Encoding;
import shinil35.shat.util.RSA;

import com.esotericsoftware.minlog.Log;

public class Main
{
	private static boolean commandReaderOn = true;
	private static Scanner keyboardScanner;
	private static Charset charset = Charset.forName("UTF-8");
	private static KeyPair rsaKeys = null;

	private static UpdaterThread updater;

	/**
	 * Close shat
	 */
	public static void close()
	{
		commandReaderOn = false;

		if (keyboardScanner != null)
			keyboardScanner.close();

		updater.close();

		NetworkManager.close();
		PeerManager.close();
		Database.close();

		// TODO: Chiude tutti i thread?
	}

	/**
	 * Loop which read commands
	 */
	public static void commandReader()
	{
		if (!commandReaderOn)
			return;

		Log.localizedInfo("[CONSOLE_COMMAND_INTRO]");

		try
		{
			keyboardScanner = new Scanner(System.in);

			while (commandReaderOn)
			{
				String text = keyboardScanner.nextLine();

				String[] parts = text.split(" ", 2); // [0] = Command, [1] Arguments
				String command = parts[0];

				switch (command)
				{
					case "close":
					case "exit":
						close();
						break;

					default:
					case "?":
					case "help":
						Log.localizedInfo("[CONSOLE_COMMAND_HELP]");
						break;
				}
			}
		}
		finally
		{
			keyboardScanner.close();
		}
	}

	/**
	 * Get the default charset used by this program.
	 */
	public static Charset getCharset()
	{
		return charset;
	}

	/**
	 * Get personal private key
	 */
	public static PrivateKey getPrivateKey()
	{
		return rsaKeys.getPrivate();
	}

	/**
	 * Get personal public key
	 */
	public static PublicKey getPublicKey()
	{
		return rsaKeys.getPublic();
	}

	/**
	 * Startup all
	 */
	public static void main(String[] args)
	{
		Configuration.loadConfig();
		Language.setLanguage(Configuration.getProperty("log.language"));

		int logLevel = Integer.parseInt(Configuration.getProperty("log.level"));

		if (logLevel < 1 || logLevel > 6)
			logLevel = 3;

		Log.set(logLevel);
		Log.localizedInfo("[CONSOLE_WELCOME]");
		Log.localizedInfo("[CONSOLE_LOADING_SQL]");

		Database.initDatabase();

		Log.localizedInfo("[CONSOLE_LOADING_KEYS]");

		try
		{
			Statement stat = Database.getConnection().createStatement();
			ResultSet rs = stat.executeQuery("SELECT value FROM data WHERE key='keypair'");

			byte[] encodedPair = null;

			if (rs.next())
			{
				encodedPair = rs.getBytes(1);

				rsaKeys = Encoding.decodeKeyPair(encodedPair);
			}

			if (encodedPair == null || encodedPair.equals(""))
			{
				rsaKeys = RSA.generatePair();
				encodedPair = Encoding.encodeKeyPair(rsaKeys);

				PreparedStatement sta = Database.getConnection().prepareStatement("INSERT INTO data VALUES ('keypair', ?)");
				sta.setBytes(1, encodedPair);
				sta.executeUpdate();
				sta.close();
			}

			stat.close();

		}
		catch (SQLException e)
		{
			Log.localizedWarn("[SQL_QUERY_ERROR]");
		}

		if (rsaKeys == null)
		{
			Log.localizedError("[RSA_LOAD_FAIL]");
			Log.localizedError("[FATAL_ERROR]");
			close();
		}

		Log.localizedInfo("[CONSOLE_LOADING_PEERS]");
		PeerManager.init();

		Log.localizedInfo("[CONSOLE_LOADING_NET]");
		NetworkManager.init();

		updater = new UpdaterThread();
		updater.start();

		Log.localizedInfo("[CONSOLE_LOAD_COMPLETE]");

		System.out.println();

		Main.commandReader();
	}
}
