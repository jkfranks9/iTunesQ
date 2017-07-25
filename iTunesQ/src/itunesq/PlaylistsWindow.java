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
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.TreeViewSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TreeNode;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the display playlists window. This window shows all 
 * playlists in a tree form. Selecting a playlist shows the associated tracks
 * on the right side of the window.
 * 
 * @author Jon
 *
 */
public class PlaylistsWindow
{
	
    //---------------- Private variables -----------------------------------

	private Window playlistsWindow = null;
	private Skins skins = null;
	private Logger uiLogger = null;
	private Logger playlistLogger = null;
	
	/*
	 * BXML variables.
	 */
	@BXML private MenuBar mainMenuBar = null;
	@BXML private Menu mainFileMenu = null;
	@BXML private Menu mainEditMenu = null;
	@BXML private Border playlistsBorder = null;
	@BXML private TreeView playlistsTreeView = null;
	@BXML private TableView playlistTracksTableView = null;
	@BXML private TableViewHeader playlistTracksTableViewHeader = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
	@BXML private PushButton playlistsDoneButton = null;
    
    /**
     * Class constructor.
     */
    public PlaylistsWindow ()
    {
    	
    	/*
    	 * Create a UI logger.
    	 */
    	String className = getClass().getSimpleName();
    	uiLogger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Create a playlist logger.
    	 */
    	playlistLogger = (Logger) LoggerFactory.getLogger(className + "_Playlist");
    	
    	/*
    	 * Get the logging object singleton.
    	 */
    	Logging logging = Logging.getInstance();
    	
    	/*
    	 * Register our loggers.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, uiLogger);
    	logging.registerLogger(Logging.Dimension.PLAYLIST, playlistLogger);
    	
    	/*
    	 * Initialize variables.
    	 */
    	skins = Skins.getInstance();
		
		uiLogger.trace("PlaylistsWindow constructor: " + this.hashCode());
    }
	
    //---------------- Public methods --------------------------------------
    
