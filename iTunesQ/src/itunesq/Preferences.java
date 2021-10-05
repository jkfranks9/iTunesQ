package itunesq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.slf4j.LoggerFactory;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class that represents user preferences. This is a singleton class.
 * 
 * @author Jon
 *
 */
public final class Preferences
{

    /*
     * NOTE: This class is serializable, but only a subset of variables actually
     * need to be serialized. The rest are declared as transient to prevent
     * serialization.
     */

    // ---------------- Singleton implementation ----------------------------

    /*
     * Singleton class instance variable.
     */
    private static transient Preferences instance = null;

    /**
     * Gets the singleton instance.
     * 
     * @return singleton class instance
     */
    public static Preferences getInstance()
    {
        if (instance == null)
        {
            instance = new Preferences();
        }

        return instance;
    }

    // ---------------- Class variables -------------------------------------

    /*
     * Variables for the actual preferences we want to serialize.
     * 
     * - input file name 
     * - list of bypass playlist preferences 
     * - list of ignored playlist preferences 
     * - various track column sets 
     * - skin name 
     * - maximum log file history 
     * - global log level flag 
     * - log levels
     * - list of duplicate track exclusions
     * - artist alternate name overrides
     */
    private String inputFileName;
    private List<BypassPreference> bypassPrefs;
    private List<String> ignoredPrefs;
    private List<List<String>> trackColumnsFullView;
    private List<List<String>> trackColumnsDuplicatesView;
    private List<List<String>> trackColumnsFilteredView;
    private List<List<String>> trackColumnsPlaylistView;
    private String skinName;
    private int maxLogHistory;
    private boolean globalLogLevel;
    private Map<String, Level> logLevels;
    private List<String> duplicateTrackExclusions;
    
    /*
     * The artist alternate name overrides are special. They need to be saved, which is why they're
     * here. But they are not part of the Edit -> Preferences window. Instead, they are saved when a
     * user creates an override from the artists display window, and used during file processing to
     * properly set up the artists database.
     */
    private List<ArtistAlternateNameOverride> artistOverrides;

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

    /*
     * The serialized file name includes the class name. That's initialized in
     * updateSaveDirectory, which is called by MainWindow before our
     * constructor.
     */
    private static transient String prefsFile;

    // ---------------- Private variables -----------------------------------

    /*
     * Serialized file name suffix.
     */
    private static transient final String PREFS_SUFFIX = ".json";

    /*
     * Other variables.
     */
    private transient Logger uiLogger = null;
    private transient Logger artistLogger = null;

