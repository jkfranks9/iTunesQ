package itunesq;

import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

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
	
	/**
	 * Constructor.
	 */
	public Logging ()
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
	
	public void setDefaultLogLevel (Level level)
	{
		this.defaultLevel = level;
	}
	
	/**
	 * Get the list of log levels.
	 * 
	 * @return List of log levels.
	 */
	public Sequence<String> getLogLevels ()
	{
		Sequence<String> levels = new ArrayList<String>();
		
		levels.add(Level.ERROR.toString());
		levels.add(Level.WARN.toString());
		levels.add(Level.INFO.toString());
		levels.add(Level.DEBUG.toString());
		levels.add(Level.TRACE.toString());
		
		return levels;
	}
	
	/**
	 * Set the log levels from the preferences.
	 */
	public void setLogLevelsFromPrefs ()
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
}
