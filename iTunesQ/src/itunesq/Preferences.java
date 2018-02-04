package itunesq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
     * - iTunes XML file name - list of bypass playlist preferences - list of
     * ignored playlist preferences - various track column sets - show remote
     * tracks flag - skin name - maximum log file history - global log level
     * flag - log levels
     */
    private String xmlFileName;
    private List<BypassPreference> bypassPrefs;
    private List<String> ignoredPrefs;
    private List<List<String>> trackColumnsFullView;
    private List<List<String>> trackColumnsDuplicatesView;
    private List<List<String>> trackColumnsFilteredView;
    private List<List<String>> trackColumnsPlaylistView;
    private boolean showRemoteTracks;
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

    // ---------------- Private variables -----------------------------------

    /*
     * Serialized file name suffix.
     */
    private static transient final String PREFS_SUFFIX = ".ser";

    /*
     * The serialized file name includes the class name. That's initialized in
     * updateSaveDirectory, which is called by MainWindow before our
     * constructor.
     */
    private static transient String prefsFile;

    /*
     * Other variables.
     */
    private transient Logger logger = null;

    private static final long serialVersionUID = -543909365447180812L;

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

        showRemoteTracks = false;

        maxLogHistory = InternalConstants.DEFAULT_MAX_HISTORY;

        logLevels = new HashMap<String, Level>();
        globalLogLevel = true;
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the XML file name preference.
     * 
     * @return XML file name
     */
    public String getXMLFileName()
    {
        return xmlFileName;
    }

    /**
     * Sets the XML file name preference.
     * 
     * @param xmlFile XML file name
     */
    public void setXMLFileName(String xmlFile)
    {
        xmlFileName = xmlFile;
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
     * Gets the show remote tracks indicator.
     * 
     * @return show remote tracks indicator
     */
    public boolean getShowRemoteTracks()
    {
        return showRemoteTracks;
    }

    /**
     * Sets the show remote tracks indicator.
     * 
     * @param showRemoteTracks show remote tracks indicator
     */
    public void setShowRemoteTracks(boolean showRemoteTracks)
    {
        this.showRemoteTracks = showRemoteTracks;
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
     * Gets the default save directory.
     * 
     * @return default save directory
     */
    public static String getDefaultSaveDirectory()
    {
        return DEFAULT_SAVE_DIRECTORY;
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
        logger.trace("replaceBypassPrefs: " + this.hashCode());

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
        logger.trace("replaceIgnoredPrefs: " + this.hashCode());

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
        logger.trace("replaceTrackColumnsFullView: " + this.hashCode());

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
        logger.trace("replaceTrackColumnsDuplicatesView: " + this.hashCode());

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
        logger.trace("replaceTrackColumnsFilteredView: " + this.hashCode());

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
        logger.trace("replaceTrackColumnsPlaylistView: " + this.hashCode());

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
     * Initializes the logger for this class.
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
     * Updates the preferences from a preferences object. This is expected to be
     * called after reading the serialized preferences.
     * 
     * @param prefs deserialized preferences object
     */
    public void updatePreferences(Preferences prefs)
    {
        logger.info("updating preferences");

        if (prefs == null)
        {
            throw new IllegalArgumentException("prefs argument is null");
        }

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
        this.trackColumnsDuplicatesView = prefs.trackColumnsDuplicatesView;
        this.trackColumnsFilteredView = prefs.trackColumnsFilteredView;
        this.trackColumnsPlaylistView = prefs.trackColumnsPlaylistView;
        this.showRemoteTracks = prefs.showRemoteTracks;
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
     * @throws ClassNotFoundException If the class of a serialized object cannot
     * be found.
     */
    public Preferences readPreferences() throws IOException, ClassNotFoundException
    {
        Preferences prefs = null;
        logger.info("reading preferences from '" + prefsFile + "'");

        FileInputStream prefsInputStream = null;
        ObjectInputStream input = null;

        try
        {
            prefsInputStream = new FileInputStream(prefsFile);
            input = new ObjectInputStream(prefsInputStream);
            prefs = (Preferences) input.readObject();
        }
        catch (FileNotFoundException e)
        {
            // Not an error - ignore.
        }
        finally
        {
            if (input != null)
            {
                input.close();
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
    public void writePreferences() throws IOException
    {
        logger.info("writing preferences to '" + prefsFile + "'");

        FileOutputStream prefsOutputStream = null;
        ObjectOutputStream output = null;

        try
        {
            prefsOutputStream = new FileOutputStream(prefsFile);
            output = new ObjectOutputStream(prefsOutputStream);
            output.writeObject(this);
        }
        finally
        {
            if (output != null)
            {
                output.close();
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
         * XML file name.
         */
        if (xmlFileName != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "XML file name:" + lineSeparator);
            output.append(indent + xmlFileName + lineSeparator);
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
         * Show remote tracks flag.
         */
        output.append(String.format("%2d", ++itemNum) + ") " + "Show remote tracks:" + lineSeparator);
        output.append(indent + showRemoteTracks + lineSeparator);

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
         * Log it!
         */
        Logging logging = Logging.getInstance();
        Logger diagLogger = logging.getDiagLogger();
        diagLogger.info(output.toString());
    }
}
