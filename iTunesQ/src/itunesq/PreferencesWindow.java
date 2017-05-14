package itunesq;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class that handles the user preferences window.
 * 
 * @author Jon
 *
 */
public class PreferencesWindow
{
	
    //---------------- Private variables -----------------------------------
	
    private Sheet preferencesSheet = null;
    private MenuBars owningWindow = null;
	private int plusButtonYCoordinate = -1;
	private int minusButtonYCoordinate = -1;
	private Preferences userPrefs = null;
	private Dialog skinPreviewDialog = null;
	private Logging logging = null;
	
	private boolean bypassPrefsUpdated;
	private boolean filteredPrefsUpdated;
	private boolean fullTrackColumnsUpdated;
	private boolean filteredTrackColumnsUpdated;
	private boolean playlistTrackColumnsUpdated;
	private boolean skinPrefsUpdated;
	private boolean logLevelPrefsUpdated;
	private boolean saveDirectoryUpdated;
	private Logger logger;
	private String owningWindowTitle;

	/*
	 * BXML variables.
	 */
	@BXML private Border primaryBorder = null;
	@BXML private TabPane tabPane = null;
	@BXML private Border bypassPrefsBorder = null;
	@BXML private BoxPane bypassPrefsBoxPane = null;
	@BXML private Label bypassPrefsBorderLabel = null;
	@BXML private TablePane bypassPrefsTablePane = null;
	@BXML private Border filteredPrefsBorder = null;
	@BXML private BoxPane filteredPrefsBoxPane = null;
	@BXML private Label filteredPrefsBorderLabel = null;
	@BXML private TablePane filteredPrefsTablePane = null;
	@BXML private Label columnPrefsBorderLabel = null;
	@BXML private Border columnPrefsBorder = null;
	@BXML private TablePane columnPrefsTablePane = null;
	
	@BXML private BoxPane fullColumnPrefsBoxPane = null;
	@BXML private Label fullColumnPrefsLabel = null;
	@BXML private Checkbox fullNumberCheckbox = null;
	@BXML private Checkbox fullNameCheckbox = null;
	@BXML private Checkbox fullArtistCheckbox = null;
	@BXML private Checkbox fullAlbumCheckbox = null;
	@BXML private Checkbox fullKindCheckbox = null;
	@BXML private Checkbox fullDurationCheckbox = null;
	@BXML private Checkbox fullYearCheckbox = null;
	@BXML private Checkbox fullAddedCheckbox = null;
	@BXML private Checkbox fullRatingCheckbox = null;

	@BXML private BoxPane filteredColumnPrefsBoxPane = null;
	@BXML private Label filteredColumnPrefsLabel = null;
	@BXML private Checkbox filteredNumberCheckbox = null;
	@BXML private Checkbox filteredNameCheckbox = null;
	@BXML private Checkbox filteredArtistCheckbox = null;
	@BXML private Checkbox filteredAlbumCheckbox = null;
	@BXML private Checkbox filteredKindCheckbox = null;
	@BXML private Checkbox filteredDurationCheckbox = null;
	@BXML private Checkbox filteredYearCheckbox = null;
	@BXML private Checkbox filteredAddedCheckbox = null;
	@BXML private Checkbox filteredRatingCheckbox = null;

	@BXML private BoxPane playlistColumnPrefsBoxPane = null;
	@BXML private Label playlistColumnPrefsLabel = null;
	@BXML private Checkbox playlistNumberCheckbox = null;
	@BXML private Checkbox playlistNameCheckbox = null;
	@BXML private Checkbox playlistArtistCheckbox = null;
	@BXML private Checkbox playlistAlbumCheckbox = null;
	@BXML private Checkbox playlistKindCheckbox = null;
	@BXML private Checkbox playlistDurationCheckbox = null;
	@BXML private Checkbox playlistYearCheckbox = null;
	@BXML private Checkbox playlistAddedCheckbox = null;
	@BXML private Checkbox playlistRatingCheckbox = null;

	@BXML private Label skinPrefsBorderLabel = null;
	@BXML private Border skinPrefsBorder = null;
	@BXML private BoxPane skinPrefsBoxPane = null;
	@BXML private Spinner skinPrefsSpinner = null;
	@BXML private Label saveDirectoryBorderLabel = null;
	@BXML private Border saveDirectoryBorder = null;
	@BXML private BoxPane saveDirectoryBoxPane = null;
	@BXML private TextInput saveDirectoryTextInput = null;
	
	@BXML private PushButton skinPrefsButton = null;
	@BXML private Label logLevelPrefsBorderLabel = null;
	@BXML private Border logLevelPrefsBorder = null;
	@BXML private TablePane logLevelPrefsTablePane = null;
	@BXML private BoxPane logLevelPrefsBoxPane = null;
	@BXML private Spinner logLevelPrefsSpinner = null;
	@BXML private Checkbox logLevelPrefsCheckbox = null;
	@BXML private BoxPane uiLogLevelPrefsBoxPane = null;
	@BXML private Label uiLogLevelPrefsLabel = null;
	@BXML private Spinner uiLogLevelPrefsSpinner = null;
	@BXML private BoxPane xmlLogLevelPrefsBoxPane = null;
	@BXML private Label xmlLogLevelPrefsLabel = null;
	@BXML private Spinner xmlLogLevelPrefsSpinner = null;
	@BXML private BoxPane trackLogLevelPrefsBoxPane = null;
	@BXML private Label trackLogLevelPrefsLabel = null;
	@BXML private Spinner trackLogLevelPrefsSpinner = null;
	@BXML private BoxPane playlistLogLevelPrefsBoxPane = null;
	@BXML private Label playlistLogLevelPrefsLabel = null;
	@BXML private Spinner playlistLogLevelPrefsSpinner = null;
	@BXML private BoxPane filterLogLevelPrefsBoxPane = null;
	@BXML private Label filterLogLevelPrefsLabel = null;
	@BXML private Spinner filterLogLevelPrefsSpinner = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
	@BXML private PushButton preferencesDoneButton = null;

	@BXML private Border previewPrimaryBorder = null;
	@BXML private Border previewTextBorder = null;
	@BXML private BoxPane previewTextBoxPane = null;
	@BXML private Label previewTextLabel = null;
	@BXML private TextInput previewTextInput = null;
	@BXML private Border previewTableBorder = null;
	@BXML private TableView previewTableView = null;
	@BXML private TableViewHeader previewTableViewHeader = null;
	@BXML private Border previewButtonBorder = null;
	@BXML private BoxPane previewButtonBoxPane = null;
	@BXML private PushButton previewButton = null;
	
