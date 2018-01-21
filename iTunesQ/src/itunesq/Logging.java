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
 * Class that provides some assistance for using logback/SLF4J for logging. 
 * This is a singleton class.
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
	 * Gets the singleton instance.
	 * 
	 * @return singleton class instance
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
	 * The dimension, or scope, of a logger. For example, <code>XML</code> only 
	 * concerns logging related to the reading and processing of the iTunes 
	 * XML file.
	 * <p>
	 * Each dimension contains the associated log level.
	 */
	public enum Dimension
	{
		
		/**
		 * logging associated with all dimensions
		 */
		ALL("All"),
		
		/**
		 * logging associated with the user interface
		 */
		UI("UI"),
		
		/**
		 * logging associated with processing the iTunes XML file
		 */
		XML("XML"),
		
		/**
		 * logging associated with processing of tracks
		 */
		TRACK("Track"),
		
		/**
		 * logging associated with processing of playlists
		 */
		PLAYLIST("Playlist"),
		
		/**
		 * logging associated with processing of query filters
		 */
		FILTER("Filter");
		
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
		 * Gets the display value.
		 * 
		 * @return enum display value
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}

		/**
		 * Gets the current log level.
		 * 
		 * @return current log level
		 */
		public Level getLogLevel()
		{
			return logLevel;
		}

		/**
		 * Sets the current log level.
		 * 
		 * @param logLevel current log level
		 */
		public void setLogLevel(Level logLevel)
		{
			this.logLevel = logLevel;
		}
		
		/**
		 * Performs a reverse lookup of the <code>enum</code> from the display
		 * value.
		 * 
		 * @param value display value to look up
		 * @return enum value
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
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the default log level.
	 * 
	 * @return default log level
	 */
	public Level getDefaultLogLevel ()
	{
		return defaultLevel;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Sets the default log level. This is called once early in initialization.
	 * 
	 * @param level default log level
	 */
	public void setDefaultLogLevel (Level level)
	{
		this.defaultLevel = level;

		/*
		 * Bootstrap logging by setting the global log level flag true and setting the input
		 * default log level for the ALL dimension.
		 */
		globalLogLevel = true;
		Dimension.ALL.setLogLevel(level);
	}
	
	/**
	 * Registers a logger.
	 * <p>
	 * Loggers are obtained on a per-class basis. A class can contain several
	 * loggers, each representing a different dimension. The registry is a map
	 * of dimension to a list of loggers using that dimension.
	 * 
	 * @param dimension dimension for this logger
	 * @param logger logger to be registered
	 */
	public void registerLogger (Dimension dimension, Logger logger)
	{
		if (dimension == null)
		{
			throw new IllegalArgumentException("dimension argument is null");
		}
		
		if (logger == null)
		{
			throw new IllegalArgumentException("logger argument is null");
		}
		
		boolean registrationNeeded = true;
		
		/*
		 * If no loggers exist for the specified dimension, initialize a new array list.
		 */
		List<Logger> loggers = loggerRegistry.get(dimension);
		if (loggers == null)
		{
			loggers = new ArrayList<Logger>();
		}
		
		/*
		 * Loggers exist for the specified dimension. So first check if the input logger is already
		 * registered. Only continue if not.
		 */
		else
		{
    		Iterator<Logger> loggersIter = loggers.iterator();
    		while (loggersIter.hasNext())
    		{
    			Logger existingLogger = loggersIter.next();

    			if (existingLogger.getName().equals(logger.getName()))
    			{
    				registrationNeeded = false;
    			}
    		}
		}
		
		/*
		 * Now register the logger if needed.
		 */
		if (registrationNeeded == true)
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
				saveDirectory = Preferences.getDefaultSaveDirectory();
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
	}
	
	/**
	 * Moves all log files when the save directory has been changed.
	 * 
	 * @param oldDirectory directory where log files are currently located
	 * @param newDirectory new save directory
	 */
	public static void saveDirectoryUpdated (String oldDirectory, String newDirectory)
	{
    	if (oldDirectory == null)
    	{
    		throw new IllegalArgumentException("oldDirectory argument is null");
    	}
    	
    	if (newDirectory == null)
    	{
    		throw new IllegalArgumentException("newDirectory argument is null");
    	}
		
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
	 * Gets the list of log level values.
	 * 
	 * @return list of log level values
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
	 * Sets the log levels for all dimensions from the preferences.
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
        		dimension.setLogLevel(level);
        	}
        	else
        	{
        		dimension.setLogLevel(defaultLevel);
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
	 * Sets the maximum log history from the preference.
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