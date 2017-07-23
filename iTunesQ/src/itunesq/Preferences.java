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

    //---------------- Singleton implementation ----------------------------
	
	/*
	 * Singleton class instance variable.
	 */
	private static transient Preferences instance = null;
	
	/**
	 * Get the singleton instance.
	 * 
	 * @return Class instance.
	 */
	public static Preferences getInstance ()
	{
		if (instance == null)
		{
			instance = new Preferences();
		}
		
		return instance;
	}

    //---------------- Public variables ------------------------------------
	
	/*
	 * Serialized file directory constants.
	 * 
	 * (Of course only of these is public, but I like to put public variables first, and I need to
	 * define the private variables before the public one.) 
	 */
	
	private static transient final String HOME_ENV = System.getenv("HOME");
	private static transient final String DEFAULT_PREFS_PATH = "itq";
	
	public static transient final String DEFAULT_SAVE_DIRECTORY = HOME_ENV + "/" + DEFAULT_PREFS_PATH;

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
	 * - global log level
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
	
    //---------------- Private variables -----------------------------------
	
	/*
	 * Save directory name.
	 */
	private static transient String saveDirectory;
	
	/*
	 * Serialized file name suffix.
	 */
	private static transient final String PREFS_SUFFIX = ".ser";
	
	/*
	 * Default maximum log history.
	 */
	private static transient final int DEFAULT_MAX_HISTORY = 30;
	
	/*
	 * The serialized file name includes the class name. That's initialized in setSaveDirectory(),
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
		
		maxLogHistory = DEFAULT_MAX_HISTORY;
		
		logLevels = new HashMap<String, Level>();
		globalLogLevel = true;
	}
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Get the XML File name preference.
	 * 
	 * @return XML file name.
	 */
	public String getXMLFileName ()
	{
		return xmlFileName;
	}
	
	/**
	 * Set the XML File name preference.
	 * 
	 * @param xmlFile XML file name.
	 */
	public void setXMLFileName (String xmlFile)
	{
		xmlFileName = xmlFile;
	}

	/**
	 * Get the list of bypass playlist preferences.
	 * 
	 * @return Bypass playlist preferences list.
	 */
	public List<BypassPreference> getBypassPrefs ()
	{
		return bypassPrefs;
	}

	/**
	 * Get the list of ignored playlist preferences.
	 * 
	 * @return Ignored playlist preferences list.
	 */
	public List<String> getIgnoredPrefs ()
	{
		return ignoredPrefs;
	}
	
	/**
	 * Get the full tracks column preferences.
	 * 
	 * @return List of full tracks column preferences.
	 */
	public List<List<String>> getTrackColumnsFullView()
	{
		return trackColumnsFullView;
	}

	/**
	 * Set the full tracks column preferences.
	 * 
	 * @param trackColumnsFullView List of full tracks column preferences.
	 */
	public void setTrackColumnsFullView(List<List<String>> trackColumnsFullView)
	{
		this.trackColumnsFullView = trackColumnsFullView;
	}
	
	/**
	 * Get the filtered tracks column preferences.
	 * 
	 * @return List of filtered tracks column preferences.
	 */
	public List<List<String>> getTrackColumnsFilteredView()
	{
		return trackColumnsFilteredView;
	}

	/**
	 * Set the filtered tracks column preferences.
	 * 
	 * @param trackColumnsFilteredView List of filtered tracks column preferences.
	 */
	public void setTrackColumnsFilteredView(List<List<String>> trackColumnsFilteredView)
	{
		this.trackColumnsFilteredView = trackColumnsFilteredView;
	}
	
	/**
	 * Get the playlist tracks column preferences.
	 * 
	 * @return List of playlist tracks column preferences.
	 */
	public List<List<String>> getTrackColumnsPlaylistView()
	{
		return trackColumnsPlaylistView;
	}

	/**
	 * Set the playlist tracks column preferences.
	 * 
	 * @param trackColumnsPlaylistView List of playlist tracks column preferences.
	 */
	public void setTrackColumnsPlaylistView(List<List<String>> trackColumnsPlaylistView)
	{
		this.trackColumnsPlaylistView = trackColumnsPlaylistView;
	}
	
	/**
	 * Get the skin name.
	 * 
	 * @return Skin name.
	 */
	public String getSkinName ()
	{
		return skinName;
	}
	
	/**
	 * Set the skin name.
	 * 
	 * @param skin Skin name.
	 */
	public void setSkinName (String skin)
	{
		this.skinName = skin;
	}
	
	/**
	 * Get the maximum log history.
	 * 
	 * @return Maximum log history.
	 */
	public int getMaxLogHistory ()
	{
		return maxLogHistory;
	}
	
	/**
	 * Set the maximum log history.
	 * 
	 * @param maxLogHistory Maximum log history.
	 */
	public void setMaxLogHistory (int maxHistory)
	{
		this.maxLogHistory = maxHistory;
	}
	
	/**
	 * Get the global log level indicator.
	 * 
	 * @return true or false.
	 */
	public boolean getGlobalLogLevel ()
	{
		return globalLogLevel;
	}
	
	/**
	 * Set the global log level indicator.
	 * 
	 * @param value true or false.
	 */
	public void setGlobalLogLevel (boolean value)
	{
		globalLogLevel = value;
	}
	
	/**
	 * Get the log level.
	 * 
	 * @return Log level.
	 */
	public Level getLogLevel (Logging.Dimension dimension)
	{
		return logLevels.get(dimension.getDisplayValue());
		
	}
	
	/**
	 * Set the log level.
	 * 
	 * @param level Log level.
	 */
	public void setLogLevel (Logging.Dimension dimension, Level level)
	{
		logLevels.put(dimension.getDisplayValue(), level);
	}
	
	/**
	 * Get the preferences save directory.
	 * 
	 * @return Preferences save directory.
	 */
	public static String getSaveDirectory ()
	{
		return saveDirectory;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Replace the list of bypass playlist preferences.
	 * 
	 * @param bypassPrefs List of new bypass playlist preferences.
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
	 * Replace the list of ignored playlist preferences.
	 * 
	 * @param ignoredPrefs List of new ignored playlist preferences.
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
	 * Replace the list of full tracks column preferences.
	 * 
	 * @param trackColumnsPrefs List of new tracks column preferences.
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
	 * Replace the list of filtered tracks column preferences.
	 * 
	 * @param trackColumnsPrefs List of new tracks column preferences.
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
	 * Replace the list of playlist tracks column preferences.
	 * 
	 * @param trackColumnsPrefs List of new tracks column preferences.
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
	 * Set the preferences save directory, and move the existing preferences file to the new
	 * directory.
	 * 
	 * Note that this method is called during initialization before our constructor. That's because
	 * the constructor registers a logger, which in turn requires the correct save directory to be
	 * set.
	 * 
	 * @param directory Preferences save directory.
	 */
	public static void updateSaveDirectory (String directory)
	{
		
		/*
		 * Initialize the save path suffix (everything after the directory name).
		 */
		String prefsName = Preferences.class.getName();
		String savePathSuffix = "/" + prefsName + PREFS_SUFFIX;
		
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
		 * Rename (move) the existing file to the new directory if it exists.
		 */
		if (existingFile.exists())
		{
			existingFile.renameTo(newFile);
		}
		
		/*
		 * Move all log files to the new directory as well.
		 */
		if (saveDirectory != null)
		{
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
	 * Initialize the logger for this class.
	 * 
	 * This cannot be done in the constructor, because calling registerLogger() from the
	 * constructor would cause an endless loop, in turn because registerLogger() needs to
	 * access this class.
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
	 * Update the preferences from a preferences object. This is expected to be called after reading
	 * the serialized preferences.
	 * 
	 * @param prefs Deserialized preferences object.
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
	 * Deserialize the preferences from disk.
	 * 
	 * @return Deserialized preferences.
	 */
	public Preferences readPreferences ()
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
		catch (IOException | ClassNotFoundException e)
		{
			logger.error("caught " + e.getClass().getSimpleName());
			e.printStackTrace();
		}
		
		return prefs;
	}
	
	/**
	 * Serialize the preferences to disk.
	 */
	public void writePreferences ()
	{
		logger.info("writing preferences to '" + prefsFile + "'");
		
		try
		{
			prefsOutputStream = new FileOutputStream(prefsFile);
			ObjectOutputStream output = new ObjectOutputStream(prefsOutputStream);
			output.writeObject(this);
			output.close();
			prefsOutputStream.close();
		} 
		catch (IOException e)
		{
			logger.error("caught " + e.getClass().getSimpleName());
			e.printStackTrace();
		}
	}
}