	/**
	 * Displays the playlists in a new window.
	 * 
	 * @param display display object for managing windows
	 * @throws IOException If an exception occurs trying to read the BXML file.
	 * @throws SerializationException If an exception occurs trying to 
	 * deserialize the BXML file.
	 */
    public void displayPlaylists (Display display) 
    		throws IOException, SerializationException
    {
    	uiLogger.trace("displayPlaylists: " + this.hashCode());
    	
    	/*
    	 * Get the BXML information for the playlists window, and generate the list of components
    	 * to be skinned.
    	 */
		List<Component> components = new ArrayList<Component>();
		initializeBxmlVariables(components);
        
        /*
         * Listener to handle the done button press.
         */
        playlistsDoneButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	uiLogger.info("done button pressed");
            	
            	/*
            	 * Close the window.
            	 */
            	playlistsWindow.close();
            	
            	/*
            	 * Pop the window off the skins window stack.
            	 */
            	skins.popSkinnedWindow();
            }
        });
		
		/*
		 * Set the window title.
		 */
		playlistsWindow.setTitle(Skins.Window.PLAYLISTS.getDisplayValue());
		
		/*
		 * Register the tracks window skin elements.
		 */
		Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);		
		skins.registerWindowElements(Skins.Window.PLAYLISTS, windowElements);
        
        /*
         * Create the playlist column set.
         */
    	TrackDisplayColumns.
    		createColumnSet(TrackDisplayColumns.ColumnSet.PLAYLIST_VIEW, playlistTracksTableView);
        
        /*
         * Populate the display tracks from the list of track IDs in the playlist. This gets
         * control when a playlist is selected in the tree.
         */
        playlistsTreeView.getTreeViewSelectionListeners().add(new TreeViewSelectionListener.Adapter() 
        {
            @Override
            public void selectedNodeChanged(TreeView treeView, Object previousSelectedNode)
            {
                
                /*
                 * Create a list suitable for the setTableData() method.
                 */
                List<HashMap<String, String>> displayTracks = new ArrayList<HashMap<String, String>>();
                
                /*
                 * Get the selected playlist ID.
                 */
            	TreeNode node = (TreeNode) treeView.getSelectedNode();
            	String playlistID = (String) node.getUserData();
            	
            	/*
            	 * Get the selected playlist object.
            	 */
            	Playlist playlist = XMLHandler.getPlaylists().get(playlistID);
            	uiLogger.info("playlist '" + playlist.getName() + "' selected");
            	
            	/*
            	 * Get the track IDs for the selected playlist into a set.
            	 */
            	List<Integer> trackIDs = playlist.getTracks();

            	/*
            	 * Walk the list of track IDs for the selected playlist.
            	 */
            	if (trackIDs != null)
            	{
            		int trackNum = 0;
            		
            		Iterator<Integer> trackIDsIter = trackIDs.iterator();
            		while (trackIDsIter.hasNext())
            		{
            			Integer trackID = trackIDsIter.next();

            			/*
            			 * Get the track for this track ID.
            			 */
            			Integer trackIndex = XMLHandler.getTracksMap().get(trackID);
            			Track track = XMLHandler.getTracks().get(trackIndex);
            			playlistLogger.debug("track ID " + trackID + ", index " + trackIndex +
            					", name " + track.getName() + " found");

            			/*
            			 * Get the display map and add it to the displayable tracks.
            			 */
            			HashMap<String, String> trackAttrs = track.toDisplayMap(++trackNum);
            			displayTracks.add(trackAttrs);
            		}
            	}
                
                /*
                 * Add the tracks to the window table view.
                 */
                playlistTracksTableView.setTableData(displayTracks);
            }
        });
        
        playlistTracksTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @SuppressWarnings("unchecked")
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
                	HashMap<String, String> selectedTrackRowData = 
                			(HashMap<String, String>) table.getSelectedRow();
					
					/*
					 * Create and open the track details popup dialog.
					 */
                	TracksWindow tracksWindowHandler = new TracksWindow();
					tracksWindowHandler.handleTrackDetailsPopup(selectedTrackRowData, display);
            	}
 
                return false;
            }
        });
        
        /*
         * Gather the playlist tree.
         */
        playlistsTreeView.setTreeData(PlaylistTree.createPlaylistTree());
		
		/*
		 * Skin the playlists window.
		 */
		skins.skinMe(Skins.Window.PLAYLISTS);
		
		/*
		 * Push the skinned window onto the skins window stack. It gets popped from our done button press
		 * handler.
		 */
		skins.pushSkinnedWindow(Skins.Window.PLAYLISTS);
        
        /*
         * Open the playlists window.
         */
		uiLogger.info("opening playlists window");
        playlistsWindow.open(display);
    }

    //---------------- Private methods -------------------------------------
    
    /*
     * Initialize BXML variables and collect the list of components to be skinned.
     */
    private void initializeBxmlVariables (List<Component> components) 
    		throws IOException, SerializationException
    {
		uiLogger.trace("initializeBxmlVariables: " + this.hashCode());
		
        BXMLSerializer windowSerializer = new BXMLSerializer();
        playlistsWindow = (Window)windowSerializer.
        		readObject(getClass().getResource("playlistsWindow.bxml"));

        mainMenuBar = 
        		(MenuBar)windowSerializer.getNamespace().get("mainMenuBar");
		components.add(mainMenuBar);
        mainFileMenu = 
        		(Menu)windowSerializer.getNamespace().get("mainFileMenu");
		components.add(mainFileMenu);
        mainEditMenu = 
        		(Menu)windowSerializer.getNamespace().get("mainEditMenu");
		components.add(mainEditMenu);
        playlistsBorder = 
        		(Border)windowSerializer.getNamespace().get("playlistsBorder");
		components.add(playlistsBorder);
        playlistsTreeView = 
        		(TreeView)windowSerializer.getNamespace().get("playlistsTreeView");
		components.add(playlistsTreeView);
        playlistTracksTableView = 
        		(TableView)windowSerializer.getNamespace().get("playlistTracksTableView");
		components.add(playlistTracksTableView);
        playlistTracksTableViewHeader = 
        		(TableViewHeader)windowSerializer.getNamespace().get("playlistTracksTableViewHeader");
		components.add(playlistTracksTableViewHeader);
        actionBorder = 
        		(Border)windowSerializer.getNamespace().get("actionBorder");
		components.add(actionBorder);
        actionBoxPane = 
        		(BoxPane)windowSerializer.getNamespace().get("actionBoxPane");
		components.add(actionBoxPane);
        playlistsDoneButton = 
        		(PushButton)windowSerializer.getNamespace().get("playlistsDoneButton");
		components.add(playlistsDoneButton);
    }
}
