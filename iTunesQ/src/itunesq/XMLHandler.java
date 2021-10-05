package itunesq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that reads and processes the XML file.
 * <p>
 * This is a final class consisting entirely of static methods.
 * <p>
 * The main public method is <code>processXML</code>, that uses JDOM to walk
 * through the XML file. I'd like to note that the XML file (original iTunes 
 * design) is rather ridiculous and difficult to work with. Just saying.
 * 
 * @author Jon
 *
 */
public final class XMLHandler
{

    // ---------------- Private variables -----------------------------------

    private static String className = XMLHandler.class.getSimpleName();
    private static Logger fileLogger = (Logger) LoggerFactory.getLogger(className + "_File");
    private static Logger trackLogger = (Logger) LoggerFactory.getLogger(className + "_Track");
    private static Logger playlistLogger =
            (Logger) LoggerFactory.getLogger(className + "_Playlist");
    private static Logger artistLogger = (Logger) LoggerFactory.getLogger(className + "_Artist");

    private static Date fileDate = null;

    /*
     * Static string definitions for the XML file.
     */
    private static final String ELEM_ARRAY = "array";
    private static final String ELEM_DATE = "date";
    private static final String ELEM_DICT = "dict";
    private static final String ELEM_INTEGER = "integer";
    private static final String ELEM_KEY = "key";
    private static final String ELEM_STRING = "string";

    private static final String KEY_DATE = "Date";
    private static final String KEY_PLAYLISTS = "Playlists";
    private static final String KEY_TRACKS = "Tracks";

    // ---------------- Public methods --------------------------------------

    /**
     * Initializes logging. This is called once at application initialization.
     */
    public static void initializeLogging()
    {
        Logging logging = Logging.getInstance();
        logging.registerLogger(Logging.Dimension.FILE, fileLogger);
        logging.registerLogger(Logging.Dimension.TRACK, trackLogger);
        logging.registerLogger(Logging.Dimension.PLAYLIST, playlistLogger);
        logging.registerLogger(Logging.Dimension.ARTIST, artistLogger);
    }

    /**
     * Gets the timestamp of the XML file.
     * 
     * @return file timestamp
     */
    public static String getXMLFileTimestamp()
    {
        return (fileDate != null) ? Utilities.formatDate(fileDate) : "";
    }

