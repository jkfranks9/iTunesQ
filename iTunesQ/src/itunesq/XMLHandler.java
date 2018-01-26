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
 * <p>
 * This is a final class consisting entirely of static methods.
 * <p>
 * The main public method is <code>processXML</code>, that uses JDOM to walk 
 * through the XML file. I'd like to note that the iTunes XML is rather 
 * ridiculous and difficult to work with. Just saying.
 * 
 * @author Jon
 *
 */
public final class XMLHandler 
{

    //---------------- Class variables -------------------------------------
	
	/*
	 * The list of tracks; just a simple list.
	 */
	private static ArrayList<Track> Tracks = null;
	
	/*
	 * The tracks map is a map of the track ID to its index value in the tracks list. This 
	 * means it can't be created until the entire list has been created and sorted. This map
	 * facilitates quick searches of a track given its track ID (tracks within a playlist are
	 * identified only by ID).
	 */
	private static Map<Integer, Integer> TracksMap = null;
	
	/*
	 * The duplicates map is a map of the track name to a list of track IDs. This allows us 
	 * to find duplicates quickly on demand, at the cost of longer time to process the XML
	 * file. 
	 */
	private static Map<String, List<Integer>> DuplicatesMap = null;
	
	/*
	 * The list of playlists. This is a map of the playlist ID to its corresponding Playlist
	 * object.
	 */
	private static Map<String, Playlist> Playlists = null;
	
	/*
	 * The playlist map is a map of the playlist name to its playlist ID. This map
	 * facilitates quick searches of a playlist given its name (playlists referenced by a track
	 * are identified by name).
	 */
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
	
	/*
	 * Map of artist name to artist object.
	 * 
	 * NOTE: The keys for this map are the artist names converted to lower case. This is because
	 * the same artist might be spelled with different case on different tracks. This also means 
	 * that the name shown in the artists display will be the first such name encountered.
	 * However, we do keep a list of alternate names that the user can see by right clicking
	 * on an artist.
	 */
	private static Map<String, Artist> Artists = null;
	
	/*
	 * Number of ignored playlists.
	 */
	private static Integer PlaylistIgnoredCount = 0;
	
    //---------------- Private variables -----------------------------------

	private static String className = XMLHandler.class.getSimpleName();
	private static Logger logger = (Logger) LoggerFactory.getLogger(className + "_XML");
	private static Logging logging = Logging.getInstance();
	private static Preferences userPrefs = Preferences.getInstance();
	
	private static Date XMLDate = null;
	
	private static Integer remoteTracksCount = 0;
	private static Integer remoteArtistsCount = 0;
	
	/*
	 * Static string definitions for the XML file.
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
	 * Gets the list of tracks found in the input XML file.
	 * 
	 * @return list of tracks
	 */
	public static List<Track> getTracks ()
	{
		return Tracks;
	}
	
	/**
	 * Gets the mapping of track IDs to track list indices.
	 *  
	 * @return mapping of track IDs to indices
	 */
	public static Map<Integer, Integer> getTracksMap ()
	{
		return TracksMap;
	}
	
	/**
	 * Gets the mapping of duplicate track names to track IDs.
	 *  
	 * @return mapping of duplicate track names to track IDs
	 */
	public static Map<String, List<Integer>> getDuplicatesMap ()
	{
		return DuplicatesMap;
	}

	/**
	 * Gets the list of playlists found in the input XML file.
	 * 
	 * @return list of playlists
	 */
	public static Map<String, Playlist> getPlaylists ()
	{
		return Playlists;
	}
	
	/**
	 * Gets the mapping of playlist names to playlist IDs.
	 *  
	 * @return mapping of playlist names to IDs
	 */
	public static Map<String, String> getPlaylistsMap ()
	{
		return PlaylistsMap;
	}

	/**
	 * Gets the list of playlist names found in the input XML file.
	 * 
	 * @return list of playlist names
	 */
	public static ArrayList<String> getPlaylistNames ()
	{
		return PlaylistNames;
	}

	/**
	 * Gets the list of track artist names found in the input XML file.
	 * 
	 * @return list of artist names
	 */
	public static ArrayList<String> getArtistNames ()
	{
		return ArtistNames;
	}
	
	/**
	 * Gets the mapping of artist names to artist objects.
	 * 
	 * @return mapping of artist names to artist objects
	 */
	public static Map<String, Artist> getArtists ()
	{
		return Artists;
	}
	
