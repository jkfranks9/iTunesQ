package itunesq;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
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
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.Window;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the display tracks window. This window shows all tracks
 * or a set of tracks resulting from a query.
 * 
 * @author Jon
 *
 */
public class TracksWindow
{
	
    //---------------- Public variables ------------------------------------
	
	/**
	 * The type of query associated with the list of tracks.
	 */
	public enum QueryType
	{
		
		/**
		 * the tracks list is not associated with a query
		 */
		NONE(""),
		
		/**
		 * the tracks list is the result of a tracks query
		 */
		TRACKS(StringConstants.TRACK_QUERY_TRACKS),
		
		/**
		 * the tracks list is the result of a playlist query
		 */
		PLAYLISTS(StringConstants.TRACK_QUERY_PLAYLISTS);
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private QueryType (String s)
		{
			displayValue = s;
		}
		
		/**
		 * Gets the display value.
		 * 
		 * @return <code>enum</code> display value
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}
	}
	
    //---------------- Private variables -----------------------------------

	private Window tracksWindow = null;
	private Dialog trackInfoDialog = null;
	private Skins skins = null;
	private Logger logger = null;
	
	/*
	 * BXML variables.
	 */
	@BXML private Border infoBorder = null;
	@BXML private FillPane infoFillPane = null;
	@BXML private Label numTracksLabel = null;
	@BXML private Border tracksBorder = null;
	@BXML private TableView tracksTableView = null;
	@BXML private TableViewHeader tracksTableViewHeader = null;
	@BXML private TableView trackPlaylistsTableView = null;
	@BXML private TableView.Column trackPlaylistsTableViewNameColumn = null;
	@BXML private TableView.Column trackPlaylistsTableViewBypassColumn = null;
	@BXML private TableViewHeader trackPlaylistsTableViewHeader = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
	@BXML private PushButton tracksDoneButton = null;

	@BXML private Border detailsPrimaryBorder = null;
	@BXML private TablePane detailsTablePane = null;
    
    /**
     * Class constructor.
     */
    public TracksWindow ()
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
    	
    	logger.trace("TracksWindow constructor: " + this.hashCode());
    }
	
    //---------------- Public methods --------------------------------------

