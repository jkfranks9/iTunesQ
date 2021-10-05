package itunesq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Version;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class that represents the Apache Pivot application for the Library Query Tool.
 * <p>
 * This application operates on the input file containing music library tracks
 * and playlists. The input file is exported by the library application. As such,
 * this application does not have access to the actual songs, album art,
 * and so on. The layout of, and access to, all that stuff is proprietary.
 * <p>
 * What this application provides is the following:
 * <ul>
 * <li>show all tracks in the library</li>
 * <li>show duplicate tracks using exact or fuzzy match criteria</li>
 * <li>show all playlists in the library as a tree</li>
 * <li>show all artists in the library, along with associated data</li>
 * <li>allow automatic and manual artist alternate names, along with user
 * overrides</li>
 * <li>query tracks using a collection of filters</li>
 * <li>compare two or more playlists</li>
 * <li>expand a playlist into a family of tracks or additional playlists</li>
 * <li>save or print the results of a track or playlist query, or a list of
 * duplicate tracks</li>
 * </ul>
 * <p>
 * This is the main class for the application. The <code>startup</code> method
 * is called when the application starts. Its primary job is to manage the Pivot
 * UI.
 * 
 * @author Jon
 * @see <a href="http://pivot.apache.org/">Apache Pivot</a>
 *
 */
public class MainWindow implements Application, Application.UncaughtExceptionHandler, DialogCloseListener
{

    // ---------------- Class variables -------------------------------------

    private static DiagTrigger diagTrigger = null;
    private static final String DIAG_TRIGGER_PROPERTY_KEY = "diag-trigger";

    /**
     * Diagnostic trigger, to enable breakpoints based on item names, or special logging.
     */
    
    /*
     * Details:
     * 
     *   - The diag trigger is passed as a program argument of the form: --diag-trigger=artist (for example).
     *   
     *   - From Eclipse, use the Debug Configurations panel, Arguments tab ... if running outside Eclipse
     *     pass as a program argument.
     *     
     *   - Some of the triggers require a diag value that is supplied in a file (for example the artist trigger).
     *     The file must exist in the save directory under a subdirectory named "diag-trigger". The file name must
     *     match the enum constructor value below, for example "artist". This file must contain (for an artist trigger)
     *     the lower case name of an artist of interest. Code can be used (if it exists) or added (if it doesn't) that 
     *     then allows a breakpoint to be set at any interesting point when the artist name in the file matches an artist
     *     being processed. Look up the DiagTrigger.ARTIST enum for examples. Note that TRACK and PLAYLIST are defined
     *     here but have no current uses, but they would follow the same scheme.
     *     
     *   - The SKIN_LOGGING and LOGGER_LOGGING triggers don't have a corresponding diag value; they just enable additional
     *     low level skin or logger logging to help debug those components.
     *     
     *   - The FORCE_LOGLEVEL trigger allows the log level to be set for a given dimension very early, which could be 
     *     useful in a situation where we crash and thus don't have a chance to set the log level preference for a future
     *     iteration. This trigger uses a diag value in a file as described above. The value takes the form of
     *     <log dimension>:<log level>.
     */
    public enum DiagTrigger
    {
    	
    	/**
    	 * no diagnostic trigger in use
    	 */
        NONE("none"),
        
        /**
         * diagnose tracks
         */
        TRACK("track"), 
        
        /**
         * diagnose playlists
         */
        PLAYLIST("playlist"),
        
        /**
         * diagnose artists
         */
        ARTIST("artist"), 
        
        /**
         * activate additional logging for the skins component
         */
        SKIN_LOGGING("skin"), 
        
        /**
         * activate additional logging for the logging component
         */
        LOGGER_LOGGING("logger"), 
        
        /**
         * activate specific log level for a component during startup
         */
        FORCE_LOGLEVEL("loglevel");

        private String displayValue;

        /*
         * Constructor.
         */
        private DiagTrigger(String s)
        {
            displayValue = s;
        }

        /**
         * Gets the display value.
         * 
         * @return the display value
         */
        public String getDisplayValue()
        {
            return displayValue;
        }

        /**
         * Performs a reverse lookup of the <code>enum</code> from the display value.
         * 
         * @param value display value to look up
         * @return <code>enum</code>
         */
        public static DiagTrigger getEnum(String value)
        {
            return lookup.get(value);
        }

