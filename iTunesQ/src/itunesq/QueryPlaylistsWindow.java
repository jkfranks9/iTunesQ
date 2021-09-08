package itunesq;

import java.io.IOException;
import java.util.Comparator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.HashSet;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Set;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the query playlists window. This window allows the user to
 * compare a set of playlists. For example, this can be used to find
 * intersecting tracks on multiple playlists.
 * 
 * @author Jon
 *
 */
public class QueryPlaylistsWindow
{

    // ---------------- Private variables -----------------------------------

    private Window queryPlaylistsWindow = null;
    private Window familyPlaylistsWindow = null;
    private Skins skins = null;
    private int plusButtonYCoordinate = -1;
    private int minusButtonYCoordinate = -1;
    private Logger uiLogger = null;
    private Logger filterLogger = null;
    private boolean evaluateComparisonNeeded = true;
    private String queryStr = null;

    private Set<PlaylistComparisonTrack> allIDs;
    private Set<PlaylistComparisonTrack> someIDs;
    private Set<PlaylistComparisonTrack> oneIDs;
    private Set<PlaylistComparisonTrack> recursiveIDs;

    /*
     * BXML variables ...
     */
    
    /*
     * ... query playlists window.
     */
    @BXML private Border compareBorder = null;
    @BXML private BoxPane compareHolderBoxPane = null;
    @BXML private BoxPane compareBoxPane = null;
    @BXML private Label compareLabel = null;
    @BXML private TablePane compareTablePane = null;
    @BXML private BoxPane compareButtonsBoxPane = null;
    @BXML private PushButton showAllButton = null;
    @BXML private PushButton showSomeButton = null;
    @BXML private PushButton showOneButton = null;
    @BXML private Border recursiveCompareBorder = null;
    @BXML private BoxPane recursiveCompareHolderBoxPane = null;
    @BXML private BoxPane recursiveCheckboxesBoxPane = null;
    @BXML private Checkbox recursiveCompareAllCheckbox = null;
    @BXML private Checkbox recursiveExcludeBypassedCheckbox = null;
    @BXML private BoxPane recursiveCompareBoxPane = null;
    @BXML private Label recursiveCompareLabel = null;
    @BXML private TablePane recursiveCompareTablePane = null;
    @BXML private BoxPane recursiveCompareButtonsBoxPane = null;
    @BXML private PushButton showButton = null;
    @BXML private Border familyBorder = null;
    @BXML private FillPane familyFillPane = null;
    @BXML private BoxPane familyBoxPane = null;
    @BXML private Label familyLabel = null;
    @BXML private TextInput familyTextInput = null;
    @BXML private BoxPane familyButtonsBoxPane = null;
    @BXML private PushButton familyPlaylistsButton = null;
    @BXML private Checkbox familyExcludeBypassedCheckbox = null;
    @BXML private PushButton familyTracksButton = null;
    @BXML private Border actionBorder = null;
    @BXML private BoxPane actionBoxPane = null;
    @BXML private PushButton queryDoneButton = null;

    /*
     * ... family expansion playlists window.
     */
    @BXML private Border infoBorder = null;
    @BXML private FillPane infoFillPane = null;
    @BXML private Label numPlaylistsLabel = null;
    @BXML private Border playlistsBorder = null;
    @BXML private TableView playlistsTableView = null;
    @BXML private TableViewHeader playlistsTableViewHeader = null;
    @BXML private TableView playlistTracksTableView = null;
    @BXML private TableViewHeader playlistTracksTableViewHeader = null;
    @BXML private Border familyActionBorder = null;
    @BXML private BoxPane familyActionBoxPane = null;
    @BXML private PushButton playlistsDoneButton = null;

    /*
     * Type of playlist comparison.
     */
    private enum CompareType
    {
        ALL, SOME, ONE, RECURSIVE;
    }

    /*
     * Type of playlist table.
     */
    private enum TableType
    {
        COMPARE, RECURSIVE_COMPARE;
    }

