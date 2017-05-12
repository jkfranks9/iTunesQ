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

    //---------------- Class variables -------------------------------------
	
	/*
	 * Variables for the actual preferences we want to serialize.
	 * 
	 * - iTunes XML file name
	 * - list of bypass playlist preferences
	 * - list of filtered playlist preferences
	 * - various track column sets
	 * - skin name
	 * - global log level
	 * - log levels
	 */
	private String xmlFileName;
	private List<BypassPreference> bypassPrefs;
	private List<String> filteredPrefs;
	private List<List<String>> trackColumnsFullView;
	private List<List<String>> trackColumnsFilteredView;
	private List<List<String>> trackColumnsPlaylistView;
	private String skinName;
	private boolean globalLogLevel;
	private Map<Logging.Dimension, Level> logLevels;
	
	/*
	 * Other class variables.
	 */
	private transient String defaultSaveDirectory;
	private transient String saveDirectory;
	
    //---------------- Private variables -----------------------------------
	
	/*
	 * Serialized file path constants.
	 */
	private static transient final String HOME_ENV = System.getenv("HOME");
	private static transient final String DEFAULT_PREFS_PATH = "itq";
	private static transient final String PREFS_SUFFIX = ".ser";
	
	/*
	 * Since I want to include the class name without hard-coding it, the remaining parts of the
	 * path need to be initialized in the constructor.
	 */
	private static transient String prefsName;
	private static transient String prefsFile;

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
    	 * Get the logging object singleton.
    	 */
    	Logging logging = Logging.getInstance();
    	
    	/*
    	 * The name of the logger is "classname_UI", since this class is all about UI management.
    	 */
    	String className = getClass().getSimpleName();
    	logger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Register our logger.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, logger);
    	
    	/*
    	 * Initialize variables.
    	 * 
    	 * NOTE: We initialize the filtered playlists with the default value. They get replaced when
    	 * the preferences are read, if they exist in the serialized object.
    	 */
		bypassPrefs = new ArrayList<BypassPreference>();
		filteredPrefs = new ArrayList<String>(Playlist.DEFAULT_FILTERED_PLAYLISTS);
		
		/*
		 * Initialize the default save directory and file name. The directory can be changed with a
		 * preference.
		 */
		prefsName = this.getClass().getName();
		defaultSaveDirectory = HOME_ENV + "/" + DEFAULT_PREFS_PATH;
		prefsFile = defaultSaveDirectory + "/" + prefsName + PREFS_SUFFIX;
		
		logLevels = new HashMap<Logging.Dimension, Level>();
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
	 * Replace the list of bypass playlist preferences.
	 * 
	 * @param bypassPrefs List of new bypass playlist preferences.
	 */
	public void replaceBypassPrefs (List<BypassPreference> bypassPrefs)
	{
		this.bypassPrefs.clear();
		Iterator<BypassPreference> bypassPrefsIter = bypassPrefs.iterator();
		while (bypassPrefsIter.hasNext())
		{
			this.bypassPrefs.add(bypassPrefsIter.next());
		}
	}

	/**
	 * Get the list of filtered playlist preferences.
	 * 
	 * @return Filtered playlist preferences list.
	 */
	public List<String> getFilteredPrefs ()
	{
		return filteredPrefs;
	}
	
	/**
	 * Replace the list of filtered playlist preferences.
	 * 
	 * @param filteredPrefs List of new filtered playlist preferences.
	 */
	public void replaceFilteredPrefs (List<String> filteredPrefs)
	{
		this.filteredPrefs.clear();
		Iterator<String> filteredPrefsIter = filteredPrefs.iterator();
		while (filteredPrefsIter.hasNext())
		{
			this.filteredPrefs.add(filteredPrefsIter.next());
		}
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
	 * Replace the list of full tracks column preferences.
	 * 
	 * @param trackColumnsPrefs List of new tracks column preferences.
	 */
	public void replaceTrackColumnsFullView (List<List<String>> trackColumnsPrefs)
	{
		this.trackColumnsFullView.clear();
		Iterator<List<String>> trackColumnsPrefsIter = trackColumnsPrefs.iterator();
		while (trackColumnsPrefsIter.hasNext())
		{
			this.trackColumnsFullView.add(trackColumnsPrefsIter.next());
		}
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
	 * Replace the list of filtered tracks column preferences.
	 * 
	 * @param trackColumnsPrefs List of new tracks column preferences.
	 */
	public void replaceTrackColumnsFilteredView (List<List<String>> trackColumnsPrefs)
	{
		this.trackColumnsFilteredView.clear();
		Iterator<List<String>> trackColumnsPrefsIter = trackColumnsPrefs.iterator();
		while (trackColumnsPrefsIter.hasNext())
		{
			this.trackColumnsFilteredView.add(trackColumnsPrefsIter.next());
		}
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
	 * Replace the list of playlist tracks column preferences.
	 * 
	 * @param trackColumnsPrefs List of new tracks column preferences.
	 */
	public void replaceTrackColumnsPlaylistView (List<List<String>> trackColumnsPrefs)
	{
		this.trackColumnsPlaylistView.clear();
		Iterator<List<String>> trackColumnsPrefsIter = trackColumnsPrefs.iterator();
		while (trackColumnsPrefsIter.hasNext())
		{
			this.trackColumnsPlaylistView.add(trackColumnsPrefsIter.next());
		}
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
		return logLevels.get(dimension);
		
	}
	
	/**
	 * Set the log level.
	 * 
	 * @param level Log level.
	 */
	public void setLogLevel (Logging.Dimension dimension, Level level)
	{
		logLevels.put(dimension, level);
	}
	
	/**
	 * Get the default preferences save directory.
	 * 
	 * @return Default preferences save directory.
	 */
	public String getDefaultSaveDirectory ()
	{
		return defaultSaveDirectory;
	}
	
	/**
	 * Get the preferences save directory.
	 * 
	 * @return Preferences save directory.
	 */
	public String getSaveDirectory ()
	{
		return saveDirectory;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Set the preferences save directory, and move the existing preferences file to the new
	 * directory.
	 * 
	 * @param saveDirectory Preferences save directory.
	 */
	public void setSaveDirectory (String saveDirectory)
	{
		String savePathSuffix = "/" + prefsName + PREFS_SUFFIX;
		
		/*
		 * Create File objects for the existing and new files. It's possible we don't have an
		 * existing file or directory.
		 */
		File existingFile;
		if (this.saveDirectory != null)
		{
			existingFile = new File(this.saveDirectory + savePathSuffix);
		}
		else
		{
			existingFile = new File(this.defaultSaveDirectory + savePathSuffix);
		}
		File newFile = new File(saveDirectory + savePathSuffix);
		
		/*
		 * Rename (move) the existing file to the new directory if it exists.
		 */
		if (existingFile.exists())
		{
			existingFile.renameTo(newFile);
		}
		
		/*
		 * Update our saved directory name.
		 */
		this.saveDirectory = saveDirectory;

		/*
		 * Rebuild the preferences file path name.
		 */
		prefsFile = saveDirectory + "/" + prefsName + PREFS_SUFFIX;
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
		if (prefs.filteredPrefs != null)
		{
			replaceFilteredPrefs(prefs.filteredPrefs);
		}
		this.trackColumnsFullView = prefs.trackColumnsFullView;
		this.trackColumnsFilteredView = prefs.trackColumnsFilteredView;
		this.trackColumnsPlaylistView = prefs.trackColumnsPlaylistView;
		this.skinName = prefs.skinName;
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
			e.printStackTrace();
		}
	}
}
