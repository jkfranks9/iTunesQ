package itunesq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.json.simple.DeserializationException;
import org.json.simple.JsonArray;
import org.json.simple.JsonKey;
import org.json.simple.JsonObject;
import org.json.simple.Jsoner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that reads and processes the input JSON file.
 * <p>
 * This is a final class consisting entirely of static methods.
 * <p>
 * The main public method is <code>processJSON</code>, that uses a JSON 
 * reader to walk through the JSON file.
 * 
 * @author Jon
 *
 */

public final class JSONHandler
{

    // ---------------- Private variables -----------------------------------

    private static String className = JSONHandler.class.getSimpleName();
    private static Logger fileLogger = (Logger) LoggerFactory.getLogger(className + "_File");
    private static Logger trackLogger = (Logger) LoggerFactory.getLogger(className + "_Track");
    private static Logger playlistLogger =
            (Logger) LoggerFactory.getLogger(className + "_Playlist");
    private static Logger artistLogger = (Logger) LoggerFactory.getLogger(className + "_Artist");

    private static Date fileDate = null;
    
    private enum JsonKeys implements JsonKey
    {
		TIMESTAMP("TimeStamp"), 
		TRACKS("Tracks"), 
		PLAYLISTS("Playlists");
    	
        private String displayValue;

		JsonKeys(String s)
		{
            displayValue = s;
		}

		@Override
		public String getKey()
		{
			return displayValue;
		}

		@Override
		public Object getValue()
		{
			return null;
		}    	
    }
    
    private enum TrackKeys implements JsonKey
    {
		ALBUM("Album"),
		ARTIST("Artist"),
		BITRATE("Bitrate"),
		COMPOSER("Composer"),
		DATE_ADDED("DateAdded"),
		DATE_MODIFIED("DateModified"),
		DURATION("Duration"),
		ENCODER("Encoder"),
		GENRE("Genre"),
		KIND("Kind"),
		NAME("Name"),
		PLAY_COUNT("PlayCount"),
		RATING("Rating"),
		SAMPLE_RATE("SampleRate"),
		SIZE("Size"),
		TRACK_ID("TrackID"),
		YEAR("Year");
    	
        private String displayValue;

        TrackKeys(String s)
		{
            displayValue = s;
		}

		@Override
		public String getKey()
		{
			return displayValue;
		}

		@Override
		public Object getValue()
		{
			return null;
		}
    	
    }
    
    private enum PlaylistKeys implements JsonKey
    {
		ITEMS("Items"),
		NAME("Name"),
		NUM_ITEMS("NumItems"),
		PARENT_PERSISTENT_ID("ParentPersistentID"),
		PERSISTENT_ID("PersistentID");
    	
        private String displayValue;

        PlaylistKeys(String s)
		{
            displayValue = s;
		}

		@Override
		public String getKey()
		{
			return displayValue;
		}

		@Override
		public Object getValue()
		{
			return null;
		}
    	
    }

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
     * Gets the timestamp of the JSON file.
     * 
     * @return file timestamp
     */
    public static String getJSONFileTimestamp()
    {
        return (fileDate != null) ? Utilities.formatDate(fileDate) : "";
    }

