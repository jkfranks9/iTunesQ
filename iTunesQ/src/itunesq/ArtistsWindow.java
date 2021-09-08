package itunesq;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
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
import org.apache.pivot.wtk.TableViewHeaderPressListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeView.SelectMode;
import org.apache.pivot.wtk.Window;
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
    private Dialog setAltNameSelectionDialog = null;
    private Dialog removeAltNameSelectionDialog = null;
    private Dialog artistOverridesDialog = null;
    private List<String> selectedArtists = null;
    private ImmutableList<Span> selectedRanges = null;
    private String primaryForRemoval = null;
    private String tableSortColumnName = null;
    
    private Skins skins = null;
    private Preferences userPrefs = null;
    private Logger uiLogger = null;
    private Logger artistLogger = null;
    
    private static final int NUM_SET_SELECTION_THRESHOLD = 5;

    /*
     * Map key to hold the index into the full list of artists. This is a column definition, 
     * but is not displayed as such. We need it for set alternate name processing. When the
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
    @BXML private Border alphaBorder = null;
    @BXML private BoxPane alphaBoxPane = null;
    @BXML private Label alphaLabel = null;
    @BXML private PushButton alphaAButton = null;
    @BXML private PushButton alphaBButton = null;
    @BXML private PushButton alphaCButton = null;
    @BXML private PushButton alphaDButton = null;
    @BXML private PushButton alphaEButton = null;
    @BXML private PushButton alphaFButton = null;
    @BXML private PushButton alphaGButton = null;
    @BXML private PushButton alphaHButton = null;
    @BXML private PushButton alphaIButton = null;
    @BXML private PushButton alphaJButton = null;
    @BXML private PushButton alphaKButton = null;
    @BXML private PushButton alphaLButton = null;
    @BXML private PushButton alphaMButton = null;
    @BXML private PushButton alphaNButton = null;
    @BXML private PushButton alphaOButton = null;
    @BXML private PushButton alphaPButton = null;
    @BXML private PushButton alphaQButton = null;
    @BXML private PushButton alphaRButton = null;
    @BXML private PushButton alphaSButton = null;
    @BXML private PushButton alphaTButton = null;
    @BXML private PushButton alphaUButton = null;
    @BXML private PushButton alphaVButton = null;
    @BXML private PushButton alphaWButton = null;
    @BXML private PushButton alphaXButton = null;
    @BXML private PushButton alphaYButton = null;
    @BXML private PushButton alphaZButton = null;
    @BXML private Border artistsBorder = null;
    @BXML private TableView artistsTableView = null;
    @BXML private TableViewHeader artistsTableViewHeader = null;
    @BXML private Border actionBorder = null;
    @BXML private BoxPane actionBoxPane = null;
    @BXML private PushButton doneButton = null;
    @BXML private PushButton setAltNameButton = null;
    @BXML private PushButton removeAltNameButton = null;
    @BXML private PushButton reviewOverridesButton = null;

    /*
     * ... alternate names dialog.
     */
    @BXML private Border altNamesPrimaryBorder = null;
    @BXML private TablePane altNamesTablePane = null;

    /*
     * ... set alternate name selection dialog.
     */
    @BXML private Border setAltSelectLabelsBorder = null;
    @BXML private BoxPane setAltSelectLabelsBoxPane = null;
    @BXML private Label setAltSelectWarningLabel = null;
    @BXML private Label setAltSelectInstructionsLabel = null;
    @BXML private Border setAltSelectTableBorder = null;
    @BXML private ScrollPane setAltSelectTableScrollPane = null;
    @BXML private TableView setAltSelectTableView = null;
    @BXML private TableView.Column setAltSelectTableColumnArtist = null;
    @BXML private Border setAltSelectButtonBorder = null;
    @BXML private BoxPane setAltSelectButtonBoxPane = null;
    @BXML private PushButton setAltSelectCancelButton = null;
    @BXML private PushButton setAltSelectProceedButton = null;

    /*
     * ... remove alternate name selection dialog.
     */
    @BXML private Border removeAltSelectLabelsBorder = null;
    @BXML private BoxPane removeAltSelectLabelsBoxPane = null;
    @BXML private Label removeAltSelectInstructionsLabel = null;
    @BXML private Border removeAltSelectTableBorder = null;
    @BXML private ScrollPane removeAltSelectTableScrollPane = null;
    @BXML private TableView removeAltSelectTableView = null;
    @BXML private TableView.Column removeAltSelectTableColumnArtist = null;
    @BXML private Border removeAltSelectButtonBorder = null;
    @BXML private BoxPane removeAltSelectButtonBoxPane = null;
    @BXML private PushButton removeAltSelectCancelButton = null;
    @BXML private PushButton removeAltSelectProceedButton = null;

    /*
     * ... review artist overrides dialog.
     */
    @BXML private Border artistOverridesLabelBorder = null;
    @BXML private BoxPane artistOverridesLabelBoxPane = null;
    @BXML private Label artistOverridesManualLabel = null;
    @BXML private Label artistOverridesAutomaticLabel = null;
    @BXML private Border artistOverridesTreeBorder = null;
    @BXML private ScrollPane artistOverridesTreeScrollPane = null;
    @BXML private TreeView artistOverridesTreeView = null;
    @BXML private Border artistOverridesButtonBorder = null;
    @BXML private BoxPane artistOverridesButtonBoxPane = null;
    @BXML private PushButton artistOverridesDoneButton = null;

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
        tableSortColumnName = ArtistDisplayColumns.ColumnNames.ARTIST.getHeaderValue();

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
         * Get the BXML information for the artists window, and generate the
         * list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeBxmlVariables(components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers();
        createAlphaEventHandlers();

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
        ArtistDisplayColumns.createColumnSet(ArtistDisplayColumns.ColumnSet.ARTIST_VIEW, 
                artistsTableView);

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
         * Add a listener to detect column sort. We save the column being sorted for the alpha
         * bar logic. 
         */
        artistsTableViewHeader.getTableViewHeaderPressListeners().add(new TableViewHeaderPressListener()
        {
            @Override
            public void headerPressed(TableViewHeader tableViewHeader,
                    int index)
            {
            	TableView table = tableViewHeader.getTableView();
            	tableSortColumnName = table.getColumns().get(index).getName();
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
         * Set the width of the (empty) alpha bar label. The label's only purpose is to
         * allow centering the alpha bar.
         */
        int displayWidth = display.getWidth();
        int alphaLabelWidth = (displayWidth - InternalConstants.ALPHA_BAR_PADDING - InternalConstants.ALPHA_BAR_WIDTH) / 2;
        alphaLabel.setPreferredWidth(alphaLabelWidth);

        /*
         * Add widget texts.
         */
        instructionsLabel.setText(StringConstants.ARTISTS_SET_INSTRUCTIONS);
        doneButton.setButtonData(StringConstants.DONE);
        setAltNameButton.setButtonData(StringConstants.ARTISTS_SET_ALTNAME_BUTTON);
        setAltNameButton.setTooltipText(StringConstants.ARTISTS_SET_ALTNAME_BUTTON_TIP);
        setAltNameButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        removeAltNameButton.setButtonData(StringConstants.ARTISTS_REMOVE_ALTNAME_BUTTON);
        removeAltNameButton.setTooltipText(StringConstants.ARTISTS_REMOVE_ALTNAME_BUTTON_TIP);
        removeAltNameButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        reviewOverridesButton.setButtonData(StringConstants.ARTISTS_REVIEW_OVERRIDES_BUTTON);
        reviewOverridesButton.setTooltipText(StringConstants.ARTISTS_REVIEW_OVERRIDES_BUTTON_TIP);
        reviewOverridesButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        
        /*
         * Disable the set and remove alternate name buttons to start. They only get enabled when 
         * two or more rows are selected, or a row with existing alternate names is selected,
         * respectively. 
         */
        setAltNameButton.setEnabled(false);
        removeAltNameButton.setEnabled(false);

        /*
         * Set the window title.
         */
        artistsWindow.setTitle(Skins.Window.ARTISTS.getDisplayValue());

        /*
         * Register the artists window skin elements.
         */
        skins.registerWindowElements(Skins.Window.ARTISTS, components);

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
         * Listener to handle the set alternate name button press.
         */
        setAltNameButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("set alternate name button pressed");

                Display display = button.getDisplay();
                
                /*
                 * Set up and open the set alternate names dialog.
                 */
                setAlternateNames(display);
            }
        });

        /*
         * Listener to handle the remove alternate name button press.
         */
        removeAltNameButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("remove alternate name button pressed");

                Display display = button.getDisplay();
                
                /*
                 * Set up and open the remove alternate names dialog.
                 */
                removeAlternateNames(display);
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
                 * The selections get used if the set alternate name button is pressed. That button 
                 * is grayed out unless at least 2 rows are selected, so count the number of 
                 * selections and enable or disable the button.
                 */
                int numSelections = 0;
                for (Span span : selectedRanges)
                {
                    numSelections += (span.end - span.start) + 1;
                }

                setAltNameButton.setEnabled(numSelections > 1);
                
                /*
                 * Enable the remove alternate name button if a single row is selected.
                 */
                removeAltNameButton.setEnabled(numSelections == 1);
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
    }
    
    /*
     * Create the alpha bar event handlers.
     */
    private void createAlphaEventHandlers()
    {
        uiLogger.trace("createAlphaEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the A button press.
         */
        alphaAButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar numeric button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the A button press.
         */
        alphaAButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar A button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the B button press.
         */
        alphaBButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar B button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the C button press.
         */
        alphaCButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar C button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the D button press.
         */
        alphaDButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar D button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the E button press.
         */
        alphaEButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar E button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the F button press.
         */
        alphaFButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar F button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the G button press.
         */
        alphaGButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar G button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the H button press.
         */
        alphaHButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar H button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the I button press.
         */
        alphaIButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar I button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the J button press.
         */
        alphaJButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar J button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the K button press.
         */
        alphaKButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar K button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the L button press.
         */
        alphaLButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar L button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the M button press.
         */
        alphaMButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar M button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the N button press.
         */
        alphaNButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar N button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the O button press.
         */
        alphaOButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar O button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the P button press.
         */
        alphaPButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar P button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the Q button press.
         */
        alphaQButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar Q button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the R button press.
         */
        alphaRButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar R button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the S button press.
         */
        alphaSButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar S button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the T button press.
         */
        alphaTButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar T button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the U button press.
         */
        alphaUButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar U button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the V button press.
         */
        alphaVButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar V button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the W button press.
         */
        alphaWButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar W button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the X button press.
         */
        alphaXButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar X button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the Y button press.
         */
        alphaYButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar Y button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });

        /*
         * Listener to handle the Z button press.
         */
        alphaZButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("alpha bar Z button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_ARTIST:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
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
             * Build a list of the alternate names.
             */
            List<String> altNamesForDisplay = new ArrayList<String>();
            for (String altName : altNames)
            {
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
            altNamesDialog.setTitle(Skins.Window.ALT_NAMES.getDisplayValue());

            /*
             * Register the window elements.
             */
            skins.registerWindowElements(Skins.Window.ALT_NAMES, components);

            /*
             * Skin the alternate names dialog.
             */
            skins.skinMe(Skins.Window.ALT_NAMES);

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
         * Get the BXML information for the set alternate name selection dialog, and
         * gather the list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        try
        {
            initializeSetAltNameSelectionDialogBxmlVariables(components);
        }
        catch (IOException | SerializationException e)
        {
            MainWindow.logException(uiLogger, e);
            throw new InternalErrorException(true, e.getMessage());
        }
        
        /*
         * Listener to handle the cancel button press.
         */
        setAltSelectCancelButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("set alternates cancel button pressed");
                
                /*
                 * Clean up the selected artists.
                 */
                cleanSelectedArtists();
                
                setAltNameSelectionDialog.close();
            }
        });
        
        /*
         * Listener to handle the proceed button press.
         */
        setAltSelectProceedButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("set alternates proceed button pressed");
                
                /*
                 * Merge all the alternates into the selected primary artist.
                 */
                mergeAlternatesToPrimary();
                
                setAltNameSelectionDialog.close();
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
         * Create the table data list.
         */
        List<HashMap<String, String>> altSelectTableData = new ArrayList<HashMap<String, String>>();
        
        /*
         * Get the selected rows.
         */
        @SuppressWarnings("unchecked") 
        Sequence<HashMap<String, String>> selectedRows = 
                (Sequence<HashMap<String, String>>) artistsTableView.getSelectedRows();
        
        /*
         * Gather the selected artists into a list.
         */
        boolean dissimilarNames = false;
        for (int rowIndex = 0; rowIndex < selectedRows.getLength(); rowIndex++)
        {
            HashMap<String, String> rowData = selectedRows.get(rowIndex);
            String artistName = rowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
            HashMap<String, String> artistData = new HashMap<String, String>();

            /*
             * Add the artist to the table data to be displayed.
             */
            artistData.put(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue(), artistName);
            
            /*
             * Also add a hidden column with the index into the artist in the full list of
             * artists. This is needed to later remove the user selected primary artist
             * from the list of selected rows, so we can then proceed to remove all the 
             * alternate artist rows.
             */
            artistData.put(MAP_PRIMARY_ARTIST, 
                    Integer.toString(getSelectedIndexForRelativeIndex(rowIndex)));
            
            altSelectTableData.add(artistData);

            /*
             * Save the artist for later processing if the proceed button is pressed.
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

        artistLogger.info("found " + altSelectTableData.getLength()
                + " artist names to display");
        
        /*
         * Assume we don't need the warning label.
         */
        setAltSelectWarningLabel.setVisible(false);
        
        /*
         * Include the warning label if the number of selections seems excessive, or if the 
         * selected names are dissimilar.
         */
        if (selectedArtists.getLength() > NUM_SET_SELECTION_THRESHOLD || dissimilarNames == true)
        {
            setAltSelectWarningLabel.setVisible(true);
            setAltSelectWarningLabel.setText(StringConstants.ARTISTS_ALTSELECT_WARNING);
            
            /*
             * Set the text red. This label is not part of the components list, so it
             * won't get skinned.
             * TODO this will need to be revisited if I ever support more than 1 font
             */
            Map<String, Object> warningLabelStyles = new HashMap<String, Object>();
            warningLabelStyles.put("color", "#FF0000");
            setAltSelectWarningLabel.setStyles(warningLabelStyles);
        }

        /*
         * Set the selected artists table data.
         */
        setAltSelectTableView.setTableData(altSelectTableData);
        
        /*
         * Set the preferred height of the scrollable table view. This is apparently the secret that
         * prevents the scrollable area from growing in size to accommodate all the data instead
         * of actually scrolling. Determined via much trial and error since Pivot's documentation
         * is horrid.
         */
        setAltSelectTableScrollPane.setPreferredHeight(InternalConstants.ARTIST_OVERRIDES_SCROLLPANE_HEIGHT);

        /*
         * Add widget texts.
         */
        setAltSelectInstructionsLabel.setText(StringConstants.ARTISTS_ALTSELECT_INSTRUCTIONS);
        setAltSelectTableColumnArtist.setHeaderData(StringConstants.TRACK_COLUMN_ARTIST);
        setAltSelectTableColumnArtist.setName(StringConstants.TRACK_COLUMN_ARTIST);
        setAltSelectCancelButton.setButtonData(StringConstants.CANCEL);
        setAltSelectProceedButton.setButtonData(StringConstants.PROCEED);

        /*
         * Set the window title.
         */
        setAltNameSelectionDialog.setTitle(Skins.Window.SET_ALT_NAME_SELECTION.getDisplayValue());

        /*
         * Register the set alternate name selection dialog skin elements.
         */
        skins.registerWindowElements(Skins.Window.SET_ALT_NAME_SELECTION, components);

        /*
         * Skin the set alternate name selection dialog.
         */
        skins.skinMe(Skins.Window.SET_ALT_NAME_SELECTION);

        /*
         * Open the set alternate name selection dialog.
         */
        uiLogger.info("opening set alternate name selection dialog");
        setAltNameSelectionDialog.open(display);
    }
    
    /*
     * Handle the remove alternate names button.
     */
    private void removeAlternateNames (Display display)
    {
        uiLogger.trace("removeAlternateNames: " + this.hashCode());

        /*
         * Get the BXML information for the remove alternate name selection dialog, and
         * gather the list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        try
        {
            initializeRemoveAltNameSelectionDialogBxmlVariables(components);
        }
        catch (IOException | SerializationException e)
        {
            MainWindow.logException(uiLogger, e);
            throw new InternalErrorException(true, e.getMessage());
        }
        
        /*
         * Listener to handle the cancel button press.
         */
        removeAltSelectCancelButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("remove alternates cancel button pressed");
                
                /*
                 * Clean up the selected artists.
                 */
                cleanSelectedArtists();
                
                removeAltNameSelectionDialog.close();
            }
        });
        
        /*
         * Listener to handle the proceed button press.
         */
        removeAltSelectProceedButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("remove alternates proceed button pressed");
                
                /*
                 * Remove all the selected alternates from the primary artist.
                 */
                removeAlternatesFromPrimary();
                
                removeAltNameSelectionDialog.close();
            }
        });
        
        /*
         * Set multiple selection mode.
         */
        removeAltSelectTableView.setSelectMode(TableView.SelectMode.MULTI);
        
        /*
         * Create the table data list.
         */
        List<HashMap<String, String>> altRemoveTableData = new ArrayList<HashMap<String, String>>();
        
        /*
         * Get the selected rows.
         */
        @SuppressWarnings("unchecked") 
        Sequence<HashMap<String, String>> selectedRows = 
                (Sequence<HashMap<String, String>>) artistsTableView.getSelectedRows();

        /*
         * Get the primary artist and save it for the proceed button handler.
         */
        HashMap<String, String> rowData = selectedRows.get(0);
        primaryForRemoval = rowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());

        /*
         * The alternate artist names are kept in an ArtistNames object, which is obtained from the
         * artist object. But to get the artist object we need the artist correlator.
         */
        ArtistCorrelator artistCorr = XMLHandler.findArtistCorrelator(primaryForRemoval);
        Artist artistObj = XMLHandler.getArtists().get(artistCorr.getArtistKey());
        ArtistNames artistNames = artistObj.getArtistNames();

        /*
         * Get the artist alternate names.
         */
        Map<String, ArtistTrackData> altNames = artistNames.getAltNames();

        /*
         * Only display the dialog if there is at least one alternate name.
         */
        if (altNames.getCount() > 0)
        {
            
            /*
             * Build a list of alternate artist data.
             */
            for (String altName : altNames)
            {
                HashMap<String, String> altArtistData = new HashMap<String, String>();

                altArtistData.put(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue(), altName);
                
                altRemoveTableData.add(altArtistData);
            }

            artistLogger.info("found " + altRemoveTableData.getLength()
                    + " alternate artist names to display");

            /*
             * Set the alternate artists table data.
             */
            removeAltSelectTableView.setTableData(altRemoveTableData);

            /*
             * Set the preferred height of the scrollable table view. This is apparently the secret that
             * prevents the scrollable area from growing in size to accommodate all the data instead
             * of actually scrolling. Determined via much trial and error since Pivot's documentation
             * is horrid.
             */
            removeAltSelectTableScrollPane.setPreferredHeight(InternalConstants.ARTIST_OVERRIDES_SCROLLPANE_HEIGHT);

            /*
             * Add widget texts.
             */
            removeAltSelectInstructionsLabel.setText(StringConstants.ARTISTS_REMOVE_INSTRUCTIONS);
            removeAltSelectTableColumnArtist.setHeaderData(StringConstants.TRACK_COLUMN_ARTIST);
            removeAltSelectTableColumnArtist.setName(StringConstants.TRACK_COLUMN_ARTIST);
            removeAltSelectCancelButton.setButtonData(StringConstants.CANCEL);
            removeAltSelectProceedButton.setButtonData(StringConstants.PROCEED);

            /*
             * Set the window title.
             */
            removeAltNameSelectionDialog.setTitle(Skins.Window.REMOVE_ALT_NAME_SELECTION.
                    getDisplayValue());

            /*
             * Register the remove alternate name selection dialog skin elements.
             */
            skins.registerWindowElements(Skins.Window.REMOVE_ALT_NAME_SELECTION, components);

            /*
             * Skin the remove alternate name selection dialog.
             */
            skins.skinMe(Skins.Window.REMOVE_ALT_NAME_SELECTION);

            /*
             * Open the remove alternate name selection dialog.
             */
            uiLogger.info("opening remove alternate name selection dialog");
            removeAltNameSelectionDialog.open(display);
        }
        else
        {
            Alert.alert(MessageType.INFO, StringConstants.ALERT_NO_ALTERNATE_NAMES, artistsWindow);
        }
    }
    
    /*
     * Display the current artist overrides for user review.
     */
    private void reviewOverrides (Display display)
    {
        uiLogger.trace("reviewOverrides: " + this.hashCode());

        /*
         * Get the BXML information for the artist overrides dialog, and
         * gather the list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        try
        {
            initializeArtistOverridesDialogBxmlVariables(components);
        }
        catch (IOException | SerializationException e)
        {
            MainWindow.logException(uiLogger, e);
            throw new InternalErrorException(true, e.getMessage());
        }
        
        /*
         * Listener to handle the done button press.
         */
        artistOverridesDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("artist overrides done button pressed");
                
                artistOverridesDialog.close();
            }
        });
        
        /*
         * Set multiple selection mode.
         */
        artistOverridesTreeView.setSelectMode(SelectMode.MULTI);
        
        /*
         * Initialize the top branch for displaying the overrides.
         */
        TreeBranch topBranch = new TreeBranch();
        
        /*
         * Gather the tree of artist overrides for display.
         */
        List<ArtistAlternateNameOverride> artistOverrides = userPrefs.getArtistOverrides();
        
        for (ArtistAlternateNameOverride override : artistOverrides)
        {
            String primaryArtist = override.getPrimaryArtist() + " (" 
                    + override.getOverrideType().toString() + ")";
            
            /*
             * Each primary is a branch.
             */
            TreeBranch primary = new TreeBranch(primaryArtist);
            topBranch.add(primary);
            
            /*
             * Process the alternate artists for this primary.
             */
            List<String> alternateArtists = override.getAlternateArtists();
            for (String alternateArtist : alternateArtists)
            {
                
                /*
                 * Each alternate is a node (leaf).
                 */
                TreeNode alternate = new TreeNode(alternateArtist);
                primary.add(alternate);
            }
        }

        /*
         * Set the artist overrides tree data and expand all branches.
         */
        artistOverridesTreeView.setTreeData(topBranch);
        artistOverridesTreeView.expandAll();
        
        /*
         * Set the preferred height of the scrollable tree view. This is apparently the secret that
         * prevents the scrollable area from growing in size to accommodate all the data instead
         * of actually scrolling. Determined via much trial and error since Pivot's documentation
         * is horrid.
         */
        artistOverridesTreeScrollPane.setPreferredHeight(InternalConstants.
                ARTIST_OVERRIDES_SCROLLPANE_HEIGHT);

        /*
         * Add widget texts.
         */
        artistOverridesDoneButton.setButtonData(StringConstants.DONE);
        artistOverridesManualLabel.setText(StringConstants.ARTISTS_REVIEW_OVERRIDES_MANUAL);
        artistOverridesAutomaticLabel.setText(StringConstants.ARTISTS_REVIEW_OVERRIDES_AUTOMATIC);

        /*
         * Set the window title.
         */
        artistOverridesDialog.setTitle(Skins.Window.ARTIST_OVERRIDES.getDisplayValue());

        /*
         * Register the artist overrides dialog skin elements.
         */
        skins.registerWindowElements(Skins.Window.ARTIST_OVERRIDES, components);

        /*
         * Skin the artist overrides dialog.
         */
        skins.skinMe(Skins.Window.ARTIST_OVERRIDES);

        /*
         * Open the artist overrides dialog.
         */
        uiLogger.info("opening review artist overrides dialog");
        artistOverridesDialog.open(display, artistsWindow);
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
     * Clean up selected artists. This is called from the cancel button handlers for the set 
     * and remove alternate name dialogs.
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
     * This is called from the set alternate name proceed button handler.
     */
    private void mergeAlternatesToPrimary ()
    {
        artistLogger.trace("mergeAlternatesToPrimary: " + this.hashCode());

        /*
         * Get the primary artist (the one that should be selected).
         */
        @SuppressWarnings("unchecked")
        HashMap<String, String> rowData = (HashMap<String, String>) setAltSelectTableView.getSelectedRow();
        
        /*
         * Proceed if a primary was selected.
         */
        if (rowData != null)
        { 
            String primaryArtist = rowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
            int primaryTableIndex = Integer.parseInt(rowData.get(MAP_PRIMARY_ARTIST));
            artistLogger.debug("primary artist '" + primaryArtist + "' selected, index " 
                    + primaryTableIndex);
            
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
                 * If we have an automatic artist override for the primary, remove the alternate from 
                 * the override.
                 */
                if (userPrefs.getArtistOverridePrimaryName(altArtist, 
                        ArtistAlternateNameOverride.OverrideType.AUTOMATIC) != null)
                {
                    userPrefs.removeArtistOverride(primaryArtist, altArtist);
                }
                
                /*
                 * Otherwise add an artist manual override.
                 */
                else
                {
                    userPrefs.addArtistOverride(primaryArtist, altArtist, 
                            ArtistAlternateNameOverride.OverrideType.MANUAL);
                }
            }
            
            /*
             * Get the current table data, containing all the artist rows.
             */
            @SuppressWarnings("unchecked") 
            List<HashMap<String, String>> artistRows = 
                    (List<HashMap<String, String>>) artistsTableView.getTableData();
            
            /*
             * Access primary artist objects and update the primary in the list of displayed artists.
             */
            ArtistCorrelator primaryArtistCorr = XMLHandler.findArtistCorrelator(primaryArtist);
            Artist primaryArtistObj = XMLHandler.getArtists().get(primaryArtistCorr.getArtistKey());
            
            HashMap<String, String> primaryRowData = primaryArtistObj.toDisplayMap();
            artistRows.update(primaryTableIndex, primaryRowData);
            
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
             * Update the preferences and window data.
             */
            updatePrefsAndWindowData(artistRows);
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
     * Remove one or more alternate artists from a selected primary.
     */
    private void removeAlternatesFromPrimary ()
    {
        
        /*
         * Get the current table data, containing all the artist rows.
         */
        @SuppressWarnings("unchecked") 
        List<HashMap<String, String>> artistRows = 
                (List<HashMap<String, String>>) artistsTableView.getTableData();
        
        /*
         * Get the selected alternate artists.
         */
        @SuppressWarnings("unchecked") 
        Sequence<HashMap<String, String>> selectedRows = 
                (Sequence<HashMap<String, String>>) removeAltSelectTableView.getSelectedRows();
        
        /*
         * Loop through the selected alternates.
         */
        boolean needWarning = false;
        for (int index = 0; index < selectedRows.getLength(); index++)
        {
            
            /*
             * Get the alternate artist name.
             */
            HashMap<String, String> artistRowData = selectedRows.get(index);
            String altArtist = artistRowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
            
            /*
             * We can't allow an alternate artist that has the same normalized name as the primary
             * to be removed. For example primary = "Beatles" and alternate = "The Beatles". Doing
             * so is chaos because the correlator list is sorted by normalized name, and depends on
             * such automatic alternates naturally finding the primary.
             * 
             * So detect this and set a flag to warn the user when we're done with all alternates,
             * then continue the loop without processing such an alternate.
             */
            ArtistNames primaryNames = new ArtistNames(primaryForRemoval);
            String primaryNormalized = primaryNames.normalizeName();
            ArtistNames altNames = new ArtistNames(altArtist);
            String altNormalized = altNames.normalizeName();
            
            if (primaryNormalized.equals(altNormalized))
            {
                needWarning = true;
                continue;
            }
            
            /*
             * Fix the database by transferring the alternate to become its own standalone artist.
             */
            XMLHandler.transferArtistFromPrimary(primaryForRemoval, altArtist);
            
            /*
             * If we have a manual artist override for the primary, remove the alternate from the override.
             */
            if (userPrefs.getArtistOverridePrimaryName(altArtist, 
                    ArtistAlternateNameOverride.OverrideType.MANUAL) != null)
            {
                userPrefs.removeArtistOverride(primaryForRemoval, altArtist);
            }
            
            /*
             * Otherwise add an artist automatic override.
             */
            else
            {
                userPrefs.addArtistOverride(primaryForRemoval, altArtist, 
                        ArtistAlternateNameOverride.OverrideType.AUTOMATIC);
            }
            
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
        }
        
        /*
         * Get the index of the primary in the artist rows.
         */
        int index = findPrimaryRowIndex(artistRows, primaryForRemoval);
        
        /*
         * Access primary artist objects and update the primary in the list of displayed artists.
         */
        ArtistCorrelator primaryArtistCorr = XMLHandler.findArtistCorrelator(primaryForRemoval);
        Artist primaryArtistObj = XMLHandler.getArtists().get(primaryArtistCorr.getArtistKey());
        
        HashMap<String, String> primaryRowData = primaryArtistObj.toDisplayMap();
        artistRows.update(index, primaryRowData);
        
        /*
         * Update the preferences and window data.
         */
        updatePrefsAndWindowData(artistRows);
        
        /*
         * Issue a warning if we found any alternates we couldn't process.
         */
        if (needWarning == true)
        {
            Alert.alert(MessageType.INFO, StringConstants.ALERT_CANT_REMOVE_ALTERNATE, artistsWindow);
        }
    }
    
    /*
     * Find the row in the table of artists that contains a given primary.
     */
    private int findPrimaryRowIndex (List<HashMap<String, String>> artistRows, String primaryArtist)
    {
        int index = 0;
        
        /*
         * This is ugly: we have no way to find primaries other than searching the entire list.
         */
        boolean foundPrimary = false;
        for (HashMap<String, String> rowData : artistRows)
        {
            String artistName = rowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
            if (artistName.equals(primaryArtist))
            {
                foundPrimary = true;
                break;
            }
            
            index++;
        }
        
        return (foundPrimary == true) ? index : -1;
    }
    
    /*
     * Update the preferences and main/artists window data when artist overrides have been changed.
     */
    private void updatePrefsAndWindowData (List<HashMap<String, String>> artistRows)
    {
        
        /*
         * Write the user preferences.
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
         * Update the artist count label.
         */
        numArtistsLabel.setText(StringConstants.ARTISTS_NUM_ARTISTS + XMLHandler.getNumberOfArtists());
        
        /*
         * Save the updated table data.
         */
        artistsTableView.setTableData(artistRows);
        
        /*
         * Resort the table according to the current sort. I'm assuming there is only one entry
         * (if any) in the sort dictionary. If there is no current sort, then sort by the name.
         */
        TableView.SortDictionary sort = artistsTableView.getSort();
        if (sort.getLength() > 0)
        {
            Dictionary.Pair<String, SortDirection> dict = artistsTableView.getSort().get(0);
            artistsTableView.setSort(dict.key, dict.value);
        }
        else
        {
            artistsTableView.setSort(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue(), 
                    SortDirection.ASCENDING);
        }
        
        /*
         * Clear the selection and repaint the table.
         */
        artistsTableView.clearSelection();
        artistsTableView.repaint(true);
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
     * Scroll to the alphanumeric name in the current column being sorted, based on 
     * the alpha bar button that was pressed.
     */
    private void scrollToName(String buttonData)
    {
    	String buttonID = buttonData.trim().toLowerCase();

    	/*
    	 * Get the artists table view data.
    	 */
        @SuppressWarnings("unchecked") 
        List<HashMap<String, String>> tableData = 
            (List<HashMap<String, String>>) artistsTableView.getTableData();
        
        /*
         * Loop through the table rows.
         */
        for (int i = 0; i < tableData.getLength(); i++)
        {
            HashMap<String, String> row = tableData.get(i);

            /*
             * If the row name starts with the character according to the button, select the
             * corresponding name in the table.
             */
            String name = row.get(tableSortColumnName).replaceAll("^(?i)The ", "");
            if (name.toLowerCase().startsWith(buttonID))
            {
            	artistsTableView.setSelectedIndex(i);
                break;
            }
        }
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
        alphaBorder = 
                (Border) windowSerializer.getNamespace().get("alphaBorder");
        components.add(alphaBorder);
        alphaBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("alphaBoxPane");
        components.add(alphaBoxPane);
        alphaLabel = 
                (Label) windowSerializer.getNamespace().get("alphaLabel");
        components.add(alphaLabel);
        alphaAButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaAButton");
        components.add(alphaAButton);
        alphaBButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaBButton");
        components.add(alphaBButton);
        alphaCButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaCButton");
        components.add(alphaCButton);
        alphaDButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaDButton");
        components.add(alphaDButton);
        alphaEButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaEButton");
        components.add(alphaEButton);
        alphaFButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaFButton");
        components.add(alphaFButton);
        alphaGButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaGButton");
        components.add(alphaGButton);
        alphaHButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaHButton");
        components.add(alphaHButton);
        alphaIButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaIButton");
        components.add(alphaIButton);
        alphaJButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaJButton");
        components.add(alphaJButton);
        alphaKButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaKButton");
        components.add(alphaKButton);
        alphaLButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaLButton");
        components.add(alphaLButton);
        alphaMButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaMButton");
        components.add(alphaMButton);
        alphaNButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaNButton");
        components.add(alphaNButton);
        alphaOButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaOButton");
        components.add(alphaOButton);
        alphaPButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaPButton");
        components.add(alphaPButton);
        alphaQButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaQButton");
        components.add(alphaQButton);
        alphaRButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaRButton");
        components.add(alphaRButton);
        alphaSButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaSButton");
        components.add(alphaSButton);
        alphaTButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaTButton");
        components.add(alphaTButton);
        alphaUButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaUButton");
        components.add(alphaUButton);
        alphaVButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaVButton");
        components.add(alphaVButton);
        alphaWButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaWButton");
        components.add(alphaWButton);
        alphaXButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaXButton");
        components.add(alphaXButton);
        alphaYButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaYButton");
        components.add(alphaYButton);
        alphaZButton = 
                (PushButton) windowSerializer.getNamespace().get("alphaZButton");
        components.add(alphaZButton);
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
        setAltNameButton = 
                (PushButton) windowSerializer.getNamespace().get("setAltNameButton");
        components.add(setAltNameButton);
        removeAltNameButton = 
                (PushButton) windowSerializer.getNamespace().get("removeAltNameButton");
        components.add(removeAltNameButton);
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
     * Initialize set alternate name selection dialog BXML variables and collect the components
     * to be skinned.
     */
    private void initializeSetAltNameSelectionDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeSetAltNameSelectionDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        setAltNameSelectionDialog = (Dialog) dialogSerializer.
                readObject(getClass().getResource("artistSetAltNameSelectionDialog.bxml"));
        
        setAltSelectLabelsBorder = 
                (Border) dialogSerializer.getNamespace().get("setAltSelectLabelsBorder");
        components.add(setAltSelectLabelsBorder);
        setAltSelectLabelsBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("setAltSelectLabelsBoxPane");
        components.add(setAltSelectLabelsBoxPane);
        
        /*
         * Don't add this to the components list because we want to set the text red, instead
         * of the skin color.
         */
        setAltSelectWarningLabel = 
                (Label) dialogSerializer.getNamespace().get("setAltSelectWarningLabel");
        
        setAltSelectInstructionsLabel = 
                (Label) dialogSerializer.getNamespace().get("setAltSelectInstructionsLabel");
        components.add(setAltSelectInstructionsLabel);
        setAltSelectTableBorder = 
                (Border) dialogSerializer.getNamespace().get("setAltSelectTableBorder");
        components.add(setAltSelectTableBorder);
        setAltSelectTableScrollPane = 
                (ScrollPane) dialogSerializer.getNamespace().get("setAltSelectTableScrollPane");
        components.add(setAltSelectTableScrollPane);
        setAltSelectTableView = 
                (TableView) dialogSerializer.getNamespace().get("setAltSelectTableView");
        components.add(setAltSelectTableView);

        /*
         * This doesn't need to be added to the components list because it's a 
         * subcomponent.
         */
        setAltSelectTableColumnArtist =
                (TableView.Column) dialogSerializer.getNamespace().get("setAltSelectTableColumnArtist");

        setAltSelectButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("setAltSelectButtonBorder");
        components.add(setAltSelectButtonBorder);
        setAltSelectButtonBoxPane =
                (BoxPane) dialogSerializer.getNamespace().get("setAltSelectButtonBoxPane");
        components.add(setAltSelectButtonBoxPane);
        setAltSelectCancelButton = 
                (PushButton) dialogSerializer.getNamespace().get("setAltSelectCancelButton");
        components.add(setAltSelectCancelButton);
        setAltSelectProceedButton = 
                (PushButton) dialogSerializer.getNamespace().get("setAltSelectProceedButton");
        components.add(setAltSelectProceedButton);
    }

    /*
     * Initialize remove alternate name selection dialog BXML variables and collect the components
     * to be skinned.
     */
    private void initializeRemoveAltNameSelectionDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeRemoveAltNameSelectionDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        removeAltNameSelectionDialog = (Dialog) dialogSerializer.
                readObject(getClass().getResource("artistRemoveAltNameSelectionDialog.bxml"));
        
        removeAltSelectLabelsBorder = 
                (Border) dialogSerializer.getNamespace().get("removeAltSelectLabelsBorder");
        components.add(removeAltSelectLabelsBorder);
        removeAltSelectLabelsBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("removeAltSelectLabelsBoxPane");
        components.add(removeAltSelectLabelsBoxPane);
        removeAltSelectInstructionsLabel = 
                (Label) dialogSerializer.getNamespace().get("removeAltSelectInstructionsLabel");
        components.add(removeAltSelectInstructionsLabel);
        removeAltSelectTableBorder = 
                (Border) dialogSerializer.getNamespace().get("removeAltSelectTableBorder");
        components.add(removeAltSelectTableBorder);
        removeAltSelectTableScrollPane = 
                (ScrollPane) dialogSerializer.getNamespace().get("removeAltSelectTableScrollPane");
        components.add(removeAltSelectTableScrollPane);
        removeAltSelectTableView = 
                (TableView) dialogSerializer.getNamespace().get("removeAltSelectTableView");
        components.add(removeAltSelectTableView);

        /*
         * This doesn't need to be added to the components list because it's a 
         * subcomponent.
         */
        removeAltSelectTableColumnArtist =
                (TableView.Column) dialogSerializer.getNamespace().get("removeAltSelectTableColumnArtist");

        removeAltSelectButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("removeAltSelectButtonBorder");
        components.add(removeAltSelectButtonBorder);
        removeAltSelectButtonBoxPane =
                (BoxPane) dialogSerializer.getNamespace().get("removeAltSelectButtonBoxPane");
        components.add(removeAltSelectButtonBoxPane);
        removeAltSelectCancelButton = 
                (PushButton) dialogSerializer.getNamespace().get("removeAltSelectCancelButton");
        components.add(removeAltSelectCancelButton);
        removeAltSelectProceedButton = 
                (PushButton) dialogSerializer.getNamespace().get("removeAltSelectProceedButton");
        components.add(removeAltSelectProceedButton);
    }

    /*
     * Initialize artist overrides dialog BXML variables and collect the static
     * components to be skinned.
     */
    private void initializeArtistOverridesDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeArtistOverridesDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        artistOverridesDialog = (Dialog) dialogSerializer.
                readObject(getClass().getResource("artistOverridesDialog.bxml"));

        artistOverridesLabelBorder = 
                (Border) dialogSerializer.getNamespace().get("artistOverridesLabelBorder");
        components.add(artistOverridesLabelBorder);
        artistOverridesLabelBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("artistOverridesLabelBoxPane");
        components.add(artistOverridesLabelBoxPane);
        artistOverridesManualLabel = 
                (Label) dialogSerializer.getNamespace().get("artistOverridesManualLabel");
        components.add(artistOverridesManualLabel);
        artistOverridesAutomaticLabel = 
                (Label) dialogSerializer.getNamespace().get("artistOverridesAutomaticLabel");
        components.add(artistOverridesAutomaticLabel);
        artistOverridesTreeBorder = 
                (Border) dialogSerializer.getNamespace().get("artistOverridesTreeBorder");
        components.add(artistOverridesTreeBorder);
        artistOverridesTreeScrollPane = 
                (ScrollPane) dialogSerializer.getNamespace().get("artistOverridesTreeScrollPane");
        components.add(artistOverridesTreeScrollPane);
        artistOverridesTreeView = 
                (TreeView) dialogSerializer.getNamespace().get("artistOverridesTreeView");
        components.add(artistOverridesTreeView);
        artistOverridesButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("artistOverridesButtonBorder");
        components.add(artistOverridesButtonBorder);
        artistOverridesButtonBoxPane =
                (BoxPane) dialogSerializer.getNamespace().get("artistOverridesButtonBoxPane");
        components.add(artistOverridesButtonBoxPane);
        artistOverridesDoneButton = 
                (PushButton) dialogSerializer.getNamespace().get("artistOverridesDoneButton");
        components.add(artistOverridesDoneButton);
    }
}