        /*
         * Reverse lookup capability to get the enum based on its display value.
         */
        private static final Map<String, DiagTrigger> lookup = new HashMap<String, DiagTrigger>();
        static
        {
            for (DiagTrigger value : DiagTrigger.values())
            {
                lookup.put(value.getDisplayValue(), value);
            }
        }
    }

    // ---------------- Private variables -----------------------------------

    private Window mainWindow = null;
    private Display display = null;
    private String inputFileName = null;
    private boolean inputFileExists = false;
    private Logger logger = null;
    private Logging logging = null;
    private Preferences userPrefs = null;
    private String saveDirectory = null;
    private static boolean exceptionLogged = false;
    private static String diagTriggerValue = null;

    /*
     * BXML variables.
     */
    @BXML private Border infoBorder = null;
    @BXML private FillPane infoFillPane = null;
    @BXML private Label titleLabel = null;
    @BXML private static ActivityIndicator activityIndicator = null;
    @BXML private Separator fileSeparator = null;
    @BXML private BoxPane fileBoxPane = null;
    @BXML private Label fileLabel = null;
    @BXML private Separator dataSeparator = null;
    @BXML private BoxPane dataBoxPane = null;
    @BXML private Label numAudioTracksLabel = null;
    @BXML private Label numVideoTracksLabel = null;
    @BXML private Label numPlaylistsLabel = null;
    @BXML private Label numArtistsLabel = null;
    @BXML private Border actionBorder = null;
    @BXML private BoxPane actionBoxPane = null;
    @BXML private static PushButton viewAudioTracksButton = null;
    @BXML private static PushButton viewVideoTracksButton = null;
    @BXML private static PushButton viewPlaylistsButton = null;
    @BXML private static PushButton viewArtistsButton = null;
    @BXML private static PushButton queryTracksButton = null;
    @BXML private static PushButton queryPlaylistsButton = null;

