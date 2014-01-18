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

import java.util.HashMap;

public class Language
{

	private static String language = "";
	private static HashMap<String, String> texts;

	/**
	 * @return the localized message for that language code.
	 */
	public static String getLocalizedText(String value)
	{
		return getLocalizedText(value, null);
	}

	/**
	 * @return the localized message for that language code and arguments.
	 */
	public static String getLocalizedText(String value, String arg)
	{
		if (texts == null || texts.isEmpty())
			loadLanguage();

		if (!texts.containsKey(value))
			return value;

		String finalText = texts.get(value);

		if (arg != null && !arg.equals(""))
			finalText = finalText.replace("$arg$", arg);

		return finalText;
	}

	/**
	 * Load the default language
	 */
	public static void loadLanguage()
	{
		texts = new HashMap<String, String>();

		switch (language)
		{
			case "IT":
			{
				texts.put("[CONSOLE_WELCOME]", "Benvenuto in Shat!");
				texts.put("[CONSOLE_LOADING_SQL]", "Caricamento database sql..");
				texts.put("[CONSOLE_LOADING_KEYS]", "Caricamento coppia di chiavi..");
				texts.put("[CONSOLE_LOADING_NET]", "Caricamento network..");
				texts.put("[CONSOLE_LOADING_PEERS]", "Caricamento peer manager..");
				texts.put("[CONSOLE_LOAD_COMPLETE]", "Caricamento riuscito!");

				texts.put(
						"[CONSOLE_COMMAND_HELP]",
						"Utilizzo comandi: \nconnectionlist - Visualizza la lista delle connessioni\npeerlist - Visualizza la lista dei peer conosciuti\nhelp - Visualizza questo messaggio\nexit - Chiudi il programma");
				texts.put("[CONSOLE_COMMAND_INTRO]", "Per informazioni su come usare i comandi digitare \"help\"");

				texts.put("[CONFIG_LOAD_ERROR]", "Caricamento della configurazione fallito");
				texts.put("[CONFIG_KEY_ERROR]", "La chiave di configurazione \"$arg$\" non è valida");

				texts.put("[SQL_INIT_ERROR]", "Errore nel caricamento del database: \"$arg$\"");
				texts.put("[SQL_DELETEDB_ERROR]", "Errore nella cancellazione del database: \"$arg$\"");
				texts.put("[SQL_QUERY_ERROR]", "Errore nell'esecuzione della query: \"$arg$\"");

				texts.put("[RSA_LOAD_FAIL]", "Caricamento delle chiavi RSA fallito");
				texts.put("[RSA_GENERATION_ERROR]", "Errore nella generazione di un keypair RSA: \"$arg$\"");
				texts.put("[RSA_ENCRYPTION_ERROR]", "Errore nella criptazione RSA: \"$arg$\"");
				texts.put("[RSA_DECRYPTION_ERROR]", "Errore nella decriptazione RSA: \"$arg$\"");

				texts.put("[HASHING_ERROR]", "Errore durante un hashing: \"$arg$\"");

				texts.put("[ENCODING_ERROR]", "Errore nella codifica: \"$arg$\"");
				texts.put("[DECODING_ERROR]", "Errore nella decodifica: \"$arg$\"");

				texts.put("[NETWORK_PORT_INVALID]", "Porta inserita non valida");
				texts.put("[NETWORK_LOAD_FAILED]", "Avvio del server fallito");
				texts.put("[NETWORK_CONNECTION_FAILED]", "Connessione fallita: \"$arg$\"");
				texts.put("[NETWORK_LISTENING_FAILED]", "Creazione server fallita: \"$arg$\"");
				texts.put("[NETWORK_BOOTSTRAP_INVALID]", "Dati per il bootstrap invalidi");
				
				texts.put("[UNKNOW_PACKET]", "Ricevuto un pacchetto sconosciuto, classe: \"$arg$\"");

				texts.put("[FATAL_ERROR]", "Errore fatale, inviare il log della console ad uno sviluppatore.");
				break;
			}

			default:
			case "EN":
			{
				texts.put("[CONSOLE_WELCOME]", "Welcome!");
				texts.put("[CONSOLE_LOADING_SQL]", "Loading sql database..");
				texts.put("[CONSOLE_LOADING_KEYS]", "Loading rsa keypair..");
				texts.put("[CONSOLE_LOADING_NET]", "Loading network..");
				texts.put("[CONSOLE_LOADING_PEERS]", "Loading peer manager..");
				texts.put("[CONSOLE_LOAD_COMPLETE]", "Loading succeful!");

				texts.put("[CONSOLE_COMMAND_HELP]", "Command usage: \nhelp - Show this message\nexit - Close the program");
				texts.put("[CONSOLE_COMMAND_INTRO]", "For informations of command usage digit \"help\"");

				texts.put("[CONFIG_LOAD_ERROR]", "Loading of configuration file failed");
				texts.put("[CONFIG_KEY_ERROR]", "The configuration key \"$arg$\" is not valid");

				texts.put("[SQL_INIT_ERROR]", "Database loading failed: \"$arg$\"");
				texts.put("[SQL_DELETEDB_ERROR]", "Database deleting failed: \"$arg$\"");
				texts.put("[SQL_QUERY_ERROR]", "Query failed: \"$arg$\"");

				texts.put("[RSA_LOAD_FAIL]", "Loading of rsa keys failed");
				texts.put("[RSA_GENERATION_ERROR]", "RSA KeyPair generation failed: \"$arg$\"");
				texts.put("[RSA_ENCRYPTION_ERROR]", "RSA Encryption failed: \"$arg$\"");
				texts.put("[RSA_DECRYPTION_ERROR]", "RSA Decryption failed: \"$arg$\"");

				texts.put("[HASHING_ERROR]", "Hashing failed: \"$arg$\"");

				texts.put("[ENCODING_ERROR]", "Encoding failed: \"$arg$\"");
				texts.put("[DECODING_ERROR]", "Decoding failed: \"$arg$\"");

				texts.put("[NETWORK_PORT_INVALID]", "Invalid port number");
				texts.put("[NETWORK_LOAD_FAILED]", "Server start failed");
				texts.put("[NETWORK_CONNECTION_FAILED]", "Connection failed: \"$arg$\"");
				texts.put("[NETWORK_LISTENING_FAILED]", "Server creation failed: \"$arg$\"");
				texts.put("[NETWORK_BOOTSTRAP_INVALID]", "Invalid bootstrap informations");

				texts.put("[UNKNOW_PACKET]", "Unknow packet received, class: \"$arg$\"");
				
				texts.put("[FATAL_ERROR]", "Fatal error, send the console log at one developer.");
				break;
			}
		}

	}

	/**
	 * Set the default language.
	 */
	public static void setLanguage(String lang)
	{
		language = lang.toUpperCase();
	}

}
