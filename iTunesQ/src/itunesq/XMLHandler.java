package itunesq;

import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that reads and processes the iTunes XML file.
 * 
 * The main public method is processXML(), that uses JDOM to walk through the XML file.
 * 
 * The iTunes XML is rather ridiculous and difficult to work with. See comments scattered
 * throughout for details.
 * 
 * @author Jon
 *
 */
public final class XMLHandler 
{

    //---------------- Class variables -------------------------------------
	
	/*
	 * The lists of tracks and playlists.
	 */
	private static List<Track> Tracks = null;
	private static Map<Integer, Integer> TracksMap = null;
	private static Map<String, Playlist> Playlists = null;
	private static Map<String, String> PlaylistsMap = null;
	
	/*
	 * List of playlist names. This is to provide typing assistance when the user wants to enter a
	 * playlist name.
	 */
	private static ArrayList<String> PlaylistNames = null;
	
	/*
	 * List of track artist names. This is to provide typing assistance when the user wants to enter 
	 * an artist name on a track filter.
	 */
	private static ArrayList<String> ArtistNames = null;
	
    //---------------- Private variables -----------------------------------

	private static String className = XMLHandler.class.getSimpleName();
	private static Logger logger = (Logger) LoggerFactory.getLogger(className + "_XML");
	private static Logging logging = Logging.getInstance();
	
	private static Date XMLDate = null;
	private static int playlistFilteredCount = 0;
	
	/*
	 * Static string definitions.
	 */
	private static final String ELEM_ARRAY    = "array";
	private static final String ELEM_DATE     = "date";
	private static final String ELEM_DICT     = "dict";
	private static final String ELEM_FALSE    = "false";
	private static final String ELEM_INTEGER  = "integer";
	private static final String ELEM_KEY      = "key";
	private static final String ELEM_STRING   = "string";
	private static final String ELEM_TRUE     = "true";
	
	private static final String KEY_DATE      = "Date";
	private static final String KEY_PLAYLISTS = "Playlists";
	private static final String KEY_TRACKS    = "Tracks";
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Get the list of tracks found in the input XML file.
	 * 
	 * @return List of Element objects representing tracks.
	 */
	public static List<Track> getTracks ()
	{
		return Tracks;
	}
	
	/**
	 * Get the mapping of track IDs to track list indices.
	 *  
	 * @return Mapping of track IDs to indices.
	 */
	public static Map<Integer, Integer> getTracksMap ()
	{
		return TracksMap;
	}

	/**
	 * Get the list of playlists found in the input XML file.
	 * 
	 * @return List of Element objects representing playlists.
	 */
	public static Map<String, Playlist> getPlaylists ()
	{
		return Playlists;
	}
	
	/**
	 * Get the mapping of playlist names to playlist IDs.
	 *  
	 * @return Mapping of playlist names to IDs.
	 */
	public static Map<String, String> getPlaylistsMap ()
	{
		return PlaylistsMap;
	}

	/**
	 * Get the list of playlist names found in the input XML file.
	 * 
	 * @return List of playlist names.
	 */
	public static ArrayList<String> getPlaylistNames ()
	{
		return PlaylistNames;
	}