    /**
     * Class constructor.
     */
    public MainWindow()
    {

        /*
         * The order of events in the startup sequence is important:
         * 
         * 1) We have to get the save directory from the Java preferences, in
         * order to start logging in the correct directory.
         * 
         * 2) We then have to set the save directory in the user preferences,
         * because it's obtained from there when we register the first logger.
         * Note that this is a static method, because we haven't done step 3
         * yet.
         * 
         * 3) Create the user preferences singleton. Its constructor sets the
         * default max log history, and the global log level flag. These logging
         * variables may get changed when the user preferences are read from
         * disk (in the startup method).
         * 
         * 4) Create and register the first logger, which relies on steps 1
         * through 3.
         */

        /*
         * Get the save directory using the Java preferences API. We have to
         * maintain this directory using the Java API instead of in our
         * preferences file to avoid a catch-22. In hindsight I would save all
         * our preferences using the Java API, but I already did it using a file
         * and don't feel like rewriting a bunch of code.
         */
        saveDirectory = Utilities.accessJavaPreference(Utilities.JAVA_PREFS_KEY_SAVEDIR);

        /*
         * Save the save directory in the user preferences.
         */
        Preferences.updateSaveDirectory(saveDirectory);

        /*
         * Create the preferences object singleton.
         */
        userPrefs = Preferences.getInstance();

        /*
         * Create the logging object singleton.
         */
        logging = Logging.getInstance();

        /*
         * Create the diagnostic logger, used for logging things like current
         * preferences and input file statistics. This logger lives outside of the
         * logger registry and always uses the INFO log level.
         */
        logging.createDiagLogger();

        /*
         * Create a UI logger.
         */
        String className = getClass().getSimpleName();
        logger = (Logger) LoggerFactory.getLogger(className + "_UI");

        /*
         * Set the default log level, which is obtained from the logback
         * configuration file. This bootstraps logging so that we use the
         * default level until such time as we read and process any saved user
         * preferences.
         */
        logging.setDefaultLogLevel(logger.getEffectiveLevel());

        /*
         * Now register our logger, which is the first one registered.
         */
        logging.registerLogger(Logging.Dimension.UI, logger);

        /*
         * Initialize the Preferences logger. This could not be done in the
         * Preferences constructor, because doing so would result in an endless
         * loop between Preferences and Logging.
         */
        userPrefs.initializeLogging();

        /*
         * Initialize loggers in static classes.
         */
        ArtistNames.initializeLogging();
        PlaylistCollection.initializeLogging();
        PlaylistTree.initializeLogging();
        XMLHandler.initializeLogging();
        JSONHandler.initializeLogging();

        /*
         * Initialize variables.
         */
        diagTrigger = DiagTrigger.NONE;

        logger.trace("MainWindow constructor: " + this.hashCode());
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the diagnostic trigger.
     * 
     * @return diagnostic trigger
     */
    public static DiagTrigger getDiagTrigger()
    {
        return diagTrigger;
    }

    /**
     * Gets the diagnostic trigger data value.
     * 
     * @return diagnostic trigger data value, or a string that should not match
     * anything if the trigger value could not be obtained
     */
    public static String getDiagTriggerValue()
    {
        return (diagTriggerValue != null) ? diagTriggerValue : "__NON_MATCHING__";
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Starts up the application when it's launched.
     * 
     * @param display display object for managing windows
     * @param properties properties passed to the application
     * @throws IOException If an error occurs trying to read the BXML file; or
     * an error occurs trying to read or write the user preferences.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     * @throws ClassNotFoundException If the class of a serialized object cannot
     * be found.
     */
    @Override
    public void startup(Display display, Map<String, String> properties)
            throws IOException, SerializationException, ClassNotFoundException
    {
        logger.info("application started");

        /*
         * Save the display, in case we need to open a bare bones window for an
         * uncaught exception.
         */
        this.display = display;

        /*
         * Log diagnostic info about our environment.
         */
        Logger diagLogger = logging.getDiagLogger();
        Version pivotVersion = DesktopApplicationContext.getPivotVersion();
        if (pivotVersion != null)
        {
            diagLogger.info("Pivot version: " + pivotVersion.toString());
        }

        /*
         * Set the diag trigger if the property is set. This lets us create breakpoints based on the 
         * names of items like tracks or artists, or perform other special diagnostic processing. 
         * Ignore any errors by setting the trigger to "none" if a bogus value was specified.
         */
        String diagProperty = properties.get(DIAG_TRIGGER_PROPERTY_KEY);
        if (diagProperty != null)
        {
            diagTrigger = DiagTrigger.getEnum(diagProperty);
            if (diagTrigger == null)
            {
                diagTrigger = DiagTrigger.NONE;
            }
        }

        /*
         * Collect the diag trigger value if needed for the type.
         */
        switch (diagTrigger)
        {
        case NONE:
        case SKIN_LOGGING:
        case LOGGER_LOGGING:
            break;

        default:
            getDiagTriggerData();
        }

        /*
         * Get the BXML information for the main window, and generate the list
         * of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeBxmlVariables(components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers(display);

        /*
         * Read the preferences, if they exist, and update the running copy.
         */
        Preferences existingPrefs = userPrefs.readPreferences();
        if (existingPrefs != null)
        {
            userPrefs.updatePreferences(existingPrefs);
        }

        /*
         * Log the current preferences.
         */
        userPrefs.logPreferences("startup");

        /*
         * If the force log level diag trigger is set, implement it. We have to wait until the 
         * preferences are read before we override the log level.
         * 
         * Note that we don't save the preferences, although that will probably happen somewhere
         * down the line. Not to worry since this is just the developer trying to debug something.
         */
        if (diagTrigger == DiagTrigger.FORCE_LOGLEVEL)
        {
            String[] loglevelVals = diagTriggerValue.split(":");
            Logging.Dimension dimension = Logging.Dimension.getEnum(loglevelVals[0]);
            Level level = Level.toLevel(loglevelVals[1]);
            if (dimension != null)
            {
                userPrefs.setGlobalLogLevel(false);
                userPrefs.setLogLevel(dimension, level);
            }
        }

        /*
         * Set the log levels from any existing preferences.
         */
        logging.updateLogLevelsFromPrefs();

        /*
         * Create the skins object singleton.
         * 
         * NOTE: This must be done after the running preferences have been
         * updated, because the Skins constructor needs to read the preferences
         * to initialize the preferred skin.
         */
        Skins skins = Skins.getInstance();

        /*
         * Save the main window information labels so they can be used from
         * other windows.
         */
        Utilities.saveFileLabel(fileLabel);
        Utilities.saveNumAudioTracksLabel(numAudioTracksLabel);
        Utilities.saveNumVideoTracksLabel(numVideoTracksLabel);
        Utilities.saveNumPlaylistsLabel(numPlaylistsLabel);
        Utilities.saveNumArtistsLabel(numArtistsLabel);

        /*
         * Initialize the track display column defaults.
         */
        TrackDisplayColumns.initializeDefaults();

        /*
         * Initialize the artist display column sets.
         */
        ArtistDisplayColumns.initializeColumnSets();

        /*
         * Initialize the playlist display column sets.
         */
        PlaylistDisplayColumns.initializeColumnSets();
        
        /*
         * Initialize the internal database.
         */
        Database.initializeDB();

        /*
         * Get the input file name, if it exists.
         */
        if ((inputFileName = userPrefs.getInputFileName()) != null)
        {
            inputFileExists = true;
        }

        /*
         * Gray out the main buttons until the input file is successfully processed.
         */
        updateMainButtonsState(false);

        /*
         * Set the activity indicator size.
         */
        activityIndicator.setPreferredWidth(InternalConstants.ACTIVITY_INDICATOR_SIZE);
        activityIndicator.setPreferredHeight(InternalConstants.ACTIVITY_INDICATOR_SIZE);

        /*
         * Add widget texts.
         */
        titleLabel.setText(StringConstants.SKIN_WINDOW_MAIN);
        fileSeparator.setHeading(StringConstants.MAIN_FILE_INFO);
        dataSeparator.setHeading(StringConstants.MAIN_FILE_STATS);
        viewAudioTracksButton.setButtonData(StringConstants.MAIN_VIEW_AUDIO_TRACKS);
        viewAudioTracksButton.setTooltipText(StringConstants.MAIN_VIEW_AUDIO_TRACKS_TIP);
        viewAudioTracksButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        viewVideoTracksButton.setButtonData(StringConstants.MAIN_VIEW_VIDEO_TRACKS);
        viewVideoTracksButton.setTooltipText(StringConstants.MAIN_VIEW_VIDEO_TRACKS_TIP);
        viewVideoTracksButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        viewPlaylistsButton.setButtonData(StringConstants.MAIN_VIEW_PLAYLISTS);
        viewPlaylistsButton.setTooltipText(StringConstants.MAIN_VIEW_PLAYLISTS_TIP);
        viewPlaylistsButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        viewArtistsButton.setButtonData(StringConstants.MAIN_VIEW_ARTISTS);
        viewArtistsButton.setTooltipText(StringConstants.MAIN_VIEW_ARTISTS_TIP);
        viewArtistsButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        queryTracksButton.setButtonData(StringConstants.QUERY_TRACKS);
        queryTracksButton.setTooltipText(StringConstants.MAIN_QUERY_TRACKS_TIP);
        queryTracksButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        queryPlaylistsButton.setButtonData(StringConstants.QUERY_PLAYLISTS);
        queryPlaylistsButton.setTooltipText(StringConstants.MAIN_QUERY_PLAYLISTS_TIP);
        queryPlaylistsButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * Set the window title.
         */
        mainWindow.setTitle(Skins.Window.MAIN.getDisplayValue());

        /*
         * Register the main window skin elements.
         */
        skins.registerWindowElements(Skins.Window.MAIN, components);

        /*
         * Skin the main window.
         */
        skins.skinMe(Skins.Window.MAIN);

        /*
         * Push the skinned window onto the window stack. Note that since the
         * main window never goes away we don't need to pop it off the stack.
         */
        skins.pushSkinnedWindow(Skins.Window.MAIN);

        /*
         * Open the main window.
         */
        logger.info("opening main window");
        mainWindow.open(display);

        /*
         * We have an input file name, so read and process it.
         * 
         * We're going to read and process the input file in a background task, with an activity
         * indicator on the main window. This lets the user know that something is going on in
         * the background. When the task completes successfully, we update the main window labels
         * and inactivate the activity indicator.
         */
        if (inputFileName != null)
        {
            logger.info("using input file '" + inputFileName + "'");

            updateFromInputFile(inputFileName, mainWindow);
        }
    }

    /**
     * Processes the input file, and updates the main window file information.
     * 
     * @param inputFileName name of the input file to be processed
     * @param owningWindow owning window
     * @throws IOException If an error occurs trying to read the input
     * file.
     */
    public static void updateFromInputFile(String inputFileName, Window owningWindow) 
            throws IOException
    {

        /*
         * Start the activity indicator.
         */
        MainWindow.updateActivityIndicator(true);
        
        /*
         * Create the concurrent task.
         */
        ReadFileTask fileTask = new ReadFileTask(inputFileName);
        
        /*
         * Listener that gets called when the task completes.
         */
        TaskListener<Integer> taskListener = new TaskListener<Integer>()
        {
            
            /*
             * The task completed successfully. Stop the activity indicator, update the main
             * window labels and repaint the main window.
             */
            @Override
            public void taskExecuted(Task<Integer> task)
            {
                MainWindow.updateActivityIndicator(false);
                Utilities.updateMainWindowLabels(inputFileName);
                owningWindow.repaint(true);
            }

            /*
             * The task failed. I think this only happens if an exception was throw, in which
             * case we convert it to an internal error exception. But also throw an exception
             * if getFault returns null, because we really can't do anything without the input
             * file being processed.
             */
            @Override
            public void executeFailed(Task<Integer> task)
            {
                MainWindow.updateActivityIndicator(false);
                
                if (task.getFault() != null)
                {
                    throw new InternalErrorException(true, task.getFault().getMessage());
                }
                else
                {
                    throw new InternalErrorException(true, "failed to execute ReadFileTask");
                }
            }
        };
        
        /*
         * All set. Run the background task.
         */
        fileTask.execute(new TaskAdapter<Integer>(taskListener));
    }

    /**
     * Sets the main window activity indicator active or inactive.
     * 
     * @param value true or false
     */
    public static void updateActivityIndicator(boolean value)
    {
        activityIndicator.setActive(value);

        /*
         * We use the activity indicator going inactive as the trigger to enable the main window
         * buttons.
         */
        if (value == false)
        {
            updateMainButtonsState(true);
        }
    }

    /**
     * Enables or disables the main window buttons.
     * 
     * @param state true or false
     */
    public static void updateMainButtonsState(boolean state)
    {
        viewAudioTracksButton.setEnabled(state);
        viewVideoTracksButton.setEnabled(state);
        viewPlaylistsButton.setEnabled(state);
        viewArtistsButton.setEnabled(state);
        queryTracksButton.setEnabled(state);
        queryPlaylistsButton.setEnabled(state);
    }

    /**
     * Logs an exception and notes that fact in a static variable, so that the
     * uncaught exception handler will know not to log the exception again.
     * 
     * @param logger logger to be used
     * @param exception exception to be logged
     */
    public static void logException(Logger logger, Exception exception)
    {
        logger.error("caught " + exception.getClass().getSimpleName(), exception);

        exceptionLogged = true;
    }

    /**
     * Handles an uncaught exception, by alerting the user or shutting down the
     * application.
     * 
     * @param exception uncaught exception
     */
    @Override
    public void uncaughtExceptionThrown(Exception exception)
    {

        /*
         * Log the exception if it hasn't been logged already.
         */
        if (exceptionLogged == false)
        {

            /*
             * Special case for XMLProcessingException: log the line and column
             * of the XML file where the error was found.
             */
            if (exception instanceof XMLProcessingException)
            {
                XMLProcessingException xmlException = (XMLProcessingException) exception;
                logger.error("uncaught exception " + exception.getClass().getSimpleName() + " at line "
                        + xmlException.getLine() + ", column " + xmlException.getColumn(), exception);
            }
            else
            {
                logger.error("uncaught exception " + exception.getClass().getSimpleName(), exception);
            }
        }

        /*
         * Create a diagnostics zip file.
         */
        createDiagZipFile();

        /*
         * The main window might not have been created yet. If not, try to
         * create a bare bones window for the alert.
         */
        Window window = mainWindow;
        if (window == null)
        {
            BXMLSerializer windowSerializer = new BXMLSerializer();
            try
            {
                window = (Window) windowSerializer.readObject(getClass().getResource("barebonesWindow.bxml"));
            }
            catch (IOException | SerializationException e)
            {
                logger.error("caught exception " + e.getMessage()
                        + " trying to create bare bones window ... no alert possible", e);
                window = null;
            }
        }

        /*
         * Continue if we have a window with which to operate.
         */
        if (window != null)
        {
            /*
             * Open the window.
             */
            window.open(display);

            /*
             * For non-fatal errors, alert the user, but stay active.
             */
            if (exception instanceof InternalErrorException
                    && ((InternalErrorException) exception).getFatal() == false)
            {
                Alert.alert(MessageType.ERROR, StringConstants.ALERT_NON_FATAL_ERROR, window);

                /*
                 * Reset the exception logged flag.
                 */
                exceptionLogged = false;
            }

            /*
             * For fatal errors, alert the user, then exit the application. We
             * do this by specifying this class as the dialog close listener on
             * the alert. We implement the DialogCloseListener interface, which
             * calls dialogClosed below when the user closes the alert.
             * 
             * Note that we get to this else clause for all fatal exceptions
             * such as IOException, as well as InternalErrorException cases with
             * the fatal flag set.
             */
            else
            {
                Alert.alert(MessageType.ERROR, StringConstants.ALERT_FATAL_ERROR, window, this);
            }
        }

        /*
         * No alert is possible, so just crash and burn.
         */
        else
        {
            logger.error("application ended due to error, no alert possible");
            DesktopApplicationContext.exit(false);
        }
    }

    /**
     * Exits the application upon a fatal error. This gets control when the
     * fatal error dialog is closed by the user.
     * 
     * @param dialog dialog window that was closed
     * @param modal true if the dialog was modal over another window
     */
    @Override
    public void dialogClosed(Dialog dialog, boolean modal)
    {
        logger.error("application ended due to error, user alerted");
        DesktopApplicationContext.exit(false);
    }

    /**
     * Shuts down the application.
     * 
     * @param optional indicates if the shutdown is optional
     * @return <code>true</code> if further shutdown is optional, otherwise
     * <code>false</code>
     */
    @Override
    public boolean shutdown(boolean optional)
    {
        logger.info("application ended");

        if (mainWindow != null)
        {
            mainWindow.close();
        }

        return false;
    }

    /**
     * Suspends the application (this method is not used).
     */
    @Override
    public void suspend()
    {
    }

    /**
     * Resumes the application (this method is not used).
     */
    @Override
    public void resume()
    {
    }

    /**
     * Specifies the main application entry point.
     * 
     * @param args program arguments
     */
    public static void main(String[] args)
    {

        /*
         * This method instantiates our class, then calls our startup() method.
         */
        DesktopApplicationContext.main(MainWindow.class, args);
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers.
     */
    private void createEventHandlers(Display display)
    {
        logger.trace("createEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the view audio tracks button press.
         */
        viewAudioTracksButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("view audio tracks button pressed");

                try
                {
                    TracksWindow tracksWindowHandler = new TracksWindow();
                    tracksWindowHandler.displayTracks(display, Skins.Window.AUDIO_TRACKS, 
                    		Database.getTracksFromMap(Database.getAudioTracksMap()), null);
                }
                catch (IOException | SerializationException e)
                {
                    logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        });

        /*
         * Listener to handle the view video tracks button press.
         */
        viewVideoTracksButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("view video tracks button pressed");

                try
                {
                    TracksWindow tracksWindowHandler = new TracksWindow();
                    tracksWindowHandler.displayTracks(display, Skins.Window.VIDEO_TRACKS, 
                    		Database.getTracksFromMap(Database.getVideoTracksMap()), null);
                }
                catch (IOException | SerializationException e)
                {
                    logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        });

        /*
         * Listener to handle the view playlists button press.
         */
        viewPlaylistsButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("view playlists button pressed");

                try
                {
                    PlaylistsWindow playlistsWindowHandler = new PlaylistsWindow();
                    playlistsWindowHandler.displayPlaylists(display);
                }
                catch (IOException | SerializationException e)
                {
                    logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        });

        /*
         * Listener to handle the view artists button press.
         */
        viewArtistsButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("view artists button pressed");

                try
                {
                    ArtistsWindow artistsWindowHandler = new ArtistsWindow();
                    artistsWindowHandler.displayArtists(display);
                }
                catch (IOException | SerializationException e)
                {
                    logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        });

        /*
         * Listener to handle the query tracks button press.
         */
        queryTracksButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("query tracks button pressed");

                try
                {
                    FiltersWindow filtersWindowHandler = new FiltersWindow();
                    filtersWindowHandler.displayFilters(display);
                }
                catch (IOException | SerializationException e)
                {
                    logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        });

