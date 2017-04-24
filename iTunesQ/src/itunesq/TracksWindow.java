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
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TableViewRowComparator;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the display tracks window.
 * 
 * @author Jon
 *
 */
public class TracksWindow
{
	
    //---------------- Private variables -----------------------------------
	
	private Logger logger = null;
	
	/*
	 * BXML variables.
	 */
	@BXML private MenuBar mainMenuBar = null;
	@BXML private Menu mainFileMenu = null;
	@BXML private Menu mainEditMenu = null;
	@BXML private Border primaryBorder = null;
	@BXML private Border infoBorder = null;
	@BXML private FillPane infoFillPane = null;
	@BXML private Label numTracksLabel = null;
	@BXML private Border tracksBorder = null;
	@BXML private TableView tracksTableView = null;
	@BXML private TableViewHeader tracksTableViewHeader = null;
	@BXML private TableView trackPlaylistsTableView = null;
	@BXML private TableViewHeader trackPlaylistsTableViewHeader = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
	@BXML private PushButton tracksDoneButton = null;
    
    /**
     * Constructor.
     */
    public TracksWindow ()
    {
    	
    	/*
    	 * Get the logging object singleton.
    	 */
    	Logging logging = Logging.getInstance();
    	
    	/*
    	 * Get a logger.
    	 */
    	String className = getClass().getSimpleName();
    	logger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Register our logger.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, logger);
    }
	
    //---------------- Public methods --------------------------------------

	/**
	 * Display the tracks in a new window.
	 * 
	 * @param display Display object for managing windows.
	 * @param tracks The list of tracks to be displayed.
	 * @param filtered Whether or not a filtered list of tracks is being displayed.
	 * @throws IOException
	 * @throws SerializationException
	 */
    public void displayTracks (Display display, List<Track> tracks, boolean filtered) 
    		throws IOException, SerializationException
    {
    	
    	/*
    	 * Get the BXML information for the tracks display window.
    	 */
        BXMLSerializer tracksWindowSerializer = new BXMLSerializer();
        Window tracksWindow;
        if (filtered == true)
        {
        	tracksWindow = (Window)tracksWindowSerializer.
        			readObject(getClass().getResource("filteredTracksWindow.bxml"));
        }
        else
        {
        	tracksWindow = (Window)tracksWindowSerializer.
        			readObject(getClass().getResource("tracksWindow.bxml"));
        }

        mainMenuBar = 
        		(MenuBar)tracksWindowSerializer.getNamespace().get("mainMenuBar");
        mainFileMenu = 
        		(Menu)tracksWindowSerializer.getNamespace().get("mainFileMenu");
        mainEditMenu = 
        		(Menu)tracksWindowSerializer.getNamespace().get("mainEditMenu");
        primaryBorder = 
        		(Border)tracksWindowSerializer.getNamespace().get("primaryBorder");
        infoBorder = 
        		(Border)tracksWindowSerializer.getNamespace().get("infoBorder");
        infoFillPane = 
        		(FillPane)tracksWindowSerializer.getNamespace().get("infoFillPane");
        numTracksLabel = 
        		(Label)tracksWindowSerializer.getNamespace().get("numTracksLabel");
        tracksBorder = 
        		(Border)tracksWindowSerializer.getNamespace().get("tracksBorder");
        tracksTableView = 
        		(TableView)tracksWindowSerializer.getNamespace().get("tracksTableView");
        tracksTableViewHeader = 
        		(TableViewHeader)tracksWindowSerializer.getNamespace().get("tracksTableViewHeader");
        actionBorder = 
        		(Border)tracksWindowSerializer.getNamespace().get("actionBorder");
        actionBoxPane = 
        		(BoxPane)tracksWindowSerializer.getNamespace().get("actionBoxPane");
        tracksDoneButton = 
        		(PushButton)tracksWindowSerializer.getNamespace().get("tracksDoneButton");
        
        if (filtered == true)
        {
        	trackPlaylistsTableView = (TableView)tracksWindowSerializer.getNamespace().
        			get("trackPlaylistsTableView");
        	trackPlaylistsTableViewHeader = (TableViewHeader)tracksWindowSerializer.getNamespace().
        			get("trackPlaylistsTableViewHeader");
        }
        
        /*
         * Add listeners to handle the button presses.
         */
        tracksDoneButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	logger.info("done button pressed");
            	tracksWindow.close();
            }
        });
        
        if (filtered == true)
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
                    
                    String trackName = rowData.get(TrackDisplayColumns.ColumnNames.NAME.getDisplayValue());
                	logger.debug("track '" + trackName + "' selected");
                    
                	/*
                	 * Get the selected playlist ID.
                	 */
                    String playlistInfo = rowData.get(Track.MAP_PLAYLISTS);
                    String[] playlists = playlistInfo.split(",");
                    for (int i = 0; i < playlists.length; i++)
                    {
                        HashMap<String, String> playlistStr = new HashMap<String, String>();
                    	playlistStr.put("name", playlists[i]);
                        displayPlaylists.add(playlistStr);
                    }
                    
                    trackPlaylistsTableView.setTableData(displayPlaylists);
                }
            });
        }
		
		/*
		 * Get the skins object singleton.
		 */
		Skins skins = Skins.getInstance();
		tracksWindow.setTitle(Skins.Window.TRACKS.getDisplayValue());
		
		/*
		 * Register the tracks window skin elements.
		 */
		List<Component> components = new ArrayList<Component>();
		Map<Skins.Element, List<Component>> windowElements = 
				new HashMap<Skins.Element, List<Component>>();

		components.add(mainMenuBar);
		components.add(mainFileMenu);
		components.add(mainEditMenu);
		components.add(primaryBorder);
		components.add(infoBorder);
		components.add(infoFillPane);
		components.add(numTracksLabel);
		components.add(tracksBorder);
		components.add(tracksTableView);
		components.add(tracksTableViewHeader);
		components.add(actionBorder);
		components.add(actionBoxPane);
		components.add(tracksDoneButton);

        if (filtered == true)
        {
    		components.add(trackPlaylistsTableView);
    		components.add(trackPlaylistsTableViewHeader);
        }
		
		windowElements = skins.mapComponentsToSkinElements(components);
		
		skins.registerWindowElements(Skins.Window.TRACKS, windowElements);
        
        /*
         * Set the number of tracks label.
         */
    	numTracksLabel.setText("Number of Tracks: " + tracks.getLength());
        
        /*
         * Create a list suitable for the setTableData() method.
         */
        List<HashMap<String, String>> displayTracks = new ArrayList<HashMap<String, String>>();
        
        /*
         * Now walk the set, and add all tracks to the list.
         */
        Iterator<Track> tracksIter = tracks.iterator();
        while (tracksIter.hasNext())
        {
        	Track track = tracksIter.next();
        	HashMap<String, String> trackAttrs = track.toDisplayMap(0);
        	displayTracks.add(trackAttrs);
        }

        if (filtered == true)
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
        tracksTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView) {
                List<Object> tableDataOfTableView = (List<Object>)tableView.getTableData();
                tableDataOfTableView.setComparator(new TableViewRowComparator(tableView));
            }
        });
		
		/*
		 * Skin the tracks window.
		 */
		skins.skinMe(Skins.Window.TRACKS);
        
        /*
         * Open the tracks window.
         */
    	logger.info("opening tracks window");
        tracksWindow.open(display);
    }
}