    /**
     * Reads and processes the XML file.
     * 
     * @param xmlFileName XML file name
     * @throws IOException If an error occurs trying to read the XML
     * file.
     */
    public static void processXML(String xmlFileName) 
            throws IOException
    {
        fileLogger.trace("processXML");

        /*
         * Create a SAXBuilder to read the XML file.
         */
        fileLogger.info("creating SAX builder");
        SAXBuilder jdomBuilder = new SAXBuilder(null, null, new LocatedJDOMFactory());

        /*
         * Build the JDOM document.
         */
        Document jdomDocument = null;
        fileLogger.info("creating JDOM document");
        try
        {
            jdomDocument = jdomBuilder.build(xmlFileName);
        }
        catch (JDOMException e)
        {
            MainWindow.logException(fileLogger, e);
            handleJDOMError(e.getMessage());
        }

        /*
         * The first 2 elements look like this:
         * 
         * <plist version="..."> <dict>
         */
        fileLogger.info("looking for root elements");

        Element root = jdomDocument.getRootElement();
        Element mainDict = nextSibling(root);
        if (mainDict == null || !mainDict.getName().equals(ELEM_DICT))
        {
            handleJDOMError("could not find main <" + ELEM_DICT + "> element");
        }

        Element dateKey = null;
        Element dateValue = null;
        Element trackKey = null;
        Element tracksHolder = null;
        Element playlistKey = null;
        Element playlistsHolder = null;

        /*
         * Get the children of the top level <dict> element. These represent
         * global information and the lists of tracks and playlists.
         */
        List<Element> mainChildren = javaListToPivotList(mainDict);

        /*
         * Try to locate the following elements:
         * 
         *   <key>Date</key> 
         *   <key>Tracks</key> 
         *   <key>Playlists</key>
         */
        fileLogger.info("looking for global information elements");

        for (Element elem : mainChildren)
        {
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
         * 
         * NOTE: This is optional.
         */
        String xmlDate = null;
        if (dateKey != null)
        {
            dateValue = nextSibling(dateKey);
            if (dateValue != null && dateValue.getName().equals(ELEM_DATE))
            {
            	xmlDate = dateValue.getValue();
            }
            else
            {
                handleJDOMError(dateKey,
                        "could not find <" + ELEM_DATE + "> element after '" + KEY_DATE + "' key");
            }
        }
        
        /*
         * The <date> element doesn't exist, so use the file modification date.
         */
        else
        {
        	Path xmlPath = Paths.get(xmlFileName);
        	BasicFileAttributes attrs = Files.readAttributes(xmlPath, BasicFileAttributes.class);
        	xmlDate = attrs.lastModifiedTime().toString();
        }
        
        /*
         * Parse the date if we were successful.
         */
        if (xmlDate != null)
        {
            try
            {
                fileDate = Utilities.parseDate(xmlDate);
            }
            catch (ParseException e)
            {
                MainWindow.logException(fileLogger, e);
                handleJDOMError("unable to parse date value " + xmlDate);
            }
        }

        /*
         * Locate the following <dict> element for the tracks key, which in turn
         * contains the tracks as children.
         */
        if (trackKey != null)
        {
            tracksHolder = nextSibling(trackKey);
            if (tracksHolder == null || !tracksHolder.getName().equals(ELEM_DICT))
            {
                handleJDOMError(trackKey, "could not find <" + ELEM_DICT + "> element after '"
                        + KEY_TRACKS + "' key");
            }
        }
        else
        {
            handleJDOMError("could not find '" + KEY_TRACKS + "' key");
        }

        /*
         * Locate the following <array> element for the playlists key, which in
         * turn contains the playlists as children.
         */
    	if (playlistKey != null)
    	{
    		playlistsHolder = nextSibling(playlistKey);
    		if (playlistsHolder == null || !playlistsHolder.getName().equals(ELEM_ARRAY))
    		{
    			handleJDOMError(playlistKey, "could not find <" + ELEM_ARRAY + "> element after '"
    					+ KEY_PLAYLISTS + "' key");
    		}
    	}
    	else
    	{
    		handleJDOMError("could not find '" + KEY_PLAYLISTS + "' key");
    	}

        /*
         * Now gather the actual tracks.
         */
        fileLogger.info("gathering tracks");
        generateTracks(tracksHolder);

        /*
         * Now that the tracks (and artists) are all created, post-process the artists
         * to try and find additional alternate names, and to verify artist alternate name 
         * overrides.
         */
        ArtistNames.postProcessArtists();

        /*
         * Gather playlists.
         */
    	fileLogger.info("gathering playlists");
    	generatePlaylists(playlistsHolder);

    	/*
    	 * Post-process the playlists as follows:
    	 * 
    	 *   1) Set the content count for all folder playlists.
    	 *   2) Mark all bypassed playlists.
    	 *   3) Update track playlist info for all playlists.
    	 */
    	PlaylistCollection.postProcessPlaylists();

        /*
         * Log the XML file statistics.
         */
        Database.logFileStats();
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Create the tracks from the XML file.
     */
    private static void generateTracks(Element tracksHolder)
    {
        trackLogger.trace("generateTracks");

        /*
         * Get a list of the XML tracks to work with.
         */
        List<Element> tracksXML = javaListToPivotList(tracksHolder);

        /*
         * We collect all the tracks into an ArrayList of type Track.
         * Make sure it's sorted by track name.
         */
        ArrayList<Track> tracks = Database.getTracks();
        tracks.setComparator(new Comparator<Track>()
        {
            @Override
            public int compare(Track t1, Track t2)
            {
                return t1.compareTo(t2);
            }
        });

        /*
         * Get the duplicates map.
         */
        Map<String, List<Integer>> duplicatesMap = Database.getDuplicatesMap();

        /*
         * Get the list of artist names, and set a case-insensitive
         * comparator.
         */
        ArrayList<ArtistCorrelator> artistCorrelators = Database.getArtistCorrelators();
        artistCorrelators.setComparator(new Comparator<ArtistCorrelator>()
        {
            @Override
            public int compare(ArtistCorrelator c1, ArtistCorrelator c2)
            {
                return c1.compareToNormalized(c2);
            }
        });

        /*
         * Get the artists map.
         */
        Map<Integer, Artist> artists = Database.getArtists();
        
        /*
         * Get the codecs map.
         */
        Map<String, CodecStats> codecStats = Database.getCodecStats();
        codecStats.setComparator(String.CASE_INSENSITIVE_ORDER);

        /*
         * Walk through the elements of the parent <dict> element.
         * 
         * This is tricky stuff. Each track consists of a pair of sibling
         * elements: 
         * 
         * - <key>ID number</key> 
         * - <dict> ... </dict>  (whose children are the attributes of the track)
         * 
         * We walk all the children expecting this structure, so we use
         * variables that are expected to be set when the ID key is found, then
         * used after we continue the loop once the ID key is processed. I know
         * this is hideous.
         */
        int ID = 0;
        Track trackObj = null;

        trackLogger.debug("starting track loop");
        for (Element trackElem : tracksXML)
        {

            /*
             * Grab the element and process it if it's an ID key.
             */
            if (trackElem.getName().equals(ELEM_KEY))
            {

                /*
                 * Sanity check the hideous variables.
                 */
                if (ID != 0 || trackObj != null)
                {
                    handleJDOMError(trackElem,
                            "did not find <" + ELEM_DICT + "> element after track ID " + ID);
                }

                ID = Integer.valueOf(trackElem.getTextTrim());

                /*
                 * Now that the ID key has been processed, iterate the loop to
                 * process the sibling <dict> element.
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
                     * The first track attribute element of a pair is expected
                     * to be <key>.
                     */
                    if (trackAttr.getName().equals(ELEM_KEY))
                    {

                        /*
                         * Process the track attributes we care about, and
                         * ignore the rest.
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
                        	String kind = nextStringValue(trackChildIter, keyValue);
                            trackObj.setKind(kind);
                            
                            /*
                             * Set the type of track. A kind that doesn't match any known type will 
                             * result in a warning when creating the tracks map, and audio will be assumed.
                             */
                            if (kind.toLowerCase().contains("audio"))
                            {
                            	trackObj.setTrackType(Track.TrackType.AUDIO);
                            }
                            else if (kind.toLowerCase().contains("video"))
                            {
                            	trackObj.setTrackType(Track.TrackType.VIDEO);
                            }
                            else
                            {
                            	trackObj.setTrackType(Track.TrackType.UNKNOWN);
                            }
                            
                            /*
                             * Get the codec name, for example AAC. Change MPEG to MP3 since it's more common.
                             */
                            String[] kindWords = kind.split(" ");
                            String codec = kindWords[0];
                            if (codec.equals("MPEG"))
                            {
                            	codec = "MP3";
                            }
                            
                            /*
                             * Accumulate a count of each codec discovered.
                             */
                            CodecStats stats = codecStats.get(codec);
                            if (stats != null)
                            {
                            	stats.incrementCount();
                            }
                            else
                            {
                            	stats = new CodecStats();
                            	stats.setCount(1);
                            	stats.setType(trackObj.getTrackType());
                            }
                        	codecStats.put(codec, stats);
                            
                        	break;

                        case "Size":
                            trackObj.setSize(nextIntValue(trackChildIter, keyValue));
                            break;

                        case "Total Time":
                            trackObj.setDuration(nextIntValue(trackChildIter, keyValue));
                            break;

                        case "Year":
                            trackObj.setYear(nextYearValue(trackChildIter, keyValue));
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
                         * We need to skip over the sibling element for all
                         * attributes we don't process.
                         */
                        default:
                            trackAttr = trackChildIter.next();
                        }
                    }
                    else
                    {
                        handleJDOMError(trackAttr,
                                "<" + ELEM_DICT + "> element child is not <" + ELEM_KEY + ">");
                    }
                }
                
                boolean trackLogged = false;
                Track.TrackType trackType = trackObj.getTrackType();

                /*
                 * Add the track to the duplicates map if necessary. We have to
                 * do this before adding it to the main tracks list, to avoid
                 * false duplicates.
                 * 
                 * The standard track comparator compares a number of items to ensure
                 * uniqueness. But that would short-circuit the duplicates map because 
                 * it would only find duplicates for the same artist and same album.
                 * So we use just a name comparator.
                 */
                Comparator<Track> nameComparator = (new Comparator<Track>()
                {
                    @Override
                    public int compare(Track t1, Track t2)
                    {
                        return t1.compareToName(t2);
                    }
                });
                
				int index = ArrayList.binarySearch(tracks, trackObj, nameComparator);
            	if (index >= 0)
            	{
            		List<Integer> trackIDs;
            		String trackName = trackObj.getName();
            		int thisID = trackObj.getID();

            		if ((trackIDs = duplicatesMap.get(trackName)) == null)
            		{
            			trackIDs = new ArrayList<Integer>();
            			int foundID = tracks.get(index).getID();
            			trackIDs.add(foundID);
            			trackLogger.debug("initialized duplicates map entry for track '" + trackName
            					+ "', track ID " + foundID);
            		}
            		trackIDs.add(thisID);
            		trackLogger.debug("added track ID " + thisID + " type " + trackType + " to track '" + trackName + "'");
            		trackLogged = true;

            		duplicatesMap.put(trackName, trackIDs);
            	}

            	/*
            	 * Add the track object to the all tracks list.
            	 */
            	tracks.add(trackObj);
            	if (trackLogged == false)
            	{
            		trackLogger.debug("found track ID " + ID + " type " + trackType + ", name '" + trackObj.getName() + "'");
            	}
            }
            else
            {
                handleJDOMError(trackElem,
                        "did not find <" + ELEM_DICT + "> element after track ID " + ID);
            }

            /*
             * Handle tracks that have an artist.
             */
            String artist = trackObj.getArtist();
            if (artist != null)
            {

                /*
                 * Get an artist names object and normalize the artist name.
                 */
                ArtistNames artistNames = new ArtistNames(artist);
                String normalizedName = artistNames.normalizeName();

                /*
                 * Try to match the artist name to the current list of artists. This method
                 * detects alternate artist names, matching them to the existing primary name.
                 */
                int index = artistNames.matchArtist(artistCorrelators, artistLogger);
                if (index < 0)
                {

                    /*
                     * We did not find a match. Add the artist name to the list of such names
                     * (as a correlator object).
                     */
                    ArtistCorrelator artistCorr = new ArtistCorrelator(artist);
                    artistCorr.setNormalizedName(normalizedName);
                    artistCorrelators.add(artistCorr);

                    /*
                     * Create a new artist object, initialize it from the track, and add it to 
                     * the artist map.
                     */
                    Artist artistObj = new Artist(artist);
                    artistObj.setArtistNames(artistNames);
                    artistObj.addTrackToArtist(trackObj, artistLogger);
                    Integer correlator = artistObj.getCorrelator();
                    artistCorr.setArtistKey(correlator);
                    artists.put(correlator, artistObj);
                    artistLogger.debug("found artist name '" + artist + "', normalized '"
                            + normalizedName + "'");
                }

                /*
                 * The artist already exists in the list of artist names and the
                 * artist map. Update the artist object from the track and then
                 * replace it in the artist map.
                 */
                else
                {
                    ArtistCorrelator artistCorr = artistCorrelators.get(index);
                    Artist artistObj = artists.get(artistCorr.getArtistKey());
                    artistObj.addTrackToArtist(trackObj, artistLogger);
                    artists.put(artistCorr.getArtistKey(), artistObj);
                    artistLogger.debug("updated existing artist name '" + artist + "', normalized '"
                            + normalizedName + "'");
                }
            }

            /*
             * Reset these hideous variables for the next pair of elements.
             */
            ID = 0;
            trackObj = null;
        }

        /*
         * Generate the track ID to index mappings. We have to wait until all
         * tracks have been found and sorted in order for the indices to be
         * correct.
         */
        Database.generateTrackIDMappings(trackLogger);
    }

