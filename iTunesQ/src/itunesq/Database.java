package itunesq;

import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;

import ch.qos.logback.classic.Logger;

/**
 * Class that contains the internal database in which tracks, playlists
 * and artists are stored.
 * <p>
 * This is a final class consisting entirely of static methods.
 * 
 * @author Jon
 *
 */
public final class Database
{

    // ---------------- Class variables -------------------------------------

    /*
     * The list of all tracks; just a simple list.
     */
    private static ArrayList<Track> tracks = null;

    /*
     * The different tracks maps are a map of the track ID to its index value in the 
     * all tracks list. This means it can't be created until the entire list has been
     * created and sorted. This map facilitates quick searches of a track given
     * its track ID (tracks within a playlist are identified only by ID).
     */
    private static Map<Integer, Integer> tracksMap = null;
    private static Map<Integer, Integer> audioTracksMap = null;
    private static Map<Integer, Integer> videoTracksMap = null;

    /*
     * The duplicates map is a map of the track name to a list of track IDs.
     * This allows us to find duplicates quickly on demand, at the cost of
     * longer time to process the input file.
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
    
    /*
     * Miscellaneous statistics.
     */
    private static Map<String, CodecStats> codecStats = null;
    private static Map<String, Integer> encoderStats = null;

    // ---------------- Getters and setters ---------------------------------
    
    /**
     * Gets the list of all tracks.
     * 
     * @return list of all tracks
     */
    public static ArrayList<Track> getTracks()
    {
        return tracks;
    }
    
    /**
     * Gets the mapping of all track IDs to track list indices, regardless of type.
     * 
     * @return mapping of all track IDs to track list indices
     */
    public static Map<Integer, Integer> getTracksMap()
    {
        return tracksMap;
    }

    /**
     * Gets the mapping of audio track IDs to track list indices.
     * 
     * @return mapping of audio track IDs to indices
     */
    public static Map<Integer, Integer> getAudioTracksMap()
    {
        return audioTracksMap;
    }

