package itunesq;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
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
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the query playlists window. This window allows the user
 * to compare a set of playlists. For example, this can be used to find 
 * intersecting tracks on multiple playlists.
 * 
 * @author Jon
 *
 */
public class QueryPlaylistsWindow
{

    //---------------- Private variables -----------------------------------
	
    private Window queryPlaylistsWindow = null;
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
	
	/*
	 * BXML variables.
	 */
	@BXML private Border compareBorder = null;
	@BXML private BoxPane compareBoxPane = null;
	@BXML private Label compareBorderLabel = null;
	@BXML private TablePane compareTablePane = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
	@BXML private PushButton showAllButton = null;
	@BXML private PushButton showSomeButton = null;
	@BXML private PushButton showOneButton = null;
	@BXML private PushButton queryDoneButton = null;
	
	/*
	 * Type of playlist comparison.
	 */
	private enum CompareType
	{
		ALL, SOME, ONE;
	}
    
    /**
     * Class constructor.
     */
    public QueryPlaylistsWindow ()
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

    //---------------- Public methods --------------------------------------
    
	/**
	 * Displays the query playlists in a new window.
	 * 
	 * @param display display object for managing windows
	 * @throws IOException If an error occurs trying to read the BXML file.
	 * @throws SerializationException If an error occurs trying to 
	 * deserialize the BXML file.
	 */
    public void displayQueryPlaylists (Display display) 
    		throws IOException, SerializationException
    {
    	uiLogger.trace("displayQueryPlaylists: " + this.hashCode());
    	
    	/*
    	 * Get the BXML information for the query playlists window, and generate the list of components
    	 * to be skinned.
    	 */
		List<Component> components = new ArrayList<Component>();
		initializeBxmlVariables(components);
        
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
            		good2Go = evaluateComparison();
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
						uiLogger.error("caught " + e.getClass().getSimpleName());
						e.printStackTrace();
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
            		good2Go = evaluateComparison();
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
						uiLogger.error("caught " + e.getClass().getSimpleName());
						e.printStackTrace();
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
            		good2Go = evaluateComparison();
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
						uiLogger.error("caught " + e.getClass().getSimpleName());
						e.printStackTrace();
					}
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
         * Add widget texts.
         */
        compareBorderLabel.setText(StringConstants.QUERY_PLAYLIST_COMPARE_BORDER);
        showAllButton.setButtonData(StringConstants.QUERY_PLAYLIST_SHOW_ALL_BUTTON);
        showAllButton.setTooltipText(StringConstants.QUERY_PLAYLIST_SHOW_ALL_BUTTON_TIP);
        showSomeButton.setButtonData(StringConstants.QUERY_PLAYLIST_SHOW_SOME_BUTTON);
        showSomeButton.setTooltipText(StringConstants.QUERY_PLAYLIST_SHOW_SOME_BUTTON_TIP);
        showOneButton.setButtonData(StringConstants.QUERY_PLAYLIST_SHOW_ONE_BUTTON);
        showOneButton.setTooltipText(StringConstants.QUERY_PLAYLIST_SHOW_ONE_BUTTON_TIP);
        queryDoneButton.setButtonData(StringConstants.DONE);
		
		/*
		 * Set the window title.
		 */
		queryPlaylistsWindow.setTitle(Skins.Window.QUERYPLAYLISTS.getDisplayValue());
        
        /*
         * Add the initial query playlist rows. We add 2 rows because we need at least that 
         * many to compare. This populates the component list with table row components.
         */
		for (int i = 0; i < 2; i++)
		{
			TablePane.Row newRow = createPlaylistTableRow(components);
			compareTablePane.getRows().add(newRow);
		}
		
		/*
		 * Since we start with only 2 rows, disable the 'some' button. It gets enabled only if
		 * more rows are added.
		 */
		showSomeButton.setEnabled(false);
		