	/**
	 * Constructor.
	 */
	public PreferencesWindow (Sheet preferences, MenuBars owner)
	{
    	
    	/*
    	 * Create a UI logger.
    	 */
    	String className = getClass().getSimpleName();
    	logger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Get the logging object singleton.
    	 */
    	logging = Logging.getInstance();
    	
    	/*
    	 * Register our logger.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, logger);
    	
    	/*
    	 * Initialize variables.
    	 */
		preferencesSheet = preferences;
		owningWindow = owner;
		userPrefs = Preferences.getInstance();
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Display the user preferences in a new window.
	 * 
	 * @param display Display object for managing windows.
	 * @throws IOException
	 * @throws SerializationException
	 */
    public void displayPreferences (Display display) 
    		throws IOException, SerializationException
    {
    	
    	/*
    	 * Get the BXML information for the playlists window, and generate the list of components
    	 * to be skinned.
    	 */
		List<Component> components = new ArrayList<Component>();
        BXMLSerializer prefsWindowSerializer = new BXMLSerializer();
		initializeWindowBxmlVariables(prefsWindowSerializer, components);
        
        /*
         * Checkboxes and spinners have a lot of boilerplate to deal with, so do it elsewhere. 
         */
        initializeTrackColumnStuff(prefsWindowSerializer, components);
        initializeLogLevelStuff(prefsWindowSerializer, components);
        
        /*
         * Listener to handle the skin preview button press.
         * 
         * When the skin preview button is pressed, we pop up a dialog that contains a sampling
         * of window elements. We skin this dialog with the selected skin.
         */
        skinPrefsButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
				logger.info("skin preview button pressed");
				
            	Display display = button.getDisplay();

        		/*
        		 * Get the BXML information for the skin preview dialog, and gather the list of 
        		 * components to be skinned.
        		 */
    			List<Component> components = new ArrayList<Component>();
        		try
        		{
        			initializePreviewDialogBxmlVariables(components);
        		}
        		catch (IOException | SerializationException e)
        		{
        			e.printStackTrace();
        		}

				/*
				 * Text input listener to provide typing assistance in the text input box.
				 */
				previewTextInput.getTextInputContentListeners().add(
						new TextInputContentListener.Adapter()
		    	{
		            @Override
		            public void textInserted(TextInput textInput, int index, int count)
		            {
		            	
		            	/*
		            	 * We want to match weekdays in the text input box.
		            	 */
						ArrayList<String> weekdays = new ArrayList<String>(
								"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", 
								"Friday", "Saturday");
						weekdays.setComparator(String.CASE_INSENSITIVE_ORDER);
		            	boolean result = Utilities.typingAssistant(textInput, weekdays, 
		            			textInput.getText(), Filter.Operator.IS);
		            	
		            	/*
		            	 * We got a weekday match.
		            	 */
		            	if (result == true)
		            	{
		            		String text = textInput.getText();
		            		
		            		/*
		            		 * Walk through the preview table, which contains weekdays.
		            		 */
		            		@SuppressWarnings("unchecked")
							List<HashMap<String, String>> tableData = 
		            				(List<HashMap<String, String>>) previewTableView.getTableData();
		            		for (int i = 0; i < tableData.getLength(); i++)
		            		{
		            			HashMap<String, String> row = tableData.get(i);
		            			
		            			/*
		            			 * If the entered weekday matches, select the corresponding weekday
		            			 * in the table. To demonstrate our awesomeness.
		            			 */
		            			String weekday = row.get("weekday");
		            			if (text.equals(weekday))
		            			{
		            				previewTableView.setSelectedIndex(i);
		            				break;
		            			}
		            		}
		            	}
		            }    		
		    	});

				/*
				 * Get the skins singleton.
				 */
				Skins skins = Skins.getInstance();

				/*
				 * Register the preview dialog skin elements.
				 */
				Map<Skins.Element, List<Component>> windowElements = 
						new HashMap<Skins.Element, List<Component>>();
				
				windowElements = skins.mapComponentsToSkinElements(components);		
				skins.registerWindowElements(Skins.Window.SKINPREVIEW, windowElements);
				
				/*
				 * Save the current skin name so we can restore it after we've skinned the
				 * preview dialog.
				 */
				String currentSkin = skins.getCurrentSkinName();
				
				/*
				 * Get the selected skin name and initialize the skin elements.
				 */
        		String skinPref = (String) skinPrefsSpinner.getSelectedItem();
        		skins.initializeSkinElements(skinPref);

        		/*
        		 * Skin the preview dialog.
        		 */
				skins.skinMe(Skins.Window.SKINPREVIEW);

				/*
				 * Restore the current skin elements.
				 */
        		skins.initializeSkinElements(currentSkin);
				
        		/*
        		 * Open the preview dialog. The close button action is included in the BXML.
        		 */
            	logger.info("opening preview dialog");
				skinPreviewDialog.open(display);
            }
        });
    	
    	/*
    	 * Listener to handle a change to the save directory text box. 
    	 * 
    	 * This is so we can indicate the save directory has been updated when the user inserts
    	 * a directory name in the text box.
    	 */
        saveDirectoryTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
    	{
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
            	saveDirectoryUpdated = true;
            }    		
    	});
        
        /*
         * Listener to handle the done button press.
         */
        preferencesDoneButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	boolean prefsUpdated = false;
				logger.info("done button pressed");
            	
            	/*
            	 * Collect the preferences from the preferences window, and update the preferences object.
            	 */
            	
            	if (bypassPrefsUpdated == true)
            	{
					logger.info("updating bypass playlist preferences");
					
            		prefsUpdated = true;
            		
            		List<BypassPreference> bypassPrefs = collectBypassPrefs();
            		userPrefs.replaceBypassPrefs(bypassPrefs);
            	}
            	
            	if (filteredPrefsUpdated == true)
            	{
					logger.info("updating filtered playlist preferences");
					
            		prefsUpdated = true;
            		
            		List<String> filteredPrefs = collectFilteredPrefs();
            		userPrefs.replaceFilteredPrefs(filteredPrefs);
            	}
            	
            	if (fullTrackColumnsUpdated == true)
            	{
					logger.info("updating full track column preferences");
					
            		prefsUpdated = true;
            		
            		List<List<String>> trackColumnPrefs = collectFullTrackColumnPrefs();
            		userPrefs.replaceTrackColumnsFullView(trackColumnPrefs);

            		TrackDisplayColumns.ColumnSet.FULL_VIEW.buildColumnSet(trackColumnPrefs);
            	}
            	
            	if (filteredTrackColumnsUpdated == true)
            	{
					logger.info("updating filtered track column preferences");
					
            		prefsUpdated = true;
            		
            		List<List<String>> trackColumnPrefs = collectFilteredTrackColumnPrefs();
            		userPrefs.replaceTrackColumnsFilteredView(trackColumnPrefs);

            		TrackDisplayColumns.ColumnSet.FILTERED_VIEW.buildColumnSet(trackColumnPrefs);
            	}
            	
            	if (playlistTrackColumnsUpdated == true)
            	{
					logger.info("updating playlist track column preferences");
					
            		prefsUpdated = true;
            		
            		List<List<String>> trackColumnPrefs = collectPlaylistTrackColumnPrefs();
            		userPrefs.replaceTrackColumnsPlaylistView(trackColumnPrefs);

            		TrackDisplayColumns.ColumnSet.PLAYLIST_VIEW.buildColumnSet(trackColumnPrefs);
            	}
            	
            	if (skinPrefsUpdated == true)
            	{
					logger.info("updating skin preferences");
					
            		prefsUpdated = true;
            		
            		String skinPref = (String) skinPrefsSpinner.getSelectedItem();
            		userPrefs.setSkinName(skinPref);
            		
            		Skins skins = Skins.getInstance();
            		skins.initializeSkinElements(skinPref);

            		/*
            		 * Since the skin has been updated, re-skin the main window and the owning
            		 * window if different.
            		 */
            		Skins.Window skinWindow = Skins.Window.getEnum(owningWindowTitle);
            		skins.skinMe(skinWindow);
            		
            		if (skinWindow != Skins.Window.MAIN)
            		{
            			skins.skinMe(Skins.Window.MAIN);
            		}
            	}
            	
            	if (saveDirectoryUpdated == true)
            	{
					logger.info("updating preferences save directory");
					
					/*
					 * Save the directory in the user preferences. This is needed in order to read
					 * or write the serialized preferences.
					 */
					String saveDirectory = saveDirectoryTextInput.getText();
					userPrefs.setSaveDirectory(saveDirectory);
					
					/*
					 * Update the Java preference with the new directory.
					 */
					Utilities.saveJavaPreference(Utilities.JAVA_PREFS_KEY_SAVEDIR, saveDirectory);
            	}
            	
            	if (logLevelPrefsUpdated == true)
            	{
					logger.info("updating log level preferences");
					
            		prefsUpdated = true;

            		/*
            		 * Set the preferences from the user's choices.
            		 */
            		userPrefs.setGlobalLogLevel(logLevelPrefsCheckbox.isSelected());
            		
            		String levelPref = (String) logLevelPrefsSpinner.getSelectedItem();
            		userPrefs.setLogLevel(Logging.Dimension.ALL, Level.toLevel(levelPref));
            		
            		levelPref = (String) uiLogLevelPrefsSpinner.getSelectedItem();
            		userPrefs.setLogLevel(Logging.Dimension.UI, Level.toLevel(levelPref));
            		levelPref = (String) xmlLogLevelPrefsSpinner.getSelectedItem();
            		userPrefs.setLogLevel(Logging.Dimension.XML, Level.toLevel(levelPref));
            		levelPref = (String) trackLogLevelPrefsSpinner.getSelectedItem();
            		userPrefs.setLogLevel(Logging.Dimension.TRACK, Level.toLevel(levelPref));
            		levelPref = (String) playlistLogLevelPrefsSpinner.getSelectedItem();
            		userPrefs.setLogLevel(Logging.Dimension.PLAYLIST, Level.toLevel(levelPref));
            		levelPref = (String) filterLogLevelPrefsSpinner.getSelectedItem();
            		userPrefs.setLogLevel(Logging.Dimension.FILTER, Level.toLevel(levelPref));
            		
            		/*
            		 * Update the actual log levels from the preferences.
            		 */
            		logging.setLogLevelsFromPrefs();
            	}
            	
            	/*
            	 * Write the updated user preferences.
            	 */
            	if (prefsUpdated == true)
            	{
            		userPrefs.writePreferences();
            	}
            	
            	owningWindow.setTitle(owningWindowTitle);
            	preferencesSheet.close();
            }
        });
        
        /*
         * Listener to handle the skin preference spinner.
         */
        skinPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
        	@Override
        	public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
        	{
        		skinPrefsUpdated = true;
        	}

			@Override
			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
			{
			}
        });
        
        /*
         * Add tooltip texts.
         */
        bypassPrefsBorderLabel.setTooltipText("Playlist counts are accumulated for every track, "
        		+ "but you can bypass the counts for certain playlists.");
        filteredPrefsBorderLabel.setTooltipText("iTunes includes built-in playlists that might be "
        		+ "considered clutter. You can ignore them if you want, and also add your own "
        		+ "playlists to completely ignore.");
        columnPrefsBorderLabel.setTooltipText("Select the columns you want to be displayed when "
        		+ "displaying all tracks, filtered tracks, and the tracks shown for a "
        		+ "selected playlist.");
        skinPrefsBorderLabel.setTooltipText("Select a skin name and click the 'Preview' "
        		+ "button to see how it looks.");
        saveDirectoryBorderLabel.setTooltipText("Select the directory in which to save your "
        		+ "user preferences.");
        logLevelPrefsBorderLabel.setTooltipText("Log levels determine how much information is "
        		+ "logged for debugging purposes. These should only be changed if directed by "
        		+ "support personnel.");
        
        final String globalLogLevelTooltip = "This is the global log level, if '" 
        		+ logLevelPrefsCheckbox.getButtonData() + "' is checked.";
        logLevelPrefsSpinner.setTooltipText(globalLogLevelTooltip);
        logLevelPrefsCheckbox.setTooltipText(globalLogLevelTooltip);
        
        final String uiLogLevelTooltip = "This is the log level for the user interface component.";
        uiLogLevelPrefsLabel.setTooltipText(uiLogLevelTooltip);
        uiLogLevelPrefsSpinner.setTooltipText(uiLogLevelTooltip);
        
        final String xmlLogLevelTooltip = "This is the log level for the XML management component.";
        xmlLogLevelPrefsLabel.setTooltipText(xmlLogLevelTooltip);
        xmlLogLevelPrefsSpinner.setTooltipText(xmlLogLevelTooltip);
        
        final String trackLogLevelTooltip = "This is the log level for the track management component.";
        trackLogLevelPrefsLabel.setTooltipText(trackLogLevelTooltip);
        trackLogLevelPrefsSpinner.setTooltipText(trackLogLevelTooltip);
        
        final String playlistLogLevelTooltip = "This is the log level for the playlist management component.";
        playlistLogLevelPrefsLabel.setTooltipText(playlistLogLevelTooltip);
        playlistLogLevelPrefsSpinner.setTooltipText(playlistLogLevelTooltip);
        
        final String filterLogLevelTooltip = "This is the log level for the filter management component.";
        filterLogLevelPrefsLabel.setTooltipText(filterLogLevelTooltip);
        filterLogLevelPrefsSpinner.setTooltipText(filterLogLevelTooltip);
		
		/*
		 * Get the skins object singleton.
		 */
		Skins skins = Skins.getInstance();
		preferencesSheet.setTitle(Skins.Window.PREFERENCES.getDisplayValue());
		
		/*
		 * Start with the preferences window skin elements known from the BXML.
		 */
		Map<Skins.Element, List<Component>> windowElements = 
				new HashMap<Skins.Element, List<Component>>();
        
        /*
         * Add bypass playlist preference rows if such preferences exist. This populates the 
         * component list with table row components.
         */
        List<BypassPreference> bypassPrefs = userPrefs.getBypassPrefs();
        
        if (bypassPrefs != null && bypassPrefs.getLength() > 0)
        {
        	Iterator<BypassPreference> bypassPrefsIter = bypassPrefs.iterator();
        	while (bypassPrefsIter.hasNext())
        	{
        		BypassPreference bypassPref = bypassPrefsIter.next();
        		
            	TablePane.Row newRow = addBypassPrefsTableRow(bypassPref, components);
            	bypassPrefsTablePane.getRows().add(newRow);
        	}
        }
        
        /*
         * No bypass playlist preferences exist, so add an empty playlist preferences row.
         */
        else
        {
        	TablePane.Row newRow = addBypassPrefsTableRow(null, components);
        	bypassPrefsTablePane.getRows().add(newRow);
        }
        
        /*
         * Add filtered playlist preference rows if such preferences exist. This populates the 
         * component list with table row components.
         */
        List<String> filteredPrefs = userPrefs.getFilteredPrefs();
        
        if (filteredPrefs != null && filteredPrefs.getLength() > 0)
        {
        	Iterator<String> filteredPrefsIter = filteredPrefs.iterator();
        	while (filteredPrefsIter.hasNext())
        	{
        		String filteredPref = filteredPrefsIter.next();
        		
            	TablePane.Row newRow = addFilteredPrefsTableRow(filteredPref, components);
            	filteredPrefsTablePane.getRows().add(newRow);
        	}
        }
        
        /*
         * No filtered playlist preferences exist, so add the default filtered playlist rows.
         */
        else
        {
        	Iterator<String> defaultFilteredIter = Playlist.DEFAULT_FILTERED_PLAYLISTS.iterator();
        	while (defaultFilteredIter.hasNext())
        	{
        		String playlist = defaultFilteredIter.next();
            	TablePane.Row newRow = addFilteredPrefsTableRow(playlist, components);
            	filteredPrefsTablePane.getRows().add(newRow);
        	}
        }
        
        /*
         * Populate the list of skin names.
         */
        Sequence<String> skinNames = skins.getSkinNames();
        List<String> skinArray = new ArrayList<String>(skinNames);
        skinPrefsSpinner.setSpinnerData(skinArray);
        skinPrefsSpinner.setCircular(true);
        skinPrefsSpinner.setPreferredWidth(120);
        
        /*
         * Set the spinner selected index to the current preference if one exists. Otherwise set it
         * to the default skin.
         */
        String skinName;
        int index;
        if ((skinName = userPrefs.getSkinName()) != null)
        {
        	index = skinNames.indexOf(skinName);
        }
        else
        {
        	index = skinNames.indexOf(Skins.defaultSkin);
        }
        skinPrefsSpinner.setSelectedIndex(index);
        
        /*
         * Initialize the preferences save directory.
         */
        saveDirectoryTextInput.setText(userPrefs.getSaveDirectory());
        
        /*
         * Initialize the log level spinners.
         */
        setupLogLevelSpinner(Logging.Dimension.ALL, logLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.UI, uiLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.XML, xmlLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.TRACK, trackLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.PLAYLIST, playlistLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.FILTER, filterLogLevelPrefsSpinner);
        
        /*
         * Set up the dimensional log levels according to the global log level preference.
         */
        boolean globalLogLevel = userPrefs.getGlobalLogLevel();
        if (globalLogLevel == true)
        {
        	logLevelPrefsCheckbox.setSelected(true);
        	uiLogLevelPrefsLabel.setEnabled(false);
        	uiLogLevelPrefsSpinner.setEnabled(false);
        	xmlLogLevelPrefsLabel.setEnabled(false);
        	xmlLogLevelPrefsSpinner.setEnabled(false);
        	trackLogLevelPrefsLabel.setEnabled(false);
        	trackLogLevelPrefsSpinner.setEnabled(false);
        	playlistLogLevelPrefsLabel.setEnabled(false);
        	playlistLogLevelPrefsSpinner.setEnabled(false);
        	filterLogLevelPrefsLabel.setEnabled(false);
        	filterLogLevelPrefsSpinner.setEnabled(false);
        }
        else
        {
        	logLevelPrefsCheckbox.setSelected(false);
        	uiLogLevelPrefsLabel.setEnabled(true);
        	uiLogLevelPrefsSpinner.setEnabled(true);
        	xmlLogLevelPrefsLabel.setEnabled(true);
        	xmlLogLevelPrefsSpinner.setEnabled(true);
        	trackLogLevelPrefsLabel.setEnabled(true);
        	trackLogLevelPrefsSpinner.setEnabled(true);
        	playlistLogLevelPrefsLabel.setEnabled(true);
        	playlistLogLevelPrefsSpinner.setEnabled(true);
        	filterLogLevelPrefsLabel.setEnabled(true);
        	filterLogLevelPrefsSpinner.setEnabled(true);
        }

    	/*
    	 * Now register the preferences window skin elements.
    	 */
		windowElements = skins.mapComponentsToSkinElements(components);
		skins.registerWindowElements(Skins.Window.PREFERENCES, windowElements);
        
        /*
         * Fill in the column checkboxes if preferences exist.
         */
        addFullTrackColumnPrefsCheckboxes(userPrefs.getTrackColumnsFullView());
        addFilteredTrackColumnPrefsCheckboxes(userPrefs.getTrackColumnsFilteredView());
        addPlaylistTrackColumnPrefsCheckboxes(userPrefs.getTrackColumnsPlaylistView());
        
        /*
         * Reset the updated indicators.
         */
    	bypassPrefsUpdated = false;
    	filteredPrefsUpdated = false;
    	fullTrackColumnsUpdated = false;
    	filteredTrackColumnsUpdated = false;
    	playlistTrackColumnsUpdated = false;
    	skinPrefsUpdated = false;
    	saveDirectoryUpdated = false;
    	logLevelPrefsUpdated = false;
		
		/*
		 * Skin the preferences window.
		 */
		skins.skinMe(Skins.Window.PREFERENCES);
    	
    	/*
    	 * Open the preferences window.
    	 */
    	logger.info("opening preferences window");
    	owningWindowTitle = owningWindow.getTitle();
    	owningWindow.setTitle("Preferences");
    	preferencesSheet.open(display, owningWindow);
    }
	
    //---------------- Private methods -------------------------------------
    
    /*
     * Create and add a bypass playlist preferences row.
     * 
     * ANNOYED RANT: Pivot listeners are a pain to deal with. It would be real nice to be able to use
     * a single method to handle both bypass and filtered playlist preferences, since it's a lot of
     * code and most of it is identical. But the listeners don't provide any way for me to distinguish
     * between the two table views, so things like the + and - buttons don't work if a single method
     * is used. So I have no choice but to duplicate a lot of code. The next two methods are very close
     * cousins.
     */
    private TablePane.Row addBypassPrefsTableRow (BypassPreference bypassPref, 
    		List<Component> components)
    {
    	
    	/*
    	 * New table row object.
    	 */
    	TablePane.Row newRow = new TablePane.Row();
        
        /*
         * Create the label for the text box.
         */
    	Label textLabel = new Label();
    	textLabel.setText("Playlist Name");
        
        /*
         * Create the text input box. Add the playlist name if we were provided a playlist 
         * preference object.
         */
    	TextInput text = new TextInput();
    	if (bypassPref != null)
    	{
    		text.setText(bypassPref.getPlaylistName());
    	}
    	
    	/*
    	 * Add a text input listener so we can indicate the playlist preferences have been updated
    	 * when the user inserts a playlist name in the text box.
    	 * 
    	 * Also, call the typing assistant to fill in the playlist name as soon as enough
    	 * characters are entered.
    	 */
    	text.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
    	{
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
            	bypassPrefsUpdated = true;
            	
            	Utilities.typingAssistant(textInput, XMLHandler.getPlaylistNames(), 
            			textInput.getText(), Filter.Operator.IS);
            }    		
    	});
    	
    	/*
    	 * Create the include children checkbox. Set the selected box according to any provided 
    	 * playlist preference object.
    	 */
    	Checkbox includeChildren = new Checkbox("Include Children?");
    	includeChildren.setTooltipText("Should counts also be bypassed for children playlists?");
		if (bypassPref != null)
		{
			includeChildren.setSelected((bypassPref.getIncludeChildren()));
		}
    	
    	/*
    	 * Create the set of buttons:
    	 * 
    	 * '+' = insert a new row after this one
    	 * '-' = delete this row
    	 */
    	PushButton plusButton = new PushButton();
    	plusButton.setButtonData("+");
    	plusButton.setTooltipText("Add a new playlist for which counts should be bypassed.");
    	
    	PushButton minusButton = new PushButton();
    	minusButton.setButtonData("-");
    	minusButton.setTooltipText("Remove this playlist from the bypass counts list.");
        
    	/*
    	 * We need the ability to insert and remove playlist preference rows based on the specific 
    	 * table rows where the + and - buttons exist. But this a bit of a complex dance. 
    	 * The buttonPressed() method does not provide a means to know on which row the button 
    	 * exists. So we need a ComponentMouseButtonListener to get that function. However, based 
    	 * on the hierarchy of listener calls, and the bubbling up of events through the component 
    	 * hierarchy, the buttonPressed() method gets called before the mouseClick() method. :(
    	 * 
    	 * So the buttonPressed() method just records the Y coordinate of the button in a class
    	 * variable, which is relative to its parent component, which is the TablePane.
    	 * 
    	 * Then the mouseClick() method gets the parent, uses the recorded Y coordinate to get the
    	 * table row, then uses that information to insert a new row, or delete the current one.
    	 */
    	
    	/*
    	 * Mouse click listener for the + button.
    	 */
    	plusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
            		TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(plusButtonYCoordinate);
                    logger.info("plus button pressed for playlist pref index " + playlistPrefsRowIndex);
                    
                    /*
                     * Add the table row and collect the components that need to be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                	TablePane.Row tableRow = addBypassPrefsTableRow(null, rowComponents);
            		bypassPrefsTablePane.getRows().insert(tableRow, playlistPrefsRowIndex + 1);
            		
                	/*
                	 * Register the new components and skin them.
                	 */
            		Skins skins = Skins.getInstance();
            		Map<Skins.Element, List<Component>> windowElements = 
            				skins.mapComponentsToSkinElements(rowComponents);            		
            		skins.registerDynamicWindowElements(Skins.Window.PREFERENCES, windowElements);
            		skins.skinMe(Skins.Window.PREFERENCES);
                	
                	preferencesSheet.repaint();
            	}
 
                return false;
            }
        });
    	
    	/*
    	 * Button press listener for the + button.
    	 */
    	plusButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	plusButtonYCoordinate = button.getY();
            }
        });
    	
    	/*
    	 * Mouse click listener for the - button.
    	 */
    	minusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
                	bypassPrefsUpdated = true;
                	
            		TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(minusButtonYCoordinate);
                    logger.info("minus button pressed for playlist pref index " + playlistPrefsRowIndex);
                    
                    /*
                     * Remove the table row.
                     */
                    bypassPrefsTablePane.getRows().remove(playlistPrefsRowIndex, 1);
                	
                	preferencesSheet.repaint();
            	}
 
                return false;
            }
        });

    	/*
    	 * Button press listener for the - button.
    	 */
        minusButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button)
            {
            	minusButtonYCoordinate = button.getY();
            }
        });

        /*
         * Assemble the new row.
         */ 
    	newRow.add(textLabel);
		components.add(textLabel);
    	newRow.add(text);
		components.add(text);
		newRow.add(includeChildren);
		components.add(includeChildren);
    	newRow.add(plusButton);
		components.add(plusButton);
    	newRow.add(minusButton);
		components.add(minusButton);
    	
    	return newRow;
    }
    
    /*
     * Create and add a filtered playlist preferences row.
     */
    private TablePane.Row addFilteredPrefsTableRow (String filteredPref, 
    		List<Component> components)
    {
    	
    	/*
    	 * New table row object.
    	 */
    	TablePane.Row newRow = new TablePane.Row();
        
        /*
         * Create the label for the text box.
         */
    	Label textLabel = new Label();
    	textLabel.setText("Playlist Name");
        
        /*
         * Create the text input box. Add the playlist name if we were provided a playlist 
         * preference object.
         */
    	TextInput text = new TextInput();
    	if (filteredPref != null)
    	{
    		text.setText(filteredPref);
    	}
    	
    	/*
    	 * Add a text input listener so we can indicate the playlist preferences have been updated
    	 * when the user inserts a playlist name in the text box.
    	 * 
    	 * Also, call the typing assistant to fill in the playlist name as soon as enough
    	 * characters are entered.
    	 */
    	text.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
    	{
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
            	filteredPrefsUpdated = true;
            	
            	Utilities.typingAssistant(textInput, XMLHandler.getPlaylistNames(), 
            			textInput.getText(), Filter.Operator.IS);
            }    		
    	});
    	
    	/*
    	 * Create the set of buttons:
    	 * 
    	 * '+' = insert a new row after this one
    	 * '-' = delete this row
    	 */
    	PushButton plusButton = new PushButton();
    	plusButton.setButtonData("+");
    	plusButton.setTooltipText("Add a new playlist to be completely ignored.");
    	
    	PushButton minusButton = new PushButton();
    	minusButton.setButtonData("-");
    	minusButton.setTooltipText("Remove this playlist from being completely ignored.");
        
    	/*
    	 * We need the ability to insert and remove playlist preference rows based on the specific 
    	 * table rows where the + and - buttons exist. But this a bit of a complex dance. 
    	 * The buttonPressed() method does not provide a means to know on which row the button 
    	 * exists. So we need a ComponentMouseButtonListener to get that function. However, based 
    	 * on the hierarchy of listener calls, and the bubbling up of events through the component 
    	 * hierarchy, the buttonPressed() method gets called before the mouseClick() method. :(
    	 * 
    	 * So the buttonPressed() method just records the Y coordinate of the button in a class
    	 * variable, which is relative to its parent component, which is the TablePane.
    	 * 
    	 * Then the mouseClick() method gets the parent, uses the recorded Y coordinate to get the
    	 * table row, then uses that information to insert a new row, or delete the current one.
    	 */
    	
    	/*
    	 * Mouse click listener for the + button.
    	 */
    	plusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
            		TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(plusButtonYCoordinate);
                    logger.info("plus button pressed for playlist pref index " + playlistPrefsRowIndex);
                    
                    /*
                     * Add the table row and collect the components that need to be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                	TablePane.Row tableRow = 
                			addFilteredPrefsTableRow(null, rowComponents);
            		filteredPrefsTablePane.getRows().insert(tableRow, playlistPrefsRowIndex + 1);
            		
                	/*
                	 * Register the new components and skin them.
                	 */
            		Skins skins = Skins.getInstance();
            		Map<Skins.Element, List<Component>> windowElements = 
            				skins.mapComponentsToSkinElements(rowComponents);            		
            		skins.registerDynamicWindowElements(Skins.Window.PREFERENCES, windowElements);
            		skins.skinMe(Skins.Window.PREFERENCES);
                	
                	preferencesSheet.repaint();
            	}
 
                return false;
            }
        });
    	
    	/*
    	 * Button press listener for the + button.
    	 */
    	plusButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	plusButtonYCoordinate = button.getY();
            }
        });
    	
    	/*
    	 * Mouse click listener for the - button.
    	 */
    	minusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
                	filteredPrefsUpdated = true;
                	
            		TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(minusButtonYCoordinate);
                    logger.info("minus button pressed for playlist pref index " + playlistPrefsRowIndex);
                    
                    /*
                     * Remove the table row.
                     */
                    filteredPrefsTablePane.getRows().remove(playlistPrefsRowIndex, 1);
                	
                	preferencesSheet.repaint();
            	}
 
                return false;
            }
        });

    	/*
    	 * Button press listener for the - button.
    	 */
        minusButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button)
            {
            	minusButtonYCoordinate = button.getY();
            }
        });

        /*
         * Assemble the new row.
         */ 
    	newRow.add(textLabel);
		components.add(textLabel);
    	newRow.add(text);
		components.add(text);
    	newRow.add(plusButton);
		components.add(plusButton);
    	newRow.add(minusButton);
		components.add(minusButton);
    	
    	return newRow;
    }
    
    /*
     * Collect the entered bypass playlist preferences and update them.
     */
    private List<BypassPreference> collectBypassPrefs ()
    {
    	
    	/*
    	 * Indexes into the row elements.
    	 * 
    	 * IMPORTANT: These must match the design of the row. See preferencesWindow.bxml for the column
    	 * definition, and addPlaylistPrefsTableRow() for the logic to create a row.
    	 */
    	final int textIndex            = 1;
    	final int includeChildrenIndex = 2;
    	
    	/*
    	 * Iterate through the bypass playlist preferences table rows.
    	 */
    	List<BypassPreference> bypassPrefs = new ArrayList<BypassPreference>();
    	TablePane.RowSequence rows = bypassPrefsTablePane.getRows();
    	Iterator<TablePane.Row> rowsIterator = rows.iterator();
    	while (rowsIterator.hasNext())
    	{
    		TablePane.Row row = rowsIterator.next();
    		
    		/*
    		 * Initialize a new bypass preference object.
    		 */
    		BypassPreference bypassPref = new BypassPreference();
			
			/*
			 * Handle the text input.
			 */
			TextInput text = (TextInput) row.get(textIndex);
			bypassPref.setPlaylistName(text.getText());
			
			Checkbox includeChildren = (Checkbox) row.get(includeChildrenIndex);
			boolean selected = includeChildren.isSelected();
			bypassPref.setIncludeChildren(selected);
			
			/*
			 * Add this playlist preference to the collection.
			 */
			bypassPrefs.add(bypassPref);
    	}
    	
    	return bypassPrefs;
    }
    
    /*
     * Collect the entered filtered playlist preferences and update them.
     */
    private List<String> collectFilteredPrefs ()
    {
    	
    	/*
    	 * Indexes into the row elements.
    	 * 
    	 * IMPORTANT: These must match the design of the row. See preferencesWindow.bxml for the column
    	 * definition, and addPlaylistPrefsTableRow() for the logic to create a row.
    	 */
    	final int textIndex = 1;
    	
    	/*
    	 * Iterate through the filtered playlist preferences table rows.
    	 */
    	List<String> filteredPrefs = new ArrayList<String>();
    	TablePane.RowSequence rows = filteredPrefsTablePane.getRows();
    	Iterator<TablePane.Row> rowsIterator = rows.iterator();
    	while (rowsIterator.hasNext())
    	{
    		TablePane.Row row = rowsIterator.next();
			
			/*
			 * Add this playlist preference to the collection.
			 */
			TextInput text = (TextInput) row.get(textIndex);
			filteredPrefs.add(text.getText());
    	}
    	
    	return filteredPrefs;
    }
    
    /*
     * Collect the full track columns preferences. This involves brute force code to check all of
     * the checkboxes to see whether they are selected or not. For all that are selected, add the
     * appropriate column to the column set.
     */
    private List<List<String>> collectFullTrackColumnPrefs ()
    {
    	List<List<String>> columnPrefs = new ArrayList<List<String>>();
    	
    	if (fullNumberCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.NUMBER.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullNameCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.NAME.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullArtistCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ARTIST.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullAlbumCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ALBUM.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullKindCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.KIND.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullDurationCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.DURATION.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullYearCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.YEAR.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullAddedCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ADDED.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (fullRatingCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.RATING.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	return columnPrefs;
    }

    /*
     * Collect the filtered track columns preferences. This involves brute force code to check all of
     * the checkboxes to see whether they are selected or not. For all that are selected, add the
     * appropriate column to the column set.
     */
    private List<List<String>> collectFilteredTrackColumnPrefs ()
    {
    	List<List<String>> columnPrefs = new ArrayList<List<String>>();
    	
    	if (filteredNumberCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.NUMBER.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredNameCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.NAME.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredArtistCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ARTIST.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredAlbumCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ALBUM.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredKindCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.KIND.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredDurationCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.DURATION.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredYearCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.YEAR.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredAddedCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ADDED.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (filteredRatingCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.RATING.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	return columnPrefs;
    }

    /*
     * Collect the playlist track columns preferences. This involves brute force code to check all of
     * the checkboxes to see whether they are selected or not. For all that are selected, add the
     * appropriate column to the column set.
     */
    private List<List<String>> collectPlaylistTrackColumnPrefs ()
    {
    	List<List<String>> columnPrefs = new ArrayList<List<String>>();
    	
    	if (playlistNumberCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.NUMBER.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistNameCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.NAME.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistArtistCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ARTIST.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistAlbumCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ALBUM.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistKindCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.KIND.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistDurationCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.DURATION.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistYearCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.YEAR.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistAddedCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.ADDED.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	if (playlistRatingCheckbox.isSelected())
    	{
    		List<String> columnData = 
    				TrackDisplayColumns.
    					buildColumnData(TrackDisplayColumns.ColumnNames.RATING.getDisplayValue());
    		columnPrefs.add(columnData);
    	}
    	
    	return columnPrefs;
    }
    
    /*
     * Before opening the preferences window, select all checkboxes for which preferences exist, 
     * for the full column set. This involves more brute force code to check each preference,
     * so we know which checkboxes to select.
     */
    private void addFullTrackColumnPrefsCheckboxes (List<List<String>> columnPrefs)
    {
    	Iterator<List<String>> columnPrefsIter = columnPrefs.iterator();
    	while (columnPrefsIter.hasNext())
    	{
    		List<String> columnData = columnPrefsIter.next();
    		String columnName = columnData.get(0);
    		
    		switch (TrackDisplayColumns.ColumnNames.getEnum(columnName))
    		{
    		case NUMBER:
    			fullNumberCheckbox.setSelected(true);
    			break;

    		case NAME:
    			fullNameCheckbox.setSelected(true);
    			break;

    		case ARTIST:
    			fullArtistCheckbox.setSelected(true);
    			break;
    			
    		case ALBUM:
    			fullAlbumCheckbox.setSelected(true);
    			break;
    			
    		case KIND:
    			fullKindCheckbox.setSelected(true);
    			break;
    			
    		case DURATION:
    			fullDurationCheckbox.setSelected(true);
    			break;
    			
    		case YEAR:
    			fullYearCheckbox.setSelected(true);
    			break;
    			
    		case ADDED:
    			fullAddedCheckbox.setSelected(true);
    			break;
    			
    		case RATING:
    			fullRatingCheckbox.setSelected(true);
    			break;
    		default:
    		}
    	}
    }

    /*
     * Before opening the preferences window, select all checkboxes for which preferences exist, 
     * for the filtered column set. This involves more brute force code to check each preference,
     * so we know which checkboxes to select.
     */
    private void addFilteredTrackColumnPrefsCheckboxes (List<List<String>> columnPrefs)
    {
    	Iterator<List<String>> columnPrefsIter = columnPrefs.iterator();
    	while (columnPrefsIter.hasNext())
    	{
    		List<String> columnData = columnPrefsIter.next();
    		String columnName = columnData.get(0);
    		
    		switch (TrackDisplayColumns.ColumnNames.getEnum(columnName))
    		{
    		case NUMBER:
    			filteredNumberCheckbox.setSelected(true);
    			break;

    		case NAME:
    			filteredNameCheckbox.setSelected(true);
    			break;

    		case ARTIST:
    			filteredArtistCheckbox.setSelected(true);
    			break;
    			
    		case ALBUM:
    			filteredAlbumCheckbox.setSelected(true);
    			break;
    			
    		case KIND:
    			filteredKindCheckbox.setSelected(true);
    			break;
    			
    		case DURATION:
    			filteredDurationCheckbox.setSelected(true);
    			break;
    			
    		case YEAR:
    			filteredYearCheckbox.setSelected(true);
    			break;
    			
    		case ADDED:
    			filteredAddedCheckbox.setSelected(true);
    			break;
    			
    		case RATING:
    			filteredRatingCheckbox.setSelected(true);
    			break;
    		default:
    		}
    	}
    }

    /*
     * Before opening the preferences window, select all checkboxes for which preferences exist, 
     * for the playlist column set. This involves more brute force code to check each preference,
     * so we know which checkboxes to select.
     */
    private void addPlaylistTrackColumnPrefsCheckboxes (List<List<String>> columnPrefs)
    {
    	Iterator<List<String>> columnPrefsIter = columnPrefs.iterator();
    	while (columnPrefsIter.hasNext())
    	{
    		List<String> columnData = columnPrefsIter.next();
    		String columnName = columnData.get(0);
    		
    		switch (TrackDisplayColumns.ColumnNames.getEnum(columnName))
    		{
    		case NUMBER:
    			playlistNumberCheckbox.setSelected(true);
    			break;

    		case NAME:
    			playlistNameCheckbox.setSelected(true);
    			break;

    		case ARTIST:
    			playlistArtistCheckbox.setSelected(true);
    			break;
    			
    		case ALBUM:
    			playlistAlbumCheckbox.setSelected(true);
    			break;
    			
    		case KIND:
    			playlistKindCheckbox.setSelected(true);
    			break;
    			
    		case DURATION:
    			playlistDurationCheckbox.setSelected(true);
    			break;
    			
    		case YEAR:
    			playlistYearCheckbox.setSelected(true);
    			break;
    			
    		case ADDED:
    			playlistAddedCheckbox.setSelected(true);
    			break;
    			
    		case RATING:
    			playlistRatingCheckbox.setSelected(true);
    			break;
    		default:
    		}
    	}
    }
    
    /*
     * We have a large number of checkboxes to deal with. So do the brute force initialization
     * here so it doesn't clutter up displayPreferences().
     */
    private void initializeTrackColumnStuff (BXMLSerializer prefsWindowSerializer, 
    		List<Component> components)
    {
    	fullNumberCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullNumberCheckbox");
		components.add(fullNumberCheckbox);
        
    	fullNumberCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
    	fullNameCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullNameCheckbox");
		components.add(fullNameCheckbox);
        
    	fullNameCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        fullArtistCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullArtistCheckbox");
		components.add(fullArtistCheckbox);
        
        fullArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        fullAlbumCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullAlbumCheckbox");
		components.add(fullAlbumCheckbox);
        
        fullAlbumCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        fullKindCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullKindCheckbox");
		components.add(fullKindCheckbox);
        
        fullKindCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        fullDurationCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullDurationCheckbox");
		components.add(fullDurationCheckbox);
        
        fullDurationCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        fullYearCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullYearCheckbox");
		components.add(fullYearCheckbox);
        
        fullYearCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        fullAddedCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullAddedCheckbox");
		components.add(fullAddedCheckbox);
        
        fullAddedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        fullRatingCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("fullRatingCheckbox");
		components.add(fullRatingCheckbox);
        
        fullRatingCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	fullTrackColumnsUpdated = true;;
            }
        });
        
        filteredNumberCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredNumberCheckbox");
		components.add(filteredNumberCheckbox);
        
        filteredNumberCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredNameCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredNameCheckbox");
		components.add(filteredNameCheckbox);
        
        filteredNameCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredArtistCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredArtistCheckbox");
		components.add(filteredArtistCheckbox);
        
        filteredArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredAlbumCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredAlbumCheckbox");
		components.add(filteredAlbumCheckbox);
        
        filteredAlbumCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredKindCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredKindCheckbox");
		components.add(filteredKindCheckbox);
        
        filteredKindCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredDurationCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredDurationCheckbox");
		components.add(filteredDurationCheckbox);
        
        filteredDurationCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredYearCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredYearCheckbox");
		components.add(filteredYearCheckbox);
        
        filteredYearCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredAddedCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredAddedCheckbox");
		components.add(filteredAddedCheckbox);
        
        filteredAddedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        filteredRatingCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("filteredRatingCheckbox");
		components.add(filteredRatingCheckbox);
        
        filteredRatingCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	filteredTrackColumnsUpdated = true;;
            }
        });
        
        playlistNumberCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistNumberCheckbox");
		components.add(playlistNumberCheckbox);
        
        playlistNumberCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistNameCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistNameCheckbox");
		components.add(playlistNameCheckbox);
        
        playlistNameCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistArtistCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistArtistCheckbox");
		components.add(playlistArtistCheckbox);
        
        playlistArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistAlbumCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistAlbumCheckbox");
		components.add(playlistAlbumCheckbox);
        
        playlistAlbumCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistKindCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistKindCheckbox");
		components.add(playlistKindCheckbox);
        
        playlistKindCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistDurationCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistDurationCheckbox");
		components.add(playlistDurationCheckbox);
        
        playlistDurationCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistYearCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistYearCheckbox");
		components.add(playlistYearCheckbox);
        
        playlistYearCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistAddedCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistAddedCheckbox");
		components.add(playlistAddedCheckbox);
        
        playlistAddedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
        
        playlistRatingCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("playlistRatingCheckbox");
		components.add(playlistRatingCheckbox);
        
        playlistRatingCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	playlistTrackColumnsUpdated = true;;
            }
        });
    }
    
    /*
     * We have a large number of checkboxes to deal with. So do the brute force initialization
     * here so it doesn't clutter up displayPreferences().
     */
    private void initializeLogLevelStuff (BXMLSerializer prefsWindowSerializer, 
    		List<Component> components)
    {
    	logLevelPrefsSpinner = 
        		(Spinner)prefsWindowSerializer.getNamespace().get("logLevelPrefsSpinner");
		components.add(logLevelPrefsSpinner);
        
    	logLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
        	@Override
        	public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
        	{
        		logLevelPrefsUpdated = true;
        	}

			@Override
			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
			{
			}
        });
    	
        logLevelPrefsCheckbox = 
        		(Checkbox)prefsWindowSerializer.getNamespace().get("logLevelPrefsCheckbox");
		components.add(logLevelPrefsCheckbox);
        
        logLevelPrefsCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	logLevelPrefsUpdated = true;
            	
            	/*
            	 * Enable or disable dimensional elements based on the general checkbox selection.
            	 */
            	if (button.isSelected())
            	{
            		uiLogLevelPrefsLabel.setEnabled(false);
            		uiLogLevelPrefsSpinner.setEnabled(false);
            		xmlLogLevelPrefsLabel.setEnabled(false);
            		xmlLogLevelPrefsSpinner.setEnabled(false);
            		trackLogLevelPrefsLabel.setEnabled(false);
            		trackLogLevelPrefsSpinner.setEnabled(false);
            		playlistLogLevelPrefsLabel.setEnabled(false);
            		playlistLogLevelPrefsSpinner.setEnabled(false);
            		filterLogLevelPrefsLabel.setEnabled(false);
            		filterLogLevelPrefsSpinner.setEnabled(false);
            	}
            	else
            	{
            		uiLogLevelPrefsLabel.setEnabled(true);
            		uiLogLevelPrefsSpinner.setEnabled(true);
            		xmlLogLevelPrefsLabel.setEnabled(true);
            		xmlLogLevelPrefsSpinner.setEnabled(true);
            		trackLogLevelPrefsLabel.setEnabled(true);
            		trackLogLevelPrefsSpinner.setEnabled(true);
            		playlistLogLevelPrefsLabel.setEnabled(true);
            		playlistLogLevelPrefsSpinner.setEnabled(true);
            		filterLogLevelPrefsLabel.setEnabled(true);
            		filterLogLevelPrefsSpinner.setEnabled(true);
            	}
            }
        });        

        uiLogLevelPrefsSpinner = 
        		(Spinner)prefsWindowSerializer.getNamespace().get("uiLogLevelPrefsSpinner");
		components.add(uiLogLevelPrefsSpinner);
        
        uiLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
        	@Override
        	public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
        	{
        		logLevelPrefsUpdated = true;
        	}

			@Override
			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
			{
			}
        });
        
        xmlLogLevelPrefsSpinner = 
        		(Spinner)prefsWindowSerializer.getNamespace().get("xmlLogLevelPrefsSpinner");
		components.add(xmlLogLevelPrefsSpinner);
        
        xmlLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
        	@Override
        	public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
        	{
        		logLevelPrefsUpdated = true;
        	}

			@Override
			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
			{
			}
        });
        
        trackLogLevelPrefsSpinner = 
        		(Spinner)prefsWindowSerializer.getNamespace().get("trackLogLevelPrefsSpinner");
		components.add(trackLogLevelPrefsSpinner);
        
        trackLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
        	@Override
        	public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
        	{
        		logLevelPrefsUpdated = true;
        	}

			@Override
			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
			{
			}
        });
        
        playlistLogLevelPrefsSpinner = 
        		(Spinner)prefsWindowSerializer.getNamespace().get("playlistLogLevelPrefsSpinner");
		components.add(playlistLogLevelPrefsSpinner);
        
        playlistLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
        	@Override
        	public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
        	{
        		logLevelPrefsUpdated = true;
        	}

			@Override
			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
			{
			}
        });
        
        filterLogLevelPrefsSpinner = 
        		(Spinner)prefsWindowSerializer.getNamespace().get("filterLogLevelPrefsSpinner");
		components.add(filterLogLevelPrefsSpinner);
        
        filterLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
        	@Override
        	public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
        	{
        		logLevelPrefsUpdated = true;
        	}

			@Override
			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
			{
			}
        });
    }
    
    private void setupLogLevelSpinner (Logging.Dimension dimension, Spinner spinner)
    {
        int spinnerWidth = 70;
    	Logging logging = Logging.getInstance();
        Sequence<String> levelNames = logging.getLogLevels();
        List<String> levelArray = new ArrayList<String>(levelNames);

        /*
         * Set the spinner selected index to the current preference if one exists. Otherwise set it
         * to the current value in Logging, which should always be set to a valid value..
         */
        Level level;
    	int index;
        if ((level = userPrefs.getLogLevel(dimension)) != null)
        {
        	index = levelNames.indexOf(level.toString());
        }
        else
        {
        	index = levelNames.indexOf(dimension.getLogLevel().toString());
        }
        
        spinner.setSpinnerData(levelArray);
        spinner.setCircular(true);
        spinner.setPreferredWidth(spinnerWidth);
        spinner.setSelectedIndex(index);
    }
    
    /*
     * Initialize preferences window BXML variables and collect the list of components to be skinned.
     */
    private void initializeWindowBxmlVariables (BXMLSerializer prefsWindowSerializer, 
    		List<Component> components)
    		throws IOException, SerializationException
    {
        preferencesSheet = 
        		(Sheet)prefsWindowSerializer.readObject(getClass().getResource("preferencesWindow.bxml"));

        primaryBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("primaryBorder");
		components.add(primaryBorder);
        tabPane = 
        		(TabPane)prefsWindowSerializer.getNamespace().get("tabPane");
		components.add(tabPane);
        bypassPrefsBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("bypassPrefsBorder");
		components.add(bypassPrefsBorder);
        bypassPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("bypassPrefsBoxPane");
		components.add(bypassPrefsBoxPane);
        bypassPrefsBorderLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("bypassPrefsBorderLabel");
		components.add(bypassPrefsBorderLabel);
        bypassPrefsTablePane = 
        		(TablePane)prefsWindowSerializer.getNamespace().get("bypassPrefsTablePane");
		components.add(bypassPrefsTablePane);
        filteredPrefsBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("filteredPrefsBorder");
		components.add(filteredPrefsBorder);
        filteredPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("filteredPrefsBoxPane");
		components.add(filteredPrefsBoxPane);
        filteredPrefsBorderLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("filteredPrefsBorderLabel");
		components.add(filteredPrefsBorderLabel);
        filteredPrefsTablePane = 
        		(TablePane)prefsWindowSerializer.getNamespace().get("filteredPrefsTablePane");
		components.add(filteredPrefsTablePane);
        columnPrefsBorderLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("columnPrefsBorderLabel");
		components.add(columnPrefsBorderLabel);
        columnPrefsBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("columnPrefsBorder");
		components.add(columnPrefsBorder);
        columnPrefsTablePane = 
        		(TablePane)prefsWindowSerializer.getNamespace().get("columnPrefsTablePane");
		components.add(columnPrefsTablePane);

        fullColumnPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("fullColumnPrefsBoxPane");
		components.add(fullColumnPrefsBoxPane);
        fullColumnPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("fullColumnPrefsLabel");
		components.add(fullColumnPrefsLabel);
        filteredColumnPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("filteredColumnPrefsBoxPane");
		components.add(filteredColumnPrefsBoxPane);
        filteredColumnPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("filteredColumnPrefsLabel");
		components.add(filteredColumnPrefsLabel);
        playlistColumnPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("playlistColumnPrefsBoxPane");
		components.add(playlistColumnPrefsBoxPane);
        playlistColumnPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("playlistColumnPrefsLabel");
		components.add(playlistColumnPrefsLabel);

        skinPrefsBorderLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("skinPrefsBorderLabel");
		components.add(skinPrefsBorderLabel);
        skinPrefsBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("skinPrefsBorder");
		components.add(skinPrefsBorder);
        skinPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("skinPrefsBoxPane");
		components.add(skinPrefsBoxPane);
        skinPrefsSpinner = 
        		(Spinner)prefsWindowSerializer.getNamespace().get("skinPrefsSpinner");
		components.add(skinPrefsSpinner);
        skinPrefsButton = 
        		(PushButton)prefsWindowSerializer.getNamespace().get("skinPrefsButton");
        saveDirectoryBorderLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("saveDirectoryBorderLabel");
		components.add(saveDirectoryBorderLabel);
		saveDirectoryBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("saveDirectoryBorder");
		components.add(saveDirectoryBorder);
		saveDirectoryBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("saveDirectoryBoxPane");
		components.add(saveDirectoryBoxPane);
		saveDirectoryTextInput = 
        		(TextInput)prefsWindowSerializer.getNamespace().get("saveDirectoryTextInput");
		components.add(saveDirectoryTextInput);
        
		components.add(skinPrefsButton);
        logLevelPrefsBorderLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("logLevelPrefsBorderLabel");
		components.add(logLevelPrefsBorderLabel);
        logLevelPrefsBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("logLevelPrefsBorder");
		components.add(logLevelPrefsBorder);
        logLevelPrefsTablePane = 
        		(TablePane)prefsWindowSerializer.getNamespace().get("logLevelPrefsTablePane");
		components.add(logLevelPrefsTablePane);
        logLevelPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("logLevelPrefsBoxPane");
		components.add(logLevelPrefsBoxPane);
        uiLogLevelPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("uiLogLevelPrefsBoxPane");
		components.add(uiLogLevelPrefsBoxPane);
        uiLogLevelPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("uiLogLevelPrefsLabel");
		components.add(uiLogLevelPrefsLabel);
        xmlLogLevelPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("xmlLogLevelPrefsBoxPane");
		components.add(xmlLogLevelPrefsBoxPane);
        xmlLogLevelPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("xmlLogLevelPrefsLabel");
		components.add(xmlLogLevelPrefsLabel);
        trackLogLevelPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("trackLogLevelPrefsBoxPane");
		components.add(trackLogLevelPrefsBoxPane);
        trackLogLevelPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("trackLogLevelPrefsLabel");
		components.add(trackLogLevelPrefsLabel);
        playlistLogLevelPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("playlistLogLevelPrefsBoxPane");
		components.add(playlistLogLevelPrefsBoxPane);
        playlistLogLevelPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("playlistLogLevelPrefsLabel");
		components.add(playlistLogLevelPrefsLabel);
        filterLogLevelPrefsBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("filterLogLevelPrefsBoxPane");
		components.add(filterLogLevelPrefsBoxPane);
        filterLogLevelPrefsLabel = 
        		(Label)prefsWindowSerializer.getNamespace().get("filterLogLevelPrefsLabel");
		components.add(filterLogLevelPrefsLabel);
        actionBorder = 
        		(Border)prefsWindowSerializer.getNamespace().get("actionBorder");
		components.add(actionBorder);
        actionBoxPane = 
        		(BoxPane)prefsWindowSerializer.getNamespace().get("actionBoxPane");
		components.add(actionBoxPane);
        preferencesDoneButton = 
        		(PushButton)prefsWindowSerializer.getNamespace().get("preferencesDoneButton");
		components.add(preferencesDoneButton);
    }
    
    /*
     * Initialize skin preview dialog BXML variables and collect the components to be skinned.
     */
    private void initializePreviewDialogBxmlVariables (List<Component> components) 
    		throws IOException, SerializationException
    {
        BXMLSerializer dialogSerializer = new BXMLSerializer();
    	skinPreviewDialog = (Dialog)dialogSerializer.readObject(getClass().
				getResource("skinPreviewWindow.bxml"));

		previewPrimaryBorder = 
        		(Border)dialogSerializer.getNamespace().get("previewPrimaryBorder");
		components.add(previewPrimaryBorder);
		previewTextBorder = 
        		(Border)dialogSerializer.getNamespace().get("previewTextBorder");
		components.add(previewTextBorder);
		previewTextBoxPane = 
        		(BoxPane)dialogSerializer.getNamespace().get("previewTextBoxPane");
		components.add(previewTextBoxPane);
		previewTextLabel = 
        		(Label)dialogSerializer.getNamespace().get("previewTextLabel");
		components.add(previewTextLabel);
		previewTextInput = 
        		(TextInput)dialogSerializer.getNamespace().get("previewTextInput");
		components.add(previewTextInput);
		previewTableBorder = 
        		(Border)dialogSerializer.getNamespace().get("previewTableBorder");
		components.add(previewTableBorder);
		previewTableView = 
        		(TableView)dialogSerializer.getNamespace().get("previewTableView");
		components.add(previewTableView);
		previewTableViewHeader = 
        		(TableViewHeader)dialogSerializer.getNamespace().get("previewTableViewHeader");
		components.add(previewTableViewHeader);
		previewButtonBorder = 
        		(Border)dialogSerializer.getNamespace().get("previewButtonBorder");
		components.add(previewButtonBorder);
		previewButtonBoxPane = 
        		(BoxPane)dialogSerializer.getNamespace().get("previewButtonBoxPane");
		components.add(previewButtonBoxPane);
		previewButton = 
        		(PushButton)dialogSerializer.getNamespace().get("previewButton");
		components.add(previewButton);
    }
}
