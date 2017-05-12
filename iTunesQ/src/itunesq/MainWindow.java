package itunesq;

import java.io.IOException;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that represents a Pivot application.
 * 
 * This is the main class for the application. The startup() method is called when the application
 * starts. Its primary job is to manage the Pivot UI.
 * 
 * @author Jon
 *
 */
public class MainWindow implements Application 
{

    //---------------- Private variables -----------------------------------
	
    private Window mainWindow = null;
	private boolean xmlFileExists = false;
	private Logger logger = null;
	private Logging logging = null;
	
	/*
	 * BXML variables.
	 */
	@BXML private MenuBar mainMenuBar = null;
	@BXML private Menu mainFileMenu = null;
	@BXML private Menu mainEditMenu = null;
	@BXML private Border primaryBorder = null;
	@BXML private Border infoBorder = null;
	@BXML private TablePane infoTablePane = null;
    @BXML private Label titleLabel = null;
    @BXML private Label fileNameStaticLabel = null;
    @BXML private Label fileNameLabel = null;
    @BXML private Label fileDateStaticLabel = null;
    @BXML private Label fileDateLabel = null;
    @BXML private Label numTracksStaticLabel = null;
    @BXML private Label numTracksLabel = null;
    @BXML private Label numPlaylistsStaticLabel = null;
    @BXML private Label numPlaylistsLabel = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
    @BXML private PushButton viewTracksButton = null;
    @BXML private PushButton viewPlaylistsButton = null;
    @BXML private PushButton queryButton = null;
    @BXML private ActivityIndicator activityIndicator = null;
    
    /**
     * Constructor.
     */
    public MainWindow ()
    {
    	
    	/*
    	 * Create a UI logger.
    	 */
    	String className = getClass().getSimpleName();
    	logger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Create the logging object singleton.
    	 */
    	logging = Logging.getInstance();
    	
    	/*
    	 * Register our logger, and set the default logging level.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, logger);
    	logging.setDefaultLogLevel(logger.getEffectiveLevel());
    	
    	/*
    	 * Initialize loggers in static classes. 
    	 */
    	PlaylistCollection.initializeLogging();
    	PlaylistTree.initializeLogging();
    	XMLHandler.initializeLogging();
    }

    //---------------- Public methods --------------------------------------

