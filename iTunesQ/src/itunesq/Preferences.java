package itunesq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class that represents user preferences. This is a singleton class.
 * 
 * @author Jon
 *
 */
public final class Preferences implements Serializable
{
	
	/*
	 * NOTE: This class is serializable, but only a subset of variables actually need to be
	 * serialized. The rest are declared as transient to prevent serialization.
	 */

    //---------------- Singleton implementation ----------------------------
	
	/*
	 * Singleton class instance variable.
	 */
	private static transient Preferences instance = null;
	
	/**
	 * Gets the singleton instance.
	 * 
	 * @return singleton class instance
	 */
	public static Preferences getInstance ()
	{
		if (instance == null)
		{
			instance = new Preferences();
		}
		
		return instance;
	}

    //---------------- Class variables -------------------------------------
	
	/*
	 * Variables for the actual preferences we want to serialize.
	 * 
	 * - iTunes XML file name
	 * - list of bypass playlist preferences
	 * - list of ignored playlist preferences
	 * - various track column sets
	 * - skin name
	 * - maximum log file history
	 * - global log level flag
	 * - log levels
	 */
	private String xmlFileName;
	private List<BypassPreference> bypassPrefs;
	private List<String> ignoredPrefs;
	private List<List<String>> trackColumnsFullView;
	private List<List<String>> trackColumnsFilteredView;
	private List<List<String>> trackColumnsPlaylistView;
	private String skinName;
	private int maxLogHistory;
	private boolean globalLogLevel;
	private Map<String, Level> logLevels;
	
	/*
	 * Default save directory for things like preferences and log files.
	 */
	
	private static transient final String HOME_ENV = System.getenv("HOME");
	private static transient final String DEFAULT_PREFS_PATH = "itq";
	private static transient final String DEFAULT_SAVE_DIRECTORY = HOME_ENV + "/" + DEFAULT_PREFS_PATH;
	
	/*
	 * Save directory name.
	 */
	private static transient String saveDirectory;
	
    //---------------- Private variables -----------------------------------
	
	/*
	 * Serialized file name suffix.
	 */
	private static transient final String PREFS_SUFFIX = ".ser";
	
	/*
	 * The serialized file name includes the class name. That's initialized in updateSaveDirectory(),
	 * which is called by MainWindow() before our constructor.
	 */
	private static transient String prefsFile;

	/*
	 * Other variables.
	 */
	private static transient FileInputStream prefsInputStream;
	private static transient FileOutputStream prefsOutputStream;
	private transient Logger logger = null;
	
	private static final long serialVersionUID = -543909365447180812L;
	
	/*
	 * Constructor. Making it private prevents instantiation by any other class.
	 */
	private Preferences ()
	{
    	
    	/*
    	 * Initialize variables.
    	 * 
    	 * NOTE: We initialize the ignored playlists with the default value. They get replaced when
    	 * the preferences are read, if they exist in the serialized object.
    	 */
		bypassPrefs = new ArrayList<BypassPreference>();
		ignoredPrefs = new ArrayList<String>(Playlist.DEFAULT_IGNORED_PLAYLISTS);
		
		maxLogHistory = InternalConstants.DEFAULT_MAX_HISTORY;
		
		logLevels = new HashMap<String, Level>();
		globalLogLevel = true;
	}
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the XML File name preference.
	 * 
	 * @return XML file name
	 */
	public String getXMLFileName ()
	{
		return xmlFileName;
	}
	
	/**
	 * Sets the XML File name preference.
	 * 
	 * @param xmlFile XML file name
	 */
	public void setXMLFileName (String xmlFile)
	{
		xmlFileName = xmlFile;
	}

	/**
	 * Gets the list of bypass playlist preferences.
	 * 
	 * @return list of bypass playlist preferences
	 */
	public List<BypassPreference> getBypassPrefs ()
	{
		return bypassPrefs;
	}

	/**
	 * Gets the list of ignored playlist preferences.
	 * 
	 * @return list of ignored playlist preferences
	 */
	public List<String> getIgnoredPrefs ()
	{
		return ignoredPrefs;
	}
	