	/**
	 * Get the list of track artist names found in the input XML file.
	 * 
	 * @return List of artist names.
	 */
	public static ArrayList<String> getArtistNames ()
	{
		return ArtistNames;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Initialize logging. This is called once at application initialization.
	 */
	public static void initializeLogging ()
	{
		logging.registerLogger(Logging.Dimension.XML, logger);
	}

	/**
	 * Get the number of tracks found in the input XML file.
	 * 
	 * @return Number of tracks.
	 */
	public static int getNumberOfTracks ()
	{
		return (Tracks != null) ? Tracks.getLength() : 0;
	}

	/**
	 * Get the number of playlists found in the input XML file, reduced by the number of playlists
	 * filtered out.
	 * 
	 * @return Number of playlists.
	 */
	public static int getNumberOfPlaylists ()
	{
		return (Playlists != null) ? Playlists.getCount() - playlistFilteredCount : 0;
	}

	/**
	 * Get the timestamp of the XML file.
	 * 
	 * @return Timestamp string.
	 */
	public static String getXMLFileTimestamp ()
	{
		return (XMLDate != null) ? Utilities.formatDate(XMLDate) : "";
	}
	
	/**
	 * Read and process the XML file.
	 * 
	 * @param xmlFileName The XML file name.
	 * @throws JDOMException
	 */
	public static void processXML (String xmlFileName)
		throws JDOMException
	{
		logger.trace("processXML");
		
        /*
         * Create a SAXBuilder to read the XML file.
         */
    	logger.info("creating SAX builder");
        SAXBuilder jdomBuilder = new SAXBuilder();
  
        /*
         * Build the JDOM document.
         */
        Document jdomDocument;
		try
		{
	    	logger.info("creating JDOM document");
			jdomDocument = jdomBuilder.build(xmlFileName);
			  
	        /*
	         * The first 2 elements look like this:
	         * 
	         * <plist version="...">
	         * <dict>
	         */
	    	logger.info("looking for root elements");
	    	
	        Element root = jdomDocument.getRootElement();
	        Element mainDict = nextSibling(root);
	        if (mainDict == null || !mainDict.getName().equals(ELEM_DICT))
	        {
	        	throw new JDOMException("could not find main <" + ELEM_DICT + "> element");
	        }

	        Element dateKey = null;
	        Element dateValue = null;
	        Element trackKey = null;
	        Element tracksHolder = null;
	        Element playlistKey = null;
	        Element playlistsHolder = null;
	  
	        /*
	         * Get the children of the top level <dict> element. These represent global information
	         * and the lists of tracks and playlists.
	         */
	        List<Element> mainChildren = javaListToPivotList(mainDict);
	        
	        /*
	         * Try to locate the following elements:
	         * 
	         * <key>Date</key>
	         * <key>Tracks</key>
	         * <key>Playlists</key>
	         */
	    	logger.info("looking for global information elements");
	    	
	        Iterator<Element> elementIter = mainChildren.iterator();
	        while (elementIter.hasNext())
	        {
	        	Element elem = elementIter.next();
	        	
	        	if (elem.getName().equals(ELEM_KEY) && elem.getTextTrim().equals(KEY_DATE))
	        	{
	        		dateKey = elem;
	        	}
	        	if (elem.getName().equals(ELEM_KEY) && elem.getTextTrim().equals(KEY_TRACKS))
	        	{
	        		trackKey = elem;
	        	}
	        	if (elem.getName().equals(ELEM_KEY) && elem.getTextTrim().equals(KEY_PLAYLISTS))
	        	{
	        		playlistKey = elem;
	        	}
	        }
	        
	        /*
	         * Locate the following <date> element for the date key.
	         */
	        if (dateKey != null)
	        {
	        	dateValue = nextSibling(dateKey);
		        if (dateValue != null && dateValue.getName().equals(ELEM_DATE))
		        {
		        	try
		    		{
			        	XMLDate = Utilities.parseDate(dateValue.getValue());
		    		}		        	
		        	catch (ParseException e)
		    		{
		        		logger.error("caught " + e.getClass().getSimpleName());
		            	throw new JDOMException("unable to parse date value " + dateValue.getValue());
		    		}
		        }
		        else
		        {
		        	throw new JDOMException(
		        			"could not find <" + ELEM_DATE + "> element after '" + KEY_DATE + "' key");
		        }
	        }
	        else
	        {
	        	throw new JDOMException("could not find '" + KEY_DATE + "' key");
	        }
	        
	        /*
	         * Locate the following <dict> element for the track key, which in turn contains the 
	         * tracks as children.
	         */
	        if (trackKey != null)
	        {
	        	tracksHolder = nextSibling(trackKey);
		        if (tracksHolder == null || !tracksHolder.getName().equals(ELEM_DICT))
		        {
		        	throw new JDOMException(
		        			"could not find <" + ELEM_DICT + "> element after '" + KEY_TRACKS + "' key");
		        }
	        }
	        else
	        {
	        	throw new JDOMException("could not find '" + KEY_TRACKS + "' key");
	        }

	        /*
	         * Locate the following <array> element for the playlists key, which in turn contains the 
	         * playlists as children.
	         */
	        if (playlistKey != null)
	        {
	        	playlistsHolder = nextSibling(playlistKey);
		        if (playlistsHolder == null || !playlistsHolder.getName().equals(ELEM_ARRAY))
		        {
		        	throw new JDOMException(
		        			"could not find <" + ELEM_ARRAY + "> element after '" + KEY_PLAYLISTS + "' key");
		        }
	        }
	        else
	        {
	        	throw new JDOMException("could not find '" + KEY_PLAYLISTS + "' key");
	        }
	  
	        /*
	         * Now gather the actual tracks and playlists.
	         */
	    	logger.info("gathering tracks");
	        generateTracks(tracksHolder);
	        
	    	logger.info("gathering playlists");
	        generatePlaylists(playlistsHolder);
	        
	        /*
	         * We don't want to update track playlists for certain playlist names, identified as
	         * such through a preference. Mark such playlists now.
	         */
	        PlaylistCollection.markPlaylists();
	        
	        /*
	         * Now we can go through all the playlists, and for those not skipped, update the track
	         * playlist information.
	         */
	        PlaylistCollection.updateTrackPlaylistInfo();
		}
		
		catch (IOException | JDOMException e)
		{
    		logger.error("caught " + e.getClass().getSimpleName());
			e.printStackTrace();
		}
    }
	
    //---------------- Private methods -------------------------------------
	
	/*
	 * Create the tracks from the XML file.
	 */
	private static void generateTracks (Element tracksHolder) 
			throws JDOMException
	{
		logger.trace("generateTracks");
		
		/*
		 * Get a list of the XML tracks to work with.
		 */
        List<Element> tracksXML = javaListToPivotList(tracksHolder);
		
		/*
		 * We collect all the tracks into a ArrayList of type Track. Initialize it now. Also,
		 * make sure it's sorted by track name.
		 */
		Tracks = new ArrayList<Track>();
		Tracks.setComparator(new Comparator<Track>() {
            @Override
            public int compare(Track t1, Track t2) {
                return t1.compareTo(t2);
            }
        });
		
		/*
		 * To be able to find a given track easily, we also create a HashMap of the track ID to
		 * the index in the above ArrayList.
		 */
		TracksMap = new HashMap<Integer, Integer>();
		
		/*
		 * Initialize the list of artist names, and set a case-insensitive comparator.
		 */
		ArtistNames = new ArrayList<String>();
		ArtistNames.setComparator(String.CASE_INSENSITIVE_ORDER);

		/*
		 * Walk through the elements of the parent <dict> element.
		 * 
		 * This is tricky stuff. Each track consists of a pair of sibling elements:
		 * - <key>ID number</key>
		 * - <dict> element, whose children are the attributes of the track.
		 * 
		 * We walk all the children expecting this structure, so we use variables that are
		 * expected to be set when the ID key is found, then used after we continue the loop
		 * once the ID key is processed. I know this is hideous.
		 */
    	int ID = 0;
    	Track trackObj = null;

    	logger.debug("starting track loop");
        Iterator<Element> tracksIter = tracksXML.iterator();
        while (tracksIter.hasNext())
        {
        	
        	/*
        	 * Grab the element and process it if it's an ID key.
        	 */
        	Element trackElem = tracksIter.next();
    		if (trackElem.getName().equals(ELEM_KEY))
    		{
    			ID = Integer.valueOf(trackElem.getTextTrim());
    			
    			/*
    			 * Now that the ID key has been processed, iterate the loop to process the sibling
    			 * <dict> element.
    			 */
    			continue;
    		}
    		
    		/*
    		 * Process the <dict> element that is a sibling to the ID key.
    		 */
    		if (trackElem.getName().equals(ELEM_DICT))
    		{
    			
    			/*
    			 * Get the children elements that are the track attributes.
    			 */
    	        List<Element> trackChildren = javaListToPivotList(trackElem);
				
				/*
				 * Initialize the track object.
				 */
    			trackObj = new Track(ID);
    			
    			/*
    			 * Walk through the track attribute elements.
    			 */
    			Iterator<Element> trackChildIter = trackChildren.iterator();
    			while (trackChildIter.hasNext())
    			{
    				Element trackAttr = trackChildIter.next();
    				
    				/*
    				 * The first track attribute element of a pair is expected to be <key>.
    				 */
    				if (trackAttr.getName().equals(ELEM_KEY))
    				{
						
						/*
						 * Process the track attributes we care about, and ignore the rest.
						 */
						String keyValue = trackAttr.getTextTrim();
						switch (keyValue)
						{
						case "Name":
							trackObj.setName(nextStringValue(trackChildIter, keyValue));
							break;

						case "Artist":
							trackObj.setArtist(nextStringValue(trackChildIter, keyValue));
							break;

						case "Composer":
							trackObj.setComposer(nextStringValue(trackChildIter, keyValue));
							break;

						case "Album":
							trackObj.setAlbum(nextStringValue(trackChildIter, keyValue));
							break;

						case "Genre":
							trackObj.setGenre(nextStringValue(trackChildIter, keyValue));
							break;

						case "Kind":
							trackObj.setKind(nextStringValue(trackChildIter, keyValue));
							break;

						case "Size":
							trackObj.setSize(nextIntValue(trackChildIter, keyValue));
							break;

						case "Total Time":
							trackObj.setDuration(nextIntValue(trackChildIter, keyValue));
							break;

						case "Year":
							trackObj.setYear(nextIntValue(trackChildIter, keyValue));
							break;

						case "Date Modified":
							trackObj.setModified(nextDateValue(trackChildIter, keyValue));
							break;

						case "Date Added":
							trackObj.setDateAdded(nextDateValue(trackChildIter, keyValue));
							break;

						case "Bit Rate":
							trackObj.setBitRate(nextIntValue(trackChildIter, keyValue));
							break;

						case "Sample Rate":
							trackObj.setSampleRate(nextIntValue(trackChildIter, keyValue));
							break;

						case "Play Count":
							trackObj.setPlayCount(nextIntValue(trackChildIter, keyValue));
							break;

						case "Release Date":
							trackObj.setReleased(nextDateValue(trackChildIter, keyValue));
							break;

						case "Rating":
							trackObj.setRating(nextIntValue(trackChildIter, keyValue));
							break;
							
						/*
						 * We need to skip over the sibling element for all attributes we
						 * don't process.
						 */
						default:
							trackAttr = trackChildIter.next();
						}
    				}
    	    		else
    	    		{
    	    			throw new JDOMException(
    	    					"<" + ELEM_DICT + "> element child is not <" + ELEM_KEY + ">");
    	    		}
    			}
    			
    			/*
    			 * Add the track object to the list.
    			 */
    			Tracks.add(trackObj);
    	    	logger.debug("found track ID " + ID + ", name " + trackObj.getName());
    		}
    		else
    		{
    			throw new JDOMException(
    					"did not find <" + ELEM_DICT + "> element after track ID " + ID);
    		}    		

    		/*
    		 * Add the artist name to the list of such names, if it ain't already there.
    		 */
    		String artist = trackObj.getArtist();
    		if (artist != null)
    		{
    			int index = ArrayList.binarySearch(ArtistNames, artist, ArtistNames.getComparator());
    			if (index < 0)
    			{
    				ArtistNames.add(artist);
    			}
    		}
    		
    		/*
    		 * Reset these hideous variables for the next pair of elements.
    		 */
        	ID = 0;
        	trackObj = null;
        }
        
        /*
         * Generate the track ID to index mapping. We have to wait until all tracks have been found and
         * sorted in order for the indices to be correct.
         */
        int index = 0;
        Iterator<Track> trackIter = Tracks.iterator();
        while (trackIter.hasNext())
        {
        	Track track = trackIter.next();

        	int trackID = track.getID();
			TracksMap.put(trackID, index++);
			logger.debug("mapped track ID " + trackID + " to index " + index);
        }
	}
	
	/*
	 * Create the playlists from the XML file.
	 */
	private static void generatePlaylists (Element playlistsHolder) 
			throws JDOMException
	{
		logger.trace("generatePlaylists");
		
		/*
		 * Reset the playlist filtered count, so it doesn't keep growing if we reread the XML file.
		 */
		playlistFilteredCount = 0;
		
		/*
		 * Get a list of the XML playlists to work with.
		 */
        List<Element> playlistsXML = javaListToPivotList(playlistsHolder);
		
		/*
		 * We collect all the playlists into a HashMap of type Playlist. Initialize it now.
		 */
		Playlists = new HashMap<String, Playlist>();
		
		/*
		 * To be able to find a given playlist by name, we also create a HashMap of the playlist
		 * name to ID.
		 */
		PlaylistsMap = new HashMap<String, String>();
		
		/*
		 * Initialize the list of playlist names, and set a case-insensitive comparator.
		 */
		PlaylistNames = new ArrayList<String>();
		PlaylistNames.setComparator(String.CASE_INSENSITIVE_ORDER);

		/*
		 * Walk through the elements of the parent <array> element.
		 */
    	logger.debug("starting playlist loop");
        Iterator<Element> playlistsIter = playlistsXML.iterator();
        while (playlistsIter.hasNext())
        {
        	Element playlistElem = playlistsIter.next();

        	/*
        	 * Create a new playlist object and add it to the playlists collection.
        	 */
        	Playlist playlistObj = new Playlist();
    			
        	/*
        	 * Get the children elements that are the playlist attributes.
        	 */
            List<Element> playlistChildren = javaListToPivotList(playlistElem);

        	/*
        	 * Walk through the playlist attribute elements.
        	 */
        	Iterator<Element> playlistChildIter = playlistChildren.iterator();
        	while (playlistChildIter.hasNext())
        	{
        		Element playlistAttr = playlistChildIter.next();

        		/*
        		 * The first playlist attribute element of a pair is expected to be <key>.
        		 */
        		if (playlistAttr.getName().equals(ELEM_KEY))
        		{

    				/*
    				 * Process the playlist attributes we care about, and ignore the rest.
    				 */
    				String keyValue = playlistAttr.getTextTrim();
    				switch (keyValue)
    				{
    				case "Name":
    					String plName = nextStringValue(playlistChildIter, keyValue);
    					playlistObj.setName(plName);
    					
    					/*
    					 * If this playlist is to be filtered out, indicate so.
    					 */
    					if (PlaylistCollection.isPlaylistFiltered(plName))
    					{
    						playlistObj.setFilteredOut(true);
    						playlistFilteredCount++;
    					}
    					break;

    				case "Playlist Persistent ID":
    					playlistObj.setPersistentID(nextStringValue(playlistChildIter, keyValue));
    					break;

    				case "Folder":
    					playlistObj.setIsFolder(nextBooleanValue(playlistChildIter, keyValue));
    					break;

    				case "Parent Persistent ID":
    					playlistObj.setParentPersistentID(nextStringValue(playlistChildIter, keyValue));
    					break;

    				case "Playlist Items":
    					
    					/*
    					 * Gather the playlist track IDs.
    					 */
    					List<Integer> playlistTracks = new LinkedList<Integer>();
    					playlistTracks = gatherPlaylistTracks(playlistAttr);
    					
    					playlistObj.setTracks(playlistTracks);
    					
    					/*
    					 * Skip over the entire playlist tracks element.
    					 */
    					playlistAttr = playlistChildIter.next();
    					break;
						
					/*
					 * We need to skip over the sibling element for all attributes we
					 * don't process.
					 */
    				default:
    					playlistAttr = playlistChildIter.next();
    				}
        		}
        		else
        		{
        			throw new JDOMException(
        					"<" + ELEM_DICT + "> element child is not <" + ELEM_KEY + ">");
        		}
        	}
        	
        	/*
        	 * Add the playlist object to the collection.
        	 */
        	Playlists.put(playlistObj.getPersistentID(), playlistObj);
        	PlaylistsMap.put(playlistObj.getName(), playlistObj.getPersistentID());
	    	logger.debug("found playlist name " + playlistObj.getName());
        	
        	/*
        	 * If the playlist is not filtered out, add its name to the playlist name list.
        	 */
        	if (playlistObj.getFilteredOut() == false)
        	{
        		PlaylistNames.add(playlistObj.getName());
        	}
        }
	}
	
	/*
	 * Gather the list of tracks for a playlist.
	 * 
	 * Once again we have XML ridiculousness to deal with. The playlist tracks are laid out like so:
	 * 
	 * <key>Playlist Items</key>
	 * <array>
	 *     <dict>
	 *         <key>Track ID</key><integer>nnnn</integer>
	 *     </dict>
	 * </array>
	 * 
	 * We're passed the element for 'Playlist Items'.
	 */
	private static List<Integer> gatherPlaylistTracks (Element playlistTracksKeyElem) 
			throws JDOMException
	{
		logger.trace("gatherPlaylistTracks");
		
		List<Integer> playlistTracks = new LinkedList<Integer>();
		
		/*
		 * Get the next sibling, which should be the <array> element.
		 */
    	Element playlistTracksHolder = nextSibling(playlistTracksKeyElem);
        if (playlistTracksHolder == null || !playlistTracksHolder.getName().equals(ELEM_ARRAY))
        {
        	throw new JDOMException(
        			"could not find <" + ELEM_ARRAY + "> element after '" + 
        	         playlistTracksKeyElem.getTextTrim() + "' key");
        }

        /*
         * Get the children of the <array>, which is a list of <dict> elements.
         */
        List<Element> playlistTracksDictList = javaListToPivotList(playlistTracksHolder);

		/*
		 * Walk the <dict> list.
		 */
        Iterator<Element> playlistTracksDictListIter = playlistTracksDictList.iterator();
        while (playlistTracksDictListIter.hasNext())
        {
        	Element playlistTracksDictElem = playlistTracksDictListIter.next();

        	/*
        	 * Get the children of this <dict> element, which are the playlist track attributes.
        	 * Yay.
        	 */
            List<Element> playlistTrackAttrs = javaListToPivotList(playlistTracksDictElem);
    		
    		/*
    		 * Walk the list of playlist track attributes.
    		 */
            Iterator<Element> playlistTrackAttrsIter = playlistTrackAttrs.iterator();
            while (playlistTrackAttrsIter.hasNext())
            {
            	Element playlistTracksAttr = playlistTrackAttrsIter.next();
            	
            	/*
            	 * The first element of the pair is expected to be <key>.
            	 */
        		if (playlistTracksAttr.getName().equals(ELEM_KEY))
        		{

    				/*
    				 * The name of the <key> is expected to be "Track ID".
    				 */
    				String keyValue = playlistTracksAttr.getTextTrim();
    				
    				if (keyValue.equals("Track ID"))
    				{
    					
    					/*
    					 * So we finally have the track ID. Add it to the collection we will return.
    					 */
    					Integer playlistTrackID = 
    							new Integer(nextIntValue(playlistTrackAttrsIter, keyValue));
    					playlistTracks.add(playlistTrackID);    					
    				}
            		else
            		{
            			throw new JDOMException("'Playlist Items' child is not 'Track ID");
            		}
        		}
        		else
        		{
        			throw new JDOMException(
        					"<" + ELEM_DICT + "> element child is not <" + ELEM_KEY + ">");
        		}
            }
        }
        
        return playlistTracks;
	}
	
	/*
	 * Guess I'm stupid, because I could not find a way to easily convert java.util.List to
	 * Pivot's List. Hence this ridiculous method :(
	 */
	private static List<Element> javaListToPivotList (Element elem)
	{
		List<Element> listChildren = new ArrayList<Element>();
		
        java.util.List<Element> xmlChildren = elem.getChildren();		
		ListIterator<Element> xmlIter = xmlChildren.listIterator();
		while (xmlIter.hasNext())
		{
			Element childElem = xmlIter.next();
			listChildren.add(childElem);
		}
		
		return listChildren;
	}
	
	/*
	 * JDOM doesn't include a method to find sibling elements. But we need to locate the next
	 * sibling in many cases, due to the weird structure of the iTunes XML. 
	 */
	private static Element nextSibling (Element current) 
			throws JDOMException
	{
		Element nextSibling = null;
		
		try
		{
			
			/*
			 * If the current element is root, just grab the next element (1).
			 */
			if (current.isRootElement())
			{
				nextSibling = (Element) current.getContent(1);
			}

			/*
			 * For non-root elements, get the parent's child list, locate the current element
			 * within that list, then grab the next sequential element.
			 */
			else
			{
				Element parent = current.getParentElement();
				List<Element> children = javaListToPivotList(parent);
				int myIndex = children.indexOf(current);

				if(myIndex >= 0 && myIndex < children.getLength() - 1) 
				{
					nextSibling = (Element)children.get(myIndex + 1);
				}
			}
		}
		catch (IndexOutOfBoundsException e)
		{
    		logger.error("caught " + e.getClass().getSimpleName());
        	throw new JDOMException(
        			"expected sibling element not found after " + current.getTextTrim());
		}
		
		return nextSibling;
	}
	
	/*
	 * Track and playlist attributes come in sibling pairs, for example:
	 * 
	 * <key>Name</key><string>Silence Is Golden</string>
	 * 
	 * We need to process these with an iterator, so can't use getNextSibling() to find the sibling
	 * with the attribute value. This method uses the iterator to get the value from the second
	 * element, so as to keep the integrity of the iterator.
	 * 
	 * Note that getNextSibling() is still needed for cases when we don't have an iterator.
	 */
	private static String nextStringValue (Iterator<Element> trackChildIter, String keyName) 
			throws JDOMException
	{
		Element nextTrackAttr = trackChildIter.next();
		if (!nextTrackAttr.getName().equals(ELEM_STRING))
		{
        	throw new JDOMException(
        			"expected <" + ELEM_STRING + "> element not found after '" + keyName + "' key");
		}
		
		return nextTrackAttr.getTextTrim();
	}
	
	/*
	 * See getNextStringValue() for more information.
	 */
	private static int nextIntValue (Iterator<Element> trackChildIter, String keyName) 
			throws JDOMException
	{
		Element nextTrackAttr = trackChildIter.next();
		if (!nextTrackAttr.getName().equals(ELEM_INTEGER))
		{
        	throw new JDOMException(
        			"expected <" + ELEM_INTEGER + "> element not found after '" + keyName + "' key");
		}
		
		return Integer.valueOf(nextTrackAttr.getTextTrim());
	}
	
	/*
	 * See getNextStringValue() for more information.
	 */
	private static boolean nextBooleanValue (Iterator<Element> trackChildIter, String keyName) 
			throws JDOMException
	{
		Element nextTrackAttr = trackChildIter.next();
		if (nextTrackAttr.getName().equals(ELEM_TRUE))
		{
			return true;
		}
		else if (nextTrackAttr.getName().equals(ELEM_TRUE))
		{
			return false;
		}
		else
		{
        	throw new JDOMException(
        			"expected <" + ELEM_TRUE + "> or <" + ELEM_FALSE + "> element not found after '" + keyName + "' key");
		}
	}
	
	/*
	 * See getNextStringValue() for more information.
	 */
	private static Date nextDateValue (Iterator<Element> trackChildIter, String keyName) 
			throws JDOMException
	{
		Element nextTrackAttr = trackChildIter.next();
		if (!nextTrackAttr.getName().equals(ELEM_DATE))
		{
        	throw new JDOMException(
        			"expected <" + ELEM_DATE + "> element not found after '" + keyName + "' key");
		}
		
    	try
		{
    		return Utilities.parseDate(nextTrackAttr.getTextTrim());
		}		        	
    	catch (ParseException e)
		{
    		logger.error("caught " + e.getClass().getSimpleName());
        	throw new JDOMException("unable to parse date value " + nextTrackAttr.getTextTrim());
		}
	}
}