    /**
     * Class constructor.
     */
    public QueryPlaylistsWindow()
    {

        /*
         * Create a UI logger.
         */
        String className = getClass().getSimpleName();
        uiLogger = (Logger) LoggerFactory.getLogger(className + "_UI");

        /*
         * Create a filter logger. I consider this class a form of filtering.
         */
        filterLogger = (Logger) LoggerFactory.getLogger(className + "_Filter");

        /*
         * Get the logging object singleton.
         */
        Logging logging = Logging.getInstance();

        /*
         * Register our loggers.
         */
        logging.registerLogger(Logging.Dimension.UI, uiLogger);
        logging.registerLogger(Logging.Dimension.FILTER, filterLogger);

        /*
         * Initialize variables.
         */
        skins = Skins.getInstance();

        uiLogger.trace("QueryPlaylistsWindow constructor: " + this.hashCode());
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Displays the query playlists in a new window.
     * 
     * @param display display object for managing windows
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayQueryPlaylists(Display display) 
            throws IOException, SerializationException
    {
        uiLogger.trace("displayQueryPlaylists: " + this.hashCode());

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        /*
         * Get the BXML information for the query playlists window, and generate
         * the list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeBxmlVariables(components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers();

        /*
         * Add the initial query playlist rows. We add 2 rows because we need at
         * least that many to compare. This populates the component list with
         * table row components.
         */
        for (int i = 0; i < 2; i++)
        {
            TablePane.Row newRow = createPlaylistTableRow(TableType.COMPARE, components);
            compareTablePane.getRows().add(newRow);
        }

        /*
         * Since we start with only 2 rows, disable the 'some' button. It gets
         * enabled only if more rows are added.
         */
        showSomeButton.setEnabled(false);

        /*
         * Disable the recursive compare exclude bypassed checkbox. It only gets enabled
         * if the associated compare all checkbox is checked.
         */
        recursiveExcludeBypassedCheckbox.setEnabled(false);

        /*
         * Indicate we need to evaluate the comparison. Since that process is
         * rather involved, we don't want to do the evaluation each time one of
         * the buttons is pressed, unless some change was made to the comparison
         * objects, such as changing a playlist name or adding or removing a
         * row. We start out here with the switch true, then set it false once
         * the evaluation process is complete. If a change is made between
         * pressing buttons, the switch is set true again.
         */
        evaluateComparisonNeeded = true;

        /*
         * Add the initial recursive compare query playlist rows. We add 2 rows because we need at
         * least that many to compare. This populates the component list with
         * table row components.
         */
        for (int i = 0; i < 2; i++)
        {
            TablePane.Row newRow = createPlaylistTableRow(TableType.RECURSIVE_COMPARE, components);
            recursiveCompareTablePane.getRows().add(newRow);
        }

        /*
         * Add widget texts.
         */
        compareLabel.setText(StringConstants.QUERY_PLAYLIST_COMPARE_BORDER);
        showAllButton.setButtonData(StringConstants.QUERY_PLAYLIST_SHOW_ALL_BUTTON);
        showAllButton.setTooltipText(StringConstants.QUERY_PLAYLIST_SHOW_ALL_BUTTON_TIP);
        showAllButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        showSomeButton.setButtonData(StringConstants.QUERY_PLAYLIST_SHOW_SOME_BUTTON);
        showSomeButton.setTooltipText(StringConstants.QUERY_PLAYLIST_SHOW_SOME_BUTTON_TIP);
        showSomeButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        showOneButton.setButtonData(StringConstants.QUERY_PLAYLIST_SHOW_ONE_BUTTON);
        showOneButton.setTooltipText(StringConstants.QUERY_PLAYLIST_SHOW_ONE_BUTTON_TIP);
        showOneButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        recursiveCompareLabel.setText(StringConstants.QUERY_PLAYLIST_RECURSIVE_COMPARE_BORDER);
        recursiveCompareAllCheckbox.setButtonData(StringConstants.QUERY_PLAYLIST_RECURSIVE_COMPARE_ALL);
        recursiveCompareAllCheckbox.setTooltipText(StringConstants.QUERY_PLAYLIST_RECURSIVE_COMPARE_ALL_TIP);
        recursiveCompareAllCheckbox.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        recursiveExcludeBypassedCheckbox.setButtonData(StringConstants.EXCLUDE_BYPASSED);
        recursiveExcludeBypassedCheckbox.setTooltipText(StringConstants.EXCLUDE_BYPASSED_TIP);
        recursiveExcludeBypassedCheckbox.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        showButton.setButtonData(StringConstants.QUERY_PLAYLIST_SHOW_BUTTON);
        showButton.setTooltipText(StringConstants.QUERY_PLAYLIST_SHOW_BUTTON_TIP);
        showButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        familyLabel.setText(StringConstants.QUERY_PLAYLIST_FAMILY);
        familyPlaylistsButton.setButtonData(StringConstants.QUERY_PLAYLIST_FAMILY_PLAYLISTS);
        familyPlaylistsButton.setTooltipText(StringConstants.QUERY_PLAYLIST_FAMILY_PLAYLISTS_TIP);
        familyPlaylistsButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        familyExcludeBypassedCheckbox.setButtonData(StringConstants.EXCLUDE_BYPASSED);
        familyExcludeBypassedCheckbox.setTooltipText(StringConstants.EXCLUDE_BYPASSED_TIP);
        familyExcludeBypassedCheckbox.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        familyTracksButton.setButtonData(StringConstants.QUERY_PLAYLIST_FAMILY_TRACKS);
        familyTracksButton.setTooltipText(StringConstants.QUERY_PLAYLIST_FAMILY_TRACKS_TIP);
        familyTracksButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        queryDoneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        queryPlaylistsWindow.setTitle(Skins.Window.QUERY_PLAYLISTS.getDisplayValue());

        /*
         * Now register the query playlists window skin elements.
         */
        skins.registerWindowElements(Skins.Window.QUERY_PLAYLISTS, components);

        /*
         * Skin the query playlists window.
         */
        skins.skinMe(Skins.Window.QUERY_PLAYLISTS);

        /*
         * Push the skinned window onto the skins window stack. It gets popped
         * from our done button press handler.
         */
        skins.pushSkinnedWindow(Skins.Window.QUERY_PLAYLISTS);

        /*
         * Open the query playlists window.
         */
        uiLogger.info("opening query playlists window");
        queryPlaylistsWindow.open(display);

        /*
         * Request focus for the table pane, so that the user can type in the
         * first row.
         */
        compareTablePane.requestFocus();
    }

    /**
     * Gets the list of playlists table data for a family playlist display.
     * 
     * @return list of playlists table data
     */
    @SuppressWarnings("unchecked")
    public List<HashMap<String, String>> getFamilyPlaylistData()
    {
        return (List<HashMap<String, String>>) playlistsTableView.getTableData();
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers for the query playlists window.
     */
    private void createEventHandlers()
    {
        uiLogger.trace("createEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the show all button press.
         */
        showAllButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("show all button pressed");

                Display display = button.getDisplay();

                /*
                 * Evaluate the comparison of the specified playlists if needed.
                 */
                boolean good2Go = true;
                if (evaluateComparisonNeeded == true)
                {

                    /*
                     * Collect the playlists from the window.
                     */
                    List<String> playlists = collectPlaylists(TableType.COMPARE);
                    
                    /*
                     * Evaluate the comparison.
                     */
                    good2Go = evaluateComparison(playlists);
                }

                /*
                 * Display tracks included in all specified playlists.
                 */
                if (good2Go == true)
                {
                    try
                    {
                        displayComparedPlaylistTracks(display, CompareType.ALL);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
            }
        });

        /*
         * Listener to handle the show some button press.
         */
        showSomeButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("show some button pressed");

                Display display = button.getDisplay();

                /*
                 * Evaluate the comparison of the specified playlists if needed.
                 */
                boolean good2Go = true;
                if (evaluateComparisonNeeded == true)
                {

                    /*
                     * Collect the playlists from the window.
                     */
                    List<String> playlists = collectPlaylists(TableType.COMPARE);
                    
                    /*
                     * Evaluate the comparison.
                     */
                    good2Go = evaluateComparison(playlists);
                }

                /*
                 * Display tracks included in some specified playlists.
                 */
                if (good2Go == true)
                {
                    try
                    {
                        displayComparedPlaylistTracks(display, CompareType.SOME);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
            }
        });

        /*
         * Listener to handle the show one button press.
         */
        showOneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("show one button pressed");

                Display display = button.getDisplay();

                /*
                 * Evaluate the comparison of the specified playlists if needed.
                 */
                boolean good2Go = true;
                if (evaluateComparisonNeeded == true)
                {

                    /*
                     * Collect the playlists from the window.
                     */
                    List<String> playlists = collectPlaylists(TableType.COMPARE);
                    
                    /*
                     * Evaluate the comparison.
                     */
                    good2Go = evaluateComparison(playlists);
                }

                /*
                 * Display tracks included in one of the specified playlists.
                 */
                if (good2Go == true)
                {
                    try
                    {
                        displayComparedPlaylistTracks(display, CompareType.ONE);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
            }
        });

        /*
         * Listener to handle the compare all checkbox.
         */
        recursiveCompareAllCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
            	
            	/*
            	 * Enable or disable the exclude bypassed checkbox.
            	 */
                if (button.isSelected())
                {
                	recursiveExcludeBypassedCheckbox.setEnabled(true);
                	recursiveExcludeBypassedCheckbox.setSelected(true);
                }
                else
                {
                	recursiveExcludeBypassedCheckbox.setEnabled(false);
                	recursiveExcludeBypassedCheckbox.setSelected(false);
                }
                
                /*
                 * Set the evaluate comparison switch true since we've changed the set of playlists.
                 */
                evaluateComparisonNeeded = true;
            }
        });

        /*
         * Listener to handle the exclude bypassed checkbox.
         */
        recursiveExcludeBypassedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                
                /*
                 * Set the evaluate comparison switch true since we've changed the set of playlists.
                 */
                evaluateComparisonNeeded = true;
            }
        });

        /*
         * Listener to handle the show button press.
         */
        showButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("show button pressed");

                Display display = button.getDisplay();

                /*
                 * Evaluate the comparison of the specified playlists if needed.
                 */
                boolean good2Go = true;
                if (evaluateComparisonNeeded == true)
                {

                    /*
                     * Collect the playlists from the window.
                     */
                    List<String> playlists = collectPlaylists(TableType.RECURSIVE_COMPARE);
                    
                    /*
                     * We need to build our own query string ... the one built by evaluateComparison() only
                     * includes the final two playlists in the below algorithm.
                     */
                    StringBuilder query = new StringBuilder();
                    boolean firstTime = true;
                    for (String playlistName : playlists)
                    {
                        if (firstTime ==false)
                        {
                            query.append(" + ");
                        }                    	
                        query.append(playlistName);
                        firstTime = false;
                    }

                    /*
                     * Execute the recursive compare.
                     */
                    recursiveIDs = new HashSet<PlaylistComparisonTrack>();                    
                    good2Go = recursiveCompare(playlists);

                    /*
                     * Save the query string constructed earlier.
                     */
                    queryStr = query.toString();
                }

                /*
                 * Display tracks included in all specified playlists.
                 */
                if (good2Go == true)
                {
                    try
                    {
                        displayComparedPlaylistTracks(display, CompareType.RECURSIVE);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
            }
        });

        /*
         * Listener to handle the family expansion to playlists button press.
         */
        familyPlaylistsButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("expand family to playlists button pressed");

                Display display = button.getDisplay();

                /*
                 * Get the entered playlist name.
                 */
                String playlistName = familyTextInput.getText();
                
                /*
                 * Verify a playlist has been entered.
                 */
                if (playlistName != null && !playlistName.isEmpty())
                {

                    /*
                     * Expand the playlist family into a playlists display.
                     */
                    try
                    {
                        generateFamilyPlaylistResults(display, playlistName, 
                                familyExcludeBypassedCheckbox.isSelected());
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
                else
                {
                    Alert.alert(MessageType.ERROR, StringConstants.ALERT_NO_FAMILY_PLAYLIST,
                            queryPlaylistsWindow);
                }
            }
        });

        /*
         * Listener to handle the family expansion to tracks button press.
         */
        familyTracksButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("expand family to tracks button pressed");

                Display display = button.getDisplay();

                /*
                 * Get the entered playlist name.
                 */
                String playlistName = familyTextInput.getText();
                
                /*
                 * Verify a playlist has been entered.
                 */
                if (playlistName != null && !playlistName.isEmpty())
                {

                    /*
                     * Expand the playlist family into a tracks display.
                     */
                    try
                    {
                        generateFamilyTrackResults(display, playlistName);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
                else
                {
                    Alert.alert(MessageType.ERROR, StringConstants.ALERT_NO_FAMILY_PLAYLIST,
                            queryPlaylistsWindow);
                }
            }
        });

        /*
         * Listener to handle the done button press.
         */
        queryDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("done button pressed");

                /*
                 * Close the window.
                 */
                queryPlaylistsWindow.close();

                /*
                 * Pop the window off the skins window stack.
                 */
                skins.popSkinnedWindow();
            }
        });

        /*
         * Add a text input listener to the family playlist text box so we can call the typing assistant
         * to fill in the playlist name as soon as enough characters are entered.
         */
        familyTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
        {
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
                Utilities.typingAssistant(textInput, XMLHandler.getPlaylistNames(), textInput.getText(),
                        Filter.Operator.IS);
            }
        });
    }

    /*
     * Create and add a playlist row.
     * 
     * NOTE: This method populates the input list of components with the
     * components of a row.
     */
    private TablePane.Row createPlaylistTableRow(TableType tableType, List<Component> components)
    {
        uiLogger.trace("createPlaylistTableRow: " + this.hashCode());

        /*
         * New table row object.
         */
        TablePane.Row newRow = new TablePane.Row();

        /*
         * Create the label for the text box.
         */
        Label textLabel = new Label();
        textLabel.setText(StringConstants.PLAYLIST_NAME);

        /*
         * Create the text input box.
         */
        TextInput text = new TextInput();

        /*
         * Add a text input listener so we can call the typing assistant to fill
         * in the playlist name as soon as enough characters are entered.
         * 
         * Also, set the evaluate comparison switch true if the text is mucked
         * with in any way.
         */
        text.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
        {
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
                evaluateComparisonNeeded = true;

                Utilities.typingAssistant(textInput, XMLHandler.getPlaylistNames(), textInput.getText(),
                        Filter.Operator.IS);
            }

            @Override
            public void textRemoved(TextInput textInput, int index, int count)
            {
                evaluateComparisonNeeded = true;
            }

            @Override
            public void textChanged(TextInput textInput)
            {
                evaluateComparisonNeeded = true;
            }
        });

        /*
         * Create the include children checkbox for a recursive compare.
         */
        Checkbox includeChildren = null;
        if (tableType == TableType.RECURSIVE_COMPARE)
        {
        	includeChildren = new Checkbox(StringConstants.BYPASS_INCLUDE);
        	includeChildren.setTooltipText(StringConstants.QUERY_PLAYLIST_BYPASS_INCLUDE_TIP);
        	includeChildren.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        }

        /*
         * Create the set of buttons:
         * 
         * '+' = insert a new row after this one 
         * '-' = delete this row
         */
        PushButton plusButton = new PushButton();
        plusButton.setButtonData("+");
        plusButton.setTooltipText(StringConstants.QUERY_PLAYLIST_PLUS_BUTTON);
        plusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        PushButton minusButton = new PushButton();
        minusButton.setButtonData("-");
        minusButton.setTooltipText(StringConstants.QUERY_PLAYLIST_MINUS_BUTTON);
        minusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * We need the ability to insert and remove playlist rows based on the
         * specific table rows where the + and - buttons exist. But this a bit
         * of a complex dance. The buttonPressed() method does not provide a
         * means to know on which row the button exists. So we need a
         * ComponentMouseButtonListener to get that function. However, based on
         * the hierarchy of listener calls, and the bubbling up of events
         * through the component hierarchy, the buttonPressed() method gets
         * called before the mouseClick() method. :(
         * 
         * So the buttonPressed() method just records the Y coordinate of the
         * button in a class variable, which is relative to its parent
         * component, which is the TablePane.
         * 
         * Then the mouseClick() method gets the parent, uses the recorded Y
         * coordinate to get the table row, then uses that information to insert
         * a new row, or delete the current one.
         */

        /*
         * Mouse click listener for the + button.
         */
        plusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
                evaluateComparisonNeeded = true;

                Object parent = component.getParent();
                if (parent instanceof TablePane)
                {
                    TablePane tablePane = (TablePane) parent;
                    String tableName = tablePane.getName();
                    int playlistRowIndex = tablePane.getRowAt(plusButtonYCoordinate);
                    uiLogger.info("plus button pressed for playlist index " + playlistRowIndex + " on table " + tableName);

                    /*
                     * Add the table row and collect the components that need to
                     * be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                    TablePane.Row tableRow = createPlaylistTableRow(tableType, rowComponents);
                    
                    /*
                     * Insert the row into the proper table, based on the name.
                     */
                    switch (tableName)
                    {
                    case "compareTablePane":
                        compareTablePane.getRows().insert(tableRow, playlistRowIndex + 1);

                        /*
                         * Enable or disable the 'some' button based on the current
                         * number of table rows.
                         */
                        int numRows = tablePane.getRows().getLength();
                        if (numRows >= 3)
                        {
                            showSomeButton.setEnabled(true);
                        }
                        else
                        {
                            showSomeButton.setEnabled(false);
                        }
                        break;
                        
                    case "recursiveCompareTablePane":
                        recursiveCompareTablePane.getRows().insert(tableRow, playlistRowIndex + 1);
                        break;

                    default:
                        throw new InternalErrorException(true, "unknown table name '" + tableName + "'");
                    }

                    /*
                     * Request focus for the text input on the newly added row.
                     */
                    for (Component rowComponent : tableRow)
                    {
                        if (rowComponent instanceof TextInput)
                        {
                            rowComponent.requestFocus();
                        }
                    }

                    /*
                     * Register the new components and skin them.
                     */
                    Map<Skins.Element, List<Component>> windowElements = 
                            skins.registerDynamicWindowElements(Skins.Window.QUERY_PLAYLISTS, rowComponents);
                    skins.skinMe(Skins.Window.QUERY_PLAYLISTS, windowElements);

                    queryPlaylistsWindow.repaint();
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
                evaluateComparisonNeeded = true;

                Object parent = component.getParent();
                if (parent instanceof TablePane)
                {
                    TablePane tablePane = (TablePane) parent;
                    String tableName = tablePane.getName();
                    int playlistRowIndex = tablePane.getRowAt(minusButtonYCoordinate);
                    uiLogger.info("minus button pressed for playlist index " + playlistRowIndex + " on table " + tableName);

                    /*
                     * Get the number of rows and make sure we don't go below
                     * two rows.
                     */
                    int numRows = tablePane.getRows().getLength();
                    if (numRows <= 2)
                    {
                        Alert.alert(MessageType.ERROR, StringConstants.ALERT_PLAYLIST_TOO_FEW_ROWS,
                                component.getWindow());
                    }
                    else
                    {

                        /*
                         * Remove the table row from the proper table, based on the name..
                         */
                        switch (tableName)
                        {
                        case "compareTablePane":
                            compareTablePane.getRows().remove(playlistRowIndex, 1);

                            /*
                             * Enable or disable the 'some' button based on the
                             * current number of table rows.
                             */
                            if (numRows >= 3)
                            {
                                showSomeButton.setEnabled(true);
                            }
                            else
                            {
                                showSomeButton.setEnabled(false);
                            }
                            break;
                            
                        case "recursiveCompareTablePane":
                            recursiveCompareTablePane.getRows().remove(playlistRowIndex, 1);
                            break;

                        default:
                            throw new InternalErrorException(true, "unknown table name '" + tableName + "'");
                        }

                        queryPlaylistsWindow.repaint();
                    }
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
        if (tableType == TableType.RECURSIVE_COMPARE)
        {
            newRow.add(includeChildren);
            components.add(includeChildren);        	
        }
        newRow.add(plusButton);
        components.add(plusButton);
        newRow.add(minusButton);
        components.add(minusButton);

        return newRow;
    }
    
    /*
     * Perform the recursive compare. This doesn't mean this method is recursive, it 
     * just refers to iterating by comparing two playlists at a time.
     */
    private boolean recursiveCompare(List<String> playlists)
    {
        boolean good2Go = true;
        
        /*
         * Loop through the set of playlists and compare recursively. Stop when only one
         * playlist remains.
         */
        int numPlaylists = playlists.getLength();
        filterLogger.debug("recursively comparing " + numPlaylists + " playlists");
        while (numPlaylists > 1)
        {
        	
        	/*
        	 * Remove a playlist from the set, designated as the top.
        	 */
        	String top = playlists.remove(0, 1).get(0);
        	numPlaylists = playlists.getLength();
        	filterLogger.debug("removed top playlist '" + top + "', num = " + numPlaylists);
            
        	/*
        	 * Now compare the top to all other playlists, one at a time.
        	 */
            for (String playlist : playlists)
            {
                List<String> compareList = new ArrayList<String>();
                compareList.add(top);
                compareList.add(playlist);

                good2Go = evaluateComparison(compareList);
                
                /*
                 * Add all discovered tracks (if any) to the recursive compare list.
                 */
                filterLogger.debug("discovered " + allIDs.getCount() + " tracks from '" + playlist + "'");
                for (PlaylistComparisonTrack pcTrack : allIDs)
                {
                	if (findTrackID(recursiveIDs, pcTrack.getTrackID()) == null)
                	{
                		if (filterLogger.isDebugEnabled())
                		{
                            Integer trackIndex = XMLHandler.getTracksMap().get(pcTrack.getTrackID());
                            Track track = XMLHandler.getTracks().get(trackIndex);
                            filterLogger.debug("adding track '" + track.getName() + "' to recursive IDs");
                		}
                		recursiveIDs.add(pcTrack);
                	}
                }                  	
            }
        }
        
    	return good2Go;
    }

    /*
     * Collect the entered playlists.
     */
    private List<String> collectPlaylists(TableType tableType)
    {
        filterLogger.trace("collectPlaylists: " + this.hashCode());

        /*
         * Iterate through the playlist table rows.
         */
        List<String> playlists = new ArrayList<String>();
        
        TablePane.RowSequence rows = null;
        switch (tableType)
        {
        case COMPARE:
            rows = compareTablePane.getRows();
        	playlists = collectIncludedPlaylists(rows, tableType);
        	break;
        	
        case RECURSIVE_COMPARE:
            rows = recursiveCompareTablePane.getRows();
            
            if (recursiveCompareAllCheckbox.isSelected())
            {
            	playlists = collectAllNonexcludedPlaylists(rows);
            }
            else
            {
            	playlists = collectIncludedPlaylists(rows, tableType);
            }
        	break;
        	
        default:
            throw new InternalErrorException(true, "unknown table type '" + tableType + "'");
        }

        filterLogger.debug("found " + playlists.getLength() + " playlists");
        return playlists;
    }
    
    /*
     * Collect playlists that are included in the selection. This applies to:
     * 
     * - The normal compare process
     * - The recursive compare process when the all checkbox is not checked
     */
    private List<String> collectIncludedPlaylists(TablePane.RowSequence rows, TableType tableType)
    {
        filterLogger.trace("collectIncludedPlaylists: " + this.hashCode());

        /*
         * Indexes into the row elements.
         * 
         * IMPORTANT: These must match the design of the row. See
         * queryPlaylistsWindow.bxml for the column definition, and
         * createPlaylistTableRow() for the logic to create a row.
         */
        final int textIndex = 1;
        final int includeChildrenIndex = 2;
        
        List<String> playlists = new ArrayList<String>();
        for (TablePane.Row row : rows)
        {

            /*
             * Get the text input.
             */
            TextInput text = (TextInput) row.get(textIndex);

            /*
             * Add this playlist to the collection.
             */
            String playlistName = text.getText();
            playlists.add(playlistName);
            filterLogger.debug("added playlist '" + playlistName + "' (name match)");
            
            /*
             * Handle a recursive compare.
             */
            if (tableType == TableType.RECURSIVE_COMPARE)
            {
                
                /*
                 * Get the include children checkbox.
                 */
            	Checkbox includeChildren = (Checkbox) row.get(includeChildrenIndex);
                
                /*
                 * If we're to include all children then we have to search through all playlists.
                 */
                if (includeChildren.isSelected())
                {
                	
                	/*
                	 * Get the ID of the entered playlist.
                	 */
                    Playlist selectedPlaylistObj = XMLHandler.getPlaylists().get(XMLHandler.getPlaylistsMap().get(playlistName));
                    String selectedID = selectedPlaylistObj.getPersistentID();
                    
                    /*
                     * Loop through all playlists.
                     */
                    Map<String, Playlist> allPlaylists = XMLHandler.getPlaylists();                
                    for (String playlistKey : allPlaylists)
                    {
                        Playlist playlistObj = allPlaylists.get(playlistKey);
                        
                        /*
                         * If this is a child of the parent, add it to the collection.
                         */
                        String parentID = playlistObj.getParentPersistentID();
                        if (parentID != null && parentID.equals(selectedID))
                        {
                        	String childName = playlistObj.getName();
                            playlists.add(childName);
                            filterLogger.debug("added playlist '" + childName + "' (child match)");
                        }
                    }            	
                }
            }
        }
    	
        return playlists;
    }
    
    /*
     * Collect all playlists except those that are excluded in the selection. This applies to:
     * 
     * - The recursive compare process when the all checkbox is checked
     */
    private List<String> collectAllNonexcludedPlaylists(TablePane.RowSequence rows)
    {
        filterLogger.trace("collectAllNonexcludedPlaylists: " + this.hashCode());

        /*
         * Indexes into the row elements.
         * 
         * IMPORTANT: These must match the design of the row. See
         * queryPlaylistsWindow.bxml for the column definition, and
         * createPlaylistTableRow() for the logic to create a row.
         */
        final int textIndex = 1;
        final int includeChildrenIndex = 2;
        
        /*
         * Walk through all playlists.
         */
        List<String> playlists = new ArrayList<String>();
        Map<String, Playlist> allPlaylists = XMLHandler.getPlaylists();
        
        for (String playlistKey : allPlaylists)
        {
            Playlist playlistObj = allPlaylists.get(playlistKey);
            String playlistName = playlistObj.getName();
            
            /*
             * Ignore bypassed playlists if requested.
             */
            if (recursiveExcludeBypassedCheckbox.isSelected() && playlistObj.getBypassed() == true)
            {
                filterLogger.debug("ignoring bypassed playlist '" + playlistName + "'");
            	continue;
            }
            
            /*
             * Walk through the selection list that was entered, to see if this playlist is excluded or not.
             */
            boolean excluded = false;
            
            for (TablePane.Row row : rows)
            {

                /*
                 * Get the text input.
                 */
                TextInput text = (TextInput) row.get(textIndex);
                
                /*
                 * Get the include children checkbox.
                 */
                Checkbox includeChildren = (Checkbox) row.get(includeChildrenIndex);

                /*
                 * Check if this playlist is excluded.
                 */
                String selectedName = text.getText();
                if (playlistName.equals(selectedName))
                {
                    filterLogger.debug("excluding playlist '" + playlistName + "' (name match)");
                	excluded = true;
                	break;
                }
                
                /*
                 * It's not, but continue if we're to check children playlists.
                 */
                else if (includeChildren.isSelected())
                {
                	
                	/*
                	 * Get the playlist object for the current selection in the inner loop.
                	 */
                	Playlist selectedPlaylistObj = XMLHandler.getPlaylists().get(XMLHandler.getPlaylistsMap().get(selectedName));
                	
                	/*
                	 * Get the parent ID of the current playlist in the outer loop.
                	 */
                	Playlist tmpPlaylistObj = playlistObj;
                	String parentID = null;
                	while ((parentID = tmpPlaylistObj.getParentPersistentID()) != null)
                	{

                		/*
                		 * Check if this playlist is excluded via the include children checkbox.
                		 */
                		if (parentID.equals(selectedPlaylistObj.getPersistentID()))
                		{
                			filterLogger.debug("excluding playlist '" + playlistName + "' (child match)");
                			excluded = true;
                			break;
                		}
                		
                		tmpPlaylistObj = XMLHandler.getPlaylists().get(parentID);
                	}
                }
            }
            
            /*
             * Add the playlist unless it's excluded.
             */
            if (excluded == false)
            {
            	playlists.add(playlistName);
            }
        }
    	
        return playlists;
    }

    /*
     * Evaluate the set of playlists to be compared.
     * 
     * We only do this once per set of playlists, creating all of the sets of
     * track IDs at once. The different display buttons then just need to
     * display the results (see displayComparedPlaylistTracks method). However,
     * if any change is made to the set of playlists, a switch is set to run
     * this method again to re-evaluate the comparison.
     */
    private boolean evaluateComparison(List<String> playlists)
    {
        filterLogger.trace("evaluateComparison: " + this.hashCode());

        boolean playlistsValid = true;

        /*
         * Initialize lists for all compare types.
         */
        allIDs = new HashSet<PlaylistComparisonTrack>();
        someIDs = new HashSet<PlaylistComparisonTrack>();
        oneIDs = new HashSet<PlaylistComparisonTrack>();

        /*
         * Walk through the specified playlists.
         * 
         * We make 2 passes to establish all compare lists. In the first pass,
         * we walk through the list of track IDs for all playlists, and create
         * the 'one' list and a potential 'some' list.
         * 
         * We also create the query string during the first pass. This is passed
         * to TracksWindow when the tracks are displayed.
         */
        int playlistLoopIndex = 0;
        StringBuilder query = new StringBuilder();

        for (String playlistName : playlists)
        {
            filterLogger.trace("playlist index: " + playlistLoopIndex);

            /*
             * Add this playlist to the query string, along with a "+" separator
             * for all but the first.
             */
            if (playlistLoopIndex != 0)
            {
                query.append(" + ");
            }
            query.append(playlistName);

            /*
             * Get the playlist object.
             */
            String playlistID = XMLHandler.getPlaylistsMap().get(playlistName);

            if (playlistID == null)
            {
                playlistsValid = false;
                Alert.alert(MessageType.ERROR, StringConstants.ALERT_PLAYLIST_INVALID_NAME_1 + playlistName
                        + StringConstants.ALERT_PLAYLIST_INVALID_NAME_2, queryPlaylistsWindow);
                break;
            }

            Playlist playlist = XMLHandler.getPlaylists().get(playlistID);

            /*
             * Get the list of track IDs for this playlist.
             */
            List<Integer> trackIDs = playlist.getTracks();

            /*
             * Walk through the list of track IDs.
             */
            for (Integer trackID : trackIDs)
            {
                filterLogger.trace("track ID: " + trackID);
                PlaylistComparisonTrack existingTrack;
                PlaylistComparisonTrack newTrack;

                /*
                 * Everything in the first playlist goes unconditionally on the
                 * 'one' list, by definition.
                 */
                if (playlistLoopIndex == 0)
                {
                    newTrack = new PlaylistComparisonTrack();
                    newTrack.setTrackID(trackID);
                    newTrack.setPlaylistCount(1);

                    filterLogger.trace("add to 'one' ID: " + trackID);
                    oneIDs.add(newTrack);
                    continue;
                }

                /*
                 * If this ID already exists in the 'some' list, then bump its
                 * playlist count. Otherwise, keep checking.
                 * 
                 * NOTE: This can only happen once we reach the third playlist,
                 * if it exists, so bypass the check if the loop index is too
                 * small for that.
                 */
                if (playlistLoopIndex >= 2 && (existingTrack = findTrackID(someIDs, trackID)) != null)
                {
                    Integer updatedCount = existingTrack.getPlaylistCount() + 1;
                    filterLogger.trace("bump 'some' count to " + updatedCount + " for ID: " + trackID);
                    existingTrack.setPlaylistCount(updatedCount);
                }
                else
                {

                    /*
                     * If this ID exists in the 'one' list, then remove it and
                     * add it instead to the 'some' list (and bump its count).
                     */
                    if ((existingTrack = findTrackID(oneIDs, trackID)) != null)
                    {
                        Integer updatedCount = existingTrack.getPlaylistCount() + 1;
                        filterLogger.trace("move to 'some' ID: " + trackID + " with count " + updatedCount);
                        oneIDs.remove(existingTrack);
                        existingTrack.setPlaylistCount(updatedCount);
                        someIDs.add(existingTrack);
                    }

                    /*
                     * This ID does not exist in either list, so add it to the
                     * 'one' list.
                     */
                    else
                    {
                        newTrack = new PlaylistComparisonTrack();
                        newTrack.setTrackID(trackID);
                        newTrack.setPlaylistCount(1);

                        filterLogger.trace("add to 'one' ID: " + trackID);
                        oneIDs.add(newTrack);
                    }
                }
            }

            playlistLoopIndex++;
        }

        /*
         * Only continue if valid playlists were entered.
         */
        if (playlistsValid == true)
        {

            /*
             * Save the query string constructed during the first pass.
             */
            queryStr = query.toString();

            /*
             * For the second pass, walk through the potential 'some' list. Move
             * all IDs that have a count equal to the number of playlists to the
             * 'all' list.
             * 
             * NOTE: if we only have 2 playlists to compare, then all IDs should
             * move to the 'all' list naturally.
             * 
             * ANOTHER NOTE: I had to break this into 2 pieces: one to add the
             * appropriate IDs to the 'all' list, and another to remove them
             * from the 'some' list. Otherwise, the underlying HashSet (in turn
             * implemented by a HashMap) throws an exception. The problem is
             * that invoking remove() while using the iterator on the 'some'
             * list doesn't work. It's supposed to, but doesn't. Perhaps using
             * native Java classes instead of Pivot classes would work, but the
             * below workaround solves the issue. Even though it's yucky.
             */
            for (PlaylistComparisonTrack pcTrack : someIDs)
            {
                if (pcTrack.getPlaylistCount() == playlists.getLength())
                {
                    filterLogger.trace("add to 'all' ID: " + pcTrack.getTrackID());
                    allIDs.add(pcTrack);
                }
            }

            for (PlaylistComparisonTrack pcTrack : allIDs)
            {
                filterLogger.trace("remove from 'some' ID: " + pcTrack.getTrackID());
                someIDs.remove(pcTrack);
            }

            /*
             * Reset the evaluate switch.
             */
            evaluateComparisonNeeded = false;
        }

        return playlistsValid;
    }

    /*
     * Display the set of tracks from the comparison evaluation according to
     * which button was pressed (compareType).
     */
    private void displayComparedPlaylistTracks(Display display, CompareType compareType)
            throws IOException, SerializationException
    {
        filterLogger.trace("displayComparedPlaylistTracks: " + this.hashCode());

        /*
         * Initialize a list of track objects, and sort it by name.
         */
        List<Track> displayableTracks = new ArrayList<Track>();
        displayableTracks.setComparator(new Comparator<Track>()
        {
            @Override
            public int compare(Track t1, Track t2)
            {
                return t1.compareTo(t2);
            }
        });

        /*
         * Set up for walking through the list of track IDs.
         */
        Set<PlaylistComparisonTrack> trackIDs = null;
        String compareStr = null;
        switch (compareType)
        {
        case ALL:
            trackIDs = allIDs;
            compareStr = StringConstants.QUERY_PLAYLIST_COMPARE_ALL;
            break;

        case SOME:
            trackIDs = someIDs;
            compareStr = StringConstants.QUERY_PLAYLIST_COMPARE_SOME;
            break;

        case ONE:
            trackIDs = oneIDs;
            compareStr = StringConstants.QUERY_PLAYLIST_COMPARE_ONE;
            break;

        case RECURSIVE:
            trackIDs = recursiveIDs;
            compareStr = StringConstants.QUERY_PLAYLIST_COMPARE_RECURSIVE;
            break;

        default:
            throw new InternalErrorException(true, "unknown compare type '" + compareType + "'");
        }

        /*
         * Walk through the appropriate list of track IDs.
         */
        for (PlaylistComparisonTrack pcTrack : trackIDs)
        {

            /*
             * Add the associated track object to the list of tracks.
             */
            Integer trackIndex = XMLHandler.getTracksMap().get(pcTrack.getTrackID());
            Track track = XMLHandler.getTracks().get(trackIndex);
            displayableTracks.add(track);
        }

        filterLogger.info("found " + displayableTracks.getLength() + " tracks for display");

        /*
         * Now display the list of tracks.
         */
        TracksWindow tracksWindowHandler = new TracksWindow();
        tracksWindowHandler.saveWindowAttributes(ListQueryType.Type.TRACK_COMPARE,
                ListQueryType.Type.TRACK_COMPARE.getDisplayValue() + " " + compareStr + ": " + queryStr,
                TrackDisplayColumns.ColumnSet.FILTERED_VIEW.getNamesList());
        tracksWindowHandler.displayTracks(display, Skins.Window.TRACKS, displayableTracks, null);
    }

    /*
     * Check if a given track ID is contained in a comparison set. If so, return
     * it; otherwise return null.
     */
    private PlaylistComparisonTrack findTrackID(Set<PlaylistComparisonTrack> trackIDs, Integer trackID)
    {
        filterLogger.trace("findTrackID: " + this.hashCode());

        PlaylistComparisonTrack target = null;

        /*
         * Walk through the input comparison set.
         */
        for (PlaylistComparisonTrack pcTrack : trackIDs)
        {

            /*
             * If the input track ID matches, we're done.
             */
            if (pcTrack.getTrackID().equals(trackID))
            {
                target = pcTrack;
                break;
            }
        }

        return target;
    }
    
    /*
     * Set up the various event handlers for the family playlists window.
     */
    private void createFamilyPlaylistsEventHandlers()
    {
        uiLogger.trace("createFamilyPlaylistsEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the done button press.
         */
        playlistsDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("family playlists done button pressed");

                /*
                 * Close the window.
                 */
                familyPlaylistsWindow.close();

                /*
                 * Pop the window off the skins window stack.
                 */
                skins.popSkinnedWindow();
            }
        });

        /*
         * Listener to handle playlist selection. This gets control when a playlist is selected in 
         * the filtered tree.
         */
        playlistsTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter()
        {
            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow)
            {

                /*
                 * Create a list suitable for the setTableData() method.
                 */
                List<HashMap<String, String>> displayTracks = new ArrayList<HashMap<String, String>>();

                /*
                 * Get the selected row.
                 */
                @SuppressWarnings("unchecked") 
                HashMap<String, String> rowData = (HashMap<String, String>) tableView.getSelectedRow();

                /*
                 * We may get called for an actual selected row, or for other reasons such as a sort 
                 * by column. No need to build a list of track information unless we actually have a
                 * selected row.
                 */
                if (rowData != null)
                {
                    String playlistName = 
                            rowData.get(PlaylistDisplayColumns.ColumnNames.PLAYLIST_NAME.getNameValue());
                    uiLogger.debug("playlist '" + playlistName + "' selected");

                    /*
                     * Get the tracks for the selected playlist.
                     */
                    String[] tracks = rowData.get(PlaylistDisplayColumns.ColumnNames.
                            TRACK_NAMES.getNameValue()).split(InternalConstants.LIST_ITEM_SEPARATOR);

                    for (int i = 0; i < tracks.length; i++)
                    {
                        HashMap<String, String> trackStr = new HashMap<String, String>();
                        trackStr.put(PlaylistDisplayColumns.ColumnNames.TRACK_NAMES.getNameValue(), 
                                tracks[i]);

                        displayTracks.add(trackStr);
                    }
                }

                filterLogger.info("found " + displayTracks.getLength() + " tracks for display");

                /*
                 * Fill in the table of tracks. If a row is not selected (for example the columns were 
                 * just sorted), this clears any track information that was already displayed from
                 * a previously selected row.
                 */
                playlistTracksTableView.setTableData(displayTracks);
            }
        });
    }
    
    /*
     * Generate and display the set of tracks from the family expansion playlist.
     */
    private void generateFamilyTrackResults (Display display, String playlistName)
            throws IOException, SerializationException
    {
        filterLogger.trace("generateFamilyTrackResults: " + this.hashCode());

        /*
         * Initialize a list of track objects, and sort it by name.
         */
        List<Track> displayableTracks = new ArrayList<Track>();
        displayableTracks.setComparator(new Comparator<Track>()
        {
            @Override
            public int compare(Track t1, Track t2)
            {
                return t1.compareTo(t2);
            }
        });

        /*
         * Get the input playlist object.
         */
        Playlist playlist = XMLHandler.getPlaylists().get(XMLHandler.getPlaylistsMap().get(playlistName));

        /*
         * Get the track IDs for the input playlist into a set.
         */
        List<Integer> trackIDs = playlist.getTracks();

        /*
         * Walk the list of track IDs for the selected playlist.
         */
        if (trackIDs != null)
        {
            for (Integer trackID : trackIDs)
            {

                /*
                 * Get the track for this track ID.
                 */
                Track track = XMLHandler.getTracks().get(XMLHandler.getTracksMap().get(trackID));

                /*
                 * Add the track.
                 */
                displayableTracks.add(track);
            }

            filterLogger.info("found " + displayableTracks.getLength() + " tracks for display");

            /*
             * Now display the list of tracks.
             */
            TracksWindow tracksWindowHandler = new TracksWindow();
            tracksWindowHandler.saveWindowAttributes(ListQueryType.Type.TRACK_FAMILY,
                    ListQueryType.Type.TRACK_FAMILY.getDisplayValue() + ": " + playlistName,
                    TrackDisplayColumns.ColumnSet.FAMILY_VIEW.getNamesList());
            tracksWindowHandler.displayTracks(display, Skins.Window.TRACKS, displayableTracks, null);
        }

    }
    
    /*
     * Generate the set of playlists from the family expansion playlist.
     */
    private void generateFamilyPlaylistResults (Display display, String playlistName, boolean excludeBypassed)
            throws IOException, SerializationException
    {
        filterLogger.trace("generateFamilyPlaylistResults: " + this.hashCode());

        /*
         * Create a set of playlist names, so we can determine whether we've already found a 
         * playlist or not.
         */
        Set<String> playlistSet = new HashSet<String>();
        
        /*
         * Create a map of the playlist name to its track list. This will drive the display of
         * the results.
         */
        Map<String, List<String>> playlistMap = new HashMap<String, List<String>>();
        playlistMap.setComparator(String.CASE_INSENSITIVE_ORDER);

        /*
         * Get the input playlist object.
         */
        Playlist inputPlaylistObj = 
                XMLHandler.getPlaylists().get(XMLHandler.getPlaylistsMap().get(playlistName));

        /*
         * Get the track IDs for the input playlist into a list.
         */
        List<Integer> trackIDs = inputPlaylistObj.getTracks();

        /*
         * Walk the list of track IDs for the selected playlist, if we have any.
         */
        if (trackIDs != null)
        {
            for (Integer trackID : trackIDs)
            {

                /*
                 * Get the track for this track ID.
                 */
                Track track = XMLHandler.getTracks().get(XMLHandler.getTracksMap().get(trackID));
                
                /*
                 * Get the list of playlist info objects for this track.
                 */
                List<TrackPlaylistInfo> trackPlaylists = track.getPlaylists();
                
                /*
                 * Walk the playlist info list.
                 */
                for (TrackPlaylistInfo playlistInfo : trackPlaylists)
                {
                    String playlist = playlistInfo.getPlaylistName();
                    
                    /*
                     * Skip the input playlist - we're only interested in other playlists used
                     * by the tracks.
                     */
                    if (playlist.equals(playlistName))
                    {
                        continue;
                    }
                    
                    /*
                     * Skip bypassed playlists if so directed.
                     */
                    if (excludeBypassed == true && playlistInfo.getBypassed() == true)
                    {
                        continue;
                    }
                    
                    /*
                     * Skip children of the input playlist.
                     */
                    Playlist playlistObj = 
                            XMLHandler.getPlaylists().get(XMLHandler.getPlaylistsMap().get(playlist));
                    String parentID = playlistObj.getParentPersistentID();
                    
                    if (parentID != null && parentID.equals(inputPlaylistObj.getPersistentID()))
                    {
                        continue;
                    }
                    
                    /*
                     * For a new playlist, initialize the track count to 1 in the playlist map. Otherwise,
                     * bump the track count.
                     */
                    if (playlistSet.add(playlist) == true)
                    {
                        List<String> tracks = new ArrayList<String>(track.getName());
                        tracks.setComparator(String.CASE_INSENSITIVE_ORDER);
                        playlistMap.put(playlist, tracks);
                    }
                    else
                    {
                        List<String> tracks = playlistMap.get(playlist);
                        tracks.add(track.getName());
                        playlistMap.put(playlist, tracks);
                    }
                }
            }
            
            /*
             * Generate the query string for the File -> Save dialog.
             */
            String queryStr = ListQueryType.Type.PLAYLIST_FAMILY.getDisplayValue() + ": " + playlistName;
            
            /*
             * Display the generated results.
             */
            displayFamilyPlaylists(display, playlistMap, queryStr);
        }
    }
    
    /*
     * Display the set of playlists from the family expansion playlist.
     */
    private void displayFamilyPlaylists(Display display, Map<String, List<String>> playlistMap, String queryStr)
            throws IOException, SerializationException
    {
        uiLogger.trace("displayFamilyPlaylists: " + this.hashCode());

        /*
         * Get the BXML information for the playlists window, and generate the list
         * of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeFamilyPlaylistBxmlVariables(components);

        /*
         * Set this object as the handler attribute on the playlists window. Also set other needed 
         * attributes. The attributes are used for the File -> Save menu on queried results.
         */
        familyPlaylistsWindow.setAttribute(MenuBars.WindowAttributes.HANDLER, this);
        familyPlaylistsWindow.setAttribute(MenuBars.WindowAttributes.QUERY_TYPE, 
                ListQueryType.Type.PLAYLIST_FAMILY);
        familyPlaylistsWindow.setAttribute(MenuBars.WindowAttributes.QUERY_STRING, queryStr);
        familyPlaylistsWindow.setAttribute(MenuBars.WindowAttributes.COLUMN_NAMES, 
                PlaylistDisplayColumns.ColumnSet.FAMILY_PLAYLISTS.getNamesList());
        
        /*
         * Set up the various event handlers.
         */
        createFamilyPlaylistsEventHandlers();
        
        /*
         * Set the number of playlists label.
         */
        numPlaylistsLabel.setText(StringConstants.PLAYLIST_NUMBER + playlistMap.getCount());

        /*
         * Create a list suitable for the setTableData() method.
         */
        List<HashMap<String, String>> displayPlaylists = new ArrayList<HashMap<String, String>>();

        /*
         * Create the family playlists column set.
         */
        PlaylistDisplayColumns.createColumnSet(PlaylistDisplayColumns.ColumnSet.FAMILY_PLAYLISTS, 
                playlistsTableView);

        /*
         * Now walk the set, and add all playlists to the list.
         */
        for (String playlist : playlistMap)
        {
            HashMap<String, String> playlistAttrs = new HashMap<String, String>();
            List<String> tracks = playlistMap.get(playlist);
            
            playlistAttrs.put(PlaylistDisplayColumns.ColumnNames.PLAYLIST_NAME.getNameValue(), 
                    playlist);
            playlistAttrs.put(PlaylistDisplayColumns.ColumnNames.NUM_TRACKS.getNameValue(), 
                    Integer.toString(tracks.getLength()));
            
            /*
             * Create the string of track names.
             */
            StringBuilder tracksStr = new StringBuilder();
            
            for (String track : tracks)
            {   
                if (tracksStr.length() > 0)
                {
                    tracksStr.append(InternalConstants.LIST_ITEM_SEPARATOR);
                }
                
                tracksStr.append(track);
            }
            
            playlistAttrs.put(PlaylistDisplayColumns.ColumnNames.TRACK_NAMES.getNameValue(), 
                    tracksStr.toString());
            
            displayPlaylists.add(playlistAttrs);
        }

        filterLogger.info("found " + displayPlaylists.getLength() + " playlists for display");

        /*
         * Add the playlists to the window table view.
         */
        playlistsTableView.setTableData(displayPlaylists);

        /*
         * Add a sort listener to allow column sorting.
         */
        playlistsTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter()
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
         * Create the playlist tracks column set.
         */
        PlaylistDisplayColumns.createColumnSet(PlaylistDisplayColumns.ColumnSet.PLAYLIST_TRACKS, 
                playlistTracksTableView);

        /*
         * Add widget texts.
         */
        playlistsDoneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        familyPlaylistsWindow.setTitle(Skins.Window.FAMILY_PLAYLISTS.getDisplayValue());

        /*
         * Register the family playlists window skin elements.
         */
        skins.registerWindowElements(Skins.Window.FAMILY_PLAYLISTS, components);

        /*
         * Skin the family playlists window.
         */
        skins.skinMe(Skins.Window.FAMILY_PLAYLISTS);

        /*
         * Push the skinned window onto the skins window stack. It gets popped
         * from our done button press handler.
         */
        skins.pushSkinnedWindow(Skins.Window.FAMILY_PLAYLISTS);
        
        /*
         * Open the family playlists window.
         */
        uiLogger.info("opening family playlists window");
        familyPlaylistsWindow.open(display);
    }

    /*
     * Initialize BXML variables and collect the list of components to be
     * skinned.
     */
    private void initializeBxmlVariables(List<Component> components) 
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeBxmlVariables: " + this.hashCode());

        BXMLSerializer windowSerializer = new BXMLSerializer();

        queryPlaylistsWindow = 
                (Window) windowSerializer.readObject(getClass().getResource("queryPlaylistsWindow.bxml"));

        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars) queryPlaylistsWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);

        compareBorder = 
                (Border) windowSerializer.getNamespace().get("compareBorder");
        components.add(compareBorder);
        compareHolderBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("compareHolderBoxPane");
        components.add(compareHolderBoxPane);
        compareBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("compareBoxPane");
        components.add(compareBoxPane);
        compareLabel = 
                (Label) windowSerializer.getNamespace().get("compareLabel");
        components.add(compareLabel);
        compareTablePane = 
                (TablePane) windowSerializer.getNamespace().get("compareTablePane");
        components.add(compareTablePane);
        compareButtonsBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("compareButtonsBoxPane");
        components.add(compareButtonsBoxPane);
        showAllButton = 
                (PushButton) windowSerializer.getNamespace().get("showAllButton");
        components.add(showAllButton);
        showSomeButton = 
                (PushButton) windowSerializer.getNamespace().get("showSomeButton");
        components.add(showSomeButton);
        showOneButton = 
                (PushButton) windowSerializer.getNamespace().get("showOneButton");
        components.add(showOneButton);
        recursiveCompareBorder = 
                (Border) windowSerializer.getNamespace().get("recursiveCompareBorder");
        components.add(recursiveCompareBorder);
        recursiveCompareHolderBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("recursiveCompareHolderBoxPane");
        components.add(recursiveCompareHolderBoxPane);
        recursiveCheckboxesBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("recursiveCheckboxesBoxPane");
        components.add(recursiveCheckboxesBoxPane);
        recursiveCompareAllCheckbox = 
                (Checkbox) windowSerializer.getNamespace().get("recursiveCompareAllCheckbox");
        components.add(recursiveCompareAllCheckbox);
        recursiveExcludeBypassedCheckbox = 
                (Checkbox) windowSerializer.getNamespace().get("recursiveExcludeBypassedCheckbox");
        components.add(recursiveExcludeBypassedCheckbox);
        recursiveCompareBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("recursiveCompareBoxPane");
        components.add(recursiveCompareBoxPane);
        recursiveCompareLabel = 
                (Label) windowSerializer.getNamespace().get("recursiveCompareLabel");
        components.add(recursiveCompareLabel);
        recursiveCompareTablePane = 
                (TablePane) windowSerializer.getNamespace().get("recursiveCompareTablePane");
        components.add(recursiveCompareTablePane);
        recursiveCompareButtonsBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("recursiveCompareButtonsBoxPane");
        components.add(recursiveCompareButtonsBoxPane);
        showButton = 
                (PushButton) windowSerializer.getNamespace().get("showButton");
        components.add(showButton);
        familyBorder = 
                (Border) windowSerializer.getNamespace().get("familyBorder");
        components.add(familyBorder);
        familyFillPane = 
                (FillPane) windowSerializer.getNamespace().get("familyFillPane");
        components.add(familyFillPane);
        familyBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("familyBoxPane");
        components.add(familyBoxPane);
        familyLabel = 
                (Label) windowSerializer.getNamespace().get("familyLabel");
        components.add(familyLabel);
        familyTextInput = 
                (TextInput) windowSerializer.getNamespace().get("familyTextInput");
        components.add(familyTextInput);
        familyButtonsBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("familyButtonsBoxPane");
        components.add(familyButtonsBoxPane);
        familyPlaylistsButton = 
                (PushButton) windowSerializer.getNamespace().get("familyPlaylistsButton");
        components.add(familyPlaylistsButton);
        familyExcludeBypassedCheckbox = 
                (Checkbox) windowSerializer.getNamespace().get("familyExcludeBypassedCheckbox");
        components.add(familyExcludeBypassedCheckbox);
        familyTracksButton = 
                (PushButton) windowSerializer.getNamespace().get("familyTracksButton");
        components.add(familyTracksButton);
        actionBorder = 
                (Border) windowSerializer.getNamespace().get("actionBorder");
        components.add(actionBorder);
        actionBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("actionBoxPane");
        components.add(actionBoxPane);
        queryDoneButton = 
                (PushButton) windowSerializer.getNamespace().get("queryDoneButton");
        components.add(queryDoneButton);
    }

    /*
     * Initialize tracks window BXML variables and collect the list of
     * components to be skinned.
     */
    private void initializeFamilyPlaylistBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeFamilyPlaylistBxmlVariables: " + this.hashCode());

        BXMLSerializer windowSerializer = new BXMLSerializer();

        familyPlaylistsWindow = 
                (Window) windowSerializer.readObject(getClass().getResource("filteredPlaylistsWindow.bxml"));

        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars) familyPlaylistsWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, true);

        infoBorder = 
                (Border) windowSerializer.getNamespace().get("infoBorder");
        components.add(infoBorder);
        infoFillPane = 
                (FillPane) windowSerializer.getNamespace().get("infoFillPane");
        components.add(infoFillPane);
        numPlaylistsLabel = 
                (Label) windowSerializer.getNamespace().get("numPlaylistsLabel");
        components.add(numPlaylistsLabel);
        playlistsBorder = 
                (Border) windowSerializer.getNamespace().get("playlistsBorder");
        components.add(playlistsBorder);
        playlistsTableView = 
                (TableView) windowSerializer.getNamespace().get("playlistsTableView");
        components.add(playlistsTableView);
        playlistsTableViewHeader = 
                (TableViewHeader) windowSerializer.getNamespace().get("playlistsTableViewHeader");
        components.add(playlistsTableViewHeader);
        playlistTracksTableView = 
                (TableView) windowSerializer.getNamespace().get("playlistTracksTableView");
        components.add(playlistTracksTableView);
        playlistTracksTableViewHeader = 
                (TableViewHeader) windowSerializer.getNamespace().get("playlistTracksTableViewHeader");
        components.add(playlistTracksTableViewHeader);
        familyActionBorder = 
                (Border) windowSerializer.getNamespace().get("familyActionBorder");
        components.add(familyActionBorder);
        familyActionBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("familyActionBoxPane");
        components.add(familyActionBoxPane);
        playlistsDoneButton = 
                (PushButton) windowSerializer.getNamespace().get("playlistsDoneButton");
        components.add(playlistsDoneButton);
    }
}
