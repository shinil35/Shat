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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration
{
	private static Properties defaultConfig;
	private static Properties personalConfig;

	/**
	 * @return The configuration value for that key.
	 */
	public static String getProperty(String key)
	{
		if (personalConfig != null && personalConfig.containsKey(key))
			return personalConfig.getProperty(key);
		else if (defaultConfig.containsKey(key))
			return defaultConfig.getProperty(key);
		else
			throw new IllegalStateException(Language.getLocalizedText("[CONFIG_KEY_ERROR]", key));
	}

	/**
	 * Set default configurations and load personal configuration file.
	 */
	public static void loadConfig()
	{
		loadDefaultConfig();

		File configFile = new File("config");

		if (!configFile.exists())
			return;

		try
		{
			personalConfig = new Properties();
			personalConfig.load(new FileInputStream(configFile));
		}
		catch (IOException e)
		{
			System.out.println(Language.getLocalizedText("[CONFIG_LOAD_ERROR]", e.getMessage()));
		}
	}

	/**
	 * Set the default configurations.
	 */
	private static void loadDefaultConfig()
	{
		defaultConfig = new Properties();

		defaultConfig.setProperty("log.language", "EN");
		defaultConfig.setProperty("log.level", "3");

		defaultConfig.setProperty("network.listening.enabled", "true");
		defaultConfig.setProperty("network.listening.port", "8960");

		defaultConfig.setProperty("network.bootstrap.enabled", "false");
		defaultConfig.setProperty("network.bootstrap.host", "0.0.0.0");
		defaultConfig.setProperty("network.bootstrap.port", "0");
	}

}