    /*
     * Create the playlists from the XML file.
     */
    private static void generatePlaylists(Element playlistsHolder)
    {
        playlistLogger.trace("generatePlaylists");

        /*
         * Reset the playlist ignored count, so it doesn't keep growing if we
         * reread the XML file.
         */
        Database.setPlaylistIgnoredCount(0);

        /*
         * Get a list of the XML playlists to work with.
         */
        List<Element> playlistsXML = javaListToPivotList(playlistsHolder);

        /*
         * We collect all the playlists into a HashMap of the playlist name to
         * its Playlist object. Initialize it now.
         */
        Map<String, Playlist> playlists = Database.getPlaylists();

        /*
         * To be able to find a given playlist by name, we also create a HashMap
         * of the playlist name to ID.
         */
        Map<String, String> playlistsMap = Database.getPlaylistsMap();

        /*
         * Initialize the list of playlist names, and set a case-insensitive
         * comparator.
         */
        List<String> playlistNames = Database.getPlaylistNames();
        playlistNames.setComparator(String.CASE_INSENSITIVE_ORDER);

        /*
         * Walk through the elements of the parent <array> element.
         */
        playlistLogger.debug("starting playlist loop");
        for (Element playlistElem : playlistsXML)
        {

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
                 * The first playlist attribute element of a pair is expected to
                 * be <key>.
                 */
                if (playlistAttr.getName().equals(ELEM_KEY))
                {

                    /*
                     * Process the playlist attributes we care about, and ignore
                     * the rest.
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
                            Database.incrementPlaylistIgnoredCount(1);
                        }
                        break;

                    case "Playlist Persistent ID":
                        playlistObj.setPersistentID(nextStringValue(playlistChildIter, keyValue));
                        break;

                    case "Parent Persistent ID":
                        playlistObj.setParentPersistentID(
                                nextStringValue(playlistChildIter, keyValue));
                        break;

                    case "Playlist Items":

                        /*
                         * Gather the playlist track IDs.
                         */
                        List<Integer> playlistTracks = new LinkedList<Integer>();
                        playlistTracks = gatherPlaylistTracks(playlistAttr);

                        playlistLogger.debug("playlist '" + playlistObj.getName() + "' has "
                                + playlistTracks.getLength() + " tracks");
                        playlistObj.setTracks(playlistTracks);

                        /*
                         * Skip over the entire playlist tracks element.
                         */
                        playlistAttr = playlistChildIter.next();
                        break;

                    /*
                     * We need to skip over the sibling element for all
                     * attributes we don't process.
                     */
                    default:
                        playlistAttr = playlistChildIter.next();
                    }
                }
                else
                {
                    handleJDOMError(playlistAttr,
                            "<" + ELEM_DICT + "> element child is not <" + ELEM_KEY + ">");
                }
            }