    /**
     * Reads and processes the JSON file.
     * 
     * @param jsonFileName JSON file name
     * @throws IOException If an error occurs trying to read the JSON
     * file.
     */
    public static void processJSON(String jsonFileName) 
            throws IOException
    {
        fileLogger.trace("processJSON");

        JsonObject json = null;

        fileLogger.info("creating JSON reader");
        //Reader reader = new BufferedReader(new FileReader(jsonFileName));
        Reader reader = new InputStreamReader(new FileInputStream(jsonFileName), StandardCharsets.UTF_8);

        fileLogger.info("deserializing JSON document");
        try
        {
			json = (JsonObject) Jsoner.deserialize(reader);
		}
        catch (DeserializationException e)
        {
            MainWindow.logException(fileLogger, e);
            handleJSONError(e.getMessage());
		}

        fileLogger.info("getting global information elements");
		String fileTimestamp = json.getString(JsonKeys.TIMESTAMP);
		JsonArray tracksHolder = new JsonArray(json.getCollection(JsonKeys.TRACKS));			
		JsonArray playlistsHolder = new JsonArray(json.getCollection(JsonKeys.PLAYLISTS));
        
        /*
         * Parse the date if we were successful.
         */
        if (fileTimestamp != null)
        {
            try
            {
                fileDate = Utilities.parseDate(fileTimestamp);
            }
            catch (ParseException e)
            {
                MainWindow.logException(fileLogger, e);
                handleJSONError("unable to parse date value " + fileTimestamp);
            }
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
         * Log the JSON file statistics.
         */
        Database.logFileStats();
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Create the tracks from the JSON file.
     */
    private static void generateTracks(JsonArray tracksHolder)
    {
        trackLogger.trace("generateTracks");

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
         * Get the codecs and encoder maps.
         */
        Map<String, CodecStats> codecStats = Database.getCodecStats();
        codecStats.setComparator(String.CASE_INSENSITIVE_ORDER);
        
        Map<String, Integer> encoderStats = Database.getEncoderStats();
        encoderStats.setComparator(String.CASE_INSENSITIVE_ORDER);

        /*
         * Walk through the elements of the JSON structure.
         */
        int ID = 0;

        trackLogger.debug("starting track loop");
		for (int i = 0; i < tracksHolder.size(); i++)
		{
			
			/*
			 * Grab the JSON object representing this track and get the track ID.
			 */
			JsonObject track = tracksHolder.getMap(i);
			ID = track.getInteger(TrackKeys.TRACK_ID);

            /*
             * Initialize the track object.
             */
			Track trackObj = new Track(ID);

            /*
             * Fill in most of the track object.
             */
			trackObj.setAlbum(track.getString(TrackKeys.ALBUM));
			trackObj.setArtist(track.getString(TrackKeys.ARTIST));
			trackObj.setBitRate(track.getInteger(TrackKeys.BITRATE));
			trackObj.setComposer(track.getString(TrackKeys.COMPOSER));
			trackObj.setDuration(track.getInteger(TrackKeys.DURATION));
			trackObj.setGenre(track.getString(TrackKeys.GENRE));
			trackObj.setName(track.getString(TrackKeys.NAME));
			trackObj.setPlayCount(track.getInteger(TrackKeys.PLAY_COUNT));
			trackObj.setRating(track.getInteger(TrackKeys.RATING));	
			trackObj.setSampleRate(track.getInteger(TrackKeys.SAMPLE_RATE));			
			trackObj.setSize(track.getInteger(TrackKeys.SIZE));
			trackObj.setYear(track.getInteger(TrackKeys.YEAR));

			/*
			 * Parse the dates.
			 */
			String dateAdded = track.getString(TrackKeys.DATE_ADDED);
			try
			{
				trackObj.setDateAdded(Utilities.parseDate(dateAdded));
			}
			catch (ParseException e)
			{
                MainWindow.logException(fileLogger, e);
                handleJSONError("unable to parse date value " + dateAdded);
			}
			
			String dateModified = track.getString(TrackKeys.DATE_MODIFIED);
			try
			{
				trackObj.setModified(Utilities.parseDate(dateModified));
			}
			catch (ParseException e)
			{
                MainWindow.logException(fileLogger, e);
                handleJSONError("unable to parse date value " + dateModified);
			}
			
			/*
			 * Kind is special: collect codec stats and set the track type.
			 */
        	String kind = track.getString(TrackKeys.KIND);
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
			
			/*
			 * Encoder is special: collect encoder stats.
			 */
        	String encoder = track.getString(TrackKeys.ENCODER);
            trackObj.setEncoder(encoder);
            
            /*
             * Accumulate a count of each encoder discovered.
             */
            Integer encoderNum = encoderStats.get(encoder);
            if (encoder.length() > 0)
            {
            	if (encoderNum != null)
            	{
            		encoderStats.put(encoder, ++encoderNum);
            	}
            	else
            	{
            		encoderStats.put(encoder, 1);
            	}
            }

            /*
             * Add the track to the duplicates map if necessary. We have to
             * do this before adding it to the main tracks list, to avoid
             * false duplicates.
             */
            boolean trackLogged = false;
            Track.TrackType trackType = trackObj.getTrackType();
            
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
                int artistIndex = artistNames.matchArtist(artistCorrelators, artistLogger);
                if (artistIndex < 0)
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
                    ArtistCorrelator artistCorr = artistCorrelators.get(artistIndex);
                    Artist artistObj = artists.get(artistCorr.getArtistKey());
                    artistObj.addTrackToArtist(trackObj, artistLogger);
                    artists.put(artistCorr.getArtistKey(), artistObj);
                    artistLogger.debug("updated existing artist name '" + artist + "', normalized '"
                            + normalizedName + "'");
                }
            }
		}

        /*
         * Generate the track ID to index mappings. We have to wait until all
         * tracks have been found and sorted in order for the indices to be
         * correct.
         */
        Database.generateTrackIDMappings(trackLogger);
    }

    /*
     * Create the playlists from the JSON file.
     */
    private static void generatePlaylists(JsonArray playlistsHolder)
    {
        playlistLogger.trace("generatePlaylists");

        /*
         * Reset the playlist ignored count, so it doesn't keep growing if we
         * reread the JSON file.
         */
        Database.setPlaylistIgnoredCount(0);

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
         * Walk through the elements of the JSON structure.
         */

        trackLogger.debug("starting playlist loop");
		for (int i = 0; i < playlistsHolder.size(); i++)
		{
			
			/*
			 * Grab the JSON object representing this playlist.
			 */
			JsonObject playlist = playlistsHolder.getMap(i);

            /*
             * Create a new playlist object and fill it in.
             */
            Playlist playlistObj = new Playlist();
			
            playlistObj.setName(playlist.getString(PlaylistKeys.NAME));
            playlistObj.setParentPersistentID(playlist.getString(PlaylistKeys.PARENT_PERSISTENT_ID));
            playlistObj.setPersistentID(playlist.getString(PlaylistKeys.PERSISTENT_ID));

            /*
             * Gather the playlist track IDs.
             */
            Integer numItems = playlist.getInteger(PlaylistKeys.NUM_ITEMS);
            if (numItems > 0)
            {
            	JsonArray tracksHolder = new JsonArray(playlist.getCollectionOrDefault(PlaylistKeys.ITEMS));
            	List<Integer> playlistTracks = gatherPlaylistTracks(tracksHolder);

                playlistLogger.debug("playlist '" + playlistObj.getName() + "' has "
                        + playlistTracks.getLength() + " tracks");
                
                playlistObj.setTracks(playlistTracks);
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
     */
    private static List<Integer> gatherPlaylistTracks(JsonArray tracksHolder)
    {
        playlistLogger.trace("gatherPlaylistTracks");

        List<Integer> playlistTracks = new LinkedList<Integer>();
		
        /*
         * The tracks are a simple array of tracks IDs.
         */
        for (int i = 0; i < tracksHolder.size(); i++)
        {
        	playlistTracks.add(tracksHolder.getInteger(i));
        }

        return playlistTracks;
    }

    /*
     * Handle a JSON error. This method does not return.
     */
    private static void handleJSONError(String message)
    {

        /*
         * Throw an XMLProcessingException.
         */
        throw new JSONProcessingException(message);
    }
}