package itunesq;

import java.io.IOException;

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
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewHeaderPressListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.Window;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the display tracks window. This window shows all tracks, a
 * set of tracks resulting from a query, or duplicate tracks.
 * 
 * @author Jon
 *
 */
public class TracksWindow
{

    // ---------------- Private variables -----------------------------------

    private Window tracksWindow = null;
    private Dialog trackInfoDialog = null;
    private Skins skins = null;
    private ListQueryType.Type queryType = null;
    private String queryStr = null;
    private List<String> columnNames = null;
    private String tableSortColumnName = null;
    private Logger logger = null;

    /*
     * BXML variables.
     */
    @BXML private Border infoBorder = null;
    @BXML private FillPane infoFillPane = null;
    @BXML private Label numTracksLabel = null;
    @BXML private Border alphaBorder = null;
    @BXML private BoxPane alphaBoxPane = null;
    @BXML private Label alphaLabel = null;
    @BXML private PushButton numericButton = null;
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
    @BXML private Border tracksBorder = null;
    @BXML private TableView tracksTableView = null;
    @BXML private TableViewHeader tracksTableViewHeader = null;
    @BXML private TableView trackPlaylistsTableView = null;
    @BXML private TableViewHeader trackPlaylistsTableViewHeader = null;
    @BXML private Border actionBorder = null;
    @BXML private BoxPane actionBoxPane = null;
    @BXML private PushButton findDuplicatesButton = null;
    @BXML private PushButton tracksDoneButton = null;

    @BXML private Border detailsPrimaryBorder = null;
    @BXML private TablePane detailsTablePane = null;

    /**
     * Class constructor.
     */
    public TracksWindow()
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

        /*
         * Initialize variables.
         */
        skins = Skins.getInstance();
        queryType = ListQueryType.Type.NONE;
        tableSortColumnName = TrackDisplayColumns.ColumnNames.NAME.getHeaderValue();

        logger.trace("TracksWindow constructor: " + this.hashCode());
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Saves window attributes used for various types of queries.
     * 
     * @param queryType type of query that generated the tracks to be displayed
     * @param queryStr string representation of the query
     * @param columnNames column names associated with the query
     */
    public void saveWindowAttributes(ListQueryType.Type queryType, String queryStr, List<String> columnNames)
    {
        logger.trace("saveWindowAttributes: " + this.hashCode());

        this.queryType = queryType;
        this.queryStr = queryStr;
        this.columnNames = columnNames;
    }

    /**
     * Displays the tracks in a new window.
     * 
     * @param display display object for managing windows
     * @param window the type of window containing the displayed tracks
     * @param tracks list of tracks to be displayed
     * @param owningWindow window on which to open the new window, or null
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayTracks(Display display, Skins.Window window, List<Track> tracks, Window owningWindow)
            throws IOException, SerializationException
    {
        logger.trace("displayTracks: " + this.hashCode());

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        if (tracks == null)
        {
            throw new IllegalArgumentException("tracks argument is null");
        }

        /*
         * Get the BXML information for the tracks window, and generate the list
         * of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeWindowBxmlVariables(queryType, components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers();

        /*
         * Handle a query type tracks display.
         */
        if (queryType != ListQueryType.Type.NONE)
        {
            /*
             * Set this object as the handler attribute on the tracks window. Also
             * set other needed attributes. The attributes are used for the File ->
             * Save menu on queried track results.
             */
            tracksWindow.setAttribute(MenuBars.WindowAttributes.HANDLER, this);
            tracksWindow.setAttribute(MenuBars.WindowAttributes.QUERY_TYPE, queryType);
            tracksWindow.setAttribute(MenuBars.WindowAttributes.QUERY_STRING, queryStr);
            tracksWindow.setAttribute(MenuBars.WindowAttributes.COLUMN_NAMES, columnNames);
        }
        
        /*
         * Handle a basic tracks display, which is the only one that uses the alphanumeric bar.
         */
        else
        {
        	createAlphaEventHandlers();
        }