		/*
		 * Indicate we need to evaluate the comparison. Since that process is rather involved, we 
		 * don't want to do the evaluation each time one of the buttons is pressed, unless some
		 * change was made to the comparison objects, such as changing a playlist name or adding
		 * or removing a row. We start out here with the switch true, then set it false once the
		 * evaluation process is complete. If a change is made between pressing buttons, the
		 * switch is set true again.
		 */
		evaluateComparisonNeeded = true;
		
    	/*
    	 * Now register the query playlists window skin elements.
    	 */
		Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
		skins.registerWindowElements(Skins.Window.QUERYPLAYLISTS, windowElements);
		
		/*
		 * Skin the query playlists window.
		 */
		skins.skinMe(Skins.Window.QUERYPLAYLISTS);
		
		/*
		 * Push the skinned window onto the skins window stack. It gets popped from our done button press
		 * handler.
		 */
		skins.pushSkinnedWindow(Skins.Window.QUERYPLAYLISTS);
    	
    	/*
    	 * Open the query playlists window.
    	 */
    	uiLogger.info("opening query playlists window");
    	queryPlaylistsWindow.open(display);
    	
    	/*
    	 * Request focus for the table pane, so that the user can type in the first row.
    	 */
    	compareTablePane.requestFocus();
    }

    //---------------- Private methods -------------------------------------
    
    /*
     * Create and add a playlist row.
     * 
     * NOTE: This method populates the input list of components with the components of a row.
     */
    private TablePane.Row createPlaylistTableRow (List<Component> components)
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
    	 * Add a text input listener so we can call the typing assistant to fill in the playlist 
    	 * name as soon as enough characters are entered.
    	 * 
    	 * Also, set the evaluate comparison switch true if the text is mucked with in any way.
    	 */
    	text.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
    	{
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
        		evaluateComparisonNeeded = true;
        		
            	Utilities.typingAssistant(textInput, XMLHandler.getPlaylistNames(), 
            			textInput.getText(), Filter.Operator.IS);
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
    	 * Create the set of buttons:
    	 * 
    	 * '+' = insert a new row after this one
    	 * '-' = delete this row
    	 */
    	PushButton plusButton = new PushButton();
    	plusButton.setButtonData("+");
    	plusButton.setTooltipText(StringConstants.QUERY_PLAYLIST_PLUS_BUTTON);
    	
    	PushButton minusButton = new PushButton();
    	minusButton.setButtonData("-");
    	minusButton.setTooltipText(StringConstants.QUERY_PLAYLIST_MINUS_BUTTON);
        
    	/*
    	 * We need the ability to insert and remove playlist rows based on the specific 
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
        		evaluateComparisonNeeded = true;
        		
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
            		TablePane tablePane = (TablePane) parent;
                    int playlistRowIndex = tablePane.getRowAt(plusButtonYCoordinate);
                    uiLogger.info("plus button pressed for playlist index " + playlistRowIndex);
                    
                    /*
                     * Add the table row and collect the components that need to be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                	TablePane.Row tableRow = createPlaylistTableRow(rowComponents);
            		compareTablePane.getRows().insert(tableRow, playlistRowIndex + 1);
            		
                	/*
                	 * Register the new components and skin them.
                	 */
            		Map<Skins.Element, List<Component>> windowElements = 
            				skins.mapComponentsToSkinElements(rowComponents);            		
            		skins.registerDynamicWindowElements(Skins.Window.QUERYPLAYLISTS, windowElements);
            		skins.skinMe(Skins.Window.QUERYPLAYLISTS);
            		
            		/*
            		 * Enable or disable the 'some' button based on the current number of table rows.
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
                    int playlistRowIndex = tablePane.getRowAt(minusButtonYCoordinate);
                    uiLogger.info("minus button pressed for playlist index " + playlistRowIndex);
                    
                    /*
                     * Remove the table row.
                     */
                    compareTablePane.getRows().remove(playlistRowIndex, 1);
            		
            		/*
            		 * Enable or disable the 'some' button based on the current number of table rows.
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
                	
                    queryPlaylistsWindow.repaint();
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
     * Collect the entered playlists.
     */
    private List<String> collectPlaylists ()
    {
    	uiLogger.trace("collectPlaylists: " + this.hashCode());
    	
    	/*
    	 * Indexes into the row elements.
    	 * 
    	 * IMPORTANT: These must match the design of the row. See queryPlaylistsWindow.bxml for the 
    	 * column definition, and createPlaylistTableRow() for the logic to create a row.
    	 */
    	final int textIndex = 1;
    	
    	/*
    	 * Iterate through the playlist table rows.
    	 */
    	List<String> playlists = new ArrayList<String>();
    	TablePane.RowSequence rows = compareTablePane.getRows();
    	Iterator<TablePane.Row> rowsIterator = rows.iterator();
    	while (rowsIterator.hasNext())
    	{
    		TablePane.Row row = rowsIterator.next();
			
			/*
			 * Get the text input.
			 */
			TextInput text = (TextInput) row.get(textIndex);
			
			/*
			 * Add this playlist to the collection.
			 */
			playlists.add(text.getText());
    	}
    	
    	return playlists;
    }
    
    /*
     * Evaluate the set of playlists to be compared.
     * 
     * We only do this once per set of playlists, creating all of the sets of track IDs at once.
     * The different display buttons then just need to display the results (see 
     * displayComparedPlaylistTracks() method). However, if any change is made to the set
     * of playlists, a switch is set to run this method again to re-evaluate the comparison.
     */
    private boolean evaluateComparison ()
    {
    	filterLogger.trace("evaluateComparison: " + this.hashCode());

		boolean playlistsValid = true;
    	
    	/*
    	 * Collect the playlists from the window.
    	 */
		List<String> playlists = collectPlaylists();
		
		/*
		 * Initialize lists for all compare types.
		 */
		allIDs = new HashSet<PlaylistComparisonTrack>();
		someIDs = new HashSet<PlaylistComparisonTrack>();
		oneIDs = new HashSet<PlaylistComparisonTrack>();
		
		/*
		 * Walk through the specified playlists.
		 * 
    	 * We make 2 passes to establish all compare lists. In the first pass, we walk through
    	 * the list of track IDs for all playlists, and create the 'one' list and a 
    	 * potential 'some' list.
    	 * 
    	 * We also create the query string during the first pass. This is passed to TracksWindow when
    	 * the tracks are displayed.
    	 */
		int playlistLoopIndex = 0;
		StringBuilder query = new StringBuilder();
		
		Iterator<String> playlistsIter = playlists.iterator();
		while (playlistsIter.hasNext())
		{
    		filterLogger.debug("playlist index: " + playlistLoopIndex);
    		
			String playlistName = playlistsIter.next();
			
			/*
			 * Add this playlist to the query string, along with a "+" separator for all but the
			 * first.
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
				Alert.alert(MessageType.ERROR, 
    					StringConstants.ALERT_PLAYLIST_INVALID_NAME_1 + playlistName + 
    					StringConstants.ALERT_PLAYLIST_INVALID_NAME_2, queryPlaylistsWindow);
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
        	Iterator<Integer> trackIDsIter = trackIDs.iterator();
        	while (trackIDsIter.hasNext())
        	{
        		Integer trackID = trackIDsIter.next();
        		filterLogger.debug("track ID: " + trackID);
    			PlaylistComparisonTrack existingTrack;
    			PlaylistComparisonTrack newTrack;
    			
    			/*
    			 * Everything in the first playlist goes unconditionally on the 'one' list, by
    			 * definition.
    			 */
    			if (playlistLoopIndex == 0)
    			{
        			newTrack = new PlaylistComparisonTrack();
    				newTrack.setTrackID(trackID);
    				newTrack.setPlaylistCount(1);

    				filterLogger.debug("add to 'one' ID: " + trackID);
    				oneIDs.add(newTrack);
    				continue;
    			}
        		
        		/*
        		 * If this ID already exists in the 'some' list, then bump its playlist count. 
        		 * Otherwise, keep checking.
        		 * 
        		 * NOTE: This can only happen once we reach the third playlist, if it exists, so bypass
        		 * the check if the loop index is too small for that.
        		 */
        		if (playlistLoopIndex >= 2 
        				&& (existingTrack = findTrackID(someIDs, trackID)) != null)
        		{
        			Integer updatedCount = existingTrack.getPlaylistCount() + 1;
        			filterLogger.debug("bump 'some' count to " + updatedCount + " for ID: " + trackID);
    				existingTrack.setPlaylistCount(updatedCount);
        		}
        		else
        		{
        			
        			/*
        			 * If this ID exists in the 'one' list, then remove it and add it instead
        			 * to the 'some' list (and bump its count).
        			 */
        			if ((existingTrack = findTrackID(oneIDs, trackID)) != null)
        			{
            			Integer updatedCount = existingTrack.getPlaylistCount() + 1;
        				filterLogger.debug("move to 'some' ID: " + trackID + " with count " + updatedCount);
        				oneIDs.remove(existingTrack);
        				existingTrack.setPlaylistCount(updatedCount);
        				someIDs.add(existingTrack);
        			}
        			
        			/*
        			 * This ID does not exist in either list, so add it to the 'one' list.
        			 */
        			else
        			{
            			newTrack = new PlaylistComparisonTrack();
        				newTrack.setTrackID(trackID);
        				newTrack.setPlaylistCount(1);

        				filterLogger.debug("add to 'one' ID: " + trackID);
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
	    	 * For the second pass, walk through the potential 'some' list. Move all IDs that have a
	    	 * count equal to the number of playlists to the 'all' list.
	    	 * 
	    	 * NOTE: if we only have 2 playlists to compare, then all IDs should move to the 'all'
	    	 * list naturally.
	    	 * 
	    	 * ANOTHER NOTE: I had to break this into 2 pieces: one to add the appropriate IDs to 
	    	 * the 'all' list, and another to remove them from the 'some' list. Otherwise, the 
	    	 * underlying HashSet (in turn implemented by a HashMap) throws an exception. The problem
	    	 * is that invoking remove() while using the iterator on the 'some' list doesn't work.
	    	 * It's supposed to, but doesn't. Perhaps using native Java classes instead of Pivot
	    	 * classes would work, but the below workaround solves the issue. Even though it's yucky.
	    	 */
	    	Iterator<PlaylistComparisonTrack> someIDsIter = someIDs.iterator();
	    	while (someIDsIter.hasNext())
	    	{
	    		PlaylistComparisonTrack pcTrack = someIDsIter.next();
	    		
	    		if (pcTrack.getPlaylistCount() == playlists.getLength())
	    		{
					filterLogger.debug("add to 'all' ID: " + pcTrack.getTrackID());
	    			allIDs.add(pcTrack);
	    		}
	    	}
	    	
	    	Iterator<PlaylistComparisonTrack> allIDsIter = allIDs.iterator();
	    	while (allIDsIter.hasNext())
	    	{
	    		PlaylistComparisonTrack pcTrack = allIDsIter.next();

				filterLogger.debug("remove from 'some' ID: " + pcTrack.getTrackID());
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
     * Display the set of tracks from the comparison evaluation according to which button was
     * pressed (compareType).
     */
    private void displayComparedPlaylistTracks (Display display, CompareType compareType) 
    		throws IOException, SerializationException
    {
    	filterLogger.trace("displayComparedPlaylistTracks: " + this.hashCode());
		
		/*
		 * Initialize a list of track objects, and sort it by name.
		 */
		List<Track> displayableTracks = new ArrayList<Track>();
		displayableTracks.setComparator(new Comparator<Track>() {
            @Override
            public int compare(Track t1, Track t2) {
                return t1.compareTo(t2);
            }
        });
		
		/*
		 * Walk through the appropriate list of track IDs.
		 */
		Iterator<PlaylistComparisonTrack> trackIDsIter = null;
		String compareStr = null;
		switch (compareType)
		{
		case ALL:
			trackIDsIter = allIDs.iterator();
			compareStr = StringConstants.QUERY_PLAYLIST_COMPARE_ALL;
			break;
			
		case SOME:
			trackIDsIter = someIDs.iterator();
			compareStr = StringConstants.QUERY_PLAYLIST_COMPARE_SOME;
			break;
			
		case ONE:
			trackIDsIter = oneIDs.iterator();
			compareStr = StringConstants.QUERY_PLAYLIST_COMPARE_ONE;
			break;
			
		default: ;
		}
		
		/*
		 * Walk through the appropriate list of track IDs.
		 */
		while (trackIDsIter.hasNext())
		{
			PlaylistComparisonTrack pcTrack = trackIDsIter.next();
			
			/*
			 * Add the associated track object to the list of tracks.
			 */
			Integer trackIndex = XMLHandler.getTracksMap().get(pcTrack.getTrackID());
			Track track = XMLHandler.getTracks().get(trackIndex);
			displayableTracks.add(track);
		}
		
		/*
		 * Now display the list of tracks.
		 */
		TracksWindow tracksWindowHandler = new TracksWindow();
		tracksWindowHandler.displayTracks(display, displayableTracks, 
				TracksWindow.QueryType.PLAYLISTS, 
				TracksWindow.QueryType.PLAYLISTS.getDisplayValue() + " " + compareStr + ": " + queryStr);
    }
    
    /*
     * Check if a given track ID is contained in a comparison set. If so, return it; otherwise
     * return null.
     */
    private PlaylistComparisonTrack findTrackID (Set<PlaylistComparisonTrack> trackIDs, Integer trackID)
    {
    	filterLogger.trace("findTrackID: " + this.hashCode());
    	
    	PlaylistComparisonTrack target = null;
    	
    	/*
    	 * Walk through the input comparison set.
    	 */
    	Iterator<PlaylistComparisonTrack> trackIDsIter = trackIDs.iterator();
    	while (trackIDsIter.hasNext())
    	{
    		PlaylistComparisonTrack pcTrack = trackIDsIter.next();
    		
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
     * Initialize BXML variables and collect the list of components to be skinned.
     */
    private void initializeBxmlVariables (List<Component> components) 
    		throws IOException, SerializationException
    {
		uiLogger.trace("initializeBxmlVariables: " + this.hashCode());
		
        BXMLSerializer windowSerializer = new BXMLSerializer();
        queryPlaylistsWindow = (Window)windowSerializer.
        		readObject(getClass().getResource("queryPlaylistsWindow.bxml"));
        
        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars)queryPlaylistsWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);
        
        compareBorder = 
        		(Border)windowSerializer.getNamespace().get("compareBorder");
		components.add(compareBorder);
        compareBoxPane = 
        		(BoxPane)windowSerializer.getNamespace().get("compareBoxPane");
		components.add(compareBoxPane);
        compareBorderLabel = 
        		(Label)windowSerializer.getNamespace().get("compareBorderLabel");
		components.add(compareBorderLabel);
        compareTablePane = 
        		(TablePane)windowSerializer.getNamespace().get("compareTablePane");
		components.add(compareTablePane);
        actionBorder = 
        		(Border)windowSerializer.getNamespace().get("actionBorder");
		components.add(actionBorder);
        actionBoxPane = 
        		(BoxPane)windowSerializer.getNamespace().get("actionBoxPane");
		components.add(actionBoxPane);
        showAllButton = 
        		(PushButton)windowSerializer.getNamespace().get("showAllButton");
		components.add(showAllButton);
        showSomeButton = 
        		(PushButton)windowSerializer.getNamespace().get("showSomeButton");
		components.add(showSomeButton);
        showOneButton = 
        		(PushButton)windowSerializer.getNamespace().get("showOneButton");
		components.add(showOneButton);
        queryDoneButton = 
        		(PushButton)windowSerializer.getNamespace().get("queryDoneButton");
		components.add(queryDoneButton);
    }
}