            /*
             * Add the playlist object to the collection.
             */
            playlists.put(playlistObj.getPersistentID(), playlistObj);
            playlistsMap.put(playlistObj.getName(), playlistObj.getPersistentID());
            playlistLogger.debug("found playlist name " + playlistObj.getName());

            /*
             * If the playlist is not ignored, add its name to the playlist name
             * list.
             */
            if (playlistObj.getIgnored() == false)
            {
                Database.addPlaylistName(playlistObj.getName());
            }
        }
    }

    /*
     * Gather the list of tracks for a playlist.
     * 
     * Once again we have XML ridiculousness to deal with. The playlist tracks
     * are laid out like so:
     * 
     *   <key>Playlist Items</key> 
     *     <array> 
     *       <dict> 
     *         <key>Track ID</key><integer>nnnn</integer> 
     *       </dict> 
     *     </array>
     * 
     * We're passed the element for 'Playlist Items'.
     */
    private static List<Integer> gatherPlaylistTracks(Element playlistTracksKeyElem)
    {
        playlistLogger.trace("gatherPlaylistTracks");

        List<Integer> playlistTracks = new LinkedList<Integer>();

        /*
         * Get the next sibling, which should be the <array> element.
         */
        Element playlistTracksHolder = nextSibling(playlistTracksKeyElem);
        if (playlistTracksHolder == null || !playlistTracksHolder.getName().equals(ELEM_ARRAY))
        {
            handleJDOMError(playlistTracksKeyElem, "could not find <" + ELEM_ARRAY
                    + "> element after '" + playlistTracksKeyElem.getTextTrim() + "' key");
        }

        /*
         * Get the children of the <array>, which is a list of <dict> elements.
         */
        List<Element> playlistTracksDictList = javaListToPivotList(playlistTracksHolder);

        /*
         * Walk the <dict> list.
         */
        for (Element playlistTracksDictElem : playlistTracksDictList)
        {

            /*
             * Get the children of this <dict> element, which are the playlist
             * track attributes. Yay.
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
                         * So we finally have the track ID. Add it to the
                         * collection we will return.
                         */
                        Integer playlistTrackID =
                                Integer.valueOf(nextIntValue(playlistTrackAttrsIter, keyValue));
                        playlistTracks.add(playlistTrackID);
                    }
                    else
                    {
                        handleJDOMError(playlistTracksAttr,
                                "'Playlist Items' child is not 'Track ID");
                    }
                }
                else
                {
                    handleJDOMError(playlistTracksAttr,
                            "<" + ELEM_DICT + "> element child is not <" + ELEM_KEY + ">");
                }
            }
        }

        return playlistTracks;
    }

    /*
     * Guess I'm stupid, because I could not find a way to easily convert
     * java.util.List to Pivot's List. Hence this ridiculous method :(
     */
    private static List<Element> javaListToPivotList(Element elem)
    {
        List<Element> listChildren = new ArrayList<Element>();

        java.util.List<Element> xmlChildren = elem.getChildren();
        for (Element childElem : xmlChildren)
        {
            listChildren.add(childElem);
        }

        return listChildren;
    }

    /*
     * JDOM doesn't include a method to find sibling elements. But we need to
     * locate the next sibling in many cases, due to the weird structure of the
     * XML.
     */
    private static Element nextSibling(Element current)
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
             * For non-root elements, get the parent's child list, locate the
             * current element within that list, then grab the next sequential
             * element.
             */
            else
            {
                Element parent = current.getParentElement();
                List<Element> children = javaListToPivotList(parent);
                int myIndex = children.indexOf(current);

                if (myIndex >= 0 && myIndex < children.getLength() - 1)
                {
                    nextSibling = (Element) children.get(myIndex + 1);
                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            MainWindow.logException(fileLogger, e);
            handleJDOMError("expected sibling element not found after " + current.getTextTrim());
        }

        return nextSibling;
    }

    /*
     * Track and playlist attributes come in sibling pairs, for example:
     * 
     * <key>Name</key><string>Silence Is Golden</string>
     * 
     * We need to process these with an iterator, so can't use getNextSibling()
     * to find the sibling with the attribute value. This method uses the
     * iterator to get the value from the second element, so as to keep the
     * integrity of the iterator.
     * 
     * Note that getNextSibling() is still needed for cases when we don't have
     * an iterator.
     */
    private static String nextStringValue(Iterator<Element> trackChildIter, String keyName)
    {
        Element nextTrackAttr = trackChildIter.next();
        if (!nextTrackAttr.getName().equals(ELEM_STRING))
        {
            handleJDOMError(nextTrackAttr,
                    "expected <" + ELEM_STRING + "> element not found after '" + keyName + "' key");
        }

        return nextTrackAttr.getTextTrim();
    }

    /*
     * See getNextStringValue() for more information.
     */
    private static int nextIntValue(Iterator<Element> trackChildIter, String keyName)
    {
        Element nextTrackAttr = trackChildIter.next();
        if (!nextTrackAttr.getName().equals(ELEM_INTEGER))
        {
            handleJDOMError(nextTrackAttr, "expected <" + ELEM_INTEGER
                    + "> element not found after '" + keyName + "' key");
        }

        return Integer.valueOf(nextTrackAttr.getTextTrim());
    }

    /*
     * See getNextStringValue() for more information.
     */
    private static Date nextDateValue(Iterator<Element> trackChildIter, String keyName)
    {
        Element nextTrackAttr = trackChildIter.next();
        if (!nextTrackAttr.getName().equals(ELEM_DATE))
        {
            handleJDOMError(nextTrackAttr,
                    "expected <" + ELEM_DATE + "> element not found after '" + keyName + "' key");
        }

        Date date = null;
        try
        {
            date = Utilities.parseDate(nextTrackAttr.getTextTrim());
        }
        catch (ParseException e)
        {
            MainWindow.logException(fileLogger, e);
            handleJDOMError("unable to parse date value " + nextTrackAttr.getTextTrim());
        }

        return date;
    }

    /*
     * See getNextStringValue() for more information.
     * 
     * The year can be an integer such as 1953, or a string such as "2/9/1975" or "2012-08-14T07:00:00Z".
     * We convert the string variants into an integer for consistency, ignoring all but the year.
     */
    private static int nextYearValue(Iterator<Element> trackChildIter, String keyName)
    {
    	int returnVal = 0;
    	
        Element nextTrackAttr = trackChildIter.next();
        switch (nextTrackAttr.getName())
        {
        case ELEM_INTEGER:
        	returnVal = Integer.valueOf(nextTrackAttr.getTextTrim());
        	break;
        	
        case ELEM_STRING:
    		String year = nextTrackAttr.getTextTrim();
    		
    		if (year.indexOf('/') != -1)
    		{
        		String str[] = year.split("/");
            	returnVal = Integer.parseInt(str[2]);    			
    		}
    		else if (year.indexOf('-') != -1)
    		{
    			returnVal = Integer.valueOf(year.substring(0, 4));
    		}
    		
        	break;
        	
        default:
            handleJDOMError(nextTrackAttr, "expected <" + ELEM_INTEGER + "> or <" + ELEM_STRING
            		+ "> element not found after '" + keyName + "' key");
        }

        return returnVal;
    }

    /*
     * Handle a JDOM error, message only. This method does not return.
     */
    private static void handleJDOMError(String message)
    {
        handleJDOMError(null, message);
    }

    /*
     * Handle a JDOM error, element and message. This method does not return.
     */
    private static void handleJDOMError(Element element, String message)
    {
        int line = 0;
        int column = 0;

        /*
         * If we have an element, and it's a LocatedElement, then get the line
         * and column associated with the element.
         */
        if (element != null && element instanceof LocatedElement)
        {
            LocatedElement locElem = (LocatedElement) element;
            line = locElem.getLine();
            column = locElem.getColumn();
        }

        /*
         * Throw an XMLProcessingException.
         */
        throw new XMLProcessingException(line, column, message);
    }
}