	/**
	 * Gets the list of full tracks column preferences.
	 * 
	 * @return list of full tracks column preferences
	 */
	public List<List<String>> getTrackColumnsFullView()
	{
		return trackColumnsFullView;
	}

	/**
	 * Sets the list of full tracks column preferences.
	 * 
	 * @param trackColumnsFullView list of full tracks column preferences
	 */
	public void setTrackColumnsFullView(List<List<String>> trackColumnsFullView)
	{
		this.trackColumnsFullView = trackColumnsFullView;
	}
	
	/**
	 * Gets the list of filtered tracks column preferences.
	 * 
	 * @return list of filtered tracks column preferences
	 */
	public List<List<String>> getTrackColumnsFilteredView()
	{
		return trackColumnsFilteredView;
	}

	/**
	 * Sets the list of filtered tracks column preferences.
	 * 
	 * @param trackColumnsFilteredView list of filtered tracks column preferences
	 */
	public void setTrackColumnsFilteredView(List<List<String>> trackColumnsFilteredView)
	{
		this.trackColumnsFilteredView = trackColumnsFilteredView;
	}
	
	/**
	 * Gets the list of playlist tracks column preferences.
	 * 
	 * @return list of playlist tracks column preferences
	 */
	public List<List<String>> getTrackColumnsPlaylistView()
	{
		return trackColumnsPlaylistView;
	}

	/**
	 * Sets the list of playlist tracks column preferences.
	 * 
	 * @param trackColumnsPlaylistView list of playlist tracks column preferences
	 */
	public void setTrackColumnsPlaylistView(List<List<String>> trackColumnsPlaylistView)
	{
		this.trackColumnsPlaylistView = trackColumnsPlaylistView;
	}
	
	/**
	 * Gets the skin name.
	 * 
	 * @return skin name
	 */
	public String getSkinName ()
	{
		return skinName;
	}
	
	/**
	 * Sets the skin name.
	 * 
	 * @param skin skin name
	 */
	public void setSkinName (String skin)
	{
		this.skinName = skin;
	}
	
	/**
	 * Gets the maximum log history.
	 * 
	 * @return maximum log history
	 */
	public int getMaxLogHistory ()
	{
		return maxLogHistory;
	}
	
	/**
	 * Sets the maximum log history.
	 * 
	 * @param maxHistory maximum log history
	 */
	public void setMaxLogHistory (int maxHistory)
	{
		this.maxLogHistory = maxHistory;
	}
	
	/**
	 * Gets the global log level indicator.
	 * 
	 * @return global log level indicator
	 */
	public boolean getGlobalLogLevel ()
	{
		return globalLogLevel;
	}
	
	/**
	 * Sets the global log level indicator.
	 * 
	 * @param level global log level indicator
	 */
	public void setGlobalLogLevel (boolean level)
	{
		globalLogLevel = level;
	}
	
	/**
	 * Gets the log level for a given logging dimension.
	 * 
	 * @param dimension logging dimension
	 * @return log level
	 */
	public Level getLogLevel (Logging.Dimension dimension)
	{
		return logLevels.get(dimension.getDisplayValue());
		
	}
	
	/**
	 * Sets the log level for a given logging dimension.
	 * 
	 * @param dimension logging dimension
	 * @param level log level
	 */
	public void setLogLevel (Logging.Dimension dimension, Level level)
	{
		logLevels.put(dimension.getDisplayValue(), level);
	}
	
	/**
	 * Gets the default save directory.
	 * 
	 * @return default save directory
	 */
	public static String getDefaultSaveDirectory ()
	{
		return DEFAULT_SAVE_DIRECTORY;
	}
	
	/**
	 * Gets the default maximum log history.
	 * 
	 * @return default maximum log history
	 */
	public static int getDefaultMaxLogHistory ()
	{
		return InternalConstants.DEFAULT_MAX_HISTORY;
	}
	