    /**
     * Startup method that gets control when the application is launched.
     * 
     * @param display Display object for managing windows.
     * @param properties Properties passed to the application.
     * @throws Exception
     */
    @Override
    public void startup (Display display, Map<String, String> properties) 
    		throws Exception
    {
    	logger.info("application started");
    	
    	/*
    	 * Get the BXML information for the main window, and generate the list of components
    	 * to be skinned.
    	 */
		List<Component> components = new ArrayList<Component>();
    	try
		{
			initializeBxmlVariables(components);
		} 
    	catch (IOException | SerializationException e)
		{
			e.printStackTrace();
		}
        
        /*
         * Listener to handle the view tracks button press.
         */
        viewTracksButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	logger.info("view tracks button pressed");
            	
            	try
				{
            		TracksWindow tracksWindowHandler = new TracksWindow();
            		tracksWindowHandler.displayTracks(display, XMLHandler.getTracks(), false);
				} 
            	catch (IOException | SerializationException e)
				{
					e.printStackTrace();
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
					e.printStackTrace();
				}
            }
        });

        /*
         * Listener to handle the query button press.
         */
        queryButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	logger.info("query button pressed");
            	
            	try
				{
            		FiltersWindow filtersWindowHandler = new FiltersWindow();
            		filtersWindowHandler.displayFilters(display);
				} 
            	catch (IOException | SerializationException e)
				{
					e.printStackTrace();
				}
            }
        });
        
        /*
         * This window state listener gets control when the main window opens. If we don't have an XML file,
         * gently prod the user to provide one.
         */
        mainWindow.getWindowStateListeners().add(new WindowStateListener.Adapter()
        {
        	@Override
        	public void windowOpened(Window window)
        	{
        		if (xmlFileExists == false)
        		{
        			Alert.alert(MessageType.INFO, 
        					"No XML file has been saved. Use the File ... Open menu to select a file.", 
        					mainWindow);
        		}
        	}
        });
        
        //---------------- Start of Initialization -----------------------------
		
		/*
		 * Create the preferences object singleton.
		 */
		Preferences userPrefs = Preferences.getInstance();
        
		/*
		 * Get the preferences file save directory using the Java preferences API. We have to save
		 * this directory using the Java API instead of in our preferences file to avoid a
		 * catch-22. In hindsight I would save all our preferences using the Java API, but I already
		 * did it using a file and don't feel like rewriting a bunch of code.
		 */
        String saveDirectory = Utilities.accessJavaPreference(Utilities.JAVA_PREFS_KEY_SAVEDIR);
        
        /*
         * Save the directory in the user preferences for reading and writing the serialized object.
         */
        userPrefs.setSaveDirectory(saveDirectory);
		
		/*
		 * Read the preferences, if they exist, and update the running copy.
		 */
		Preferences existingPrefs = userPrefs.readPreferences();
		if (existingPrefs != null)
		{
			userPrefs.updatePreferences(existingPrefs);
		}
		
		/*
		 * Set the log levels from any existing preferences.
		 */
		logging.setLogLevelsFromPrefs();
		
		/*
		 * Create the skins object singleton.
		 * 
		 * NOTE: This must be done after the running preferences have been updated, because the Skins
		 * constructor needs to read the preferences to initialize the preferred skin.
		 */
		Skins skins = Skins.getInstance();
		mainWindow.setTitle(Skins.Window.MAIN.getDisplayValue());
		
		/*
		 * Register the main window skin elements.
		 */
		Map<Skins.Element, List<Component>> windowElements = 
				new HashMap<Skins.Element, List<Component>>();
		
		windowElements = skins.mapComponentsToSkinElements(components);		
		skins.registerWindowElements(Skins.Window.MAIN, windowElements);

		/*
		 * Save the main window information labels so they can be used from other windows.
		 */
        Utilities.setFileNameLabel(fileNameLabel);
        Utilities.setFileDateLabel(fileDateLabel);
        Utilities.setNumTracksLabel(numTracksLabel);
        Utilities.setNumPlaylistsLabel(numPlaylistsLabel);
		
		/*
		 * Initialize the tracks display column defaults.
		 */
		TrackDisplayColumns.initializeDefaults();
		
		/*
		 * Get the XML file name, if it exists.
		 */
		String xmlFileName = null;
		xmlFileName = userPrefs.getXMLFileName();
		if (xmlFileName != null)
		{
			xmlFileExists = true;
		}
		
		/*
		 * Skin the main window.
		 */
		skins.skinMe(Skins.Window.MAIN);
        
        /*
         * Open the main window.
         */
    	logger.info("opening main window");
        mainWindow.open(display);
    	
    	/*
    	 * If we have an XML file name, proceed to digest it.
    	 */
    	Thread xmlThread = null;
    	if (xmlFileExists == true)
    	{
    		activityIndicator.setActive(true);
    		
    		/*
    		 * Process the XML file in a new thread.
    		 */
        	logger.info("starting thread to process XML file '" + xmlFileName + "'");
    		xmlThread = new Thread(new ProcessXMLThread(xmlFileName));
    		xmlThread.start();
        	
        	/*
        	 * Wait for the thread to complete.
        	 */
        	try
			{
				xmlThread.join();
			} 
        	catch (InterruptedException e)
			{
				e.printStackTrace();
			}
        	
        	logger.info("XML thread ended");
        	Exception exception = ProcessXMLThread.getSavedException();
        	if (exception != null)
        	{
        		throw exception;
        	}
        	
    		activityIndicator.setActive(false);
    		
    		/*
    		 * Update the main window information based on the XML file contents.
    		 */
			fileNameLabel.setText(xmlFileName);
			fileDateLabel.setText(XMLHandler.getXMLFileTimestamp());
			numTracksLabel.setText(Integer.toString(XMLHandler.getNumberOfTracks()));
			numPlaylistsLabel.setText(Integer.toString(XMLHandler.getNumberOfPlaylists()));

			/*
			 * Repaint the main window.
			 */
			mainWindow.repaint();
    	}
    }

    /**
     * The application is shutting down.
     * 
     * @param optional Indicates if the shutdown is optional.
     * @return true if further shutdown is optional, false otherwise.
     */
    @Override
    public boolean shutdown (boolean optional) 
    {
    	logger.info("application ended");
    	
        if (mainWindow != null) 
        {
            mainWindow.close();
        }

        return false;
    }

    @Override
    public void suspend () 
    {
    }

    @Override
    public void resume () 
    {
    }

    /**
     * Main application entry.
     * 
     * @param args Arguments.
     */
    public static void main (String[] args) 
    {
    	
    	/*
    	 * This method calls our startup() method.
    	 */
        DesktopApplicationContext.main(MainWindow.class, args);
    }

    //---------------- Private methods -------------------------------------
    
    /*
     * Initialize BXML variables and collect the list of components to be skinned.
     */
    private void initializeBxmlVariables (List<Component> components) 
    		throws IOException, SerializationException
    {
        BXMLSerializer windowSerializer = new BXMLSerializer();
        mainWindow = 
        		(Window)windowSerializer.readObject(getClass().getResource("mainWindow.bxml"));

        mainMenuBar = 
        		(MenuBar)windowSerializer.getNamespace().get("mainMenuBar");
		components.add(mainMenuBar);
        mainFileMenu = 
        		(Menu)windowSerializer.getNamespace().get("mainFileMenu");
		components.add(mainFileMenu);
        mainEditMenu = 
        		(Menu)windowSerializer.getNamespace().get("mainEditMenu");
		components.add(mainEditMenu);
        primaryBorder = 
        		(Border)windowSerializer.getNamespace().get("primaryBorder");
		components.add(primaryBorder);
        infoBorder = 
        		(Border)windowSerializer.getNamespace().get("infoBorder");
		components.add(infoBorder);
        infoTablePane = 
        		(TablePane)windowSerializer.getNamespace().get("infoTablePane");
		components.add(infoTablePane);
        titleLabel = 
        		(Label)windowSerializer.getNamespace().get("titleLabel");
		components.add(titleLabel);
        fileNameStaticLabel = 
        		(Label)windowSerializer.getNamespace().get("fileNameStaticLabel");
		components.add(fileNameStaticLabel);
        fileNameLabel = 
        		(Label)windowSerializer.getNamespace().get("fileNameLabel");
		components.add(fileNameLabel);
        fileDateStaticLabel = 
        		(Label)windowSerializer.getNamespace().get("fileDateStaticLabel");
		components.add(fileDateStaticLabel);
        fileDateLabel = 
        		(Label)windowSerializer.getNamespace().get("fileDateLabel");
		components.add(fileDateLabel);
        numTracksStaticLabel = 
        		(Label)windowSerializer.getNamespace().get("numTracksStaticLabel");
		components.add(numTracksStaticLabel);
        numTracksLabel = 
        		(Label)windowSerializer.getNamespace().get("numTracksLabel");
		components.add(numTracksLabel);
        numPlaylistsStaticLabel = 
        		(Label)windowSerializer.getNamespace().get("numPlaylistsStaticLabel");
		components.add(numPlaylistsStaticLabel);
        numPlaylistsLabel = 
        		(Label)windowSerializer.getNamespace().get("numPlaylistsLabel");
		components.add(numPlaylistsLabel);
        actionBorder = 
        		(Border)windowSerializer.getNamespace().get("actionBorder");
		components.add(actionBorder);
        actionBoxPane = 
        		(BoxPane)windowSerializer.getNamespace().get("actionBoxPane");
		components.add(actionBoxPane);
        viewTracksButton = 
        		(PushButton)windowSerializer.getNamespace().get("viewTracksButton");
		components.add(viewTracksButton);
        viewPlaylistsButton = 
        		(PushButton)windowSerializer.getNamespace().get("viewPlaylistsButton");
		components.add(viewPlaylistsButton);
        queryButton = 
        		(PushButton)windowSerializer.getNamespace().get("queryButton");
		components.add(queryButton);
        activityIndicator = 
        		(ActivityIndicator)windowSerializer.getNamespace().get("activityIndicator");
		components.add(activityIndicator);
    }
}            