	/**
	 * Gets the playlist ignored count.
	 * 
	 * @return playlist ignored count
	 */
	public static Integer getPlaylistIgnoredCount ()
	{
		return PlaylistIgnoredCount;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Initializes logging. This is called once at application initialization.
	 */
	public static void initializeLogging ()
	{
		logging.registerLogger(Logging.Dimension.XML, logger);
	}

	/**
	 * Gets the number of tracks found in the input XML file. This is reduced
	 * by the number of remote tracks if <code>Show Remote Tracks</code> is
	 * not checked in the user preferences.
	 * 
	 * @return number of tracks
	 */
	public static int getNumberOfTracks ()
	{
		int numTracks = 0;
		
		if (Tracks != null)
		{
			numTracks = Tracks.getLength();
			if (userPrefs.getShowRemoteTracks() == false)
			{
				numTracks -= remoteTracksCount;
			}
		}
		
		return numTracks;
	}

	/**
	 * Gets the number of playlists found in the input XML file, reduced by 
	 * the number of ignored playlists.
	 * 
	 * @return number of non-ignored playlists
	 */
	public static int getNumberOfPlaylists ()
	{
		return (Playlists != null) ? Playlists.getCount() - PlaylistIgnoredCount : 0;
	}
	
	/**
	 * Gets the number of artist names found in the input XML file.
	 * 
	 * @return number of artists
	 */
	public static int getNumberOfArtists ()
	{
		int numArtists = 0;
		
		if (Artists != null)
		{
			numArtists = Artists.getCount();
			if (userPrefs.getShowRemoteTracks() == false)
			{
				numArtists -= remoteArtistsCount;
			}
		}
		
		return numArtists;
	}

	/**
	 * Gets the timestamp of the XML file.
	 * 
	 * @return file timestamp
	 */
	public static String getXMLFileTimestamp ()
	{
		return (XMLDate != null) ? Utilities.formatDate(XMLDate) : "";
	}
	
	/**
	 * Adds a playlist name to the list of such names.
	 * 
	 * @param playlistName name of the playlist
	 */
	public static void addPlaylistName (String playlistName)
	{
		if (PlaylistNames.indexOf(playlistName) == -1)
		{
			PlaylistNames.add(playlistName);
		}
	}
	
	/**
	 * Removes a playlist name from the list of such names.
	 * 
	 * @param playlistName name of the playlist
	 */
	public static void removePlaylistName (String playlistName)
	{
		PlaylistNames.remove(playlistName);
	}
	
	/**
	 * Increments the number of ignored playlists by a specified amount.
	 * 
	 * This is used when a folder playlist is dynamically removed from the
	 * ignored list.
	 * 
	 * @param increment number of playlists contained in the folder removed
	 * from the ignored list
	 */
	public static void incrementPlaylistIgnoredCount (Integer increment)
	{
		PlaylistIgnoredCount += increment;
	}
	
	/**
	 * Decrements the number of ignored playlists by a specified amount.
	 * 
	 * This is used when a folder playlist is dynamically added to the
	 * ignored list.
	 * 
	 * @param decrement number of playlists contained in the folder added
	 * to the ignored list
	 */
	public static void decrementPlaylistIgnoredCount (Integer decrement)
	{
		PlaylistIgnoredCount -= decrement;
	}
	
	/**
	 * Reads and processes the XML file.
	 * 
	 * @param xmlFileName XML file name
	 * @throws JDOMException If an error occurs trying to process the iTunes 
	 * XML file.
	 * @throws IOException If an error occurs trying to read the iTunes 
	 * XML file.
	 */
	public static void processXML (String xmlFileName)
		throws JDOMException, IOException
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
         * Set the content count for all folder playlists, so that we can adjust the total number
         * of playlists if a folder playlist is dynamically added to or removed from the ignored list.
         */
        PlaylistCollection.setPlaylistFolderCounts();

        /*
         * We don't want to update track playlist counts for bypassed playlists, identified 
         * as such through a preference. Mark such playlists now.
         */
        PlaylistCollection.markBypassedPlaylists();

        /*
         * Now we can go through all the playlists, and for those not skipped, update the track
         * playlist information.
         */
        PlaylistCollection.updateTrackPlaylistInfo();
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
		 * Reset the remote tracks and remote artists count.
		 */
		remoteTracksCount = 0;
		remoteArtistsCount = 0;
		
		/*
		 * Get a list of the XML tracks to work with.
		 */
        List<Element> tracksXML = javaListToPivotList(tracksHolder);
		
		/*
		 * We collect all the tracks into a ArrayList of type Track. Initialize it now. Also,
		 * make sure it's sorted by track name.
		 */
		Tracks = new ArrayList<Track>();
		Tracks.setComparator(new Comparator<Track>()
		{
            @Override
            public int compare(Track t1, Track t2)
            {
                return t1.compareTo(t2);
            }
        });
		
		/*
		 * To be able to find a given track easily, we also create a HashMap of the track ID to
		 * the index in the above ArrayList.
		 */
		TracksMap = new HashMap<Integer, Integer>();
		
		/*
		 * Initialize the duplicates map as well.
		 */
		DuplicatesMap = new HashMap<String, List<Integer>>();
		
		/*
		 * Initialize the list of artist names, and set a case-insensitive comparator.
		 */
		ArtistNames = new ArrayList<String>();
		ArtistNames.setComparator(String.CASE_INSENSITIVE_ORDER);
		
		/*
		 * Initialize the artists map.
		 */
		Artists = new HashMap<String, Artist>();
		Artists.setComparator(String.CASE_INSENSITIVE_ORDER);

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

						case "Track Type":
							if (nextStringValue(trackChildIter, keyValue).equals("Remote"))
							{
								trackObj.setRemote(true);
								remoteTracksCount++;
							}
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
    			 * Add the track to the duplicates map if necessary. We have to do this before
    			 * adding it to the main tracks list, to avoid false duplicates.
    			 */
    			int index = ArrayList.binarySearch(Tracks, trackObj, Tracks.getComparator());
    			if (index >= 0)
    			{
    				List<Integer> trackIDs;
    				String trackName = trackObj.getName();
    				int thisID = trackObj.getID();

    				if ((trackIDs = DuplicatesMap.get(trackName)) == null)
    				{
    					trackIDs = new ArrayList<Integer>();
    					int foundID = Tracks.get(index).getID();
    					trackIDs.add(foundID);
    					logger.debug("initialized duplicates map entry for track '" 
    							+ trackName + "', track ID " + foundID);
    				}
    				trackIDs.add(thisID);
    				logger.debug("added track ID " + thisID + " to track '" + trackName + "'");

    				DuplicatesMap.put(trackName, trackIDs);
    			}

    			/*
    			 * Add the track object to the list.
    			 */
    			Tracks.add(trackObj);
    			logger.debug("found track ID " + ID + ", name '" + trackObj.getName() + "'");
    		}
    		else
    		{
    			throw new JDOMException(
    					"did not find <" + ELEM_DICT + "> element after track ID " + ID);
    		}    		

