package itunesq;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeView.SelectMode;
import org.apache.pivot.wtk.TreeViewBranchListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the display artists window.
 * 
 * @author Jon
 *
 */
public class ArtistsWindow
{

    // ---------------- Private variables -----------------------------------

    private Window artistsWindow = null;
    private Dialog altNamesDialog = null;
    private Dialog altNameSelectionDialog = null;
    private Dialog manualOverridesDialog = null;
    private List<String> selectedArtists = null;
    private ImmutableList<Span> selectedRanges = null;
    private boolean invalidOverridesFound = false;
    private Map<TreePath, String> overridePaths = null;
    
    private Skins skins = null;
    private Preferences userPrefs = null;
    private Logger uiLogger = null;
    private Logger artistLogger = null;
    
    private static final int NUM_MANUAL_SELECTION_THRESHOLD = 5;

    /*
     * Map key to hold the index into the full list of artists. This is a column definition, 
     * but is not displayed as such. We need it for manual alternate name processing. When the
     * user selects a group of artists, she is then presented with a list of those artists to
     * select which one is primary. Because that list is separate from the full list, we would
     * not be able to easily find the primary in the full list without this value.
     */
    private static final String MAP_PRIMARY_ARTIST  = "PrimaryArtist";

    /*
     * BXML variables ...
     */
    
    /*
     * ... main window.
     */
    @BXML private Border infoBorder = null;
    @BXML private BoxPane infoBoxPane = null;
    @BXML private Label numArtistsLabel = null;
    @BXML private Label instructionsLabel = null;
    @BXML private Border artistsBorder = null;
    @BXML private TableView artistsTableView = null;
    @BXML private TableViewHeader artistsTableViewHeader = null;
    @BXML private Border actionBorder = null;
    @BXML private BoxPane actionBoxPane = null;
    @BXML private PushButton doneButton = null;
    @BXML private PushButton altNameButton = null;
    @BXML private PushButton reviewOverridesButton = null;

    /*
     * ... alternate names dialog.
     */
    @BXML private Border altNamesPrimaryBorder = null;
    @BXML private TablePane altNamesTablePane = null;

    /*
     * ... alternate name selection dialog.
     */
    @BXML private Border altSelectLabelsBorder = null;
    @BXML private BoxPane altSelectLabelsBoxPane = null;
    @BXML private Label altSelectWarningLabel = null;
    @BXML private Label altSelectInstructionsLabel = null;
    @BXML private Border altSelectTableBorder = null;
    @BXML private ScrollPane altSelectTableScrollPane = null;
    @BXML private TableView altSelectTableView = null;
    @BXML private TableView.Column altSelectTableColumnArtist = null;
    @BXML private Border altSelectButtonBorder = null;
    @BXML private BoxPane altSelectButtonBoxPane = null;
    @BXML private PushButton altSelectCancelButton = null;
    @BXML private PushButton altSelectProceedButton = null;

    /*
     * ... review manual overrides dialog.
     */
    @BXML private Border manualOverridesLabelsBorder = null;
    @BXML private BoxPane manualOverridesLabelsBoxPane = null;
    @BXML private Label manualOverridesInstructionsLabel = null;
    @BXML private Label manualOverridesInfoLabel = null;
    @BXML private Border manualOverridesTreeBorder = null;
    @BXML private ScrollPane manualOverridesTreeScrollPane = null;
    @BXML private TreeView manualOverridesTreeView = null;
    @BXML private Border manualOverridesButtonBorder = null;
    @BXML private BoxPane manualOverridesButtonBoxPane = null;
    @BXML private PushButton manualOverridesDoneButton = null;
    @BXML private PushButton manualOverridesDeleteButton = null;

    /**
     * Class constructor.
     */
    public ArtistsWindow()
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

        /*
         * Initialize variables.
         */
        skins = Skins.getInstance();
        userPrefs = Preferences.getInstance();
        selectedArtists = new ArrayList<String>();
        selectedArtists.setComparator(String.CASE_INSENSITIVE_ORDER);