    /*
     * Constructor. Making it private prevents instantiation by any other class.
     */
    private Preferences()
    {

        /*
         * Initialize variables.
         * 
         * NOTE: We initialize the ignored playlists with the default value.
         * They get replaced when the preferences are read, if they exist in the
         * serialized object.
         */
        bypassPrefs = new ArrayList<BypassPreference>();
        ignoredPrefs = new ArrayList<String>(Playlist.DEFAULT_IGNORED_PLAYLISTS);

        maxLogHistory = InternalConstants.DEFAULT_MAX_HISTORY;

        globalLogLevel = true;
        logLevels = new HashMap<String, Level>();
        
        duplicateTrackExclusions = new ArrayList<String>();
        
        artistOverrides = new ArrayList<ArtistAlternateNameOverride>();
        artistOverrides.setComparator(new Comparator<ArtistAlternateNameOverride>()
        {
            @Override
            public int compare(ArtistAlternateNameOverride o1, ArtistAlternateNameOverride o2)
            {
                return o1.compareTo(o2);
            }
        });
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the input file name preference.
     * 
     * @return input file name
     */
    public String getInputFileName()
    {
        return inputFileName;
    }

    /**
     * Sets the input file name preference.
     * 
     * @param inputFile input file name
     */
    public void setInputFileName(String inputFile)
    {
        inputFileName = inputFile;
    }

    /**
     * Gets the list of bypass playlist preferences.
     * 
     * @return list of bypass playlist preferences
     */
    public List<BypassPreference> getBypassPrefs()
    {
        return bypassPrefs;
    }

    /**
     * Gets the list of ignored playlist preferences.
     * 
     * @return list of ignored playlist preferences
     */
    public List<String> getIgnoredPrefs()
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
     * Gets the list of duplicate tracks column preferences.
     * 
     * @return list of duplicate tracks column preferences
     */
    public List<List<String>> getTrackColumnsDuplicatesView()
    {
        return trackColumnsDuplicatesView;
    }

    /**
     * Sets the list of duplicate tracks column preferences.
     * 
     * @param trackColumnsDuplicatesView list of duplicate tracks column
     * preferences
     */
    public void setTrackColumnsDuplicatesView(List<List<String>> trackColumnsDuplicatesView)
    {
        this.trackColumnsDuplicatesView = trackColumnsDuplicatesView;
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
     * @param trackColumnsFilteredView list of filtered tracks column
     * preferences
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
     * @param trackColumnsPlaylistView list of playlist tracks column
     * preferences
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
    public String getSkinName()
    {
        return skinName;
    }

    /**
     * Sets the skin name.
     * 
     * @param skin skin name
     */
    public void setSkinName(String skin)
    {
        this.skinName = skin;
    }

    /**
     * Gets the maximum log history.
     * 
     * @return maximum log history
     */
    public int getMaxLogHistory()
    {
        return maxLogHistory;
    }

    /**
     * Sets the maximum log history.
     * 
     * @param maxHistory maximum log history
     */
    public void setMaxLogHistory(int maxHistory)
    {
        this.maxLogHistory = maxHistory;
    }

    /**
     * Gets the default maximum log history.
     * 
     * @return default maximum log history
     */
    public static int getDefaultMaxLogHistory()
    {
        return InternalConstants.DEFAULT_MAX_HISTORY;
    }

    /**
     * Gets the global log level indicator.
     * 
     * @return global log level indicator
     */
    public boolean getGlobalLogLevel()
    {
        return globalLogLevel;
    }

    /**
     * Sets the global log level indicator.
     * 
     * @param level global log level indicator
     */
    public void setGlobalLogLevel(boolean level)
    {
        globalLogLevel = level;
    }

    /**
     * Gets the log level for a given logging dimension.
     * 
     * @param dimension logging dimension
     * @return log level
     */
    public Level getLogLevel(Logging.Dimension dimension)
    {
        return logLevels.get(dimension.getDisplayValue());

    }

    /**
     * Sets the log level for a given logging dimension.
     * 
     * @param dimension logging dimension
     * @param level log level
     */
    public void setLogLevel(Logging.Dimension dimension, Level level)
    {
        logLevels.put(dimension.getDisplayValue(), level);
    }

    /**
     * Gets the list of duplicate track exclusion values.
     * 
     * @return list of duplicate track exclusion values
     */
    public List<String> getDuplicateTrackExclusions()
    {
        return duplicateTrackExclusions;
    }
    
    /**
     * Gets the artist alternate name overrides.
     * 
     * @return artist alternate name overrides
     */
    public List<ArtistAlternateNameOverride> getArtistOverrides ()
    {
        return artistOverrides;
    }

    /**
     * Gets the default save directory.
     * 
     * @return default save directory
     */
    public static String getDefaultSaveDirectory()
    {
        return DEFAULT_SAVE_DIRECTORY;
    }

    /**
     * Gets the preferences save directory.
     * 
     * @return preferences save directory
     */
    public static String getSaveDirectory()
    {
        return saveDirectory;
    }

    /**
     * Gets the preferences file path.
     * 
     * @return preferences file path
     */
    public static String getPrefsFilePath()
    {
        return prefsFile;
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Replaces the list of bypass playlist preferences.
     * 
     * @param bypassPrefs list of new bypass playlist preferences
     */
    public void replaceBypassPrefs(List<BypassPreference> bypassPrefs)
    {
        uiLogger.trace("replaceBypassPrefs: " + this.hashCode());

        if (bypassPrefs == null)
        {
            throw new IllegalArgumentException("bypassPrefs argument is null");
        }

        this.bypassPrefs.clear();
        for (BypassPreference bypassPref : bypassPrefs)
        {
            this.bypassPrefs.add(bypassPref);
        }
    }

    /**
     * Replaces the list of ignored playlist preferences.
     * 
     * @param ignoredPrefs list of new ignored playlist preferences
     */
    public void replaceIgnoredPrefs(List<String> ignoredPrefs)
    {
        uiLogger.trace("replaceIgnoredPrefs: " + this.hashCode());

        if (ignoredPrefs == null)
        {
            throw new IllegalArgumentException("ignoredPrefs argument is null");
        }

        this.ignoredPrefs.clear();
        for (String ignoredPref : ignoredPrefs)
        {
            this.ignoredPrefs.add(ignoredPref);
        }
    }

    /**
     * Replaces the list of full tracks column preferences.
     * 
     * @param trackColumnsPrefs list of full tracks column preferences
     */
    public void replaceTrackColumnsFullView(List<List<String>> trackColumnsPrefs)
    {
        uiLogger.trace("replaceTrackColumnsFullView: " + this.hashCode());

        if (trackColumnsPrefs == null)
        {
            throw new IllegalArgumentException("trackColumnsPrefs argument is null");
        }

        this.trackColumnsFullView.clear();
        for (List<String> trackColumnsPref : trackColumnsPrefs)
        {
            this.trackColumnsFullView.add(trackColumnsPref);
        }
    }

    /**
     * Replaces the list of duplicate tracks column preferences.
     * 
     * @param trackColumnsPrefs list of duplicate tracks column preferences
     */
    public void replaceTrackColumnsDuplicatesView(List<List<String>> trackColumnsPrefs)
    {
        uiLogger.trace("replaceTrackColumnsDuplicatesView: " + this.hashCode());

        if (trackColumnsPrefs == null)
        {
            throw new IllegalArgumentException("trackColumnsPrefs argument is null");
        }

        this.trackColumnsDuplicatesView.clear();
        for (List<String> trackColumnsPref : trackColumnsPrefs)
        {
            this.trackColumnsDuplicatesView.add(trackColumnsPref);
        }
    }

    /**
     * Replaces the list of filtered tracks column preferences.
     * 
     * @param trackColumnsPrefs list of filtered tracks column preferences
     */
    public void replaceTrackColumnsFilteredView(List<List<String>> trackColumnsPrefs)
    {
        uiLogger.trace("replaceTrackColumnsFilteredView: " + this.hashCode());

        if (trackColumnsPrefs == null)
        {
            throw new IllegalArgumentException("trackColumnsPrefs argument is null");
        }

        this.trackColumnsFilteredView.clear();
        for (List<String> trackColumnsPref : trackColumnsPrefs)
        {
            this.trackColumnsFilteredView.add(trackColumnsPref);
        }
    }

    /**
     * Replaces the list of playlist tracks column preferences.
     * 
     * @param trackColumnsPrefs list of playlist tracks column preferences
     */
    public void replaceTrackColumnsPlaylistView(List<List<String>> trackColumnsPrefs)
    {
        uiLogger.trace("replaceTrackColumnsPlaylistView: " + this.hashCode());

        if (trackColumnsPrefs == null)
        {
            throw new IllegalArgumentException("trackColumnsPrefs argument is null");
        }

        this.trackColumnsPlaylistView.clear();
        for (List<String> trackColumnsPref : trackColumnsPrefs)
        {
            this.trackColumnsPlaylistView.add(trackColumnsPref);
        }
    }

    /**
     * Replaces the list of duplicate track exclusion values.
     * 
     * @param duplicateTrackExclusions list of duplicate track exclusion values
     */
    public void replaceDuplicateTrackExclusions(List<String> duplicateTrackExclusions)
    {
        uiLogger.trace("replaceDuplicateTrackExclusions: " + this.hashCode());

        if (duplicateTrackExclusions == null)
        {
            throw new IllegalArgumentException("duplicateTrackExclusions argument is null");
        }

        this.duplicateTrackExclusions.clear();
        for (String duplicateTrackExclusion : duplicateTrackExclusions)
        {
            this.duplicateTrackExclusions.add(duplicateTrackExclusion);
        }
    }
    
    /**
     * Adds an artist alternate name override.
     * 
     * @param primaryArtist primary artist name
     * @param alternateArtist alternate artist name
     * @param type artist override type
     */
    public void addArtistOverride (String primaryArtist, String alternateArtist,
            ArtistAlternateNameOverride.OverrideType type)
    {
        artistLogger.trace("addArtistOverride: " + this.hashCode());
        
        boolean foundPrimary = false;        

        /*
         * If we already have an override for this primary, just add the alternate.
         */
        for (ArtistAlternateNameOverride override : artistOverrides)
        {
            if (primaryArtist.equals(override.getPrimaryArtist()) && type == override.getOverrideType())
            {
                artistLogger.debug("adding alternate '" + alternateArtist + "' to primary override'" 
                        + primaryArtist + "', type " + type);
                
                override.addAlternateArtist(alternateArtist);
                foundPrimary = true;
                break;
            }
        }
        
        /*
         * If we did not find an override for the primary, build and add a new override.
         */
        if (foundPrimary == false)
        {
            artistLogger.debug("adding new override type " + type
                    + ", alternate '" + alternateArtist + "', primary '" + primaryArtist + "'");
            
            ArtistAlternateNameOverride override = new ArtistAlternateNameOverride(primaryArtist, type);
            override.addAlternateArtist(alternateArtist);
            artistOverrides.add(override);
        }
    }

    /**
     * Removes an artist alternate name override.
     * 
     * @param primaryArtist primary artist name
     * @param alternateArtist alternate artist name
     * @param type override type
     */
    public void removeArtistOverride (String primaryArtist, String alternateArtist, ArtistAlternateNameOverride.OverrideType type)
    {
        uiLogger.trace("removeArtistOverride: " + this.hashCode());

        /*
         * Find the primary and remove the alternate.
         */
        Iterator<ArtistAlternateNameOverride> artistOverridesIter = artistOverrides.iterator();
        while (artistOverridesIter.hasNext())
        {
            ArtistAlternateNameOverride override = artistOverridesIter.next();
            
            if (primaryArtist.equals(override.getPrimaryArtist()) && type == override.getOverrideType())
            {
                artistLogger.debug("removing alternate '" + alternateArtist + "' from primary override '"
                        + primaryArtist + "', type " + type);
                override.removeAlternateArtist(alternateArtist);
                
                /*
                 * Delete the override if we just removed the last alternate.
                 */
                if (override.getNumAlternateArtists() == 0)
                {
                    artistOverridesIter.remove();
                }
                
                break;
            }
        }
    }
    
    /**
     * Gets the artist override associated with an alternate artist with
     * a matching type.
     * 
     * @param alternateArtist alternate artist display name to check for an
     * artist override
     * @param type artist override type
     * @return matching artist override or null
     */
    public ArtistAlternateNameOverride getArtistOverride (String alternateArtist, 
            ArtistAlternateNameOverride.OverrideType type)
    {
        uiLogger.trace("getArtistOverridePrimaryName: " + this.hashCode());
        
        ArtistAlternateNameOverride result = null;

        for (ArtistAlternateNameOverride override : artistOverrides)
        {
            List<String> altNames = override.getAlternateArtists();

            for (String alternateName : altNames)
            {
                if (alternateName.equals(alternateArtist))
                {
                    if (type == override.getOverrideType())
                    {
                    	result = override;
                        break;
                    }
                }
            }

            if (result != null)
            {
                break;
            }
        }
        
        return result;
    }

    /**
     * Sets the preferences save directory, and moves the existing preferences
     * file and log files to the new directory.
     * 
     * @param directory preferences save directory
     */
    public static void updateSaveDirectory(String directory)
    {

        /*
         * This method is called in two places:
         * 
         * 1) During initialization.
         * 
         * If this is the very first time the application has been run, we won't
         * have an existing preferences file, and the input directory will be
         * the default.
         * 
         * If the application has been run before, then we should have an
         * existing preferences file, and the input directory should be what was
         * saved using the Java preference. The input directory thus may or may
         * not be the default.
         * 
         * In either case, it can't be true that the preference file has changed
         * location - that can only happen while we're running and the user
         * changes the save directory preferences (case 2 below). So the only
         * thing we end up doing here is setting the saveDirectory and prefsFile
         * variables.
         * 
         * 2) When the directory has been changed via user preferences.
         * 
         * In this case we need to move all relevant files to the new directory.
         */

        /*
         * Initialize the save path suffix (everything after the directory
         * name).
         */
        String savePathSuffix = "/" + Preferences.class.getName() + PREFS_SUFFIX;

        /*
         * Create File objects for the existing and new files. It's possible we
         * don't have an existing file.
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
         * If we have an existing file, and it's different than the new file,
         * rename (move) the existing file and log files to the new directory.
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
     * Initializes logging for this class.
     * <p>
     * This cannot be done in the constructor, because calling
     * <code>registerLogger</code> from the constructor would cause an endless
     * loop, in turn because <code>registerLogger</code> needs to access this
     * class.
     */
    public void initializeLogging()
    {

        /*
         * Create a UI logger.
         */
        String className = getClass().getSimpleName();
        uiLogger = (Logger) LoggerFactory.getLogger(className + "_UI");

        /*
         * Create an artist logger.
         */
        artistLogger = (Logger) LoggerFactory.getLogger(className + "_Artist");

        /*
         * Get the logging object singleton.
         */
        Logging logging = Logging.getInstance();

        /*
         * Register our loggers.
         */
        logging.registerLogger(Logging.Dimension.UI, uiLogger);
        logging.registerLogger(Logging.Dimension.ARTIST, artistLogger);

        uiLogger.trace("initializeLogging: " + this.hashCode());
    }

    /**
     * Updates the preferences from a preferences object. This is expected to be
     * called after reading the serialized preferences.
     * 
     * @param prefs deserialized preferences object
     */
    public void updatePreferences(Preferences prefs)
    {
        uiLogger.info("updating preferences");

        if (prefs == null)
        {
            throw new IllegalArgumentException("prefs argument is null");
        }

        this.inputFileName = prefs.inputFileName;
        if (prefs.bypassPrefs != null)
        {
            replaceBypassPrefs(prefs.bypassPrefs);
        }
        if (prefs.ignoredPrefs != null)
        {
            replaceIgnoredPrefs(prefs.ignoredPrefs);
        }
        if (prefs.trackColumnsFullView != null)
        {
            this.trackColumnsFullView = prefs.trackColumnsFullView;
        }
        if (prefs.trackColumnsDuplicatesView != null)
        {
            this.trackColumnsDuplicatesView = prefs.trackColumnsDuplicatesView;
        }
        if (prefs.trackColumnsFilteredView != null)
        {
            this.trackColumnsFilteredView = prefs.trackColumnsFilteredView;
        }
        if (prefs.trackColumnsPlaylistView != null)
        {
            this.trackColumnsPlaylistView = prefs.trackColumnsPlaylistView;
        }
        this.skinName = prefs.skinName;
        this.maxLogHistory = prefs.maxLogHistory;
        this.globalLogLevel = prefs.globalLogLevel;
        if (prefs.logLevels != null)
        {
            this.logLevels = prefs.logLevels;
        }
        if (prefs.duplicateTrackExclusions != null)
        {
            replaceDuplicateTrackExclusions(prefs.duplicateTrackExclusions);
        }
        if (prefs.artistOverrides != null)
        {
            this.artistOverrides = prefs.artistOverrides;
        }
    }

    /**
     * Deserializes the preferences from disk.
     * 
     * @return deserialized preferences
     * @throws IOException If an error occurs trying to read the preferences
     * file.
     * @throws ClassNotFoundException If the class of a serialized object cannot
     * be found.
     */
    public Preferences readPreferences() 
            throws IOException, ClassNotFoundException
    {
        Preferences prefs = null;
        uiLogger.info("reading preferences from '" + prefsFile + "'");

        FileInputStream prefsInputStream = null;
        JsonReader reader = null;

        try
        {
            prefsInputStream = new FileInputStream(prefsFile);
            reader = new JsonReader(prefsInputStream);
            prefs = (Preferences) reader.readObject();

            /*
             * Restore the override type Enum from the serialized name.
             */
        	for (ArtistAlternateNameOverride override : prefs.artistOverrides)
        	{
        		String typeName = override.getOverrideTypeName();
        		switch (typeName)
        		{
        		case "MANUAL":
            		override.setOverrideType(ArtistAlternateNameOverride.OverrideType.MANUAL);
            		break;
            		
        		case "AUTOMATIC":
            		override.setOverrideType(ArtistAlternateNameOverride.OverrideType.AUTOMATIC);
            		break;
            		
            	default:
                    throw new InternalErrorException(true, "unknown override type name '" + typeName + "'");
        		}
        	}
        }
        catch (FileNotFoundException e)
        {
            // Not an error - ignore.
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
            if (prefsInputStream != null)
            {
                prefsInputStream.close();
            }
        }

        return prefs;
    }

    /**
     * Serializes the preferences to disk.
     * 
     * @throws IOException If an error occurs trying to write the preferences
     * file.
     */
    public void writePreferences() 
            throws IOException
    {
        uiLogger.info("writing preferences to '" + prefsFile + "'");

        FileOutputStream prefsOutputStream = null;
        JsonWriter writer = null;
        
        /*
         * Artist overrides contain an Enum. Serializing an Enum seems to be problematic, so save 
         * its name and blacklist the Enum. Note that we need to use Java constructs here 
         * because JsonWriter needs them. 
         */
    	java.util.Map<String, Object> args = new java.util.HashMap<String, Object>();
    	if (artistOverrides.getLength() > 0)
        {
    		
    		/*
    		 * We need the artist override class, so save it when we process the first override.
    		 */
        	Class<? extends ArtistAlternateNameOverride> overrideClass = null;
        	
        	for (ArtistAlternateNameOverride override : artistOverrides)
        	{
        		override.saveOverrideTypeName();
        		if (overrideClass == null)
        		{
        			overrideClass = override.getClass();
        		}
        	}

        	/*
        	 * Build the argument list for the JsonWriter.
        	 */
        	java.util.List<String> blacklistFields = new java.util.ArrayList<String>();
        	@SuppressWarnings("rawtypes")
        	java.util.Map<Class, java.util.List<String>> blacklist = 
        		new java.util.HashMap<Class, java.util.List<String>>();

        	blacklistFields.add("overrideType");
        	blacklist.put(overrideClass, blacklistFields);
        	args.put(JsonWriter.FIELD_NAME_BLACK_LIST, blacklist);
        }

        try
        {
            prefsOutputStream = new FileOutputStream(prefsFile);
            writer = new JsonWriter(prefsOutputStream, args);
            
            writer.write(this);
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
            if (prefsOutputStream != null)
            {
                prefsOutputStream.close();
            }
        }
    }

    /**
     * Write the current set of preferences to the log file.
     * 
     * @param reason string representing the type of preferences update
     */
    public void logPreferences(String reason)
    {
        final String lineSeparator = System.lineSeparator();
        final String indent = "      ";
        final String listPrefix = "- ";
        int itemNum = 0;
        StringBuilder output = new StringBuilder();

        /*
         * Indicate we've started.
         */
        output.append("***** " + reason + " preferences *****" + lineSeparator);

        /*
         * Input file name.
         */
        if (inputFileName != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Input file name:" + lineSeparator);
            output.append(indent + inputFileName + lineSeparator);
        }

        /*
         * Bypassed playlists.
         */
        List<BypassPreference> inBypassPrefs = bypassPrefs;
        if (inBypassPrefs != null && inBypassPrefs.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Bypassed playlists:" + lineSeparator);
            for (BypassPreference bypassPref : inBypassPrefs)
            {
                output.append(indent + listPrefix + bypassPref.getPlaylistName() + "("
                        + ((bypassPref.getIncludeChildren() == true) ? "Y" : "N") + ")" + lineSeparator);
            }
        }

        /*
         * Ignored playlists.
         */
        List<String> inIgnoredPrefs = ignoredPrefs;
        if (inIgnoredPrefs != null && inIgnoredPrefs.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Ignored playlists:" + lineSeparator);
            for (String ignoredPref : inIgnoredPrefs)
            {
                output.append(indent + listPrefix + ignoredPref + lineSeparator);
            }
        }

        /*
         * Full track columns. Each item is a list of the column name and the
         * column width, but we don't care about the width (that's internal). So
         * just access the column name using a list index of 0. We log all the
         * column names on a single line.
         */
        List<List<String>> inTrackColumns = trackColumnsFullView;
        if (inTrackColumns != null && inTrackColumns.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Full track columns:" + lineSeparator);
            output.append(indent);

            int index = 0;
            for (List<String> trackColumn : inTrackColumns)
            {
                if (index > 0)
                {
                    output.append(", ");
                }
                output.append(trackColumn.get(0));
                index++;
            }
            output.append(lineSeparator);
        }

        /*
         * Duplicate track columns.
         */
        inTrackColumns = trackColumnsDuplicatesView;
        if (inTrackColumns != null && inTrackColumns.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Duplicate track columns:" + lineSeparator);
            output.append(indent);

            int index = 0;
            for (List<String> trackColumn : inTrackColumns)
            {
                if (index > 0)
                {
                    output.append(", ");
                }
                output.append(trackColumn.get(0));
                index++;
            }
            output.append(lineSeparator);
        }

        /*
         * Filtered track columns.
         */
        inTrackColumns = trackColumnsFilteredView;
        if (inTrackColumns != null && inTrackColumns.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Filtered track columns:" + lineSeparator);
            output.append(indent);

            int index = 0;
            for (List<String> trackColumn : inTrackColumns)
            {
                if (index > 0)
                {
                    output.append(", ");
                }
                output.append(trackColumn.get(0));
                index++;
            }
            output.append(lineSeparator);
        }

        /*
         * Playlist track columns.
         */
        inTrackColumns = trackColumnsPlaylistView;
        if (inTrackColumns != null && inTrackColumns.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Playlist track columns:" + lineSeparator);
            output.append(indent);

            int index = 0;
            for (List<String> trackColumn : inTrackColumns)
            {
                if (index > 0)
                {
                    output.append(", ");
                }
                output.append(trackColumn.get(0));
                index++;
            }
            output.append(lineSeparator);
        }

        /*
         * Skin name.
         */
        if (skinName != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Skin name:" + lineSeparator);
            output.append(indent + skinName + lineSeparator);
        }

        /*
         * Maximum log history.
         */
        output.append(String.format("%2d", ++itemNum) + ") " + "Maximum log history:" + lineSeparator);
        output.append(indent + maxLogHistory + lineSeparator);

        /*
         * Global log level.
         */
        output.append(String.format("%2d", ++itemNum) + ") " + "Global log level:" + lineSeparator);
        output.append(indent + globalLogLevel + lineSeparator);

        /*
         * Dimensional log levels.
         */
        Map<String, Level> inLogLevels = logLevels;
        if (inLogLevels != null && inLogLevels.getCount() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Dimensional log levels:" + lineSeparator);
            for (String dimension : logLevels)
            {
                Level level = logLevels.get(dimension);
                if (level == null)
                {
                    continue;
                }
                output.append(indent + listPrefix + dimension + "(" + level.toString() + ")" + lineSeparator);
            }
        }

        /*
         * Duplicate track exclusions.
         */
        List<String> inDuplicateTrackExclusions = duplicateTrackExclusions;
        if (inDuplicateTrackExclusions != null && inDuplicateTrackExclusions.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Duplicate track exclusions:" + lineSeparator);
            for (String duplicateTrackExclusion : inDuplicateTrackExclusions)
            {
                output.append(indent + listPrefix + duplicateTrackExclusion + lineSeparator);
            }
        }
        
        /*
         * Artist alternate name overrides.
         */
        List<ArtistAlternateNameOverride> inArtistOverrides = artistOverrides;
        if (inArtistOverrides != null && inArtistOverrides.getLength() > 0)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Artist overrides:" + lineSeparator);
            
            for (ArtistAlternateNameOverride override : artistOverrides)
            {
                String primaryName = override.getPrimaryArtist();
                output.append(indent + primaryName + lineSeparator);
                List<String> altNames = override.getAlternateArtists();
                for (String alternateName : altNames)
                {
                    output.append(indent + listPrefix + alternateName + lineSeparator);
                }
            }
        }

        /*
         * Log it!
         */
        Logger diagLogger = Logging.getInstance().getDiagLogger();
        diagLogger.info(output.toString());
    }
}
