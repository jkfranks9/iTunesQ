package itunesq;

import java.util.Iterator;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that performs various operations on a collection of playlists.
 * 
 * @author Jon
 *
 */
public final class PlaylistCollection
{

    //---------------- Private variables -----------------------------------

	private static String className = PlaylistCollection.class.getSimpleName();
	private static Logger logger = (Logger) LoggerFactory.getLogger(className + "_Playlist");
	private static Logging logging = Logging.getInstance();

    //---------------- Public methods --------------------------------------
	
	/**
	 * Initialize logging. This is called once at application initialization.
	 */
	public static void initializeLogging ()
	{
		logging.registerLogger(Logging.Dimension.PLAYLIST, logger);
	}
	
	/**
	 * Determine if a playlist should be ignored.
	 * 
	 * @param playlistName Name of the playlist to check.
	 * @return true if the playlist should be ignored, false otherwise.
	 */
	public static boolean isPlaylistIgnored (String playlistName)
	{
    	logger.trace("isPlaylistIgnored");
    	
		boolean result = false;
        
        /*
         * Get the preferences object instance.
         */
        Preferences prefs = Preferences.getInstance();
		
        /*
         * Walk the list of ignored playlist preferences.
         */
    	List<String> ignoredPrefs = prefs.getIgnoredPrefs();
    	Iterator<String> ignoredPrefsIter = ignoredPrefs.iterator();
    	while (ignoredPrefsIter.hasNext())
    	{
    		String ignoredPref = ignoredPrefsIter.next();
    		
        	if (ignoredPref.equals(playlistName))
        	{
    			logger.debug("'" + playlistName + "' is ignored");
        		result = true;
        		break;
        	}
    	}
    	
		return result;
	}
	
	/**
	 * Find and mark all playlists for which we don't want to update track playlist info.
	 */
	public static void markPlaylists ()
	{
    	logger.trace("markPlaylists");
		
		/*
		 * Walk through all playlists.
		 */
		Map<String, Playlist> playlists = XMLHandler.getPlaylists();
        Iterator<String> playlistsIter = playlists.iterator();
        while (playlistsIter.hasNext())
        {
        	String playlistKey = playlistsIter.next();
        	Playlist playlistObj = playlists.get(playlistKey);
        	
        	/*
        	 * If this playlist should not be considered, mark it so.
        	 */
        	boolean[] result = checkBypassedPlaylist(playlistObj);
        	if (result[0] == true)
        	{
    			logger.debug("skipping playlist info for '" + playlistObj.getName() + "'");
        		playlistObj.setSkipPlaylistInfo(true);
        		continue;
        	}
        	
        	/*
        	 * If one of the parents of this playlist should not be considered, mark it so.
        	 */
    		String parentID;
    		Playlist workingPlaylistObj = playlistObj;
    		while ((parentID = workingPlaylistObj.getParentPersistentID()) != null)
    		{
    			workingPlaylistObj = playlists.get(parentID);
    			result = checkBypassedPlaylist(workingPlaylistObj);
    			if (result[0] == true && result[1] == true)
    			{
        			logger.debug("skipping playlist info for '" + playlistObj.getName() + 
        					"' due to parent playlist '" + workingPlaylistObj.getName() + "'");
    				playlistObj.setSkipPlaylistInfo(true);
    				break;
    			}
    		}
        }		
	}
	
	/**
	 * Update the track playlist info for all playlists.
	 */
	public static void updateTrackPlaylistInfo ()
	{
    	logger.trace("updateTrackPlaylistInfo");
		
		/*
		 * Walk through all playlists.
		 */
		Map<String, Playlist> playlists = XMLHandler.getPlaylists();
        Iterator<String> playlistsIter = playlists.iterator();
        while (playlistsIter.hasNext())
        {
        	String playlistKey = playlistsIter.next();
        	Playlist playlistObj = playlists.get(playlistKey);
    		
    		/*
    		 * Now update the track playlist info. We skip the following:
    		 * 
    		 * - Folder playlists (covered by the playlists in the folder)
    		 * - Ignored playlists
    		 */
        	if (playlistObj.getIsFolder() == false && 
        		playlistObj.getIgnored() == false)
        	{
        		
        		/*
        		 * Walk through all tracks for this playlist, if any.
        		 */
        		List<Integer> playlistTracks = playlistObj.getTracks();
        		if (playlistTracks != null)
        		{
        			Iterator<Integer> playlistTracksIter = playlistTracks.iterator();
        			while (playlistTracksIter.hasNext())
        			{
        				Integer trackID = playlistTracksIter.next();

        				/*
        				 * Get the track for this track ID.
        				 */
        				Integer trackIndex = XMLHandler.getTracksMap().get(trackID);
        				Track track = XMLHandler.getTracks().get(trackIndex);

        				/*
        				 * Add this playlist to the track playlist info.
        				 */
        				TrackPlaylistInfo playlistInfo = new TrackPlaylistInfo();
        				playlistInfo.setPlaylistName(playlistObj.getName());
        				playlistInfo.setBypassed(playlistObj.getSkipPlaylistInfo());
        				track.addPlaylistInfoToTrack(playlistInfo);
        			}
        		}
        	}
        }
	}

    //---------------- Private methods -------------------------------------
	
	/*
	 * Check if a playlist should not be considered when collecting playlist names for a track.
	 */
	private static boolean[] checkBypassedPlaylist (Playlist playlistObj)
	{
    	logger.trace("checkBypassedPlaylist");
		
		/*
		 * The result is 2 booleans:
		 * 
		 * [0] = playlist should not be considered
		 * [1] = include the children of the playlist
		 */
		boolean[] result = {false, false};
        
        /*
         * Get the preferences object instance.
         */
        Preferences prefs = Preferences.getInstance();
		
        /*
         * Walk the list of bypass playlist preferences.
         */
    	List<BypassPreference> bypassPrefs = prefs.getBypassPrefs();
    	Iterator<BypassPreference> bypassPrefsIter = bypassPrefs.iterator();
    	while (bypassPrefsIter.hasNext())
    	{
    		BypassPreference bypassPref = bypassPrefsIter.next();
    		
    		/*
    		 * Get the result for the input playlist.
    		 */
        	if (playlistObj.getName().equals(bypassPref.getPlaylistName()))
        	{
        		result[0] = true;
            	result[1] = bypassPref.getIncludeChildren();
        		break;
        	}
    	}
    	
    	return result;
	}
}
