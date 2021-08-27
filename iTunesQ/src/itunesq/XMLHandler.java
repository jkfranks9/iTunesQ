package itunesq;

import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
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

    // ---------------- Class variables -------------------------------------

    /*
     * The list of tracks; just a simple list.
     */
    private static ArrayList<Track> tracks = null;

    /*
     * The tracks map is a map of the track ID to its index value in the tracks
     * list. This means it can't be created until the entire list has been
     * created and sorted. This map facilitates quick searches of a track given
     * its track ID (tracks within a playlist are identified only by ID).
     */
    private static Map<Integer, Integer> tracksMap = null;

    /*
     * The duplicates map is a map of the track name to a list of track IDs.
     * This allows us to find duplicates quickly on demand, at the cost of
     * longer time to process the XML file.
     */
    private static Map<String, List<Integer>> duplicatesMap = null;

    /*
     * The list of playlists. This is a map of the playlist ID to its
     * corresponding Playlist object.
     */
    private static Map<String, Playlist> playlists = null;

    /*
     * The playlist map is a map of the playlist name to its playlist ID. This
     * map facilitates quick searches of a playlist given its name (playlists
     * referenced by a track are identified by name).
     */
    private static Map<String, String> playlistsMap = null;

    /*
     * List of playlist names. This is to provide typing assistance when the
     * user wants to enter a playlist name.
     */
    private static ArrayList<String> playlistNames = null;

    /*
     * List of track artist names. This is to provide typing assistance when the
     * user wants to enter an artist name on a track filter.
     * 
     * It's also used to correlate multiple artist names to a single artist
     * object, based on the fact that the multiple names all adhere to a set of
     * matching rules according to the ArtistNames class.
     * 
     * For example, this list can contain both "beatles" and "the beatles", but
     * only have one artist object. The key from the ArtistCorrelator class is
     * the same in both list entries, and is the key used in the artists map.
     */
    private static ArrayList<ArtistCorrelator> artistCorrelators = null;

    /*
     * Map of artist key to artist object. The key is kept in the ArtistCorrelator class.
     */
    private static Map<Integer, Artist> artists = null;

    /*
     * Number of ignored playlists.
     */
    private static Integer playlistIgnoredCount = 0;

    // ---------------- Private variables -----------------------------------

    private static String className = XMLHandler.class.getSimpleName();
    private static Logger xmlLogger = (Logger) LoggerFactory.getLogger(className + "_XML");
    private static Logger trackLogger = (Logger) LoggerFactory.getLogger(className + "_Track");
    private static Logger playlistLogger =
            (Logger) LoggerFactory.getLogger(className + "_Playlist");
    private static Logger artistLogger = (Logger) LoggerFactory.getLogger(className + "_Artist");
    private static Preferences userPrefs = Preferences.getInstance();

    private static Date XMLDate = null;

    private static Integer remoteTracksCount = 0;
    private static Integer remoteArtistsCount = 0;

    /*
     * Static string definitions for the XML file.
     */
    private static final String ELEM_ARRAY = "array";
    private static final String ELEM_DATE = "date";
    private static final String ELEM_DICT = "dict";
    private static final String ELEM_FALSE = "false";
    private static final String ELEM_INTEGER = "integer";
    private static final String ELEM_KEY = "key";
    private static final String ELEM_STRING = "string";
    private static final String ELEM_TRUE = "true";

    private static final String KEY_DATE = "Date";
    private static final String KEY_PLAYLISTS = "Playlists";
    private static final String KEY_TRACKS = "Tracks";

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the list of tracks found in the input XML file.
     * 
     * @return list of tracks
     */
    public static List<Track> getTracks()
    {
        return tracks;
    }

    /**
     * Gets the mapping of track IDs to track list indices.
     * 
     * @return mapping of track IDs to indices
     */
    public static Map<Integer, Integer> getTracksMap()
    {
        return tracksMap;
    }

    /**
     * Gets the mapping of duplicate track names to track IDs.
     * 
     * @return mapping of duplicate track names to track IDs
     */
    public static Map<String, List<Integer>> getDuplicatesMap()
    {
        return duplicatesMap;
    }

    /**
     * Gets the list of playlists found in the input XML file.
     * 
     * @return list of playlists
     */
    public static Map<String, Playlist> getPlaylists()
    {
        return playlists;
    }

    /**
     * Gets the mapping of playlist names to playlist IDs.
     * 
     * @return mapping of playlist names to IDs
     */
    public static Map<String, String> getPlaylistsMap()
    {
        return playlistsMap;
    }

    /**
     * Gets the list of playlist names found in the input XML file.
     * 
     * @return list of playlist names
     */
    public static ArrayList<String> getPlaylistNames()
    {
        return playlistNames;
    }

    /**
     * Gets the list of track artist names found in the input XML file.
     * 
     * @return list of artist names
     */
    public static ArrayList<ArtistCorrelator> getArtistCorrelators()
    {
        return artistCorrelators;
    }

    /**
     * Gets the mapping of artist names to artist objects.
     * 
     * @return mapping of artist names to artist objects
     */
    public static Map<Integer, Artist> getArtists()
    {
        return artists;
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Initializes logging. This is called once at application initialization.
     */
    public static void initializeLogging()
    {
        Logging logging = Logging.getInstance();
        logging.registerLogger(Logging.Dimension.XML, xmlLogger);
        logging.registerLogger(Logging.Dimension.TRACK, trackLogger);
        logging.registerLogger(Logging.Dimension.PLAYLIST, playlistLogger);
        logging.registerLogger(Logging.Dimension.ARTIST, artistLogger);
    }

    /**
     * Gets the number of tracks found in the input XML file. This is reduced by
     * the number of remote tracks if <code>Show Remote Tracks</code> is not
     * checked in the user preferences.
     * 
     * @return number of tracks
     */
    public static int getNumberOfTracks()
    {
        int numTracks = 0;

        if (tracks != null)
        {
            numTracks = tracks.getLength();
            if (userPrefs.getShowRemoteTracks() == false)
            {
                numTracks -= remoteTracksCount;
            }
        }

        return numTracks;
    }

    /**
     * Gets the number of playlists found in the input XML file, reduced by the
     * number of ignored playlists.
     * 
     * @return number of non-ignored playlists
     */
    public static int getNumberOfPlaylists()
    {
        return (playlists != null) ? playlists.getCount() - playlistIgnoredCount : 0;
    }

    /**
     * Gets the number of artist names found in the input XML file. This is
     * reduced by the number of artists with only remote tracks if
     * <code>Show Remote Tracks</code> is not checked in the user preferences.
     * 
     * @return number of artists
     */
    public static int getNumberOfArtists()
    {
        int numArtists = 0;

        if (artists != null)
        {
            numArtists = artists.getCount();
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
    public static String getXMLFileTimestamp()
    {
        return (XMLDate != null) ? Utilities.formatDate(XMLDate) : "";
    }

    /**
     * Adds a playlist name to the list of such names.
     * 
     * @param playlistName name of the playlist
     */
    public static void addPlaylistName(String playlistName)
    {
        if (playlistNames.indexOf(playlistName) == -1)
        {
            playlistNames.add(playlistName);
        }
    }

    /**
     * Removes a playlist name from the list of such names.
     * 
     * @param playlistName name of the playlist
     */
    public static void removePlaylistName(String playlistName)
    {
        playlistNames.remove(playlistName);
    }

    /**
     * Increments the number of ignored playlists by a specified amount.
     * 
     * This is used when a folder playlist is dynamically removed from the
     * ignored list.
     * 
     * @param increment number of playlists contained in the folder removed from
     * the ignored list
     */
    public static void incrementPlaylistIgnoredCount(Integer increment)
    {
        playlistIgnoredCount += increment;
    }

    /**
     * Decrements the number of ignored playlists by a specified amount.
     * 
     * This is used when a folder playlist is dynamically added to the ignored
     * list.
     * 
     * @param decrement number of playlists contained in the folder added to the
     * ignored list
     */
    public static void decrementPlaylistIgnoredCount(Integer decrement)
    {
        playlistIgnoredCount -= decrement;
    }

    /**
     * Reads and processes the XML file.
     * 
     * @param xmlFileName XML file name
     * @throws IOException If an error occurs trying to read the iTunes XML
     * file.
     */
    public static void processXML(String xmlFileName) 
            throws IOException
    {
        xmlLogger.trace("processXML");

        /*
         * Create a SAXBuilder to read the XML file.
         */
        xmlLogger.info("creating SAX builder");
        SAXBuilder jdomBuilder = new SAXBuilder(null, null, new LocatedJDOMFactory());

        /*
         * Build the JDOM document.
         */
        Document jdomDocument = null;
        xmlLogger.info("creating JDOM document");
        try
        {
            jdomDocument = jdomBuilder.build(xmlFileName);
        }
        catch (JDOMException e)
        {
            MainWindow.logException(xmlLogger, e);
            handleJDOMError(e.getMessage());
        }

        /*
         * The first 2 elements look like this:
         * 
         * <plist version="..."> <dict>
         */
        xmlLogger.info("looking for root elements");

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
         * <key>Date</key> <key>Tracks</key> <key>Playlists</key>
         */
        xmlLogger.info("looking for global information elements");

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
                    MainWindow.logException(xmlLogger, e);
                    handleJDOMError("unable to parse date value " + dateValue.getValue());
                }
            }
            else
            {
                handleJDOMError(dateKey,
                        "could not find <" + ELEM_DATE + "> element after '" + KEY_DATE + "' key");
            }
        }

        /*
         * Locate the following <dict> element for the track key, which in turn
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
        xmlLogger.info("gathering tracks");
        generateTracks(tracksHolder);

        /*
         * Now that the tracks (and artists) are all created, post-process the artists
         * to try and find additional alternate names.
         */
        artistLogger.info("looking for post-process artist alternate names");
        lookForArtistAlternateNames();
        
        /*
         * Verify that all artists contained in alternate name overrides still exist.
         */
        artistLogger.info("verifying artist alternate name overrides");
        verifyArtistOverrides();

        /*
         * Gather playlists.
         */
        xmlLogger.info("gathering playlists");
        generatePlaylists(playlistsHolder);

        /*
         * Set the content count for all folder playlists, so that we can adjust
         * the total number of playlists if a folder playlist is dynamically
         * added to or removed from the ignored list.
         */
        playlistLogger.info("setting content count for all folder playlists");
        PlaylistCollection.setPlaylistFolderCounts();

        /*
         * We don't want to update track playlist counts for bypassed playlists,
         * identified as such through a preference. Mark such playlists now.
         */
        playlistLogger.info("marking bypassed playlists");
        PlaylistCollection.markBypassedPlaylists();

        /*
         * Now we can go through all the playlists, and for those not skipped,
         * update the track playlist information.
         */
        playlistLogger.info("updating playlist information for all playlists");
        PlaylistCollection.updateTrackPlaylistInfo();

        /*
         * Log the XML file statistics.
         */
        logXMLStats();
    }

    /**
     * Transfers an artist as an alternate name to a primary artist.
     * 
     * @param altArtistCorr artist correlator object for the alternate artist
     * @param primaryIdx index into the list of artist correlators for the 
     * primary artist
     * @param altIdx index into the list of artist correlators to be used to 
     * remove the alternate artist 
     */
    public static void transferArtistToPrimary(ArtistCorrelator altArtistCorr, int primaryIdx, int altIdx)
    {
        artistLogger.trace("transferArtistToPrimary (with index)");
        
        if (altArtistCorr == null)
        {
            throw new IllegalArgumentException("altArtistCorr argument is null");
        }
        
        if (primaryIdx < 0)
        {
            throw new IllegalArgumentException("primaryIdx argument is negative");
        }
        
        if (altIdx < 0)
        {
            throw new IllegalArgumentException("altIdx argument is negative");
        }
        
        /*
         * Call the implementation method.
         */
        transferArtistToPrimaryImpl(altArtistCorr, primaryIdx);
        
        /*
         * Delete the now-alternate name from the artist correlators list.
         */
        artistCorrelators.remove(altIdx, 1);
    }

    /**
     * Transfers an artist as an alternate name to a primary artist.
     * 
     * @param altArtistCorr artist correlator object for the alternate artist
     * @param primaryIdx index into the list of artist correlators for the 
     * primary artist
     * @param artistCorrelatorsIter iterator for the list of artist correlators
     * to be used to remove the alternate artist
     */
    public static void transferArtistToPrimary(ArtistCorrelator altArtistCorr, int primaryIdx,
            Iterator<ArtistCorrelator> artistCorrelatorsIter)
    {
        artistLogger.trace("transferArtistToPrimary (with iterator)");
        
        if (altArtistCorr == null)
        {
            throw new IllegalArgumentException("altArtistCorr argument is null");
        }
        
        if (primaryIdx < 0)
        {
            throw new IllegalArgumentException("primaryIdx argument is negative");
        }
        
        if (artistCorrelatorsIter == null)
        {
            throw new IllegalArgumentException("artistCorrelatorsIter argument is null");
        }
        
        /*
         * Call the implementation method.
         */
        transferArtistToPrimaryImpl(altArtistCorr, primaryIdx);
        
        /*
         * Delete the now-alternate name from the artist correlators list.
         */
        artistCorrelatorsIter.remove();
    }
    
    /**
     * Transfers an artist from a primary artist (as an alternate) to a 
     * standalone artist.
     * 
     * @param primaryArtist primary artist name
     * @param altArtist alternate artist name
     */
    public static void transferArtistFromPrimary (String primaryArtist, String altArtist)
    {
        artistLogger.trace("transferArtistFromPrimary");
        
        /*
         * Step 1 is to create the appropriate objects and add the alternate artist to the 
         * artistCorrelators and artists lists ...
         */
        
        /*
         * Access primary artist objects.
         */
        ArtistCorrelator primaryArtistCorr = findArtistCorrelator(primaryArtist);
        Artist primaryArtistObj = artists.get(primaryArtistCorr.getArtistKey());
        ArtistNames primaryArtistNames = primaryArtistObj.getArtistNames();

        /*
         * Create the alternate artist names object.
         */
        ArtistNames altArtistNames = new ArtistNames(altArtist);

        /*
         * Create and add the alternate artist correlator object.
         */
        ArtistCorrelator altArtistCorr = new ArtistCorrelator(altArtist);
        altArtistCorr.setNormalizedName(altArtistNames.normalizeName());
        artistCorrelators.add(altArtistCorr);

        /*
         * Create the alternate artist object and attach the artist names object.
         */
        Artist altArtistObj = new Artist(altArtist);
        altArtistObj.setArtistNames(altArtistNames);
        
        /*
         * Create the correlator key and add the artist object to the list
         */
        Integer correlator = altArtistObj.getCorrelator();
        altArtistCorr.setArtistKey(correlator);
        artists.put(correlator, altArtistObj);
        
        /*
         * Step 2 is to remove the alternate artist from the primary's list of alternate names. 
         * This returns the alternate artist track data representing the alternate. 
         */
        ArtistTrackData altTrackData = primaryArtistNames.getAltNames().remove(altArtist);
        
        /*
         * Step 3 is to update the track counts and times for both the primary artist (by decrementing 
         * the data for the removed alternate), and the alternate artist (by using the artist track 
         * data retrieved above).
         */
        primaryArtistObj.getArtistTrackData().decrementNumLocalTracks(altTrackData.getNumLocalTracks());
        primaryArtistObj.getArtistTrackData().decrementNumRemoteTracks(altTrackData.getNumRemoteTracks());
        primaryArtistObj.getArtistTrackData().decrementTotalLocalTime(altTrackData.getTotalLocalTime());
        primaryArtistObj.getArtistTrackData().decrementTotalRemoteTime(altTrackData.getTotalRemoteTime());
        
        altArtistObj.getArtistTrackData().setNumLocalTracks(altTrackData.getNumLocalTracks());
        altArtistObj.getArtistTrackData().setNumRemoteTracks(altTrackData.getNumRemoteTracks());
        altArtistObj.getArtistTrackData().setTotalLocalTime(altTrackData.getTotalLocalTime());
        altArtistObj.getArtistTrackData().setTotalRemoteTime(altTrackData.getTotalRemoteTime());
    }
    
    /**
     * Finds the artist correlator object for a given artist name.
     * 
     * @param artistName artist name for which to search
     * @return artist correlator object, or null if it could not be found
     */
    public static ArtistCorrelator findArtistCorrelator (String artistName)
    {
        artistLogger.trace("findArtistCorrelator");
        
        ArtistCorrelator artistCorr = null;
        
        /*
         * Create a correlator with which to search.
         */
        ArtistCorrelator searchCorr = new ArtistCorrelator();
        ArtistNames searchNames = new ArtistNames(artistName);
        searchCorr.setNormalizedName(searchNames.normalizeName());

        /*
         * Search the correlator list for the given artist.
         */
        int index = ArrayList.binarySearch(artistCorrelators, searchCorr, artistCorrelators.getComparator());
        
        /*
         * Return the correlator object if we found it.
         */
        if (index >= 0)
        {
            artistCorr = artistCorrelators.get(index);
        }
        
        return artistCorr;
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Create the tracks from the XML file.
     */
    private static void generateTracks(Element tracksHolder)
    {
        trackLogger.trace("generateTracks");

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
         * We collect all the tracks into a ArrayList of type Track. Initialize
         * it now. Also, make sure it's sorted by track name.
         */
        tracks = new ArrayList<Track>();
        tracks.setComparator(new Comparator<Track>()
        {
            @Override
            public int compare(Track t1, Track t2)
            {
                return t1.compareTo(t2);
            }
        });

        /*
         * To be able to find a given track easily, we also create a HashMap of
         * the track ID to the index in the above ArrayList.
         */
        tracksMap = new HashMap<Integer, Integer>();

        /*
         * Initialize the duplicates map as well.
         */
        duplicatesMap = new HashMap<String, List<Integer>>();

        /*
         * Initialize the list of artist names, and set a case-insensitive
         * comparator.
         */
        artistCorrelators = new ArrayList<ArtistCorrelator>();
        artistCorrelators.setComparator(new Comparator<ArtistCorrelator>()
        {
            @Override
            public int compare(ArtistCorrelator c1, ArtistCorrelator c2)
            {
                return c1.compareToNormalized(c2);
            }
        });

        /*
         * Initialize the artists map.
         */
        artists = new HashMap<Integer, Artist>();

        /*
         * Walk through the elements of the parent <dict> element.
         * 
         * This is tricky stuff. Each track consists of a pair of sibling
         * elements: - <key>ID number</key> - <dict> element, whose children are
         * the attributes of the track.
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
                            trackObj.setKind(nextStringValue(trackChildIter, keyValue));
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

                        case "Track Type":
                            if (nextStringValue(trackChildIter, keyValue).equals("Remote"))
                            {
                                trackObj.setRemote(true);
                                remoteTracksCount++;
                            }
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

                /*
                 * Add the track to the duplicates map if necessary. We have to
                 * do this before adding it to the main tracks list, to avoid
                 * false duplicates.
                 */
                int index = ArrayList.binarySearch(tracks, trackObj, tracks.getComparator());
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
                    trackLogger.debug("added track ID " + thisID + " to track '" + trackName + "'");

                    duplicatesMap.put(trackName, trackIDs);
                }

                /*
                 * Add the track object to the list.
                 */
                tracks.add(trackObj);
                trackLogger.debug("found track ID " + ID + ", name '" + trackObj.getName() + "'");
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
                int index =
                        artistNames.matchArtist(artistCorrelators, artistLogger);
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

                    /*
                     * This is tricky. We need to keep a count of artists that
                     * ONLY contain remote tracks, so we can adjust the artist
                     * count if remote tracks are not being shown. So we bump
                     * the remote artists count here if this track is remote. On
                     * the else leg we're hitting the same artist again, so if
                     * that track is local, we then decrement the remote artists
                     * count. And then remember that fact using a flag in the
                     * artist track data object. A thing of beauty, yes?
                     */
                    if (trackObj.getRemote() == true)
                    {
                        remoteArtistsCount++;
                        artistObj.getArtistTrackData().setRemoteArtistControl(
                                ArtistTrackData.RemoteArtistControl.REMOTE);
                    }
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

                    /*
                     * See comment above in the if leg.
                     */
                    if (trackObj.getRemote() == false && 
                            artistObj.getArtistTrackData().getRemoteArtistControl() == 
                            ArtistTrackData.RemoteArtistControl.REMOTE)
                    {
                        remoteArtistsCount--;
                        artistObj.getArtistTrackData().setRemoteArtistControl(
                                ArtistTrackData.RemoteArtistControl.REMOTE_AND_LOCAL);
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
         * Generate the track ID to index mapping. We have to wait until all
         * tracks have been found and sorted in order for the indices to be
         * correct.
         */
        int index = 0;
        for (Track track : tracks)
        {
            int trackID = track.getID();
            tracksMap.put(trackID, index++);
            trackLogger.debug("mapped track ID " + trackID + " to index " + index);
        }
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
        playlistIgnoredCount = 0;

        /*
         * Get a list of the XML playlists to work with.
         */
        List<Element> playlistsXML = javaListToPivotList(playlistsHolder);

        /*
         * We collect all the playlists into a HashMap of the playlist name to
         * its Playlist object. Initialize it now.
         */
        playlists = new HashMap<String, Playlist>();

        /*
         * To be able to find a given playlist by name, we also create a HashMap
         * of the playlist name to ID.
         */
        playlistsMap = new HashMap<String, String>();

        /*
         * Initialize the list of playlist names, and set a case-insensitive
         * comparator.
         */
        playlistNames = new ArrayList<String>();
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
                            incrementPlaylistIgnoredCount(1);
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
                addPlaylistName(playlistObj.getName());
            }
        }
    }
    
    /*
     * Verify that artist names contained in any artist overrides are still valid. The iTunes
     * XML file could have been changed or updated. Fix the overrides for any invalid ones found.
     */
    private static void verifyArtistOverrides ()
    {
        artistLogger.trace("verifyArtistOverrides");
        
        /*
         * We need a search correlator to look up artists in the database.
         */
        ArtistCorrelator searchCorr = new ArtistCorrelator();
        
        /*
         * Get the artist overrides from the preferences.
         */
        List<ArtistAlternateNameOverride> artistOverrides = userPrefs.getArtistOverrides();
        boolean prefsUpdated = false;
        
        /*
         * Loop through all overrides to verify the artists still exist.
         */
        Iterator<ArtistAlternateNameOverride> artistOverridesIter = artistOverrides.iterator();
        while (artistOverridesIter.hasNext())
        {
            ArtistAlternateNameOverride override = artistOverridesIter.next();
            
            int primaryIdx;
            String primaryArtist = override.getPrimaryArtist();

            /*
             * The primary artists always exist in the database, so we search for them there.
             */
            ArtistNames primaryTemp = new ArtistNames(primaryArtist);
            searchCorr.setNormalizedName(primaryTemp.normalizeName());
            
            /*
             * If the primary from the override doesn't exist in the database, remove the entire
             * override and iterate the loop.
             */
            if ((primaryIdx = ArrayList.binarySearch(artistCorrelators, 
                    searchCorr, artistCorrelators.getComparator())) < 0)
            {
                artistLogger.debug("found invalid primary artist " + primaryArtist);
                
                artistOverridesIter.remove();
                prefsUpdated = true;
                continue;
            }

            /*
             * The primary from the override exists in the database. Check the alternates from the 
             * override.
             */
            List<String> alternateArtists = override.getAlternateArtists();
            
            /*
             * If for some reason we have no alternate artists, remove the entire override.
             */
            if (alternateArtists.getLength() == 0)
            {
                artistLogger.debug("found empty primary artist " + primaryArtist);
                
                artistOverridesIter.remove();
                prefsUpdated = true;
                continue;
            }
            
            Iterator<String> alternateArtistsIter = alternateArtists.iterator();
            while (alternateArtistsIter.hasNext())
            {
                String alternateArtist = alternateArtistsIter.next();
                
                /*
                 * Look for this alternate in the primary's alternate list if the override type is
                 * manual.
                 */
                boolean altInvalid = false;
                if (override.getOverrideType() == ArtistAlternateNameOverride.OverrideType.MANUAL)
                {
                    ArtistCorrelator primaryCorr = artistCorrelators.get(primaryIdx);
                    Artist primaryArtistObj = artists.get(primaryCorr.getArtistKey());
                    ArtistNames primaryNames = primaryArtistObj.getArtistNames();
                    Map<String, ArtistTrackData> altNames = primaryNames.getAltNames();

                    /*
                     * If the alternate from the override doesn't exist, flag it.
                     */
                    if (!altNames.containsKey(alternateArtist))
                    {
                        altInvalid = true;
                    }
                }
                
                /*
                 * Look for this alternate in the database if the override type is automatic.
                 */
                else
                {
                    ArtistNames altTemp = new ArtistNames(alternateArtist);
                    searchCorr.setNormalizedName(altTemp.normalizeName());

                    /*
                     * If the alternate from the override doesn't exist, flag it.
                     */
                    if (ArrayList.binarySearch(artistCorrelators, searchCorr, 
                            artistCorrelators.getComparator()) < 0)
                    {
                        altInvalid = true;
                    }
                }
                
                /*
                 * If the alternate is invalid remove it from the override.
                 */
                if (altInvalid == true)
                {
                    artistLogger.debug("found invalid alternate artist " + alternateArtist);

                    alternateArtistsIter.remove();
                    prefsUpdated = true;
                    
                    /*
                     * Remove the entire override if it's now empty.
                     */
                    if (alternateArtists.getLength() == 0)
                    {
                        artistOverridesIter.remove();
                    }
                }
            }
        }
        
        /*
         * Write the user preferences if the overrides were updated.
         */
        if (prefsUpdated == true)
        {
            try
            {
                userPrefs.writePreferences();
            }
            catch (IOException e)
            {
                MainWindow.logException(artistLogger, e);
                throw new InternalErrorException(true, e.getMessage());
            }
        }
    }

    /*
     * As we collected tracks and artists, we were able to find alternate artist names like
     * the following:
     * 
     *   ABC
     *   ABC Featuring Q
     * 
     * But only if the artists were found in the above order. If the name with the "featuring"
     * tag was found first, then we didn't try looking when artist ABC was found, the reason 
     * being it would be very inefficient to check all artists in such a way for a fringe case.
     * 
     * However, such an artist was flagged in the ArtistNames object. So here we run the artist
     * list looking for those flags and trying to find the primary artist. If so, we then process
     * the name with the "featuring" tag as an alternate for the primary.
     * 
     * Similar logic is used for artist overrides set by the user. We have the same 
     * out of order problem detailed above.
     */
    private static void lookForArtistAlternateNames ()
    {
        artistLogger.trace("lookForArtistAlternateNames");

        /*
         * Loop through the correlator list. We need to use an iterator instead of a foreach
         * so we can safely remove correlator items while looping.
         */
        Iterator<ArtistCorrelator> artistCorrelatorsIter = artistCorrelators.iterator();
        while (artistCorrelatorsIter.hasNext())
        {
            ArtistCorrelator artistCorr = artistCorrelatorsIter.next();

            Artist artistObj = artists.get(artistCorr.getArtistKey());
            ArtistNames artistNames = artistObj.getArtistNames();

            /*
             * If the post-processing type is set, try to find the associated primary artist.
             */
            ArtistNames.PostProcessType postProcessType = artistNames.getPostProcessType();
            if (postProcessType != ArtistNames.PostProcessType.NONE)
            {
                artistLogger.debug("artist name '" + artistCorr.getDisplayName() + "', normalized '"
                        + artistCorr.getNormalizedName() + "', post-processing type '"
                        + postProcessType.getDisplayValue() + "'");

                int index = artistNames.checkPostProcessType(artistCorrelators);

                /*
                 * If we found the primary artist then transfer this alternate to it, and remove
                 * this alternate from the correlators list.
                 */
                if (index >= 0)
                {
                    transferArtistToPrimary(artistCorr, index, artistCorrelatorsIter);
                }
            }
        }
    }
    
    /*
     * This is the implementation method for transferring an alternate artist to a primary. There
     * are multiple public methods with differing parameters for deleting the alternate artist
     * correlator, but the bulk of the logic (this method) is identical.
     */
    private static void transferArtistToPrimaryImpl (ArtistCorrelator altArtistCorr, int primaryIdx)
    {
        artistLogger.trace("transferArtistToPrimaryImpl");

        /*
         * Access the objects we need.
         */
        Artist altArtistObj = artists.get(altArtistCorr.getArtistKey());

        ArtistCorrelator primaryArtistCorr = artistCorrelators.get(primaryIdx);
        Artist primaryArtistObj = artists.get(primaryArtistCorr.getArtistKey());
        ArtistNames primaryArtistNames = primaryArtistObj.getArtistNames();

        /*
         * Add the alternate name to the primary artist.
         */
        primaryArtistNames.addAlternateName(altArtistObj.getDisplayName(), 
                altArtistObj.getArtistTrackData(), artistLogger);

        /*
         * Update the primary artist counts and times.
         */
        int updatedValue = primaryArtistObj.getArtistTrackData().getNumLocalTracks() + 
                altArtistObj.getArtistTrackData().getNumLocalTracks();
        primaryArtistObj.getArtistTrackData().setNumLocalTracks(updatedValue);
        updatedValue = primaryArtistObj.getArtistTrackData().getNumRemoteTracks() + 
                altArtistObj.getArtistTrackData().getNumRemoteTracks();
        primaryArtistObj.getArtistTrackData().setNumRemoteTracks(updatedValue);
        updatedValue = primaryArtistObj.getArtistTrackData().getTotalLocalTime() + 
                altArtistObj.getArtistTrackData().getTotalLocalTime();
        primaryArtistObj.getArtistTrackData().setTotalLocalTime(updatedValue);
        updatedValue = primaryArtistObj.getArtistTrackData().getTotalRemoteTime() + 
                altArtistObj.getArtistTrackData().getTotalRemoteTime();
        primaryArtistObj.getArtistTrackData().setTotalRemoteTime(updatedValue);

        /*
         * Delete the now-alternate name from the artist list.
         */
        artists.remove(altArtistCorr.getArtistKey());
    }

    /*
     * Gather the list of tracks for a playlist.
     * 
     * Once again we have XML ridiculousness to deal with. The playlist tracks
     * are laid out like so:
     * 
     * <key>Playlist Items</key> <array> <dict> <key>Track
     * ID</key><integer>nnnn</integer> </dict> </array>
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
     * iTunes XML.
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
            MainWindow.logException(xmlLogger, e);
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
    private static boolean nextBooleanValue(Iterator<Element> trackChildIter, String keyName)
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
            handleJDOMError(nextTrackAttr, "expected <" + ELEM_TRUE + "> or <" + ELEM_FALSE
                    + "> element not found after '" + keyName + "' key");

            /*
             * We can't get here, because handleJDOMError throws an exception.
             * This is just to make the compiler happy.
             */
            return false;
        }
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
            MainWindow.logException(xmlLogger, e);
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

    private static void logXMLStats()
    {
        final String lineSeparator = System.lineSeparator();
        int itemNum = 0;
        StringBuilder output = new StringBuilder();

        /*
         * Indicate we've started.
         */
        output.append("***** XML file statistics *****" + lineSeparator);

        /*
         * XML file timestamp.
         */
        output.append(String.format("%2d", ++itemNum) + ") " + "XML file timestamp: "
                + getXMLFileTimestamp() + lineSeparator);

        /*
         * Number of tracks and remote tracks.
         */
        if (tracks != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of tracks: "
                    + tracks.getLength() + lineSeparator);
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of remote tracks: "
                    + remoteTracksCount + lineSeparator);
        }

        /*
         * Number of playlists and ignored playlists.
         */
        if (playlists != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of playlists: "
                    + playlists.getCount() + lineSeparator);
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of ignored playlists: "
                    + playlistIgnoredCount + lineSeparator);
        }

        /*
         * Number of artists and remote artists.
         */
        if (artists != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of artists: "
                    + artists.getCount() + lineSeparator);
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of remote artists: "
                    + remoteArtistsCount + lineSeparator);
        }

        /*
         * Log it!
         */
        Logger diagLogger = Logging.getInstance().getDiagLogger();
        diagLogger.info(output.toString());
    }

    // ---------------- Nested classes --------------------------------------
    
    /**
     * Inner class that encapsulates reading the iTunes XML file in a
     * background task.
     * 
     * @author Jon
     *
     */
    public static final class ReadXMLTask extends Task<Integer>
    {
        @Override
        public Integer execute()
        {
            int result = 0;
            
            /*
             * Get the user preferences.
             */
            Preferences userPrefs = Preferences.getInstance();
            
            /*
             * Process the XML file.
             */
            try
            {
                processXML(userPrefs.getXMLFileName());
            }
            catch (IOException e)
            {
                MainWindow.logException(xmlLogger, e);
                throw new InternalErrorException(true, e.getMessage());
            }
            
            return result;
            
        }
    }
}