    /**
     * Gets the mapping of video track IDs to track list indices.
     * 
     * @return mapping of video track IDs to indices
     */
    public static Map<Integer, Integer> getVideoTracksMap()
    {
        return videoTracksMap;
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
     * Gets the list of playlists found in the input file.
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
     * Gets the list of playlist names found in the input file.
     * 
     * @return list of playlist names
     */
    public static ArrayList<String> getPlaylistNames()
    {
        return playlistNames;
    }

    /**
     * Gets the list of track artist names found in the input file.
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
    
    /**
     * Sets the playlist ignored count.
     * 
     * @param count playlist ignored count
     */
    public static void setPlaylistIgnoredCount(int count)
    {
    	playlistIgnoredCount = count;
    }

    /**
     * Gets the codec statistics.
     * 
     * @return codec statistics
     */
    public static Map<String, CodecStats> getCodecStats()
    {
        return codecStats;
    }

    /**
     * Gets the encoder statistics.
     * 
     * @return encoder statistics
     */
    public static Map<String, Integer> getEncoderStats()
    {
        return encoderStats;
    }

    // ---------------- Public methods --------------------------------------
    
    /**
     * Initializes the database. This is called once at initialization.
     */
    public static void initializeDB()
    {
    	tracks = new ArrayList<Track>();
    	tracksMap = new HashMap<Integer, Integer>();
        audioTracksMap = new HashMap<Integer, Integer>();
        videoTracksMap = new HashMap<Integer, Integer>();
        duplicatesMap = new HashMap<String, List<Integer>>();
        playlists = new HashMap<String, Playlist>();
        playlistsMap = new HashMap<String, String>();
        playlistNames = new ArrayList<String>();
        artistCorrelators = new ArrayList<ArtistCorrelator>();
        artists = new HashMap<Integer, Artist>();
        codecStats = new HashMap<String, CodecStats>();
        encoderStats = new HashMap<String, Integer>();
    }
    
    /**
     * Generates the various mappings of track ID to list index for all tracks.
     * 
     * @param trackLogger logger to use
     */
    public static void generateTrackIDMappings(Logger trackLogger)
    {        
        if (trackLogger == null)
        {
            throw new IllegalArgumentException("trackLogger argument is null");
        }
        
        int index = 0;
        
        for (Track track : tracks)
        {
            int trackID = track.getID();
            tracksMap.put(trackID, index);
            
            Track.TrackType trackType = track.getTrackType();
            switch (trackType)
            {
            case VIDEO:
                videoTracksMap.put(trackID, index);
                trackLogger.debug("mapped video track ID " + trackID + " to index " + index + ", name '" + track.getName() + "'");
            	break;

            case UNKNOWN:
            	trackLogger.warn("track ID " + trackID + ", name '" + track.getName() + "'" +
            			" unknown track type - assuming audio");
            	
            case AUDIO:
                audioTracksMap.put(trackID, index);
                trackLogger.debug("mapped audio track ID " + trackID + " to index " + index + ", name '" + track.getName() + "'");
                break;
                
            default:
                throw new InternalErrorException(true, "unknown track type '" + trackType + "'");
            }
            
            index++;
        }
    }
    
    /**
     * Gets a list of tracks based on a tracks map.
     * 
     * @param tracksMap map of track ID to index in the all tracks list
     * @return list of tracks
     */
    public static List<Track> getTracksFromMap(Map<Integer, Integer> tracksMap)
    {
    	List<Track> trackList = new ArrayList<Track>();
    	trackList.setComparator(new Comparator<Track>()
        {
            @Override
            public int compare(Track t1, Track t2)
            {
                return t1.compareTo(t2);
            }
        });
    	
    	for (Integer ID : tracksMap)
    	{
    		Integer index = tracksMap.get(ID);
    		trackList.add(tracks.get(index));
    	}
    	
    	return trackList;
    }

    /**
     * Gets the number of all tracks found in the input file.
     * 
     * @return number of all tracks
     */
    public static int getNumberOfTracks()
    {
        return (tracks != null) ? tracks.getLength() : 0;
    }

    /**
     * Gets the number of audio tracks found in the input file.
     * 
     * @return number of audio tracks
     */
    public static int getNumberOfAudioTracks()
    {
        return (audioTracksMap != null) ? audioTracksMap.getCount() : 0;
    }

    /**
     * Gets the number of video tracks found in the input file.
     * 
     * @return number of video tracks
     */
    public static int getNumberOfVideoTracks()
    {
        return (videoTracksMap != null) ? videoTracksMap.getCount() : 0;
    }

    /**
     * Gets the number of playlists found in the input file, reduced by the
     * number of ignored playlists.
     * 
     * @return number of non-ignored playlists
     */
    public static int getNumberOfPlaylists()
    {
        return (playlists != null) ? playlists.getCount() - playlistIgnoredCount : 0;
    }

    /**
     * Gets the number of artist names found in the input file.
     * 
     * @return number of artists
     */
    public static int getNumberOfArtists()
    {
        return (artists != null) ? artists.getCount() : 0;
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
     * Finds the artist correlator object for a given artist name.
     * 
     * @param artistName artist name for which to search
     * @return artist correlator object, or null if it could not be found
     */
    public static ArtistCorrelator findArtistCorrelator (String artistName)
    {        
        ArtistCorrelator artistCorr = null;

        /*
         * Search the correlator list for the given artist.
         */
        int index = findArtistCorrelatorIndex(artistName);
        
        /*
         * Return the correlator object if we found it.
         */
        if (index >= 0)
        {
            artistCorr = artistCorrelators.get(index);
        }
        
        return artistCorr;
    }
    
    /**
     * Finds the artist correlator index for a given artist name.
     * 
     * @param artistName artist name for which to search
     * @return artist correlator index, or -1 if it could not be found
     */
    public static Integer findArtistCorrelatorIndex(String artistName)
    {
    	int index;
        
        /*
         * Create a correlator with which to search.
         */
        ArtistCorrelator searchCorr = new ArtistCorrelator();
        ArtistNames searchNames = new ArtistNames(artistName);
        searchCorr.setNormalizedName(searchNames.normalizeName());

        /*
         * Search the correlator list for the given artist.
         */
        index = ArrayList.binarySearch(artistCorrelators, searchCorr, artistCorrelators.getComparator());
        
        return index;
    }

    /**
     * Logs statistics for the input file.
     */
    public static void logFileStats()
    {
        final String lineSeparator = System.lineSeparator();
        final String indent = "      ";
        final String listPrefix = "- ";
        int itemNum = 0;
        StringBuilder output = new StringBuilder();

        /*
         * Indicate we've started.
         */
        output.append("***** Input file statistics *****" + lineSeparator); 

        /*
         * Number of audio tracks.
         */
        Map<Integer, Integer> audioTracksMap = Database.getAudioTracksMap();
        if (audioTracksMap != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of audio tracks: "
                    + audioTracksMap.getCount() + lineSeparator);
        }   

        /*
         * Number of video tracks.
         */
        Map<Integer, Integer> videoTracksMap = Database.getVideoTracksMap();
        if (videoTracksMap != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of video tracks: "
                    + videoTracksMap.getCount() + lineSeparator);
        }

        /*
         * Number of playlists and ignored playlists.
         */
        Map<String, Playlist> playlists = Database.getPlaylists();
        if (playlists != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of playlists: "
                    + playlists.getCount() + lineSeparator);
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of ignored playlists: "
                    + playlistIgnoredCount + lineSeparator);
        }

        /*
         * Number of artists.
         */
        Map<Integer, Artist> artists = Database.getArtists();
        if (artists != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Number of artists: "
                    + artists.getCount() + lineSeparator);
        }
        
        /*
         * Codec statistics.
         */
        if (codecStats != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Codec statistics: " + lineSeparator);
            for (String codec : codecStats)
            {
            	CodecStats stats = codecStats.get(codec);
                output.append(indent + listPrefix + codec + ": " + stats.getCount() + lineSeparator);
            }
        }
        
        /*
         * Encoder statistics.
         */
        if (encoderStats != null)
        {
            output.append(String.format("%2d", ++itemNum) + ") " + "Encoder statistics: " + lineSeparator);
            for (String encoder : encoderStats)
            {
                output.append(indent + listPrefix + encoder + ": " + encoderStats.get(encoder) + lineSeparator);
            }
        }

        /*
         * Log it!
         */
        Logger diagLogger = Logging.getInstance().getDiagLogger();
        diagLogger.info(output.toString());
    }
}