	/**
	 * Gets the preferences save directory.
	 * 
	 * @return preferences save directory
	 */
	public static String getSaveDirectory ()
	{
		return saveDirectory;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Replaces the list of bypass playlist preferences.
	 * 
	 * @param bypassPrefs list of new bypass playlist preferences
	 */
	public void replaceBypassPrefs (List<BypassPreference> bypassPrefs)
	{
		logger.trace("replaceBypassPrefs: " + this.hashCode());
		
		this.bypassPrefs.clear();
		Iterator<BypassPreference> bypassPrefsIter = bypassPrefs.iterator();
		while (bypassPrefsIter.hasNext())
		{
			this.bypassPrefs.add(bypassPrefsIter.next());
		}
	}
	
	/**
	 * Replaces the list of ignored playlist preferences.
	 * 
	 * @param ignoredPrefs list of new ignored playlist preferences
	 */
	public void replaceIgnoredPrefs (List<String> ignoredPrefs)
	{
		logger.trace("replaceIgnoredPrefs: " + this.hashCode());
		
		this.ignoredPrefs.clear();
		Iterator<String> ignoredPrefsIter = ignoredPrefs.iterator();
		while (ignoredPrefsIter.hasNext())
		{
			this.ignoredPrefs.add(ignoredPrefsIter.next());
		}
	}
	
	/**
	 * Replaces the list of full tracks column preferences.
	 * 
	 * @param trackColumnsPrefs list of full tracks column preferences
	 */
	public void replaceTrackColumnsFullView (List<List<String>> trackColumnsPrefs)
	{
		logger.trace("replaceTrackColumnsFullView: " + this.hashCode());
		
		this.trackColumnsFullView.clear();
		Iterator<List<String>> trackColumnsPrefsIter = trackColumnsPrefs.iterator();
		while (trackColumnsPrefsIter.hasNext())
		{
			this.trackColumnsFullView.add(trackColumnsPrefsIter.next());
		}
	}
	
	/**
	 * Replaces the list of filtered tracks column preferences.
	 * 
	 * @param trackColumnsPrefs list of filtered tracks column preferences
	 */
	public void replaceTrackColumnsFilteredView (List<List<String>> trackColumnsPrefs)
	{
		logger.trace("replaceTrackColumnsFilteredView: " + this.hashCode());
		
		this.trackColumnsFilteredView.clear();
		Iterator<List<String>> trackColumnsPrefsIter = trackColumnsPrefs.iterator();
		while (trackColumnsPrefsIter.hasNext())
		{
			this.trackColumnsFilteredView.add(trackColumnsPrefsIter.next());
		}
	}
	
	/**
	 * Replaces the list of playlist tracks column preferences.
	 * 
	 * @param trackColumnsPrefs list of playlist tracks column preferences
	 */
	public void replaceTrackColumnsPlaylistView (List<List<String>> trackColumnsPrefs)
	{
		logger.trace("replaceTrackColumnsPlaylistView: " + this.hashCode());
		
		this.trackColumnsPlaylistView.clear();
		Iterator<List<String>> trackColumnsPrefsIter = trackColumnsPrefs.iterator();
		while (trackColumnsPrefsIter.hasNext())
		{
			this.trackColumnsPlaylistView.add(trackColumnsPrefsIter.next());
		}
	}
	
	/**
	 * Sets the preferences save directory, and moves the existing preferences 
	 * file and log files to the new directory.
	 * 
	 * @param directory preferences save directory
	 */
	public static void updateSaveDirectory (String directory)
	{
		
		/*
	     * This method is called in two places:
	     * 
	     * 1) During initialization. In this case, it is called before our constructor. 
	     *    That's because our constructor registers a logger, which in turn requires 
	     *    the correct save directory to be set.
	     *    
	     *    If this is the very first time the application has been run, we won't have an
	     *    existing preferences file, and saveDirectory will be null.
	     *    
	     *    If the application has been run before, then the directory cannot have
	     *    changed, so we have very little to do.
	     *    
	     * 2) When the directory has been changed via user preferences. In this case we need
	     *    to move all relevant files to the new directory.
		 */
		
		/*
		 * Initialize the save path suffix (everything after the directory name).
		 */
		String savePathSuffix = "/" + Preferences.class.getName() + PREFS_SUFFIX;
		
		/*
		 * Create File objects for the existing and new files. It's possible we don't have an
		 * existing file or directory.
		 */
		File existingFile;
		if (saveDirectory != null)
		{
			existingFile = new File(saveDirectory + savePathSuffix);
		}
		else
		{
			existingFile = new File(DEFAULT_SAVE_DIRECTORY + savePathSuffix);
		}
		File newFile = new File(directory + savePathSuffix);
		
		/*
		 * If we have an existing file, and it's different than the new file, rename (move)
		 * the existing file and log files to the new directory.
		 */
		if (existingFile.exists() && !existingFile.equals(newFile))
		{
			existingFile.renameTo(newFile);

			Logging.saveDirectoryUpdated(saveDirectory, directory);
		}
		
		/*
		 * Update our saved directory name.
		 */
		saveDirectory = directory;

		/*
		 * Rebuild the preferences file path name.
		 */
		prefsFile = directory + savePathSuffix;
	}
	
	/**
	 * Initializes the logger for this class.
	 * <p>
	 * This cannot be done in the constructor, because calling 
	 * <code>registerLogger</code> from the constructor would cause an endless
	 * loop, in turn because <code>registerLogger</code> needs to access 
	 * this class.
	 */
	public void initializeLogging ()
	{
    	
    	/*
    	 * Create a UI logger.
    	 */
    	String className = getClass().getSimpleName();
    	logger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Get the logging object singleton.
    	 */
    	Logging logging = Logging.getInstance();
    	
    	/*
    	 * Register our logger.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, logger);
    	
    	logger.trace("initializeLogging: " + this.hashCode());
	}
	
	/**
	 * Updates the preferences from a preferences object. This is expected to 
	 * be called after reading the serialized preferences.
	 * 
	 * @param prefs deserialized preferences object
	 */
	public void updatePreferences (Preferences prefs)
	{
		logger.info("updating preferences");
		
		this.xmlFileName = prefs.xmlFileName;
		if (prefs.bypassPrefs != null)
		{
			replaceBypassPrefs(prefs.bypassPrefs);
		}
		if (prefs.ignoredPrefs != null)
		{
			replaceIgnoredPrefs(prefs.ignoredPrefs);
		}
		this.trackColumnsFullView = prefs.trackColumnsFullView;
		this.trackColumnsFilteredView = prefs.trackColumnsFilteredView;
		this.trackColumnsPlaylistView = prefs.trackColumnsPlaylistView;
		this.skinName = prefs.skinName;
		this.maxLogHistory = prefs.maxLogHistory;
		this.globalLogLevel = prefs.globalLogLevel;
		this.logLevels = prefs.logLevels;
	}

	/**
	 * Deserializes the preferences from disk.
	 * 
	 * @return deserialized preferences
	 * @throws IOException If an error occurs trying to read the preferences 
	 * file.
	 * @throws ClassNotFoundException If the class of a serialized object 
	 * cannot be found.
	 */
	public Preferences readPreferences () 
			throws IOException, ClassNotFoundException
	{
		Preferences prefs = null;
		logger.info("reading preferences from '" + prefsFile + "'");
		
		try
		{
			prefsInputStream = new FileInputStream(prefsFile);
			ObjectInputStream input = new ObjectInputStream(prefsInputStream);
			prefs = (Preferences) input.readObject();
			input.close();
			prefsInputStream.close();
		} 
		catch (FileNotFoundException e)
		{
			// Not an error - ignore.
		}
		
		return prefs;
	}
	
	/**
	 * Serializes the preferences to disk.
	 * @throws IOException If an error occurs trying to write the preferences 
	 * file. 
	 */
	public void writePreferences () 
			throws IOException
	{
		logger.info("writing preferences to '" + prefsFile + "'");

		prefsOutputStream = new FileOutputStream(prefsFile);
		ObjectOutputStream output = new ObjectOutputStream(prefsOutputStream);
		output.writeObject(this);
		output.close();
		prefsOutputStream.close();
	}
}
