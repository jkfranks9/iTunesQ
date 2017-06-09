package itunesq;

import java.io.File;
import java.util.Iterator;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**
 * Class that provides some assistance to using logback/SLF4J for logging. This is a singleton class.
 * 
 * @author Jon
 *
 */
public class Logging
{

    //---------------- Singleton implementation ----------------------------
	
	/*
	 * Singleton class instance variable.
	 */
	private static Logging instance = null;
	
	/**
	 * Get the singleton instance.
	 * 
	 * @return Class instance.
	 */
	public static Logging getInstance ()
	{
		if (instance == null)
		{
			instance = new Logging();
		}
		
		return instance;
	}

    //---------------- Class variables -------------------------------------
	
	private Map<Dimension, List<Logger>> loggerRegistry;
	private boolean globalLogLevel;
	private Level defaultLevel;
	
	/**
	 * The dimension, or scope, of a logger. For example, XML only concerns logging related to the
	 * reading and processing of the iTunes XML file.
	 * 
	 * Each dimension contains the associated log level.
	 */
	public enum Dimension
	{
		ALL("All"), UI("UI"), XML("XML"), TRACK("Track"), PLAYLIST("Playlist"), FILTER("Filter");
		
		private String displayValue;
		private Level logLevel;
		
		/*
		 * Constructor.
		 */
		private Dimension (String s)
		{
			displayValue = s;
		}
		
		/**
		 * Get the display value.
		 * 
		 * @return The enum display value.
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}

		/**
		 * Get the current log level.
		 * 
		 * @return Current log level.
		 */
		public Level getLogLevel()
		{
			return logLevel;
		}

		/**
		 * Set the current log level.
		 * 
		 * @param logLevel Log level.
		 */
		public void setLogLevel(Level logLevel)
		{
			this.logLevel = logLevel;
		}
		
		/**
		 * Reverse lookup the enum from the display value.
		 * 
		 * @param value The display value to look up.
		 * @return The enum.
		 */
		public static Dimension getEnum(String value)
		{
	        return lookup.get(value);
	    }
		
		/*
		 * Reverse lookup capability to get the enum based on its display value.
		 */
		private static final Map<String, Dimension> lookup = new HashMap<String, Dimension>();		
		static
		{
	        for (Dimension value : Dimension.values())
	        {
	            lookup.put(value.getDisplayValue(), value);
	        }
	    }
	}

    //---------------- Private variables -----------------------------------
	
	private static final String logFileName = "iTunesQ";
	private static final String logFileSuffix = ".log";
	private static final String fileNamePattern = "%date %level [%thread] [%file:%line] %msg%n";
	