        uiLogger.trace("ArtistsWindow constructor: " + this.hashCode());
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Displays the artists in a new window.
     * 
     * @param display display object for managing windows
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayArtists(Display display) 
            throws IOException, SerializationException
    {
        uiLogger.trace("displayArtists: " + this.hashCode());

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        /*
         * Get the show remote tracks preference.
         */
        boolean showRemoteTracks = userPrefs.getShowRemoteTracks();

        /*
         * Get the BXML information for the artists window, and generate the
         * list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeBxmlVariables(components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers();

        /*
         * Set the number of artists label.
         */
        numArtistsLabel.setText(StringConstants.ARTISTS_NUM_ARTISTS + XMLHandler.getNumberOfArtists());

        /*
         * Create a list suitable for the setTableData() method.
         */
        List<HashMap<String, String>> displayArtists = new ArrayList<HashMap<String, String>>();

        /*
         * Get the list of artist correlators. These contain the unaltered artist display names.
         */
        ArrayList<ArtistCorrelator> artistCorrs = XMLHandler.getArtistCorrelators();

        /*
         * The correlators are currently sorted by normalized names, but we want them sorted by
         * the display (unaltered) names. Save the current comparator and set the new one.
         */
        Comparator<ArtistCorrelator> savedComparator = artistCorrs.getComparator();
        artistCorrs.setComparator(new Comparator<ArtistCorrelator>()
        {
            @Override
            public int compare(ArtistCorrelator c1, ArtistCorrelator c2)
            {
                return c1.compareToDisplay(c2);
            }
        });

        /*
         * Now walk the artists, and add them all to the list.
         */
        for (ArtistCorrelator artistCorr : artistCorrs)
        {
            Artist artistObj = XMLHandler.getArtists().get(artistCorr.getArtistKey());

            /*
             * Skip artists with no local tracks if remote tracks are not being
             * shown.
             */
            if (artistObj.getArtistTrackData().getNumLocalTracks() == 0 && showRemoteTracks == false)
            {
                continue;
            }

            HashMap<String, String> artistAttrs = artistObj.toDisplayMap();
            displayArtists.add(artistAttrs);
        }

        /*
         * Restore the saved comparator.
         */
        artistCorrs.setComparator(savedComparator);

        artistLogger.info("found " + displayArtists.getLength() + " artists for display");

        /*
         * Create the appropriate column set.
         */
        if (showRemoteTracks == false)
        {
            ArtistDisplayColumns.createColumnSet(ArtistDisplayColumns.ColumnSet.LOCAL_VIEW, artistsTableView);
        }
        else
        {
            ArtistDisplayColumns.createColumnSet(ArtistDisplayColumns.ColumnSet.REMOTE_VIEW, artistsTableView);
        }

        /*
         * Add the artists to the window table view.
         */
        artistsTableView.setTableData(displayArtists);

        /*
         * Add a sort listener to allow column sorting.
         */
        artistsTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter()
        {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView)
            {
                List<Object> tableDataOfTableView = (List<Object>) tableView.getTableData();
                tableDataOfTableView.setComparator(new ITQTableViewRowComparator(tableView, uiLogger));
            }
        });
        
        /*
         * Allow multiple rows to be selected.
         */
        artistsTableView.setSelectMode(TableView.SelectMode.MULTI);
        
        /*
         * Set the styles for the info labels box pane. This is done here instead of in the BXML
         * in order to use an internal constant for the spacing.
         */
        Map<String, Object> infoBoxPaneStyles = new HashMap<String, Object>();
        infoBoxPaneStyles.put("padding", 10);
        infoBoxPaneStyles.put("spacing", InternalConstants.ARTISTS_LABEL_SPACING);
        infoBoxPane.setStyles(infoBoxPaneStyles);

        /*
         * Add widget texts.
         */
        instructionsLabel.setText(StringConstants.ARTISTS_INSTRUCTIONS);
        doneButton.setButtonData(StringConstants.DONE);
        altNameButton.setButtonData(StringConstants.ARTISTS_ALTNAME_BUTTON);
        altNameButton.setTooltipText(StringConstants.ARTISTS_ALTNAME_BUTTON_TIP);
        altNameButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        reviewOverridesButton.setButtonData(StringConstants.ARTISTS_REVIEW_OVERRIDES_BUTTON);
        reviewOverridesButton.setTooltipText(StringConstants.ARTISTS_REVIEW_OVERRIDES_BUTTON_TIP);
        reviewOverridesButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        
        /*
         * Disable the alternate name button to start. It only gets enabled when two or more rows
         * are selected.
         */
        altNameButton.setEnabled(false);

        /*
         * Set the window title.
         */
        artistsWindow.setTitle(Skins.Window.ARTISTS.getDisplayValue());

        /*
         * Register the artists window skin elements.
         */
        Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
        skins.registerWindowElements(Skins.Window.ARTISTS, windowElements);

        /*
         * Skin the artists window.
         */
        skins.skinMe(Skins.Window.ARTISTS);

        /*
         * Push the skinned window onto the skins window stack. It gets popped
         * from our done button press handler.
         */
        skins.pushSkinnedWindow(Skins.Window.ARTISTS);

        /*
         * Open the artists window.
         */
        uiLogger.info("opening artists window");
        artistsWindow.open(display);
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers.
     */
    private void createEventHandlers()
    {
        uiLogger.trace("createEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the done button press.
         */
        doneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("done button pressed");

                /*
                 * Close the window.
                 */
                artistsWindow.close();

                /*
                 * Pop the window off the skins window stack.
                 */
                skins.popSkinnedWindow();
            }
        });

        /*
         * Listener to handle the alternate name button press.
         */
        altNameButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alternate name button pressed");

                Display display = button.getDisplay();
                
                /*
                 * Set up and open the set alternate names dialog.
                 */
                setAlternateNames(display);
            }
        });

        /*
         * Listener to handle the review overrides button press.
         */
        reviewOverridesButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("review overrides button pressed");

                Display display = button.getDisplay();
                
                /*
                 * Set up and open the review overrides dialog.
                 */
                reviewOverrides(display);
            }
        });
        
        /*
         * Listener to handle artist row selection.
         */
        artistsTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter()
        { 
            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges)
            {
                
                /*
                 * Get the currently selected ranges.
                 */
                ImmutableList<Span> selectedRanges = tableView.getSelectedRanges();
                
                /*
                 * The selections get used if the alternate name button is pressed. That button 
                 * is grayed out unless at least 2 rows are selected, so count the number of 
                 * selections and enable or disable the button.
                 */
                int numSelections = 0;
                for (Span span : selectedRanges)
                {
                    numSelections += (span.end - span.start) + 1;
                }

                altNameButton.setEnabled(numSelections > 1);
            }
        });

        /*
         * Mouse click listener for the table view.
         */
        artistsTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {

                /*
                 * For a right mouse click we pop up a dialog of alternate
                 * names.
                 */
                if (button == Mouse.Button.RIGHT)
                {
                    TableView table = (TableView) component;
                    Display display = component.getDisplay();

                    /*
                     * Get the index for the clicked row, then set that row as
                     * selected.
                     */
                    int index = table.getRowAt(y);
                    table.setSelectedIndex(index);

                    /*
                     * Get the data for the selected row.
                     */
                    @SuppressWarnings("unchecked")
                    HashMap<String, String> selectedTrackRowData = 
                            (HashMap<String, String>) table.getSelectedRow();

                    /*
                     * Create and open the alternate names popup dialog.
                     */
                    try
                    {
                        handleAltNamesPopup(selectedTrackRowData, display, artistsWindow);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }

                return false;
            }
        });

        /*
         * This window state listener gets control when the artists window opens.
         * Issue an alert if any of the manual overrides are not valid.
         */
        artistsWindow.getWindowStateListeners().add(new WindowStateListener.Adapter()
        {
            @Override
            public void windowOpened(Window window)
            {
                List<String> invalidManualOverrides = XMLHandler.getInvalidManualOverrides();
                if (invalidManualOverrides != null && invalidManualOverrides.getLength() > 0)
                {
                    invalidOverridesFound = true;
                    Alert.alert(MessageType.WARNING, StringConstants.ALERT_INVALID_MANUAL_OVERRIDES, 
                            artistsWindow);
                }
            }
        });
    }

    /*
     * Display a dialog of alternate artist names when the user right clicks
     * on an artist name.
     */
    private void handleAltNamesPopup(Map<String, String> artistRowData, Display display, Window owningWindow)
            throws IOException, SerializationException
    {
        uiLogger.trace("handleAltNamesPopup: " + this.hashCode());

        /*
         * Get the artist name and log it.
         */
        String artistName = artistRowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
        uiLogger.info("right clicked on artist '" + artistName + "'");

        /*
         * The alternate artist names are kept in an ArtistNames object, which is obtained from the
         * artist object. But to get the artist object we need the artist correlator, so get that
         * now.
         */
        ArtistCorrelator artistCorr = XMLHandler.findArtistCorrelator(artistName);
        Artist artistObj = XMLHandler.getArtists().get(artistCorr.getArtistKey());
        ArtistNames artistNames = artistObj.getArtistNames();

        /*
         * Get the artist alternate names we want to display.
         */
        Map<String, ArtistTrackData> altNames = artistNames.getAltNames();

        /*
         * Only display the dialog if there is at least one alternate name.
         */
        if (altNames.getCount() > 0)
        {

            /*
             * Get the show remote tracks preference.
             */
            boolean showRemoteTracks = userPrefs.getShowRemoteTracks();

            /*
             * Build a list of the alternate names. We need to do this so we can weed out remote
             * artists if remotes are not being shown.
             */
            List<String> altNamesForDisplay = new ArrayList<String>();
            for (String altName : altNames)
            {
                ArtistTrackData.RemoteArtistControl remoteControl = 
                        altNames.get(altName).getRemoteArtistControl();
                if (showRemoteTracks == false && remoteControl == ArtistTrackData.RemoteArtistControl.REMOTE)
                {
                    continue;
                }

                altNamesForDisplay.add(altName);
            }

            artistLogger.info("found " + altNamesForDisplay.getLength()
                    + " alternate artist names to display");

            /*
             * Get the base BXML information for the alternate names dialog, and
             * start the list of components to be skinned.
             */
            List<Component> components = new ArrayList<Component>();
            initializeAltNamesDialogBxmlVariables(components);

            /*
             * Build table rows to represent the alternate names. This method
             * also adds components that need to be skinned.
             */
            List<TablePane.Row> altNamesRows = buildAltNamesRows(altNamesForDisplay, components);

            /*
             * Add the generated rows to the owning table pane.
             */
            for (TablePane.Row detailRow : altNamesRows)
            {
                altNamesTablePane.getRows().add(detailRow);
            }

            /*
             * Set the window title.
             */
            altNamesDialog.setTitle(Skins.Window.ALTNAMES.getDisplayValue());

            /*
             * Register the window elements.
             */
            Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
            skins.registerWindowElements(Skins.Window.ALTNAMES, windowElements);

            /*
             * Skin the alternate names dialog.
             */
            skins.skinMe(Skins.Window.ALTNAMES);

            /*
             * Open the alternate names dialog. There is no close button, so the
             * user has to close the dialog using the host controls.
             */
            uiLogger.info("opening alternate names dialog");
            altNamesDialog.open(display, owningWindow);
        }
        else
        {
            Alert.alert(MessageType.INFO, StringConstants.ALERT_NO_ALTERNATE_NAMES, artistsWindow);
        }
    }
    
    /*
     * Handle the set alternate names button.
     */
    private void setAlternateNames (Display display)
    {
        uiLogger.trace("setAlternateNames: " + this.hashCode());

        /*
         * Get the BXML information for the alternate name selection dialog, and
         * gather the list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        try
        {
            initializeAltNameSelectionDialogBxmlVariables(components);
        }
        catch (IOException | SerializationException e)
        {
            MainWindow.logException(uiLogger, e);
            throw new InternalErrorException(true, e.getMessage());
        }
        
        /*
         * Listener to handle the cancel button press.
         */
        altSelectCancelButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("set alternates cancel button pressed");
                
                /*
                 * Clean up the selected artists.
                 */
                cleanSelectedArtists();
                
                altNameSelectionDialog.close();
            }
        });
        
        /*
         * Listener to handle the proceed button press.
         */
        altSelectProceedButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("set alternates proceed button pressed");
                
                /*
                 * Merge all the alternates into the selected primary artist.
                 */
                mergeAlternatesToPrimary();
                
                altNameSelectionDialog.close();
            }
        });
        
        /*
         * Save the selected ranges for use in the proceed button handler.
         */
        selectedRanges = artistsTableView.getSelectedRanges();
        
        /*
         * Clean up any previously saved artists for display.
         */
        selectedArtists.clear();
        
        /*
         * Gather the list of selected artists.
         */
        List<HashMap<String, String>> altSelectTableData = new ArrayList<HashMap<String, String>>();
        
        @SuppressWarnings("unchecked") 
        Sequence<HashMap<String, String>> selectedRows = 
                (Sequence<HashMap<String, String>>) artistsTableView.getSelectedRows();
        
        boolean dissimilarNames = false;
        for (int rowIndex = 0; rowIndex < selectedRows.getLength(); rowIndex++)
        {
            HashMap<String, String> rowData = selectedRows.get(rowIndex);
            HashMap<String, String> selectedArtist = new HashMap<String, String>();
            String artistName = rowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());

            /*
             * Add the artist to the table data to be displayed.
             */
            selectedArtist.put(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue(), artistName);
            
            /*
             * Also add a hidden column with the index into the artist in the full list of
             * artists. This is needed to later remove the user selected primary artist
             * from the list of selected rows, so we can then proceed to remove all the 
             * alternate artist rows.
             */
            selectedArtist.put(MAP_PRIMARY_ARTIST, 
                    Integer.toString(getSelectedIndexForRelativeIndex(rowIndex)));
            
            altSelectTableData.add(selectedArtist);

            /*
             * Also save the artist for later processing if the proceed button is pressed.
             */
            int index = selectedArtists.add(artistName);

            /*
             * Inject some sanity by checking if the artist does not start or end with the same
             * word as the surrounding artists we've saved. We use this to warn the user.
             */
            String[] artistWords = artistName.split(" ");
            String previousArtist = null;
            String nextArtist = null;
            if (index >= 1)
            {
                previousArtist = selectedArtists.get(index - 1);
            }
            if (index < selectedArtists.getLength() - 1)
            {
                nextArtist = selectedArtists.get(index + 1);
            }
            
            if ((previousArtist != null) && 
                    (!previousArtist.startsWith(artistWords[0]) 
                            && !previousArtist.endsWith(artistWords[artistWords.length - 1]))
                    || (nextArtist != null) && 
                    (!nextArtist.startsWith(artistWords[0]) 
                            && !nextArtist.endsWith(artistWords[artistWords.length - 1])))
            {
                dissimilarNames = true;
            }
        }
        
        /*
         * Assume we don't need the warning label.
         */
        altSelectWarningLabel.setVisible(false);
        
        /*
         * Include the warning label if the number of selections seems excessive, or if the 
         * selected names are dissimilar.
         */
        if (selectedArtists.getLength() > NUM_MANUAL_SELECTION_THRESHOLD || dissimilarNames == true)
        {
            altSelectWarningLabel.setVisible(true);
            altSelectWarningLabel.setText(StringConstants.ARTISTS_ALTSELECT_WARNING);
            
            /*
             * Set the text red. This label is not part of the components list, so it
             * won't get skinned.
             * TODO this will need to be revisited if I ever support more than 1 font
             */
            Map<String, Object> warningLabelStyles = new HashMap<String, Object>();
            warningLabelStyles.put("color", "#FF0000");
            altSelectWarningLabel.setStyles(warningLabelStyles);
        }

        /*
         * Set the selected artists table data.
         */
        altSelectTableView.setTableData(altSelectTableData);
        
        /*
         * Set the preferred height of the scrollable table view. This is apparently the secret that
         * prevents the scrollable area from growing in size to accommodate all the data instead
         * of actually scrolling. Determined via much trial and error since Pivot's documentation
         * is horrid.
         */
        altSelectTableScrollPane.setPreferredHeight(InternalConstants.MANUAL_OVERRIDES_SCROLLPANE_HEIGHT);

        /*
         * Add widget texts.
         */
        altSelectInstructionsLabel.setText(StringConstants.ARTISTS_ALTSELECT_INSTRUCTIONS);
        altSelectTableColumnArtist.setHeaderData(StringConstants.TRACK_COLUMN_ARTIST);
        altSelectTableColumnArtist.setName(StringConstants.TRACK_COLUMN_ARTIST);
        altSelectCancelButton.setButtonData(StringConstants.CANCEL);
        altSelectProceedButton.setButtonData(StringConstants.ARTISTS_ALTNAME_PROCEED_BUTTON);

        /*
         * Set the window title.
         */
        altNameSelectionDialog.setTitle(Skins.Window.ALTNAMESELECTION.getDisplayValue());

        /*
         * Register the alternate name selection dialog skin elements.
         */
        Map<Skins.Element, List<Component>> windowElements = 
                skins.mapComponentsToSkinElements(components);
        skins.registerWindowElements(Skins.Window.ALTNAMESELECTION, windowElements);

        /*
         * Skin the alternate name selection dialog.
         */
        skins.skinMe(Skins.Window.ALTNAMESELECTION);

        /*
         * Open the alternate name selection dialog.
         */
        uiLogger.info("opening alternate name selection dialog");
        altNameSelectionDialog.open(display);
    }
    
    /*
     * Display the current manual overrides for user review.
     */
    private void reviewOverrides (Display display)
    {
        uiLogger.trace("reviewOverrides: " + this.hashCode());

        /*
         * Get the BXML information for the manual overrides dialog, and
         * gather the list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        try
        {
            initializeManualOverridesDialogBxmlVariables(components);
        }
        catch (IOException | SerializationException e)
        {
            MainWindow.logException(uiLogger, e);
            throw new InternalErrorException(true, e.getMessage());
        }
        
        /*
         * Listener to handle the done button press.
         */
        manualOverridesDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("manual overrides done button pressed");
                
                manualOverridesDialog.close();
            }
        });
        
        /*
         * Listener to handle the delete button press.
         */
        manualOverridesDeleteButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("manual overrides delete button pressed");
                
                /*
                 * Undo all the selected manual overrides.
                 */
                undoManualOverrides();
                
                manualOverridesDialog.close();
            }
        });
        
        /*
         * Listener to handle tree view branch collapse or expand.
         */
        manualOverridesTreeView.getTreeViewBranchListeners().add(new TreeViewBranchListener()
        {
            @Override
            public void branchExpanded(TreeView treeView, Path path)
            {  
            }

            @Override
            public void branchCollapsed(TreeView treeView, Path path)
            {
                
                /*
                 * Don't allow the user to collapse any branches. We depend on the path to every
                 * node in the tree remaining the same between the time we create the tree and the 
                 * time the delete button is pressed.
                 */
                treeView.expandAll();
            }
        });
        
        /*
         * Set multiple selection mode.
         */
        manualOverridesTreeView.setSelectMode(SelectMode.MULTI);
        
        /*
         * Initialize the top branch for displaying the overrides.
         */
        TreeBranch topBranch = new TreeBranch();
        
        /*
         * Initialize a map of paths to override artist names. This is used in the delete button
         * handler to easily locate the artists to be deleted from the manual overrides based on
         * the selected paths.
         */
        overridePaths = new HashMap<TreePath, String>();
        
        /*
         * Get the invalid overrides list.
         */
        List<String> invalidManualOverrides = XMLHandler.getInvalidManualOverrides();
        
        /*
         * Initialize the array of paths we want to select that indicate invalid overrides.
         */
        Path[] invalidPaths = new Path[invalidManualOverrides.getLength()];
        int pathIndex = 0;
        
        /*
         * Gather the tree of manual overrides for display. As we go along, create a path for
         * each node in the tree and add it to the override paths. Also, build up the invalid
         * path array for all invalid artists, so we can highlight those in the display.
         */
        int primaryIndex = 0;
        Map<String, List<String>> manualOverrides = userPrefs.getManualOverrides();
        for (String primaryArtist : manualOverrides)
        {
            
            /*
             * Each primary is a branch.
             */
            TreeBranch primary = new TreeBranch(primaryArtist);
            topBranch.add(primary);
            
            /*
             * Add the path the override paths map.
             */
            Path primaryPath = new Path(primaryIndex);
            Integer[] primaryPathArray = primaryPath.toArray();
            TreePath primaryTreePath = new TreePath(artistLogger, primaryPathArray);
            overridePaths.put(primaryTreePath, primaryArtist);
            
            /*
             * Add the path to the invalid paths array if this artist is no longer valid.
             */
            for (String invalidOverride : invalidManualOverrides)
            {
                if (invalidOverride.equals(primaryArtist))
                {
                    invalidPaths[pathIndex++] = primaryPath;
                    break;
                }
            }
            
            /*
             * Process the alternate artists for this primary.
             */
            List<String> alternateArtists = manualOverrides.get(primaryArtist);
            int alternateIndex = 0;
            for (String alternateArtist : alternateArtists)
            {
                
                /*
                 * Each alternate is a node (leaf).
                 */
                TreeNode alternate = new TreeNode(alternateArtist);
                primary.add(alternate);
                
                /*
                 * Add the path the override paths map.
                 */
                Path alternatePath = new Path(primaryIndex, alternateIndex);
                Integer[] alternatePathArray = alternatePath.toArray();
                TreePath alternateTreePath = new TreePath(artistLogger, alternatePathArray);
                overridePaths.put(alternateTreePath, alternateArtist);
                
                /*
                 * Add the path to the invalid paths array if this artist is no longer valid.
                 */
                for (String invalidOverride : invalidManualOverrides)
                {
                    if (invalidOverride.equals(alternateArtist))
                    {
                        invalidPaths[pathIndex++] = alternatePath;
                        break;
                    }
                }
                alternateIndex++;
            }
            primaryIndex++;
        }

        /*
         * Set the manual overrides tree data and expand all branches.
         */
        manualOverridesTreeView.setTreeData(topBranch);
        manualOverridesTreeView.expandAll();

        /*
         * Set the selected paths, if any.
         */
        if (pathIndex > 0)
        {
            Sequence<Path> selectedPaths = new ArrayAdapter<Path>(invalidPaths);
            manualOverridesTreeView.setSelectedPaths(selectedPaths);
        }
        
        /*
         * Enable or disable the info label based on the presence of invalid overrides.
         */
        manualOverridesInfoLabel.setVisible(invalidOverridesFound);
        
        /*
         * Set the preferred height of the scrollable tree view. This is apparently the secret that
         * prevents the scrollable area from growing in size to accommodate all the data instead
         * of actually scrolling. Determined via much trial and error since Pivot's documentation
         * is horrid.
         */
        manualOverridesTreeScrollPane.setPreferredHeight(InternalConstants.MANUAL_OVERRIDES_SCROLLPANE_HEIGHT);

        /*
         * Add widget texts.
         */
        manualOverridesInstructionsLabel.setText(StringConstants.ARTISTS_MANUAL_OVERRIDES_INSTRUCTIONS);
        manualOverridesInfoLabel.setText(StringConstants.ARTISTS_MANUAL_OVERRIDES_INFO);
        manualOverridesDoneButton.setButtonData(StringConstants.DONE);
        manualOverridesDeleteButton.setButtonData(StringConstants.ARTISTS_MANUAL_OVERRIDES_DELETE_BUTTON);
        manualOverridesDeleteButton.setTooltipText(StringConstants.ARTISTS_MANUAL_OVERRIDES_DELETE_BUTTON_TIP);
        manualOverridesDeleteButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * Set the window title.
         */
        manualOverridesDialog.setTitle(Skins.Window.MANUALOVERRIDES.getDisplayValue());

        /*
         * Register the manual overrides dialog skin elements.
         */
        Map<Skins.Element, List<Component>> windowElements = 
                skins.mapComponentsToSkinElements(components);
        skins.registerWindowElements(Skins.Window.MANUALOVERRIDES, windowElements);

        /*
         * Skin the manual overrides dialog.
         */
        skins.skinMe(Skins.Window.MANUALOVERRIDES);

        /*
         * Open the manual overrides dialog.
         */
        uiLogger.info("opening review manual overrides dialog");
        manualOverridesDialog.open(display, artistsWindow);
    }
    
    /*
     * Get the index to a selected row from the selected ranges, based on a relative index into
     * the selected rows. For example, suppose rows 1 through 3 and 7 through 8 are selected. 
     * The selected rows obtained from the table view have index values 0 through 4. If we're called
     * for index 3 from the selected rows list, we return selected range index 7, which then 
     * represents the selected row in the entire list of table rows.
     */
    private int getSelectedIndexForRelativeIndex (int relativeIndex)
    {
        uiLogger.trace("getSelectedIndexForRelativeIndex: " + this.hashCode());
        
        int selectedIndex = -1;
        int iterationCount = 0;

        Iterator<Span> selectedRangesIter = selectedRanges.iterator();
        while (selectedRangesIter.hasNext() && selectedIndex < 0)
        {
            Span span = selectedRangesIter.next();
            
            for (int i = span.start; i < span.end + 1; i++)
            {
                if (iterationCount++ == relativeIndex)
                {
                    selectedIndex = i;
                    break;
                }
            }
        }
        
        return selectedIndex;
    }
    
    /*
     * Clean up selected artists. This is called if we cancel the manual alternate name selection
     * for some reason.
     */
    private void cleanSelectedArtists ()
    {
        uiLogger.trace("cleanSelectedArtists: " + this.hashCode());
        
        /*
         * Clear out the saved selected artists.
         */
        selectedArtists.clear();
        
        /*
         * Clear the selection and repaint the table. Clearing the selection also causes
         * the alternate name selection button to be grayed, since we have a listener
         * for selection changes.
         */
        artistsTableView.clearSelection();
        artistsTableView.repaint(true);
    }
    
    /*
     * Merge all the alternate artists that were selected by the user into the selected primary.
     */
    private void mergeAlternatesToPrimary ()
    {
        artistLogger.trace("mergeAlternatesToPrimary: " + this.hashCode());

        /*
         * Get the primary artist (the one that should be selected).
         */
        @SuppressWarnings("unchecked")
        HashMap<String, String> artistsRowData = 
                (HashMap<String, String>) altSelectTableView.getSelectedRow();
        
        /*
         * Proceed if a primary was selected.
         */
        if (artistsRowData != null)
        { 
            String primaryArtist = artistsRowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
            int primaryTableIndex = Integer.parseInt(artistsRowData.get(MAP_PRIMARY_ARTIST));
            artistLogger.debug("primary artist '" + primaryArtist + "' selected, index " + primaryTableIndex);
            
            /*
             * Remove the primary from the displayed artists, leaving just the alternates.
             */
            selectedArtists.remove(primaryArtist);
            
            /*
             * Get the artist correlator list.
             */
            ArrayList<ArtistCorrelator> artistCorrs = XMLHandler.getArtistCorrelators();
            
            /*
             * Create a search correlator for the primary.
             */
            ArtistCorrelator primaryCorr = new ArtistCorrelator();
            ArtistNames primaryNames = new ArtistNames(primaryArtist);
            primaryCorr.setNormalizedName(primaryNames.normalizeName());

            /*
             * Loop through the alternates.
             */
            int primaryIdx = 0;
            for (String altArtist : selectedArtists)
            {
                
                /*
                 * Find the primary artist correlator. We have to do this for each 
                 * iteration because the index could change as we delete the alternates.
                 */
                primaryIdx = ArrayList.binarySearch(artistCorrs, primaryCorr, artistCorrs.getComparator());
                
                /*
                 * Find the alternate artist correlator.
                 */
                ArtistCorrelator altCorr = new ArtistCorrelator();
                ArtistNames altNames = new ArtistNames(altArtist);
                altCorr.setNormalizedName(altNames.normalizeName());
                int altIdx = ArrayList.binarySearch(artistCorrs, altCorr, artistCorrs.getComparator());
                
                /*
                 * Get the real alternate artist correlator, replacing the fake one we
                 * used for searching.
                 */
                altCorr = artistCorrs.get(altIdx);
                
                /*
                 * Transfer the alternate to the primary.
                 */
                XMLHandler.transferArtistToPrimary(altCorr, primaryIdx, altIdx);
                
                /*
                 * Add a manual override preference.
                 */
                userPrefs.addArtistManualOverride(primaryArtist, altArtist);
            }
            
            /*
             * Write the preferences.
             */
            try
            {
                userPrefs.writePreferences();
            }
            catch (IOException e)
            {
                MainWindow.logException(artistLogger, e);
                throw new InternalErrorException(true, e.getMessage());
            }
            
            /*
             * Update the main window labels to get the updated number of artists.
             */
            Utilities.updateMainWindowLabels(userPrefs.getXMLFileName());
            
            /*
             * Find the primary artist correlator again. It could have changed during the above loop.
             */
            primaryIdx = ArrayList.binarySearch(artistCorrs, primaryCorr, artistCorrs.getComparator());
            
            /*
             * Get the current table data, containing all the artist rows.
             */
            @SuppressWarnings("unchecked") 
            List<HashMap<String, String>> artistRows = 
                    (List<HashMap<String, String>>) artistsTableView.getTableData();
            
            /*
             * Update the displayed data for the primary artist.
             */
            ArtistCorrelator primaryArtistCorr = artistCorrs.get(primaryIdx);
            Artist primaryArtistObj = XMLHandler.getArtists().get(primaryArtistCorr.getArtistKey());
            HashMap<String, String> primaryRowData = primaryArtistObj.toDisplayMap();
            artistRows.update(primaryTableIndex, primaryRowData);
            
            /*
             * Update the artist count label.
             */
            numArtistsLabel.setText(StringConstants.ARTISTS_NUM_ARTISTS + XMLHandler.getNumberOfArtists());
            
            /*
             * Now we want to remove the alternate artists from the full table view of artists.
             * The current selection is almost what we want, but it still includes the primary.
             * So remove the primary from the selection, then get the new selected ranges.
             */
            artistsTableView.removeSelectedIndex(primaryTableIndex);
            selectedRanges = artistsTableView.getSelectedRanges();

            /*
             * Walk through the selected ranges to remove the now-alternate rows. We do this in
             * reverse order so that the remove operation doesn't affect any of the earlier indices.
             */
            for (int i = selectedRanges.getLength() - 1; i >= 0; i--)
            {
                Span span = selectedRanges.get(i);
                
                for (int j = span.end; j >= span.start; j--)
                {
                    artistRows.remove(j, 1);
                }
            }
            
            /*
             * Save the updated table data, clear any selection, and repaint the table.
             */
            altSelectTableView.setTableData(artistRows);
            artistsTableView.clearSelection();
            artistsTableView.repaint(true);
        }
        
        /*
         * A primary artist was not selected. Yell at the user.
         */
        else
        {
            Alert.alert(MessageType.WARNING, "You must select a primary artist name.", artistsWindow);
        }
    }
    
    /*
     * Undo selected manual overrides. This is called for the delete button on the review overrides
     * dialog.
     */
    private void undoManualOverrides ()
    {
        artistLogger.trace("undoManualOverrides: " + this.hashCode());
        
        /*
         * Get the manual overrides.
         */
        Map<String, List<String>> manualOverrides = userPrefs.getManualOverrides();
        
        /*
         * Get the selected paths. These could have been set or changed by the user.
         */
        ImmutableList<Path> selectedPaths = manualOverridesTreeView.getSelectedPaths();
        
        /*
         * Get the invalid overrides list.
         */
        ArrayList<String> invalidManualOverrides = XMLHandler.getInvalidManualOverrides();
        
        /*
         * Get the current table data, containing all the artist rows.
         */
        @SuppressWarnings("unchecked") 
        List<HashMap<String, String>> artistRows = 
                (List<HashMap<String, String>>) artistsTableView.getTableData();

        /*
         * Walk through the selected paths.
         */
        boolean overridesChanged = false;
        for (Path selected : selectedPaths)
        {
            Integer[] selectedArray = selected.toArray();
            TreePath selectedPath = new TreePath(artistLogger, selectedArray);
            
            /*
             * Get the artist name from the override paths map.
             */
            String selectedArtist = overridePaths.get(selectedPath);

            /*
             * Loop through the manual overrides. We use an iterator instead of foreach so
             * we can safely remove primary artists.
             */
            Iterator<String> manualOverridesIter = manualOverrides.iterator();
            while (manualOverridesIter.hasNext())
            {
                String primaryArtist = manualOverridesIter.next();
                
                boolean primaryDeleted = false;
                
                /*
                 * If this primary is to be deleted, remove from the invalid manual overrides. 
                 * Set a flag so we also remove all alternates.
                 */
                if (selectedArtist.equals(primaryArtist))
                {
                    invalidManualOverrides.remove(selectedArtist);
                    overridesChanged = true;
                    primaryDeleted = true;
                }
                
                /*
                 * Loop through the alternates for this primary, using an iterator for 
                 * safe deletes.
                 */
                boolean foundAlternate = false;
                List<String> altArtists = manualOverrides.get(primaryArtist);
                Iterator<String> altArtistsIter = altArtists.iterator();
                while (altArtistsIter.hasNext())
                {
                    String altArtist = altArtistsIter.next();
                    
                    /*
                     * Process an alternate to be deleted.
                     */
                    if (primaryDeleted == true || selectedArtist.equals(altArtist))
                    {
                        foundAlternate = true;
                        
                        /*
                         * Fix the database by transferring the alternate to become its own
                         * standalone artist.
                         */
                        XMLHandler.transferArtistFromPrimary(primaryArtist, altArtist);
                        
                        /*
                         * Remove this alternate from the alternates list and replace the list 
                         * in the primary. Also remove it from the invalid manual overrides.
                         */
                        altArtistsIter.remove();
                        manualOverrides.put(primaryArtist, altArtists);
                        invalidManualOverrides.remove(selectedArtist);
                        overridesChanged = true;
                        
                        /*
                         * Access alternate artist objects, so we can add this artist to the list
                         * of artists being displayed.
                         */
                        ArtistCorrelator altArtistCorr = XMLHandler.findArtistCorrelator(altArtist);
                        Artist altArtistObj = XMLHandler.getArtists().get(altArtistCorr.getArtistKey());
                        
                        /*
                         * Add this alternate to the list of displayed artists.
                         */
                        HashMap<String, String> rowData = altArtistObj.toDisplayMap();
                        artistRows.add(rowData);
                        
                        /*
                         * If we've handled the one matching alternate, exit the alternates loop.
                         */
                        if (primaryDeleted == false)
                        {
                            break;
                        }
                    }
                }

                /*
                 * If the primary was deleted, remove it from the overrides list. This had
                 * to wait until we were done processing all alternates.
                 */
                if (primaryDeleted == true)
                {
                    manualOverridesIter.remove();
                }
                
                /*
                 * If we found something, we need to update the displayed primary artist, since one
                 * or more alternates were removed.
                 */
                if (foundAlternate == true)
                {
                    
                    /*
                     * We need to update the primary artist in the list of displayed artists. But this
                     * is ugly: we have no way to find primaries other than searching the entire list.
                     */
                    int index = 0;
                    boolean foundPrimary = false;
                    for (HashMap<String, String> rowData : artistRows)
                    {
                        String artistName = 
                                rowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
                        if (artistName.equals(primaryArtist))
                        {
                            foundPrimary = true;
                            break;
                        }
                        
                        index++;
                    }
                    
                    /*
                     * Update this primary if we found it in the list. Which should always happen.
                     */
                    if (foundPrimary == true)
                    {
                        
                        /*
                         * Access primary artist objects, so we can update this artist in the list
                         * of displayed artists.
                         */
                        ArtistCorrelator primaryArtistCorr = 
                                XMLHandler.findArtistCorrelator(primaryArtist);
                        Artist primaryArtistObj = 
                                XMLHandler.getArtists().get(primaryArtistCorr.getArtistKey());
                        
                        /*
                         * Update this primary.
                         */
                        HashMap<String, String> primaryRowData = primaryArtistObj.toDisplayMap();
                        artistRows.update(index, primaryRowData);
                    }

                    /*
                     * Break out of the overrides loop.
                     */
                    break;
                }
            }
        }
        
        /*
         * Update the preferences if we made any changes.
         */
        if (overridesChanged == true)
        {
            try
            {
                userPrefs.writePreferences();
            }
            catch (IOException e)
            {
                MainWindow.logException(artistLogger, e);
                throw new InternalErrorException(true, e.getMessage());
            }
            
            /*
             * Update the main window labels to get the updated number of artists.
             */
            Utilities.updateMainWindowLabels(userPrefs.getXMLFileName());
            
            /*
             * Update the artist count label.
             */
            numArtistsLabel.setText(StringConstants.ARTISTS_NUM_ARTISTS + XMLHandler.getNumberOfArtists());
            
            /*
             * Save the updated table data and resort it.
             */
            artistsTableView.setTableData(artistRows);
            artistsTableView.setSort(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue(), 
                    SortDirection.ASCENDING);
        }
    }

    /*
     * Build the alternate names data for display.
     */
    private List<TablePane.Row> buildAltNamesRows(List<String> altNames, List<Component> components)
    {
        uiLogger.trace("buildAltNamesRows: " + this.hashCode());

        List<TablePane.Row> result = new ArrayList<TablePane.Row>();

        /*
         * Build a row for all alternate names.
         */
        for (String altName : altNames)
        {

            /*
             * Start building a table row to be returned.
             */
            TablePane.Row infoRow = new TablePane.Row();
            infoRow.setHeight("1*");

            /*
             * Build the label that contains the actual alternate name.
             */
            Label altNameLabel = new Label(altName);
            Map<String, Object> altNameLabelStyles = new HashMap<String, Object>();
            altNameLabelStyles.put("padding", 0);
            altNameLabel.setStyles(altNameLabelStyles);

            /*
             * Add the label to the table row.
             */
            infoRow.add(altNameLabel);

            /*
             * Add the components we created so they can be skinned.
             */
            components.add(altNameLabel);

            /*
             * Add the table row to the result.
             */
            result.add(infoRow);
        }

        return result;
    }

    /*
     * Initialize artists window BXML variables and collect the list of
     * components to be skinned.
     */
    private void initializeBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeBxmlVariables: " + this.hashCode());

        BXMLSerializer windowSerializer = new BXMLSerializer();

        artistsWindow = (Window) windowSerializer.readObject(getClass().getResource("artistsWindow.bxml"));

        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars) artistsWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);

        infoBorder = 
                (Border) windowSerializer.getNamespace().get("infoBorder");
        components.add(infoBorder);
        infoBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("infoBoxPane");
        components.add(infoBoxPane);
        numArtistsLabel = 
                (Label) windowSerializer.getNamespace().get("numArtistsLabel");
        components.add(numArtistsLabel);
        instructionsLabel = 
                (Label) windowSerializer.getNamespace().get("instructionsLabel");
        components.add(instructionsLabel);
        artistsBorder = 
                (Border) windowSerializer.getNamespace().get("artistsBorder");
        components.add(artistsBorder);
        artistsTableView = 
                (TableView) windowSerializer.getNamespace().get("artistsTableView");
        components.add(artistsTableView);
        artistsTableViewHeader = 
                (TableViewHeader) windowSerializer.getNamespace().get("artistsTableViewHeader");
        components.add(artistsTableViewHeader);
        actionBorder = 
                (Border) windowSerializer.getNamespace().get("actionBorder");
        components.add(actionBorder);
        actionBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("actionBoxPane");
        components.add(actionBoxPane);

        doneButton = 
                (PushButton) windowSerializer.getNamespace().get("doneButton");
        components.add(doneButton);
        altNameButton = 
                (PushButton) windowSerializer.getNamespace().get("altNameButton");
        components.add(altNameButton);
        reviewOverridesButton = 
                (PushButton) windowSerializer.getNamespace().get("reviewOverridesButton");
        components.add(reviewOverridesButton);
    }

    /*
     * Initialize alternate names dialog BXML variables and collect the static
     * components to be skinned.
     */
    private void initializeAltNamesDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeAltNamesDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        altNamesDialog = (Dialog) dialogSerializer.
                readObject(getClass().getResource("artistAltNamesDialog.bxml"));

        altNamesPrimaryBorder = 
                (Border) dialogSerializer.getNamespace().get("altNamesPrimaryBorder");
        components.add(altNamesPrimaryBorder);
        altNamesTablePane = 
                (TablePane) dialogSerializer.getNamespace().get("altNamesTablePane");
        components.add(altNamesTablePane);
    }

    /*
     * Initialize alternate name selection dialog BXML variables and collect the components
     * to be skinned.
     */
    private void initializeAltNameSelectionDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeAltNameSelectionDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        altNameSelectionDialog = (Dialog) dialogSerializer.
                readObject(getClass().getResource("artistAltNameSelectionDialog.bxml"));
        
        altSelectLabelsBorder = 
                (Border) dialogSerializer.getNamespace().get("altSelectLabelsBorder");
        components.add(altSelectLabelsBorder);
        altSelectLabelsBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("altSelectLabelsBoxPane");
        components.add(altSelectLabelsBoxPane);
        
        /*
         * Don't add this to the components list because we want to set the text red, instead
         * of the skin color.
         */
        altSelectWarningLabel = 
                (Label) dialogSerializer.getNamespace().get("altSelectWarningLabel");
        
        altSelectInstructionsLabel = 
                (Label) dialogSerializer.getNamespace().get("altSelectInstructionsLabel");
        components.add(altSelectInstructionsLabel);
        altSelectTableBorder = 
                (Border) dialogSerializer.getNamespace().get("altSelectTableBorder");
        components.add(altSelectTableBorder);
        altSelectTableScrollPane = 
                (ScrollPane) dialogSerializer.getNamespace().get("altSelectTableScrollPane");
        components.add(altSelectTableScrollPane);
        altSelectTableView = 
                (TableView) dialogSerializer.getNamespace().get("altSelectTableView");
        components.add(altSelectTableView);

        /*
         * This doesn't need to be added to the components list because it's a 
         * subcomponent.
         */
        altSelectTableColumnArtist =
                (TableView.Column) dialogSerializer.getNamespace().get("altSelectTableColumnArtist");

        altSelectButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("altSelectButtonBorder");
        components.add(altSelectButtonBorder);
        altSelectButtonBoxPane =
                (BoxPane) dialogSerializer.getNamespace().get("altSelectButtonBoxPane");
        components.add(altSelectButtonBoxPane);
        altSelectCancelButton = 
                (PushButton) dialogSerializer.getNamespace().get("altSelectCancelButton");
        components.add(altSelectCancelButton);
        altSelectProceedButton = 
                (PushButton) dialogSerializer.getNamespace().get("altSelectProceedButton");
        components.add(altSelectProceedButton);
    }

    /*
     * Initialize manual overrides dialog BXML variables and collect the static
     * components to be skinned.
     */
    private void initializeManualOverridesDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeManualOverridesDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        manualOverridesDialog = (Dialog) dialogSerializer.
                readObject(getClass().getResource("artistManualOverridesDialog.bxml"));

        manualOverridesLabelsBorder = 
                (Border) dialogSerializer.getNamespace().get("manualOverridesLabelsBorder");
        components.add(manualOverridesLabelsBorder);
        manualOverridesLabelsBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("manualOverridesLabelsBoxPane");
        components.add(manualOverridesLabelsBoxPane);
        manualOverridesInstructionsLabel = 
                (Label) dialogSerializer.getNamespace().get("manualOverridesInstructionsLabel");
        components.add(manualOverridesInstructionsLabel);
        manualOverridesInfoLabel = 
                (Label) dialogSerializer.getNamespace().get("manualOverridesInfoLabel");
        components.add(manualOverridesInfoLabel);
        manualOverridesTreeBorder = 
                (Border) dialogSerializer.getNamespace().get("manualOverridesTreeBorder");
        components.add(manualOverridesTreeBorder);
        manualOverridesTreeScrollPane = 
                (ScrollPane) dialogSerializer.getNamespace().get("manualOverridesTreeScrollPane");
        components.add(manualOverridesTreeScrollPane);
        manualOverridesTreeView = 
                (TreeView) dialogSerializer.getNamespace().get("manualOverridesTreeView");
        components.add(manualOverridesTreeView);
        manualOverridesButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("manualOverridesButtonBorder");
        components.add(manualOverridesButtonBorder);
        manualOverridesButtonBoxPane =
                (BoxPane) dialogSerializer.getNamespace().get("manualOverridesButtonBoxPane");
        components.add(manualOverridesButtonBoxPane);
        manualOverridesDoneButton = 
                (PushButton) dialogSerializer.getNamespace().get("manualOverridesDoneButton");
        components.add(manualOverridesDoneButton);
        manualOverridesDeleteButton = 
                (PushButton) dialogSerializer.getNamespace().get("manualOverridesDeleteButton");
        components.add(manualOverridesDeleteButton);
    }
}