        /*
         * Set the number of tracks label.
         */
        int numTracks = tracks.getLength();
        numTracksLabel.setText(StringConstants.TRACK_NUMBER + numTracks);

        /*
         * Create a list suitable for the setTableData() method.
         */
        List<HashMap<String, String>> displayTracks = new ArrayList<HashMap<String, String>>();

        /*
         * Now walk the set, and add all requested tracks to the list.
         */
        int trackNum = 0;

        for (Track track : tracks)
        {
        	HashMap<String, String> trackAttrs;
        	switch (window)
        	{
        	case TRACKS:
                trackAttrs = track.toDisplayMap(++trackNum);
                displayTracks.add(trackAttrs);
        		break;
        	
        	case AUDIO_TRACKS:
        		if (Database.getAudioTracksMap().get(track.getID()) != null)
        		{
                    trackAttrs = track.toDisplayMap(++trackNum);
                    displayTracks.add(trackAttrs);
        		}
        		break;
        	
        	case VIDEO_TRACKS:
        		if (Database.getVideoTracksMap().get(track.getID()) != null)
        		{
                    trackAttrs = track.toDisplayMap(++trackNum);
                    displayTracks.add(trackAttrs);
        		}
        		break;
        	
        	default:
                throw new InternalErrorException(true, "unexpected window type '" + window + "'");
        	}
        }

        logger.info("found " + displayTracks.getLength() + " tracks for display");

        /*
         * Create the appropriate column set based on the query type.
         */
        switch (queryType)
        {
        case NONE:
            TrackDisplayColumns.createColumnSet(TrackDisplayColumns.ColumnSet.FULL_VIEW, tracksTableView);
            break;

        case TRACK_DUPLICATES:
            TrackDisplayColumns.createColumnSet(TrackDisplayColumns.ColumnSet.DUPLICATES_VIEW, tracksTableView);
            break;

        case TRACK_FAMILY:
            TrackDisplayColumns.createColumnSet(TrackDisplayColumns.ColumnSet.FAMILY_VIEW, tracksTableView);
            break;

        default:
            TrackDisplayColumns.createColumnSet(TrackDisplayColumns.ColumnSet.FILTERED_VIEW, tracksTableView);
        }

        /*
         * Add the tracks to the window table view.
         */
        tracksTableView.setTableData(displayTracks);