        /*
         * Listener to handle the query playlists button press.
         */
        queryPlaylistsButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("query playlists button pressed");

                try
                {
                    QueryPlaylistsWindow queryPlaylistsWindowHandler = new QueryPlaylistsWindow();
                    queryPlaylistsWindowHandler.displayQueryPlaylists(display);
                }
                catch (IOException | SerializationException e)
                {
                    logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        });

        /*
         * This window state listener gets control when the main window opens.
         * If we don't have an input file, gently prod the user to provide one.
         */
        mainWindow.getWindowStateListeners().add(new WindowStateListener.Adapter()
        {
            @Override
            public void windowOpened(Window window)
            {
                if (inputFileExists == false)
                {
                    logger.info("Input file does not exist");

                    Alert.alert(MessageType.INFO, StringConstants.ALERT_NO_INPUT_FILE, mainWindow);
                }
            }
        });
    }

    /*
     * Create a zip file of diagnostic information.
     */
    private String createDiagZipFile()
    {

        /*
         * Create the zip path.
         */
        StringBuilder zipFilename = new StringBuilder();
        zipFilename.append("diag_");
        zipFilename.append(Utilities.getCurrentTimestamp());
        zipFilename.append(".zip");
        
        Path zipPath = Paths.get(saveDirectory, zipFilename.toString());

        /*
         * Initialize the zip file stream.
         */
        boolean zipStreamUsable = true;
        ZipOutputStream zipStream = null;
        try
        {
            zipStream = new ZipOutputStream(new FileOutputStream(zipPath.toString()));
        }
        catch (FileNotFoundException e)
        {

            /*
             * We failed to create the zip file stream. We're called from the
             * uncaught exception handler, so can't do anything about this
             * exception. So clear the zip file name to tell the caller not to
             * email the file.
             */
            zipStreamUsable = false;
            zipPath = null;
        }

        /*
         * Copy files into the zip file if the zip stream is usable. We don't
         * care if any copies fail, because hopefully we still get some
         * diagnostic info.
         */
        if (zipStreamUsable == true)
        {

            /*
             * Copy the application preferences file.
             */
            copyFileToZip(Preferences.getPrefsFilePath(), zipStream);

            /*
             * Copy the input file.
             */
            copyFileToZip(userPrefs.getInputFileName(), zipStream);

            /*
             * Copy all log files.
             */
            File logDirectory = new File(saveDirectory);
            File[] logFiles = logDirectory.listFiles((d, name) -> name.endsWith(".log"));

            for (int i = 0; i < logFiles.length; i++)
            {
                copyFileToZip(logFiles[i].getPath(), zipStream);
            }

            /*
             * Close the zip stream.
             */
            try
            {
                zipStream.close();
            }
            catch (IOException e)
            {
            }
        }

        return zipPath.toString();
    }

    /*
     * Copy a single file into the diagnostic zip file.
     */
    private boolean copyFileToZip(String filePath, ZipOutputStream zipStream)
    {
        boolean result = true;

        /*
         * Create a zip entry to represent the file.
         */
        Path zipPath = Paths.get(filePath);
        ZipEntry zipEntry = new ZipEntry(zipPath.getFileName().toString());

        /*
         * Copy the file into the zip stream.
         */
        try
        {
            zipStream.putNextEntry(zipEntry);
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            zipStream.write(bytes, 0, bytes.length);
            zipStream.closeEntry();
        }
        catch (IOException e)
        {
            result = false;
        }

        return result;
    }

    /*
     * Get the diag trigger data from a secret file.
     */
    private void getDiagTriggerData()
    {
        logger.trace("getDiagTriggerData: " + this.hashCode());

        /*
         * Create the file name we expect to find, based on the type of diag trigger.
         */
        Path diagTriggerPath = Paths.get(saveDirectory, "diag-trigger", diagTrigger.getDisplayValue());
        String diagTriggerFilename = diagTriggerPath.toString();
        BufferedReader reader = null;

        /*
         * Create a reader to read the file.
         */
        try
        {
            reader = new BufferedReader(new FileReader(diagTriggerFilename));
        }
        catch (FileNotFoundException e)
        {
            // Not an error: dumb user forgot the file.
        }

        /*
         * Continue if we have a file to read.
         */
        if (reader != null)
        {

            /*
             * We expect only a single line in the file, containing the data, so that's all we read.
             */
            try
            {
                diagTriggerValue = reader.readLine();
            }
            catch (IOException e)
            {
                logException(logger, e);
                throw new InternalErrorException(true, e.getMessage());
            }
            finally
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        }
    }

    /*
     * Initialize BXML variables and collect the list of components to be
     * skinned.
     */
    private void initializeBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        logger.trace("initializeBxmlVariables: " + this.hashCode());

        BXMLSerializer windowSerializer = new BXMLSerializer();

        mainWindow = 
                (Window) windowSerializer.readObject(getClass().getResource("mainWindow.bxml"));

        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = 
                (MenuBars) mainWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);

        infoBorder = 
                (Border) windowSerializer.getNamespace().get("infoBorder");
        components.add(infoBorder);
        infoFillPane = 
                (FillPane) windowSerializer.getNamespace().get("infoFillPane");
        components.add(infoFillPane);
        titleLabel = 
                (Label) windowSerializer.getNamespace().get("titleLabel");
        components.add(titleLabel);
        activityIndicator = 
                (ActivityIndicator) windowSerializer.getNamespace().get("activityIndicator");
        components.add(activityIndicator);
        fileSeparator = 
                (Separator) windowSerializer.getNamespace().get("fileSeparator");
        components.add(fileSeparator);
        fileBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("fileBoxPane");
        components.add(fileBoxPane);
        fileLabel = 
                (Label) windowSerializer.getNamespace().get("fileLabel");
        components.add(fileLabel);
        dataSeparator = 
                (Separator) windowSerializer.getNamespace().get("dataSeparator");
        components.add(dataSeparator);
        dataBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("dataBoxPane");
        components.add(dataBoxPane);
        numAudioTracksLabel = 
                (Label) windowSerializer.getNamespace().get("numAudioTracksLabel");
        components.add(numAudioTracksLabel);
        numVideoTracksLabel = 
                (Label) windowSerializer.getNamespace().get("numVideoTracksLabel");
        components.add(numVideoTracksLabel);
        numPlaylistsLabel = 
                (Label) windowSerializer.getNamespace().get("numPlaylistsLabel");
        components.add(numPlaylistsLabel);
        numArtistsLabel = 
                (Label) windowSerializer.getNamespace().get("numArtistsLabel");
        components.add(numArtistsLabel);
        actionBorder = 
                (Border) windowSerializer.getNamespace().get("actionBorder");
        components.add(actionBorder);
        actionBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("actionBoxPane");
        components.add(actionBoxPane);
        viewAudioTracksButton = 
                (PushButton) windowSerializer.getNamespace().get("viewAudioTracksButton");
        components.add(viewAudioTracksButton);
        viewVideoTracksButton = 
                (PushButton) windowSerializer.getNamespace().get("viewVideoTracksButton");
        components.add(viewVideoTracksButton);
        viewPlaylistsButton = 
                (PushButton) windowSerializer.getNamespace().get("viewPlaylistsButton");
        components.add(viewPlaylistsButton);
        viewArtistsButton = 
                (PushButton) windowSerializer.getNamespace().get("viewArtistsButton");
        components.add(viewArtistsButton);
        queryTracksButton = 
                (PushButton) windowSerializer.getNamespace().get("queryTracksButton");
        components.add(queryTracksButton);
        queryPlaylistsButton = 
                (PushButton) windowSerializer.getNamespace().get("queryPlaylistsButton");
        components.add(queryPlaylistsButton);
    }

    // ---------------- Nested classes --------------------------------------
    
    /**
     * Inner class that encapsulates reading the input file in a
     * background task.
     * 
     * @author Jon
     *
     */
    public static final class ReadFileTask extends Task<Integer>
    {
    	
    	/*
    	 * Private variables.
    	 */
        private static Logger uiLogger = (Logger) LoggerFactory.getLogger(MainWindow.class.getSimpleName() + "_UI");
        private static String inputFileName;
        
        /*
         * The type of processors that need to be run.
         */
        private enum Processors
        {
        	XML, JSON
        }
        
        /**
         * Constructor.
         * 
         * @param fileName input file name
         */
        public ReadFileTask(String fileName)
        {
        	inputFileName = fileName;
        }
        
        /**
         * Execute on a concurrent task.
         * 
         * @return <code>zero</code> if successful
         */
        @Override
        public Integer execute()
        {
            int result = 0;
            
            /*
             * Initialize logging.
             */
            Logging logging = Logging.getInstance();
            logging.registerLogger(Logging.Dimension.UI, uiLogger);
            
            /*
             * Gather the input file name.
             */
        	String fileNameExt = FilenameUtils.getExtension(inputFileName);
        	
        	/*
        	 * Determine which file processors need to be run.
        	 */
        	Processors processors;
        	
        	if (fileNameExt.equals(StringConstants.XML))
        	{
        		processors = Processors.XML;
        	}
        	else if (fileNameExt.equals(StringConstants.JSON))
        	{
        		processors = Processors.JSON;
        	}
        	else
        	{
                throw new InternalErrorException(false, "Input file is of unknown type");
        	}
            
            /*
             * Process the input file.
             */
            try
            {
            	switch (processors)
            	{
            	case XML:
            		XMLHandler.processXML(inputFileName);
            		break;
            		
            	case JSON:
            		JSONHandler.processJSON(inputFileName);
            		break;
            	}
            }
            catch (IOException e)
            {
                MainWindow.logException(uiLogger, e);
                throw new InternalErrorException(true, e.getMessage());
            }
            
            return result;            
        }
    }
}
