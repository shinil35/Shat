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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.minlog.Log;

public class Database
{
	private static Connection connection;
	private static boolean initialized = false;

	/**
	 * Close the database connection
	 */
	public static void close()
	{
		if (!initialized)
			return;

		initialized = false;

		try
		{
			if (connection != null)
				connection.close();
		}
		catch (SQLException e)
		{
		}
	}

	/**
	 * Execute one update query.
	 * 
	 * @param query
	 *            to be executed.
	 */
	public static void executeUpdate(String query)
	{
		if (!initialized)
			return;

		try
		{
			Statement sta = connection.createStatement();
			sta.executeUpdate(query);
			sta.close();
		}
		catch (SQLException e)
		{
			Log.localizedWarn("[SQL_QUERY_ERROR]", e.getMessage());
		}
	}

	/**
	 * 
	 * @return The database connection.
	 */
	public static Connection getConnection()
	{
		return connection;
	}

	/**
	 * Connect to the database and create tables if don't exists.
	 */
	public static void initDatabase()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:shat.db");

			initialized = true;

			loadTables();
		}
		catch (Exception e)
		{
			Log.localizedError("[SQL_INIT_ERROR]", e.getMessage());
			Log.localizedError("[FATAL_ERROR]", e.getMessage());
			Main.close();
		}
	}

	/**
	 * 
	 * @return True if the connection was stabilished, false if not.
	 */
	public static boolean isInitialized()
	{
		return initialized;
	}

	/**
	 * Create new database default tables.
	 * 
	 * @throws SQLException
	 */
	public static void loadTables() throws SQLException
	{
		if (!initialized)
			return;

		Statement sta = connection.createStatement();

		ResultSet rs = sta.executeQuery("SELECT * FROM sqlite_master WHERE type='table';");

		List<String> tables = new ArrayList<String>();

		while (rs.next())
			tables.add(rs.getString(2));

		if (!tables.contains("peers") || !tables.contains("data"))
		{
			sta.executeUpdate("DELETE IF EXISTS peers");
			sta.executeUpdate("DELETE IF EXISTS data");

			sta.executeUpdate("CREATE TABLE peers (hash  text  primary key  not null, public_key  BLOB  not null, "
					+ "ip  varchar(15)  not null, port  uint  not null, last_access  int64  not null);");

			sta.executeUpdate("CREATE TABLE data (key  varchar(255)  primary key  not null, value  BLOB  not null);");
		}

		sta.close();
	}

}