	/**
	 * Displays the tracks in a new window.
	 * 
	 * @param display display object for managing windows
	 * @param tracks list of tracks to be displayed
	 * @param queryType type of query (if any) that generated the tracks to be 
	 * displayed
	 * @param queryStr string representation of the query, or null
	 * @throws IOException If an error occurs trying to read the BXML file.
	 * @throws SerializationException If an error occurs trying to 
	 * deserialize the BXML file.
	 */
    public void displayTracks (Display display, List<Track> tracks, QueryType queryType, String queryStr) 
    		throws IOException, SerializationException
    {
    	logger.trace("displayTracks: " + this.hashCode());
    	
    	/*
    	 * Get the BXML information for the tracks window, and generate the list of components
    	 * to be skinned.
    	 */
		List<Component> components = new ArrayList<Component>();
		initializeWindowBxmlVariables(queryType, components);

		/*
		 * Set this object as the handler attribute on the tracks window. Also set other needed
		 * attributes. The attributes are used for the File -> Save menu on queried track results.
		 */
		if (queryType != QueryType.NONE)
		{
			tracksWindow.setAttribute(MenuBars.WindowAttributes.HANDLER, this);
			tracksWindow.setAttribute(MenuBars.WindowAttributes.QUERY_TYPE, queryType);
			tracksWindow.setAttribute(MenuBars.WindowAttributes.QUERY_STRING, queryStr);
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
        if (queryType != QueryType.NONE)
        {
            
            /*
             * Populate the display playlists from the selected track. This gets control when a 
             * track is selected in the filtered tree.
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
                     * Get the selected row and log the track name.
                     */
                    @SuppressWarnings("unchecked")
					HashMap<String, String> rowData = (HashMap<String, String>) tableView.getSelectedRow();
                    
                    /*
                     * We may get called for an actual selected row, or for other reasons such as
                     * a sort by column. No need to build a list of playlist information unless we 
                     * actually have a selected row.
                     */
                    if (rowData != null)
                    {
                    	String trackName = rowData.get(TrackDisplayColumns.ColumnNames.NAME.getDisplayValue());
                    	logger.debug("track '" + trackName + "' selected");

                    	/*
                    	 * Get the playlists and bypassed indicator for the selected track.
                    	 */
                    	String[] playlists = rowData.get(Track.MAP_PLAYLISTS).split(",");
                    	String[] bypassed = rowData.get(Track.MAP_BYPASSED).split(",");

                    	for (int i = 0; i < playlists.length; i++)
                    	{
                    		HashMap<String, String> playlistStr = new HashMap<String, String>();
                    		playlistStr.put("name", playlists[i]);

                    		/*
                    		 * I find it more aesthetically pleasing to only show a value for the bypassed 
                    		 * column for playlists that are actually bypassed.
                    		 */
                    		if (bypassed[i].equals("Y"))
                    		{
                    			playlistStr.put("bypass", bypassed[i]);
                    		}
                    		else
                    		{
                    			playlistStr.put("bypass", "");
                    		}

                    		displayPlaylists.add(playlistStr);
                    	}
                    }

                	/*
                	 * Fill in the table of playlists. If a row is not selected (for example the
                	 * columns were just sorted), this clears any playlist information that was
                	 * already displayed from a previously selected row.
                	 */
                	trackPlaylistsTableView.setTableData(displayPlaylists);
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
            		 * Get the index for the clicked row, then set that row as selected.
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
						logger.error("caught " + e.getClass().getSimpleName());
						e.printStackTrace();
					}
            	}
 
                return false;
            }
        });
        
        /*
         * Add widget texts.
         */
        tracksDoneButton.setButtonData(StringConstants.DONE);
		
		/*
		 * Set the window title.
		 */
		tracksWindow.setTitle(Skins.Window.TRACKS.getDisplayValue());
		
		/*
		 * Register the tracks window skin elements.
		 */
		Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
		skins.registerWindowElements(Skins.Window.TRACKS, windowElements);
        
        /*
         * Set the number of tracks label.
         */
    	numTracksLabel.setText(StringConstants.TRACK_NUMBER + tracks.getLength());
        
        /*
         * Create a list suitable for the setTableData() method.
         */
        List<HashMap<String, String>> displayTracks = new ArrayList<HashMap<String, String>>();
        
        /*
         * Now walk the set, and add all tracks to the list.
         */
		int trackNum = 0;
		
        Iterator<Track> tracksIter = tracks.iterator();
        while (tracksIter.hasNext())
        {
        	Track track = tracksIter.next();
        	HashMap<String, String> trackAttrs = track.toDisplayMap(++trackNum);
        	displayTracks.add(trackAttrs);
        }

        if (queryType != QueryType.NONE)
        {
        	TrackDisplayColumns.createColumnSet(TrackDisplayColumns.ColumnSet.FILTERED_VIEW, tracksTableView);
        }
        else
        {
        	TrackDisplayColumns.createColumnSet(TrackDisplayColumns.ColumnSet.FULL_VIEW, tracksTableView);
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
            public void sortChanged(TableView tableView) {
                List<Object> tableDataOfTableView = (List<Object>)tableView.getTableData();
                tableDataOfTableView.setComparator(new TracksTableViewRowComparator(tableView));
            }
        });

    	/*
    	 * Set the playlist information column header text if we're here because of a query.
    	 */
        if (queryType != QueryType.NONE)
        {
        	trackPlaylistsTableViewNameColumn.
        		setHeaderData(StringConstants.TRACK_PLAYLISTS_NAME_COLUMN);
        	trackPlaylistsTableViewBypassColumn.
        		setHeaderData(StringConstants.TRACK_PLAYLISTS_BYPASS_COLUMN);
        }
		
		/*
		 * Skin the tracks window.
		 */
		skins.skinMe(Skins.Window.TRACKS);
		
		/*
		 * Push the skinned window onto the skins window stack. It gets popped from our done button press
		 * handler.
		 */
		skins.pushSkinnedWindow(Skins.Window.TRACKS);
        
        /*
         * Open the tracks window.
         */
    	logger.info("opening tracks window");
        tracksWindow.open(display);
    }
    
    /**
     * Creates the track info details dialog.
     * <p>
     * This is called when the user right clicks on a track in a table view,
     * for example the filtered tracks window, or the list of tracks in a 
     * selected playlist. 
     * 
     * @param trackRowData map of the track data from the table view row that
     * was clicked
     * @param display display for opening the dialog
     * @param owningWindow window that owns the track info dialog
	 * @throws IOException If an error occurs trying to read the BXML file.
	 * @throws SerializationException If an error occurs trying to 
	 * deserialize the BXML file.
     */
    public void handleTrackDetailsPopup (Map<String, String> trackRowData, Display display,
    		Window owningWindow) 
    		throws IOException, SerializationException
    {
    	logger.trace("handleTrackDetailsPopup: " + this.hashCode());
        
        /*
         * Get the track name and log it.
         */
		String trackName = 
				trackRowData.get(TrackDisplayColumns.ColumnNames.NAME.getDisplayValue());
		logger.info("right clicked on track '" + trackName + "'");

		/*
		 * Get the base BXML information for the track info dialog, and start the list of 
		 * components to be skinned.
		 */
		List<Component> components = new ArrayList<Component>();
		initializeInfoDialogBxmlVariables(components);

		/*
		 * Build table rows to represent the track details. This method also adds
		 * components that need to be skinned.
		 */
		List<TablePane.Row> detailRows = 
				buildTrackInfoRows(trackRowData, components);

		/*
		 * Add the generated rows to the owning table pane.
		 */
		Iterator<TablePane.Row> detailsRowsIter = detailRows.iterator();
		while (detailsRowsIter.hasNext())
		{
			TablePane.Row detailRow = detailsRowsIter.next();
			detailsTablePane.getRows().add(detailRow);
		}
		
		/*
		 * Set the window title.
		 */
		trackInfoDialog.setTitle(Skins.Window.TRACKINFO.getDisplayValue());

		/*
		 * Register the window elements.
		 */
		Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);		
		skins.registerWindowElements(Skins.Window.TRACKINFO, windowElements);

		/*
		 * Skin the track info dialog.
		 */
		skins.skinMe(Skins.Window.TRACKINFO);

		/*
		 * Open the track info dialog. There is no close button, so the user has to 
		 * close the dialog using the host controls.
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
	public List<HashMap<String, String>> getFilteredTrackData ()
    {
    	return (List<HashMap<String, String>>) tracksTableView.getTableData();
    }

    //---------------- Private methods -------------------------------------
    
    /*
     * Build the track info data for the track details dialog.
     */
    private List<TablePane.Row> buildTrackInfoRows (Map<String, String> rowData, 
    		List<Component> components)
    {
    	logger.trace("buildTrackInfoRows: " + this.hashCode());
    	
    	List<TablePane.Row> result = new ArrayList<TablePane.Row>();

		/*
		 * Track columns are used to contain the names of all possible track data. Loop through
		 * all values.
		 */
    	for (TrackDisplayColumns.ColumnNames columns : TrackDisplayColumns.ColumnNames.values())
    	{
    		
    		/*
    		 * Get the column name, which is the name of the corresponding track datum.
    		 */
    		String columnName = columns.getDisplayValue();
    		
    		/*
    		 * Since NUMBER is a generated value that is not part of the track data, skip it here.
    		 */
    		if (columns == TrackDisplayColumns.ColumnNames.NUMBER)
    		{
    			continue;
    		}
    		
    		/*
    		 * Get the track values from the input row that was selected.
    		 */
    		String value = rowData.get(columnName);
    		
    		/*
    		 * Not all possible values exist for every track, so only continue if the value is not null.
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
    			 * Build a "static" label that contains the column name that identifies the track value.
    			 */
    			Label infoStaticLabel = new Label(columnName);
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
     * Initialize tracks window BXML variables and collect the list of components to be skinned.
     */
    private void initializeWindowBxmlVariables (QueryType queryType, List<Component> components) 
    		throws IOException, SerializationException
    {
    	logger.trace("initializeWindowBxmlVariables: " + this.hashCode());
    	
        BXMLSerializer windowSerializer = new BXMLSerializer();
        boolean showFileSave;
        
        if (queryType != QueryType.NONE)
        {
        	showFileSave = true;
        	tracksWindow = (Window)windowSerializer.
        			readObject(getClass().getResource("filteredTracksWindow.bxml"));
        }
        else
        {
        	showFileSave = false;
        	tracksWindow = (Window)windowSerializer.
        			readObject(getClass().getResource("tracksWindow.bxml"));
        }
        
        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars)tracksWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, showFileSave);

        infoBorder = 
        		(Border)windowSerializer.getNamespace().get("infoBorder");
		components.add(infoBorder);
        infoFillPane = 
        		(FillPane)windowSerializer.getNamespace().get("infoFillPane");
		components.add(infoFillPane);
        numTracksLabel = 
        		(Label)windowSerializer.getNamespace().get("numTracksLabel");
		components.add(numTracksLabel);
        tracksBorder = 
        		(Border)windowSerializer.getNamespace().get("tracksBorder");
		components.add(tracksBorder);
        tracksTableView = 
        		(TableView)windowSerializer.getNamespace().get("tracksTableView");
		components.add(tracksTableView);
        tracksTableViewHeader = 
        		(TableViewHeader)windowSerializer.getNamespace().get("tracksTableViewHeader");
		components.add(tracksTableViewHeader);
        actionBorder = 
        		(Border)windowSerializer.getNamespace().get("actionBorder");
		components.add(actionBorder);
        actionBoxPane = 
        		(BoxPane)windowSerializer.getNamespace().get("actionBoxPane");
		components.add(actionBoxPane);
        tracksDoneButton = 
        		(PushButton)windowSerializer.getNamespace().get("tracksDoneButton");
		components.add(tracksDoneButton);
        
        if (queryType != QueryType.NONE)
        {
        	trackPlaylistsTableView = (TableView)windowSerializer.getNamespace().
        			get("trackPlaylistsTableView");
    		components.add(trackPlaylistsTableView);
        	trackPlaylistsTableViewHeader = (TableViewHeader)windowSerializer.getNamespace().
        			get("trackPlaylistsTableViewHeader");
    		components.add(trackPlaylistsTableViewHeader);
    		
    		/*
    		 * These don't need to be added to the components list because they're subcomponents
    		 * of TableView.
    		 */
        	trackPlaylistsTableViewNameColumn = (TableView.Column)windowSerializer.getNamespace().
        			get("trackPlaylistsTableViewNameColumn");
        	trackPlaylistsTableViewBypassColumn = (TableView.Column)windowSerializer.getNamespace().
        			get("trackPlaylistsTableViewBypassColumn");
        }
    }
    
    /*
     * Initialize tracks info dialog BXML variables and collect the static components to be skinned.
     */
    private void initializeInfoDialogBxmlVariables (List<Component> components) 
    		throws IOException, SerializationException
    {
    	logger.trace("initializeInfoDialogBxmlVariables: " + this.hashCode());
    	
        BXMLSerializer dialogSerializer = new BXMLSerializer();
		trackInfoDialog = (Dialog)dialogSerializer.readObject(getClass().
				getResource("trackInfoWindow.bxml"));

		detailsPrimaryBorder = 
        		(Border)dialogSerializer.getNamespace().get("detailsPrimaryBorder");
		components.add(detailsPrimaryBorder);
		detailsTablePane = 
        		(TablePane)dialogSerializer.getNamespace().get("detailsTablePane");
		components.add(detailsTablePane);
    }
}