    		/*
    		 * Handle tracks that have an artist.
    		 */
			String artist = trackObj.getArtist();
			if (artist != null)
			{

				/*
				 * Add the artist name to the list of such names, if it ain't already there.
				 * Also, create a new artist object, initialize it from the track, and add it to 
				 * the artist map.
				 */
				int index = ArrayList.binarySearch(ArtistNames, artist, ArtistNames.getComparator());
				if (index < 0)
				{
					ArtistNames.add(artist);
					Artist artistObj = new Artist(artist.toLowerCase());
					artistObj.addTrackToArtist(trackObj);
					Artists.put(artist.toLowerCase(), artistObj);
					
					/*
					 * This is tricky. We need to keep a count of artists that ONLY contain remote 
					 * tracks, so we can adjust the artist count if remote tracks are not being
					 * shown. So we bump the remote artists count here if this track is remote.
					 * On the else leg we're hitting the same artist again, so if that track is 
					 * local, we then decrement the remote artists count. And then remember that
					 * fact using a flag in the artist object. A thing of beauty, yes?
					 */
					if (trackObj.getRemote() == true)
					{
						remoteArtistsCount++;
						artistObj.setRemoteArtistControl(Artist.RemoteArtistControl.REMOTE);
					}
				}

				/*
				 * The artist already exists in the list of artist names and the artist map.
				 * Update the artist object from the track and then replace it in the artist map.
				 */
				else
				{
					Artist artistObj = Artists.get(artist.toLowerCase());
					artistObj.addTrackToArtist(trackObj);
					Artists.put(artist.toLowerCase(), artistObj);
					
					/*
					 * See comment above in the if leg.
					 */
					if (trackObj.getRemote() == false
							&& artistObj.getRemoteArtistControl() == Artist.RemoteArtistControl.REMOTE)
					{
						remoteArtistsCount--;
						artistObj.setRemoteArtistControl(Artist.RemoteArtistControl.REMOTE_AND_LOCAL);
					}
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
		 * Reset the playlist ignored count, so it doesn't keep growing if we reread the XML file.
		 */
		PlaylistIgnoredCount = 0;
		
		/*
		 * Get a list of the XML playlists to work with.
		 */
        List<Element> playlistsXML = javaListToPivotList(playlistsHolder);
		
		/*
		 * We collect all the playlists into a HashMap of the playlist name to its Playlist
		 * object. Initialize it now.
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
        	 * Create a new playlist object.
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
    					 * If this playlist is to be ignored, indicate so.
    					 */
    					if (PlaylistCollection.isPlaylistIgnored(plName))
    					{
    						playlistObj.setIgnored(true);
    						incrementPlaylistIgnoredCount(1);
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
    					
    					logger.debug("playlist '" + playlistObj.getName() + "' has " + 
    							playlistTracks.getLength() + " tracks");
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
        	 * If the playlist is not ignored, add its name to the playlist name list.
        	 */
        	if (playlistObj.getIgnored() == false)
        	{
        		addPlaylistName(playlistObj.getName());
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
		else if (nextTrackAttr.getName().equals(ELEM_FALSE))
		{
			return false;
		}
		else
		{
        	throw new JDOMException(
        			"expected <" + ELEM_TRUE + "> or <" + ELEM_FALSE + "> element not found after '" + 
        					keyName + "' key");
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