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
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.Window;
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
	
    //---------------- Private variables -----------------------------------

	private Window artistsWindow = null;
	private Skins skins = null;
	private Logger logger = null;
	
	/*
	 * BXML variables.
	 */
	@BXML private Border infoBorder = null;
	@BXML private FillPane infoFillPane = null;
	@BXML private Label numArtistsLabel = null;
	@BXML private Border artistsBorder = null;
	@BXML private TableView artistsTableView = null;
	@BXML private TableView.Column tableColumnArtist = null;
	@BXML private TableView.Column tableColumnNumTracks = null;
	@BXML private TableView.Column tableColumnTotalTime = null;
	@BXML private TableViewHeader artistsTableViewHeader = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
	@BXML private PushButton doneButton = null;
    
    /**
     * Class constructor.
     */
    public ArtistsWindow ()
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
    	
    	logger.trace("ArtistsWindow constructor: " + this.hashCode());
    }
	
    //---------------- Public methods --------------------------------------

	/**
	 * Displays the artists in a new window.
	 * 
	 * @param display display object for managing windows
	 * @throws IOException If an error occurs trying to read the BXML file.
	 * @throws SerializationException If an error occurs trying to 
	 * deserialize the BXML file.
	 */
    public void displayArtists (Display display) 
    		throws IOException, SerializationException
    {
    	logger.trace("displayArtists: " + this.hashCode());
    	
    	if (display == null)
    	{
    		throw new IllegalArgumentException("display argument is null");
    	}
    	
    	/*
    	 * Get the BXML information for the artists window, and generate the list of components
    	 * to be skinned.
    	 */
		List<Component> components = new ArrayList<Component>();
		initializeBxmlVariables(components);
    
		/*
		 * Listener to handle the done button press.
		 */
		doneButton.getButtonPressListeners().add(new ButtonPressListener() 
		{
			@Override
			public void buttonPressed(Button button) 
			{
				logger.info("done button pressed");

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
		 * Add widget texts.
		 */
		doneButton.setButtonData(StringConstants.DONE);
		
		/*
		 * Update column data.
		 */
		tableColumnArtist.setName(StringConstants.TRACK_COLUMN_ARTIST);
		tableColumnArtist.setHeaderData(StringConstants.TRACK_COLUMN_ARTIST);
		tableColumnNumTracks.setName(StringConstants.ARTISTS_NUM_TRACKS_NAME);
		tableColumnNumTracks.setHeaderData(StringConstants.ARTISTS_NUM_TRACKS_HEADER);
		tableColumnTotalTime.setName(StringConstants.ARTISTS_TOTAL_TIME_NAME);
		tableColumnTotalTime.setHeaderData(StringConstants.ARTISTS_TOTAL_TIME_HEADER);

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
		 * Set the number of artists label.
		 */
		numArtistsLabel.setText(StringConstants.ARTISTS_NUM_ARTISTS + XMLHandler.getNumberOfArtists());

		/*
		 * Create a list suitable for the setTableData() method.
		 */
		List<HashMap<String, String>> displayArtists = new ArrayList<HashMap<String, String>>();

		/*
		 * Now walk the artist names, and add all artists to the list.
		 */
		ArrayList<String> artistNames = XMLHandler.getArtistNames();

		Iterator<String> artistNamesIter = artistNames.iterator();
		while (artistNamesIter.hasNext())
		{
			String artistName = artistNamesIter.next();
			
			/*
			 * Get the artist data.
			 */
			List<Integer> artistData = XMLHandler.getArtists().get(artistName.toLowerCase());
			Integer numTracks = artistData.get(XMLHandler.ARTIST_DATA_NUMTRACKS_INDEX);
			Integer totalTime = artistData.get(XMLHandler.ARTIST_DATA_TOTALTIME_INDEX);

			/*
			 * Create the artist row.
			 */
			HashMap<String, String> artistAttrs = new HashMap<String, String>();
			artistAttrs.put(StringConstants.TRACK_COLUMN_ARTIST, artistName);
			artistAttrs.put(StringConstants.ARTISTS_NUM_TRACKS_NAME, numTracks.toString());
			artistAttrs.put(StringConstants.ARTISTS_TOTAL_TIME_NAME, 
					Utilities.convertMillisecondTime(totalTime));
			
			displayArtists.add(artistAttrs);
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
			public void sortChanged(TableView tableView) {
				List<Object> tableDataOfTableView = (List<Object>)tableView.getTableData();
				tableDataOfTableView.setComparator(new ITQTableViewRowComparator(tableView));
			}
		});

		/*
		 * Skin the artists window.
		 */
		skins.skinMe(Skins.Window.ARTISTS);

		/*
		 * Push the skinned window onto the skins window stack. It gets popped from our done button press
		 * handler.
		 */
		skins.pushSkinnedWindow(Skins.Window.ARTISTS);

		/*
		 * Open the artists window.
		 */
		logger.info("opening artists window");
		artistsWindow.open(display);
    }

    //---------------- Private methods -------------------------------------
    
    /*
     * Initialize artists window BXML variables and collect the list of components to be skinned.
     */
    private void initializeBxmlVariables (List<Component> components) 
    		throws IOException, SerializationException
    {
    	logger.trace("initializeBxmlVariables: " + this.hashCode());
    	
        BXMLSerializer windowSerializer = new BXMLSerializer();

    	artistsWindow = (Window)windowSerializer.
    			readObject(getClass().getResource("artistsWindow.bxml"));
        
        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars)artistsWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);

        infoBorder = 
        		(Border)windowSerializer.getNamespace().get("infoBorder");
		components.add(infoBorder);
        infoFillPane = 
        		(FillPane)windowSerializer.getNamespace().get("infoFillPane");
		components.add(infoFillPane);
        numArtistsLabel = 
        		(Label)windowSerializer.getNamespace().get("numArtistsLabel");
		components.add(numArtistsLabel);
		artistsBorder = 
        		(Border)windowSerializer.getNamespace().get("artistsBorder");
		components.add(artistsBorder);
		artistsTableView = 
        		(TableView)windowSerializer.getNamespace().get("artistsTableView");
		components.add(artistsTableView);

		/*
		 * These don't need to be added to the components list because they're subcomponents.
		 */
		tableColumnArtist = 
        		(TableView.Column)windowSerializer.getNamespace().get("tableColumnArtist");
		tableColumnNumTracks = 
        		(TableView.Column)windowSerializer.getNamespace().get("tableColumnNumTracks");
		tableColumnTotalTime = 
        		(TableView.Column)windowSerializer.getNamespace().get("tableColumnTotalTime");
		
		artistsTableViewHeader = 
        		(TableViewHeader)windowSerializer.getNamespace().get("artistsTableViewHeader");
		components.add(artistsTableViewHeader);
        actionBorder = 
        		(Border)windowSerializer.getNamespace().get("actionBorder");
		components.add(actionBorder);
        actionBoxPane = 
        		(BoxPane)windowSerializer.getNamespace().get("actionBoxPane");
		components.add(actionBoxPane);
		
        doneButton = 
        		(PushButton)windowSerializer.getNamespace().get("doneButton");
		components.add(doneButton);
    }
}
