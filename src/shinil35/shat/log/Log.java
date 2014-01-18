package shinil35.shat.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import shinil35.shat.Language;

/**
 * Modified for Shat Encrypted P2P Chat from Shinil35.
 * 
 * A low overhead, lightweight logging system.
 * 
 * @author Nathan Sweet <misc@n4te.com>
 */

public class Log
{
	static public class Logger
	{
		private long firstLogTime = new Date().getTime();

		public void log(int level, String category, String message, Throwable ex)
		{
			StringBuilder builder = new StringBuilder(256);

			long time = new Date().getTime() - firstLogTime;
			long minutes = time / (1000 * 60);
			long seconds = time / (1000) % 60;
			if (minutes <= 9)
				builder.append('0');
			builder.append(minutes);
			builder.append(':');
			if (seconds <= 9)
				builder.append('0');
			builder.append(seconds);

			switch (level)
			{
				case LEVEL_ERROR:
					builder.append(" ERROR: ");
					break;
				case LEVEL_WARN:
					builder.append("  WARN: ");
					break;
				case LEVEL_INFO:
					builder.append("  INFO: ");
					break;
				case LEVEL_DEBUG:
					builder.append(" DEBUG: ");
					break;
				case LEVEL_TRACE:
					builder.append(" TRACE: ");
					break;
			}

			if (category != null)
			{
				builder.append('[');
				builder.append(category);
				builder.append("] ");
			}

			builder.append(message);

			if (ex != null)
			{
				StringWriter writer = new StringWriter(256);
				ex.printStackTrace(new PrintWriter(writer));
				builder.append('\n');
				builder.append(writer.toString().trim());
			}

			print(builder.toString());
		}
		
		protected void print(String message)
		{
			System.out.println(message);
		}
	}

	static public final int LEVEL_NONE = 6;
	static public final int LEVEL_ERROR = 5;
	static public final int LEVEL_WARN = 4;
	static public final int LEVEL_INFO = 3;
	static public final int LEVEL_DEBUG = 2;
	static public final int LEVEL_TRACE = 1;

	static private int level = LEVEL_INFO;
	static public boolean ERROR = level <= LEVEL_ERROR;
	static public boolean WARN = level <= LEVEL_WARN;
	static public boolean INFO = level <= LEVEL_INFO;
	static public boolean DEBUG = level <= LEVEL_DEBUG;

	static public boolean TRACE = level <= LEVEL_TRACE;

	static private Logger logger = new Logger();

	static private void debug(String message)
	{
		if (DEBUG)
			logger.log(LEVEL_DEBUG, null, message, null);
	}

	static public void DEBUG()
	{
		set(LEVEL_DEBUG);
	}

	static private void error(String message)
	{
		if (ERROR)
			logger.log(LEVEL_ERROR, null, message, null);
	}

	static public void ERROR()
	{
		set(LEVEL_ERROR);
	}

	static private void info(String message)
	{
		if (INFO)
			logger.log(LEVEL_INFO, null, message, null);
	}

	static public void INFO()
	{
		set(LEVEL_INFO);
	}

	static public void localizedDebug(String languageString)
	{
		localizedDebug(languageString, null);
	}

	static public void localizedDebug(String languageString, String arg)
	{
		String parts[] = Language.getLocalizedText(languageString, arg).split("\n");

		for (String s : parts)
			debug(s);
	}

	static public void localizedError(String languageString)
	{
		localizedError(languageString, null);
	}

	static public void localizedError(String languageString, String arg)
	{
		String parts[] = Language.getLocalizedText(languageString, arg).split("\n");

		for (String s : parts)
			error(s);
	}

	static public void localizedInfo(String languageString)
	{
		localizedInfo(languageString, null);
	}

	static public void localizedInfo(String languageString, String arg)
	{
		String parts[] = Language.getLocalizedText(languageString, arg).split("\n");

		for (String s : parts)
			info(s);
	}

	static public void localizedTrace(String languageString)
	{
		localizedTrace(languageString, null);
	}

	static public void localizedTrace(String languageString, String arg)
	{
		String parts[] = Language.getLocalizedText(languageString, arg).split("\n");

		for (String s : parts)
			trace(s);
	}

	static public void localizedWarn(String languageString)
	{
		localizedWarn(languageString, null);
	}

	static public void localizedWarn(String languageString, String arg)
	{
		String parts[] = Language.getLocalizedText(languageString, arg).split("\n");

		for (String s : parts)
			warn(s);
	}

	static public void NONE()
	{
		set(LEVEL_NONE);
	}
	
	static public void set(int level)
	{
		Log.level = level;
		ERROR = level <= LEVEL_ERROR;
		WARN = level <= LEVEL_WARN;
		INFO = level <= LEVEL_INFO;
		DEBUG = level <= LEVEL_DEBUG;
		TRACE = level <= LEVEL_TRACE;
	}
	
	static public void setLogger(Logger logger)
	{
		Log.logger = logger;
	}

	// Public method to use specially in debugging with not-localized strings.
	static public void trace(String message, LogTraceType traceType)
	{
		if (TRACE && LogTraceManager.isToTrace(traceType))
			logger.log(LEVEL_TRACE, traceType.name(), message, null);
	}
	
	static private void trace(String message)
	{
		if (TRACE)
			logger.log(LEVEL_TRACE, null, message, null);
	}

	static public void TRACE()
	{
		set(LEVEL_TRACE);
	}

	static private void warn(String message)
	{
		if (WARN)
			logger.log(LEVEL_WARN, null, message, null);
	}

	static public void WARN()
	{
		set(LEVEL_WARN);
	}

	private Log()
	{
	}
}