	/*
	 * Constructor. Making it private prevents instantiation by any other class.
	 */
	private Logging ()
	{
		loggerRegistry = new HashMap<Dimension, List<Logger>>();
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Register a logger.
	 * 
	 * Loggers are obtained on a per-class basis. A class can contain several loggers, each representing
	 * a different dimension. The registry is a map of dimension to a list of loggers using that
	 * dimension.
	 * 
	 * @param dimension Dimension for this logger.
	 * @param logger Logger to be registered.
	 */
	public void registerLogger (Dimension dimension, Logger logger)
	{
		
		/*
		 * Get the preferences object singleton.
		 */
		Preferences userPrefs = Preferences.getInstance();
		
        /*
         * We want to create a rolling file appender for every logger. We do this programmatically
         * instead of in the logback configuration file so we can control the log file path with
         * a preference.
         * 
         * First get the logger context for the ILoggerFactory. This gets attached to the various
         * rolling file appender components.
         */
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		/*
		 * Create the rolling file appender.
		 */
		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>();
		appender.setContext(loggerContext);

		/*
		 * Create a time based rolling policy. This rolls over the log every day, keeping maxHistory
		 * days worth in the history. Since we're a short running application, setCleanHistoryOnStart
		 * ensures that we perform the rollover check on every invocation.
		 */
		TimeBasedRollingPolicy<ILoggingEvent> policy = new TimeBasedRollingPolicy<ILoggingEvent>();
		policy.setContext(loggerContext);
		policy.setParent(appender);
		
		String saveDirectory = Preferences.getSaveDirectory();
		if (saveDirectory == null)
		{
			saveDirectory = Preferences.DEFAULT_SAVE_DIRECTORY;
		}
		policy.setFileNamePattern(saveDirectory + "/" + logFileName + "-%d" + logFileSuffix);
		policy.setMaxHistory(userPrefs.getMaxLogHistory());
		policy.setCleanHistoryOnStart(true);
		policy.start();

		/*
		 * Create a pattern layout encoder that describes the format of log records.
		 */
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(loggerContext);
		encoder.setPattern(fileNamePattern);
		encoder.start();
		
		/*
		 * Add the policy and encoder to the rolling file appender.
		 */
		appender.setRollingPolicy(policy);
		appender.setEncoder(encoder);
		appender.start();
		
		/*
		 * Finally, add the appender to the input logger.
		 */
		logger.addAppender(appender);
		
		/*
		 * We have a simple logging system, so there is no need for additivity.
		 */
		logger.setAdditive(false);
		
		/*
		 * If no loggers exist for the specified dimension, initialize a new array list.
		 */
		List<Logger> loggers = loggerRegistry.get(dimension);
		if (loggers == null)
		{
			loggers = new ArrayList<Logger>();
		}
		
		/*
		 * Add this logger to the list for the specified dimension.
		 */
		loggers.add(logger);
		
		/*
		 * Add this logger to the registry.
		 */
		loggerRegistry.put(dimension, loggers);

        /*
         * Set the log level for this logger.
         */
		if (globalLogLevel == true)
		{
			logger.setLevel(Dimension.ALL.getLogLevel());
		}
		else
		{
			logger.setLevel(dimension.getLogLevel());
		}
	}
	
	/**
	 * Move all log files when the save directory has been changed.
	 * 
	 * @param oldDirectory Directory where log files are currently located.
	 * @param newDirectory New save directory.
	 */
	public static void saveDirectoryUpdated (String oldDirectory, String newDirectory)
	{
		
		/*
		 * Loop through all files in the old directory.
		 */
		File[] files = new File(oldDirectory).listFiles();
	    for (File file : files)
	    {
	    	
	    	/*
	    	 * We only care about regular files.
	    	 */
	        if (file.isFile())
	        {
	        	
	        	/*
	        	 * We only care about our log files.
	        	 */
	        	String fileName = file.getName();
	        	if (fileName.startsWith(logFileName) && fileName.endsWith(logFileSuffix))
	        	{
	        		
	        		/*
	        		 * Rename (move) the file to the new directory.
	        		 */
	        		File newFile = new File(newDirectory + "/" + fileName);
	        		file.renameTo(newFile);
	        	}
	        }
	    }
	}
	
	/**
	 * Set the default log level.
	 * 
	 * @param level Default log level.
	 */
	public void setDefaultLogLevel (Level level)
	{
		this.defaultLevel = level;
	}
	
	/**
	 * Get the list of log level values.
	 * 
	 * @return List of log level values.
	 */
	public Sequence<String> getLogLevelValues ()
	{
		Sequence<String> levelValues = new ArrayList<String>();
		
		levelValues.add(Level.ERROR.toString());
		levelValues.add(Level.WARN.toString());
		levelValues.add(Level.INFO.toString());
		levelValues.add(Level.DEBUG.toString());
		levelValues.add(Level.TRACE.toString());
		
		return levelValues;
	}
	
	/**
	 * Set the log levels from the preferences.
	 */
	public void updateLogLevelsFromPrefs ()
	{
		
        /*
         * Get the preferences object instance.
         */
        Preferences prefs = Preferences.getInstance();
        
        /*
         * Get the global log level indicator and the associated global log level.
         */
        globalLogLevel = prefs.getGlobalLogLevel();
    	Level globalLevel = Dimension.ALL.getLogLevel();
        
        /*
         * If the log level preference exists, use it. Otherwise use the default.
         */    	
        for (Dimension dimension : Dimension.values())
        {
            Level level = prefs.getLogLevel(dimension);
        	if (level != null)
        	{
        		dimension.logLevel = level;
        	}
        	else
        	{
        		dimension.logLevel = defaultLevel;
        	}
            
            /*
             * Set the proper level for all loggers.
             */        	
        	List<Logger> loggers = loggerRegistry.get(dimension);
        	if (loggers != null)
        	{
        		Iterator<Logger> loggersIter = loggers.iterator();
        		while (loggersIter.hasNext())
        		{
        			Logger logger = loggersIter.next();

        			if (globalLogLevel == true)
        			{
        				logger.setLevel(globalLevel);
        			}
        			else
        			{
        				logger.setLevel(level);
        			}
        		}
        	}
        }
	}
	
	/**
	 * Set the maximum log history from the preference.
	 */
	public void updateMaxHistoryFromPref ()
	{
		
		/*
		 * Get the preferences object singleton.
		 */
		Preferences userPrefs = Preferences.getInstance();
        
        /*
         * Get the current maximum log history.
         */
        int maxHistory = userPrefs.getMaxLogHistory();
    	
        /*
         * Loop through all dimensions.
         */
        for (Dimension dimension : Dimension.values())
        {
        	List<Logger> loggers = loggerRegistry.get(dimension);
        	if (loggers != null)
        	{
        		
        		/*
        		 * Loop through all loggers for this dimension.
        		 */
        		Iterator<Logger> loggersIter = loggers.iterator();
        		while (loggersIter.hasNext())
        		{
        			Logger logger = loggersIter.next();
        			
        			/*
        			 * Loop through all appenders for this logger. Should only be one.
        			 */
        			Iterator<Appender<ILoggingEvent>> appenderIter = logger.iteratorForAppenders();
        			while (appenderIter.hasNext())
        			{
        				RollingFileAppender<ILoggingEvent> appender = 
        						(RollingFileAppender<ILoggingEvent>) appenderIter.next();
        				
        				/*
        				 * Get the policy for this appender.
        				 */
        				@SuppressWarnings("unchecked")
						TimeBasedRollingPolicy<ILoggingEvent> policy = 
        						(TimeBasedRollingPolicy<ILoggingEvent>) appender.getRollingPolicy();
        				
        				/*
        				 * Update the maximum history, then restart the policy so it takes effect.
        				 */
        				policy.setMaxHistory(maxHistory);
        				if (policy.isStarted())
        				{
        					policy.stop();
        				}
        				policy.start();
        			}
        		}
        	}
        }
	}
}