        /*
         * Add a sort listener to allow column sorting.
         */
        tracksTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter()
        {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView)
            {
                List<Object> tableDataOfTableView = (List<Object>) tableView.getTableData();
                tableDataOfTableView.setComparator(new LQTTableViewRowComparator(tableView, logger));
            }
        });
        
        /*
         * Add a listener to detect column sort. We save the column being sorted for the alpha
         * bar logic. 
         */
        tracksTableViewHeader.getTableViewHeaderPressListeners().add(new TableViewHeaderPressListener()
        {
            @Override
            public void headerPressed(TableViewHeader tableViewHeader,
                    int index)
            {
            	TableView table = tableViewHeader.getTableView();
            	tableSortColumnName = table.getColumns().get(index).getName();

            	/*
            	 * Enable or disable the alpha bar depending on the column that was sorted.
            	 */
                if (queryType == ListQueryType.Type.NONE)
                {
                	TrackDisplayColumns.ColumnNames columnName = TrackDisplayColumns.ColumnNames.getEnum(tableSortColumnName);
                	switch (columnName)
                	{
                	case NAME:
                	case ARTIST:
                	case ALBUM:
                		setAlphaBarState(true);
                		break;

                	default:
                		setAlphaBarState(false);
                	}
                }
            }        	
        });

        /*
         * Create the track playlists column set if we're here because of a query.
         */
        if (queryType != ListQueryType.Type.NONE)
        {
            PlaylistDisplayColumns.createColumnSet(PlaylistDisplayColumns.ColumnSet.TRACK_PLAYLISTS, 
                    trackPlaylistsTableView);
        }
        
        /*
         * Not here because of a query.
         */
        else
        {
        
        	/*
        	 * Set the width of the (empty) alpha bar label. The label's only purpose is to
        	 * allow centering the alpha bar.
        	 */
        	int displayWidth = display.getWidth();
        	int alphaLabelWidth = (displayWidth - InternalConstants.ALPHA_BAR_PADDING - InternalConstants.ALPHA_BAR_WIDTH) / 2;
        	alphaLabel.setPreferredWidth(alphaLabelWidth);
        }
        
        /*
         * Add widget texts.
         */
        if (queryType == ListQueryType.Type.NONE)
        {
            findDuplicatesButton.setButtonData(StringConstants.TRACK_SHOW_DUPLICATES);
            findDuplicatesButton.setTooltipText(StringConstants.TRACK_SHOW_DUPLICATES_TIP);
            findDuplicatesButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        }
        tracksDoneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        tracksWindow.setTitle(window.getDisplayValue());

        /*
         * Register the tracks window skin elements.
         */
        skins.registerWindowElements(window, components);

        /*
         * Skin the tracks window.
         */
        skins.skinMe(window);

        /*
         * Push the skinned window onto the skins window stack. It gets popped
         * from our done button press handler.
         */
        skins.pushSkinnedWindow(window);

        /*
         * Open the tracks window.
         */
        logger.info("opening tracks window");
        if (owningWindow == null)
        {
            tracksWindow.open(display);
        }
        else
        {
            tracksWindow.open(owningWindow);
        }
    }

    /**
     * Creates the track info details dialog.
     * <p>
     * This is called when the user right clicks on a track in a table view, for
     * example the filtered tracks window, or the list of tracks in a selected
     * playlist.
     * 
     * @param trackRowData map of the track data from the table view row that
     * was clicked
     * @param display display for opening the dialog
     * @param owningWindow window that owns the track info dialog
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void handleTrackDetailsPopup(Map<String, String> trackRowData, Display display, Window owningWindow)
            throws IOException, SerializationException
    {
        logger.trace("handleTrackDetailsPopup: " + this.hashCode());

        if (trackRowData == null)
        {
            throw new IllegalArgumentException("trackRowData argument is null");
        }

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        if (owningWindow == null)
        {
            throw new IllegalArgumentException("owningWindow argument is null");
        }

        /*
         * Get the track name and log it.
         */
        String trackName = trackRowData.get(TrackDisplayColumns.ColumnNames.NAME.getNameValue());
        logger.info("right clicked on track '" + trackName + "'");

        /*
         * Get the base BXML information for the track info dialog, and start
         * the list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeInfoDialogBxmlVariables(components);

        /*
         * Build table rows to represent the track details. This method also
         * adds components that need to be skinned.
         */
        List<TablePane.Row> detailRows = buildTrackInfoRows(trackRowData, components);

        /*
         * Add the generated rows to the owning table pane.
         */
        for (TablePane.Row detailRow : detailRows)
        {
            detailsTablePane.getRows().add(detailRow);
        }

        /*
         * Set the window title.
         */
        trackInfoDialog.setTitle(Skins.Window.TRACK_INFO.getDisplayValue());

        /*
         * Register the window elements.
         */
        skins.registerWindowElements(Skins.Window.TRACK_INFO, components);

        /*
         * Skin the track info dialog.
         */
        skins.skinMe(Skins.Window.TRACK_INFO);

        /*
         * Open the track info dialog. There is no close button, so the user has
         * to close the dialog using the host controls.
         */
        logger.info("opening track info dialog");
        trackInfoDialog.open(display, owningWindow);
    }

    /**
     * Gets the list of tracks table data for a filtered list of tracks.
     * 
     * @return list of tracks table data
     */
    @SuppressWarnings("unchecked")
    public List<HashMap<String, String>> getFilteredTrackData()
    {
        return (List<HashMap<String, String>>) tracksTableView.getTableData();
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers.
     */
    private void createEventHandlers()
    {
        logger.trace("createEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the find duplicates button press. This is only
         * enabled when all tracks are displayed, not for filtered results.
         */
        if (queryType == ListQueryType.Type.NONE)
        {
            findDuplicatesButton.getButtonPressListeners().add(new ButtonPressListener()
            {
                @Override
                public void buttonPressed(Button button)
                {
                    logger.info("find duplicates button pressed");

                    Display display = button.getDisplay();
                    FindDuplicatesDialog findDuplicatesDialogHandler = new FindDuplicatesDialog(tracksWindow);

                    try
                    {
                        findDuplicatesDialogHandler.displayFindDuplicatesDialog(display);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(logger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
            });
        }

        /*
         * Listener to handle the done button press.
         */
        tracksDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("done button pressed");

                /*
                 * Close the window.
                 */
                tracksWindow.close();

                /*
                 * Pop the window off the skins window stack.
                 */
                skins.popSkinnedWindow();
            }
        });

        /*
         * Listener to handle track selection in a filtered view.
         */
        if (queryType != ListQueryType.Type.NONE)
        {

            /*
             * Populate the display playlists from the selected track. This gets
             * control when a track is selected in the filtered tree.
             */
            tracksTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter()
            {
                @Override
                public void selectedRowChanged(TableView tableView, Object previousSelectedRow)
                {

                    /*
                     * Create a list suitable for the setTableData() method.
                     */
                    List<HashMap<String, String>> displayPlaylists = new ArrayList<HashMap<String, String>>();

                    /*
                     * Create a list of rows that we want to highlight by being
                     * selected. We add non-bypassed playlists to this so they
                     * are more visible.
                     */
                    Sequence<Object> selectedPlaylists = new ArrayList<Object>();

                    /*
                     * Get the selected row and log the track name.
                     */
                    @SuppressWarnings("unchecked") 
                    HashMap<String, String> rowData = (HashMap<String, String>) tableView.getSelectedRow();

                    /*
                     * We may get called for an actual selected row, or for
                     * other reasons such as a sort by column. No need to build
                     * a list of playlist information unless we actually have a
                     * selected row.
                     */
                    if (rowData != null)
                    {
                        String trackName = rowData.get(TrackDisplayColumns.ColumnNames.NAME.getNameValue());
                        logger.debug("track '" + trackName + "' selected");

                        /*
                         * Get the playlists and corresponding bypassed indicators for the
                         * selected track.
                         */
                        String[] playlists = 
                                rowData.get(PlaylistDisplayColumns.ColumnNames.
                                        PLAYLIST_NAMES.getNameValue()).
                                            split(InternalConstants.LIST_ITEM_SEPARATOR);
                        String[] bypassed = 
                                rowData.get(PlaylistDisplayColumns.ColumnNames.
                                        BYPASSED.getNameValue()).
                                            split(InternalConstants.LIST_ITEM_SEPARATOR);

                        for (int i = 0; i < playlists.length; i++)
                        {
                            HashMap<String, String> playlistStr = new HashMap<String, String>();
                            playlistStr.put(PlaylistDisplayColumns.ColumnNames.PLAYLIST_NAMES.getNameValue(), 
                                    playlists[i]);

                            /*
                             * I find it more aesthetically pleasing to only
                             * show a value for the bypassed column for
                             * playlists that are actually bypassed.
                             */
                            if (bypassed[i].equals("Y"))
                            {
                                playlistStr.put(PlaylistDisplayColumns.ColumnNames.BYPASSED.getNameValue(), 
                                        bypassed[i]);
                            }
                            else
                            {
                                playlistStr.put(PlaylistDisplayColumns.ColumnNames.BYPASSED.getNameValue(), "");
                                selectedPlaylists.add(playlistStr);
                            }

                            displayPlaylists.add(playlistStr);
                        }
                    }

                    logger.info("found " + displayPlaylists.getLength() + " playlists for display");

                    /*
                     * Fill in the table of playlists. If a row is not selected
                     * (for example the columns were just sorted), this clears
                     * any playlist information that was already displayed from
                     * a previously selected row.
                     */
                    trackPlaylistsTableView.setTableData(displayPlaylists);

                    /*
                     * Highlight the non-bypassed playlists by making them
                     * selected.
                     */
                    trackPlaylistsTableView.setSelectedRows(selectedPlaylists);
                }
            });
        }

        /*
         * Mouse click listener for the table view.
         */
        tracksTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {

                /*
                 * For a right mouse click we pop up a dialog of all track info.
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
                     * Create and open the track details popup dialog.
                     */
                    try
                    {
                        handleTrackDetailsPopup(selectedTrackRowData, display, tracksWindow);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(logger, e);
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
    	logger.trace("createAlphaEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the numeric button press.
         */
        numericButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("alpha bar numeric button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar A button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar B button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar C button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar D button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar E button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar F button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar G button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar H button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar I button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar J button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar K button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar L button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar M button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar N button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar O button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar P button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar Q button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar R button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar S button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar T button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar U button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar V button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar W button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar X button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar Y button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
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
                logger.info("alpha bar Z button pressed");
                
                switch (tableSortColumnName)
                {
                case StringConstants.TRACK_COLUMN_NAME:
                case StringConstants.TRACK_COLUMN_ARTIST:
                case StringConstants.TRACK_COLUMN_ALBUM:
                	scrollToName((String) button.getButtonData());
                	break;
                	
                default:
                	// do nothing
                }
            }
        });
    }

    /*
     * Build the track info data for the track details dialog.
     */
    private List<TablePane.Row> buildTrackInfoRows(Map<String, String> rowData, List<Component> components)
    {
        logger.trace("buildTrackInfoRows: " + this.hashCode());

        List<TablePane.Row> result = new ArrayList<TablePane.Row>();

        /*
         * Track columns are used to contain the names of all possible track
         * data. Loop through all values.
         */
        for (TrackDisplayColumns.ColumnNames columns : TrackDisplayColumns.ColumnNames.values())
        {

            /*
             * Get the column header and name, which identify the corresponding track datum.
             */
            String columnHeader = columns.getHeaderValue();
            String columnName = columns.getNameValue();

            /*
             * Skip the following columns:
             * 
             * - NUMBER (this is a generated value that is not part of the track data)
             * - NUMPLAYLISTS (this is an internal value that is not part of the track data)
             */
            if (columns == TrackDisplayColumns.ColumnNames.NUMBER || 
                    columns == TrackDisplayColumns.ColumnNames.NUMPLAYLISTS)
            {
                continue;
            }

            /*
             * Get the track values from the input row that was selected.
             */
            String value = rowData.get(columnName);

            /*
             * Not all possible values exist for every track, so only continue
             * if the value is not null.
             */
            if (value != null)
            {

                /*
                 * Start building a table row to be returned.
                 */
                TablePane.Row infoRow = new TablePane.Row();
                infoRow.setHeight("1*");

                /*
                 * The track data are displayed in a box pane. Build it.
                 */
                BoxPane infoBox = new BoxPane(Orientation.HORIZONTAL);
                Map<String, Object> boxStyles = new HashMap<String, Object>();
                boxStyles.put("padding", 0);
                infoBox.setStyles(boxStyles);

                /*
                 * Build a "static" label that contains the column name that
                 * identifies the track value.
                 */
                Label infoStaticLabel = new Label(columnHeader);
                Map<String, Object> infoStaticLabelStyles = new HashMap<String, Object>();
                Map<String, Object> infoStaticLabelFontStyles = new HashMap<String, Object>();
                infoStaticLabelFontStyles.put("bold", true);
                infoStaticLabelStyles.put("font", infoStaticLabelFontStyles);
                infoStaticLabelStyles.put("padding", 0);
                infoStaticLabel.setPreferredWidth(InternalConstants.TRACK_DETAILS_LABEL_WIDTH);
                infoStaticLabel.setStyles(infoStaticLabelStyles);

                /*
                 * Build the label that contains the actual track value.
                 */
                Label infoLabel = new Label(value);
                Map<String, Object> infoLabelStyles = new HashMap<String, Object>();
                infoLabelStyles.put("padding", 0);
                infoLabel.setStyles(infoLabelStyles);

                /*
                 * Add the labels to the box pane.
                 */
                infoBox.add(infoStaticLabel);
                infoBox.add(infoLabel);

                /*
                 * Add the box pane to the table row.
                 */
                infoRow.add(infoBox);

                /*
                 * Add the components we created so they can be skinned.
                 */
                components.add(infoBox);
                components.add(infoStaticLabel);
                components.add(infoLabel);

                /*
                 * Add the table row to the result.
                 */
                result.add(infoRow);
            }
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
    	 * Get the tracks table view data.
    	 */
        @SuppressWarnings("unchecked") 
        List<HashMap<String, String>> tableData = 
            (List<HashMap<String, String>>) tracksTableView.getTableData();
        
        /*
         * Loop through the table rows.
         */
        int foundIndex = -1;
        for (int i = 0; i < tableData.getLength(); i++)
        {
            HashMap<String, String> row = tableData.get(i);

            /*
             * Ignore a leading "The" (any case) in the name from the row.
             */
            String name = row.get(tableSortColumnName).replaceAll("^(?i)The ", "");
        	
            /*
             * The numeric button is special - we loop through an array of all special characters, and
             * check if the row name starts with each of them.
             */
        	if (buttonID.equals("#"))
        	{
        		for (char numericChar : InternalConstants.ALPHA_BAR_NUMERIC_CHARS)
        		{
        			if (name.toLowerCase().startsWith(String.valueOf(numericChar)))
        			{
        				foundIndex = i;
        				break;
        			}
        		}
        		if (foundIndex >= 0)
        		{
        			break;
        		}
        	}
        	
        	/*
        	 * Handle all the alphabetic buttons.
        	 */
        	else
        	{
        		
                /*
                 * Check if the row name starts with the character according to the button.
                 */
        		if (name.toLowerCase().startsWith(buttonID))
        		{
        			foundIndex = i;
        			break;
        		}
            }
        }
        
        /*
         * Select the name in the table corresponding to the found index, if any.
         */
        if (foundIndex >= 0)
        {
			tracksTableView.setSelectedIndex(foundIndex);
        }
    }
    
    /*
     * Enable or disable the alpha bar buttons.
     */
    private void setAlphaBarState(boolean state)
    {
    	numericButton.setEnabled(state);
    	alphaAButton.setEnabled(state);
    	alphaBButton.setEnabled(state);
    	alphaCButton.setEnabled(state);
    	alphaDButton.setEnabled(state);
    	alphaEButton.setEnabled(state);
    	alphaFButton.setEnabled(state);
    	alphaGButton.setEnabled(state);
    	alphaHButton.setEnabled(state);
    	alphaIButton.setEnabled(state);
    	alphaJButton.setEnabled(state);
    	alphaKButton.setEnabled(state);
    	alphaLButton.setEnabled(state);
    	alphaMButton.setEnabled(state);
    	alphaNButton.setEnabled(state);
    	alphaOButton.setEnabled(state);
    	alphaPButton.setEnabled(state);
    	alphaQButton.setEnabled(state);
    	alphaRButton.setEnabled(state);
    	alphaSButton.setEnabled(state);
    	alphaTButton.setEnabled(state);
    	alphaUButton.setEnabled(state);
    	alphaVButton.setEnabled(state);
    	alphaWButton.setEnabled(state);
    	alphaXButton.setEnabled(state);
    	alphaYButton.setEnabled(state);
    	alphaZButton.setEnabled(state);
    }

    /*
     * Initialize tracks window BXML variables and collect the list of
     * components to be skinned.
     */
    private void initializeWindowBxmlVariables(ListQueryType.Type queryType, List<Component> components)
            throws IOException, SerializationException
    {
        logger.trace("initializeWindowBxmlVariables: " + this.hashCode());

        BXMLSerializer windowSerializer = new BXMLSerializer();

        boolean showFileSave;

        if (queryType != ListQueryType.Type.NONE)
        {
            showFileSave = true;
            tracksWindow = (Window) windowSerializer.
                    readObject(getClass().getResource("filteredTracksWindow.bxml"));
        }
        else
        {
            showFileSave = false;
            tracksWindow = (Window) windowSerializer.
                    readObject(getClass().getResource("tracksWindow.bxml"));
        }

        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars) tracksWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, showFileSave);

        infoBorder = 
                (Border) windowSerializer.getNamespace().get("infoBorder");
        components.add(infoBorder);
        infoFillPane = 
                (FillPane) windowSerializer.getNamespace().get("infoFillPane");
        components.add(infoFillPane);
        numTracksLabel = 
                (Label) windowSerializer.getNamespace().get("numTracksLabel");
        components.add(numTracksLabel);
        
        /*
         * The alphanumeric bar only applies to the basic tracks display, not any query type.
         */
        if (queryType == ListQueryType.Type.NONE)
        {
        	alphaBorder = 
        			(Border) windowSerializer.getNamespace().get("alphaBorder");
        	components.add(alphaBorder);
        	alphaBoxPane = 
        			(BoxPane) windowSerializer.getNamespace().get("alphaBoxPane");
        	components.add(alphaBoxPane);
        	alphaLabel = 
        			(Label) windowSerializer.getNamespace().get("alphaLabel");
        	components.add(alphaLabel);
        	numericButton = 
        			(PushButton) windowSerializer.getNamespace().get("numericButton");
        	components.add(numericButton);
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
        }
        tracksBorder = 
                (Border) windowSerializer.getNamespace().get("tracksBorder");
        components.add(tracksBorder);
        tracksTableView = 
                (TableView) windowSerializer.getNamespace().get("tracksTableView");
        components.add(tracksTableView);
        tracksTableViewHeader = 
                (TableViewHeader) windowSerializer.getNamespace().get("tracksTableViewHeader");
        components.add(tracksTableViewHeader);
        actionBorder = 
                (Border) windowSerializer.getNamespace().get("actionBorder");
        components.add(actionBorder);
        actionBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("actionBoxPane");
        components.add(actionBoxPane);

        if (queryType == ListQueryType.Type.NONE)
        {
            findDuplicatesButton = 
                    (PushButton) windowSerializer.getNamespace().get("findDuplicatesButton");
            components.add(findDuplicatesButton);
        }

        tracksDoneButton = 
                (PushButton) windowSerializer.getNamespace().get("tracksDoneButton");
        components.add(tracksDoneButton);

        if (queryType != ListQueryType.Type.NONE)
        {
            trackPlaylistsTableView = 
                    (TableView) windowSerializer.getNamespace().get("trackPlaylistsTableView");
            components.add(trackPlaylistsTableView);
            trackPlaylistsTableViewHeader = 
                    (TableViewHeader) windowSerializer.getNamespace().get("trackPlaylistsTableViewHeader");
            components.add(trackPlaylistsTableViewHeader);
        }
    }

    /*
     * Initialize tracks info dialog BXML variables and collect the static
     * components to be skinned.
     */
    private void initializeInfoDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        logger.trace("initializeInfoDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        trackInfoDialog = 
                (Dialog) dialogSerializer.readObject(getClass().getResource("trackInfoDialog.bxml"));

        detailsPrimaryBorder = 
                (Border) dialogSerializer.getNamespace().get("detailsPrimaryBorder");
        components.add(detailsPrimaryBorder);
        detailsTablePane = 
                (TablePane) dialogSerializer.getNamespace().get("detailsTablePane");
        components.add(detailsTablePane);
    }
}
