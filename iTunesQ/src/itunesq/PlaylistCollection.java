package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that performs various operations on a collection of playlists.
 * <p>
 * This is a final class consisting entirely of static methods.
 * 
 * @author Jon
 *
 */
public final class PlaylistCollection
{

    // ---------------- Private variables -----------------------------------

    private static String className = PlaylistCollection.class.getSimpleName();
    private static Logger logger = (Logger) LoggerFactory.getLogger(className + "_Playlist");
    private static Logging logging = Logging.getInstance();

    // ---------------- Public methods --------------------------------------

    /**
     * Initializes logging. This is called once at application initialization.
     */
    public static void initializeLogging()
    {
        logging.registerLogger(Logging.Dimension.PLAYLIST, logger);
    }

    /**
     * Modifies the ignored status for all playlists according to the
     * preferences. This is called if the ignored preferences are changed.
     * 
     * @param prevIgnoredPlaylists list of ignored playlists before being
     * changed
     * @param newIgnoredPlaylists list of ignored playlists after being changed
     */
    public static void modifyIgnoredPlaylists(List<String> prevIgnoredPlaylists,
            List<String> newIgnoredPlaylists)
    {
        logger.trace("modifyIgnoredPlaylists");

        if (prevIgnoredPlaylists == null)
        {
            throw new IllegalArgumentException("prevIgnoredPlaylists argument is null");
        }

        if (newIgnoredPlaylists == null)
        {
            throw new IllegalArgumentException("newIgnoredPlaylists argument is null");
        }

        logger.debug("previous ignored playlists count: " + prevIgnoredPlaylists.getLength());
        logger.debug("new ignored playlists count: " + newIgnoredPlaylists.getLength());

        /*
         * Make a difference list of added and removed ignored playlists. All we need are the playlist
         * names, because we're called after the preferences have been updated, so can use the 
         * preferences to determine whether or not the playlist in question is being ignored.
         */
        List<String> ignoredDiffs = new ArrayList<String>();

        /*
         * First, loop through the previous list. If a playlist is also contained in the new list,
         * remove it from there. Otherwise, create an entry in the difference list for the removed
         * playlist.
         */
        for (String prevIgnoredPlaylist : prevIgnoredPlaylists)
        {
            if (newIgnoredPlaylists.indexOf(prevIgnoredPlaylist) != -1)
            {
                newIgnoredPlaylists.remove(prevIgnoredPlaylist);
            }
            else
            {
                ignoredDiffs.add(prevIgnoredPlaylist);
            }
        }

        /*
         * Any playlists still in the new list represent added playlists, so create entries in the
         * difference list to represent them.
         */
        for (String newIgnoredPlaylist : newIgnoredPlaylists)
        {
            ignoredDiffs.add(newIgnoredPlaylist);
        }

        logger.debug("difference ignored playlists count: " + ignoredDiffs.getLength());

        /*
         * Get the preferences object instance.
         */
        Preferences prefs = Preferences.getInstance();

        /*
         * Walk through all playlists.
         */
        Map<String, Playlist> playlists = Database.getPlaylists();

        if (playlists != null)
        {
            for (String playlistKey : playlists)
            {
                Playlist playlistObj = playlists.get(playlistKey);
                String playlistName = playlistObj.getName();

                /*
                 * We only want to process playlists in the difference list.
                 */

                if (ignoredDiffs.indexOf(playlistName) == -1)
                {
                    continue;
                }

                /*
                 * Set or reset the ignored flag.
                 */
                boolean playlistIgnored = isPlaylistIgnored(playlistName);
                playlistObj.setIgnored(playlistIgnored);

                /*
                 * Add or remove the playlist from the list of playlist names, so typing assistance will
                 * function properly.
                 * 
                 * Also, increment or decrement the number of ignored playlists accordingly.
                 */
                boolean playlistCountModified = false;
                int contentCount = playlistObj.getFolderContentCount();

                if (playlistIgnored == true)
                {
                    Database.removePlaylistName(playlistName);
                    if (contentCount > 0)
                    {
                        Database.incrementPlaylistIgnoredCount(contentCount);
                        playlistCountModified = true;
                    }
                }
                else
                {
                    Database.addPlaylistName(playlistName);
                    if (contentCount > 0)
                    {
                        Database.decrementPlaylistIgnoredCount(contentCount);
                        playlistCountModified = true;
                    }
                }

                /*
                 * Update the main window playlist count if it has been modified.
                 */
                if (playlistCountModified == true)
                {
                    Utilities.updateMainWindowLabels(prefs.getInputFileName());
                }
            }
        }
    }

    /**
     * Determines if a playlist should be ignored.
     * 
     * @param playlistName name of the playlist to check
     * @return <code>true</code> if the playlist should be ignored, otherwise
     * <code>false</code>
     */
    public static boolean isPlaylistIgnored(String playlistName)
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
        for (String ignoredPref : ignoredPrefs)
        {
            if (ignoredPref.equals(playlistName))
            {
                logger.debug("playlist '" + playlistName + "' is ignored");
                result = true;
                break;
            }
        }

        return result;
    }
    
    /**
     * Performs post-processing on all playlists.
     */
    public static void postProcessPlaylists()
    {
    	
        /*
         * Set the content count for all folder playlists, so that we can adjust
         * the total number of playlists if a folder playlist is dynamically
         * added to or removed from the ignored list.
         */
    	logger.info("setting content count for all folder playlists");
        setPlaylistFolderCounts();

        /*
         * We don't want to update track playlist counts for bypassed playlists,
         * identified as such through a preference. Mark such playlists now.
         */
        logger.info("marking bypassed playlists");
        markBypassedPlaylists();
        
        /*
         * Folder playlists don't contain any of the tracks of their children. But it makes
         * sense that such tracks should be show for all parents of a "real" playlist.
         * So copy all such tracks to all folder parents.
         */
        bubbleUpPlaylistTracks();

        /*
         * Now we can go through all the playlists, and for those not skipped,
         * update the track playlist information.
         */
        logger.info("updating playlist information for all playlists");
        updateTrackPlaylistInfo();
    	
    }
    
    /**
     * Finds and marks all playlists that are bypassed by user preferences.
     */
    public static void markBypassedPlaylists()
    {
        logger.trace("markBypassedPlaylists");

        /*
         * Walk through all playlists.
         */
        Map<String, Playlist> playlists = Database.getPlaylists();
        for (String playlistKey : playlists)
        {
            Playlist playlistObj = playlists.get(playlistKey);

            /*
             * If this playlist is bypassed, mark it so.
             */
            boolean[] result = checkBypassedPlaylist(playlistObj);
            if (result[0] == true)
            {
                logger.debug("marking playlist '" + playlistObj.getName() + "' as bypassed");
                playlistObj.setBypassed(true);
                continue;
            }

            /*
             * If one of the parents of this playlist is bypassed, mark this one so.
             */
            String parentID;
            Playlist workingPlaylistObj = playlistObj;
            while ((parentID = workingPlaylistObj.getParentPersistentID()) != null)
            {
                workingPlaylistObj = playlists.get(parentID);
                result = checkBypassedPlaylist(workingPlaylistObj);
                if (result[0] == true && result[1] == true)
                {
                    logger.debug("marking playlist '" + playlistObj.getName()
                            + "' as bypassed due to parent playlist '" + workingPlaylistObj.getName() + "'");
                    playlistObj.setBypassed(true);
                    break;
                }
            }
        }
    }

    /**
     * Updates the track playlist info for all playlists that are not ignored.
     * The playlist info consists of the playlist name and a bypassed indicator.
     */
    public static void updateTrackPlaylistInfo()
    {
        logger.trace("updateTrackPlaylistInfo");

        /*
         * Walk through all playlists.
         */
        Map<String, Playlist> playlists = Database.getPlaylists();
        for (String playlistKey : playlists)
        {
            Playlist playlistObj = playlists.get(playlistKey);

            /*
             * Now update the track playlist info. We skip the following:
             * 
             * - Folder playlists (covered by the playlists in the folder)
             * - Ignored playlists
             */
            if (playlistObj.getFolderContentCount() == 0 && playlistObj.getIgnored() == false)
            {

                /*
                 * Walk through all tracks for this playlist, if any.
                 */
                List<Integer> playlistTracks = playlistObj.getTracks();
                if (playlistTracks != null)
                {
                    for (Integer trackID : playlistTracks)
                    {

                        /*
                         * Get the track for this track ID.
                         */
                        Integer trackIndex = Database.getTracksMap().get(trackID);
                        Track track = Database.getTracks().get(trackIndex);
                        String trackName = track.getName();
                        String playlistName = playlistObj.getName();

                        /*
                         * Add this playlist to the track playlist info.
                         */
                        TrackPlaylistInfo playlistInfo = new TrackPlaylistInfo();
                        playlistInfo.setPlaylistName(playlistName);
                        playlistInfo.setBypassed(playlistObj.getBypassed());
                        track.addPlaylistInfoToTrack(playlistInfo);
                        
                        logger.debug("added playlist info for '" + playlistName + "' to track '" + trackName + "' (" + trackID + ")");
                    }
                }
            }
        }
    }

    // ---------------- Private methods -------------------------------------
    
    /*
     * Copy playlist tracks to all parents.
     */
    private static void bubbleUpPlaylistTracks()
    {
        logger.trace("bubbleUpPlaylistTracks");

        /*
         * Walk through all playlists.
         */
        Map<String, Playlist> playlists = Database.getPlaylists();
        List<PlaylistPair> playlistPairs = new ArrayList<PlaylistPair>();
        
        for (String playlistKey : playlists)
        {
            Playlist playlistObj = playlists.get(playlistKey);
            
            String parentID;
            Playlist parentObj = null;
        	
            /*
             * Find the parent if we have one.
             */
            if ((parentID = playlistObj.getParentPersistentID()) != null)
        	{
        		parentObj = playlists.get(parentID);
        	}
            
            /*
             * Interesting playlists are those with both a parent and a list of tracks.
             * Save the playlist pair for each such playlist we find.
             */
            if (playlistObj.getTracks() != null && parentObj != null)
            {
            	PlaylistPair playlistPair = new PlaylistPair(playlistObj, parentObj);
            	playlistPairs.add(playlistPair);
            }
        }
        
        /*
         * Now walk through the list of playlist pairs we found in the above loop.
         */
        for (PlaylistPair playlistPair : playlistPairs)
        {
        	Playlist child = playlistPair.getChild();
        	Playlist parent = playlistPair.getParent();
        	
        	List<Integer> tracks = child.getTracks();
        	
        	/*
        	 * COpy the tracks from the child to the parent.
        	 */
        	copyPlaylistTracks(tracks, parent);
        	
        	/*
        	 * Keep walking up the playlist chain, copying tracks until no more parents exist.
        	 */
        	while (parent != null)
        	{
        		String grandparentID;
        		Playlist grandparentObj = null;
                
        		if ((grandparentID = parent.getParentPersistentID()) != null)
            	{
            		grandparentObj = playlists.get(grandparentID);
                	copyPlaylistTracks(tracks, grandparentObj);
            	}
        		
        		parent = grandparentObj;
        	}
        }
    }
    
    /*
     * Copy child tracks to the parent. We skip any tracks that already exist in the parent.
     */
    private static void copyPlaylistTracks(List<Integer> tracks, Playlist parent)
    {
        logger.trace("copyPlaylistTracks");
        
    	for (Integer trackID : tracks)
    	{
    		boolean found = false;
    		List<Integer> parentTracks = parent.getTracks();
    		
    		for (Integer parentTrackID : parentTracks)
    		{
    			if (parentTrackID.equals(trackID))
    			{
    				found = true;
    				break;
    			}
    		}
    		
    		if (found == false)
    		{
    			parent.getTracks().add(trackID);
    		}
    	}
    }
    
    /*
     * Set the playlist folder content count for all folder playlists.
     * 
     * This indicates the number of playlists contained in a folder. We need
     * this so that the number of playlists can be properly adjusted if folder
     * playlists are dynamically added to or removed from the ignored list.
     */
    private static void setPlaylistFolderCounts()
    {
        logger.trace("setPlaylistFolderCounts");

        /*
         * Walk through all playlists.
         */
        Map<String, Playlist> playlists = Database.getPlaylists();
        for (String playlistKey : playlists)
        {
            Playlist playlistObj = playlists.get(playlistKey);

            String parentID;
            Playlist parentPlaylistObj;
            if ((parentID = playlistObj.getParentPersistentID()) != null)
            {
                parentPlaylistObj = playlists.get(parentID);
                parentPlaylistObj.incrementFolderContentCount();
            }
        }
    }

    /*
     * Check if a playlist is bypassed.
     */
    private static boolean[] checkBypassedPlaylist(Playlist playlistObj)
    {
        logger.trace("checkBypassedPlaylist");

        /*
         * The result is 2 booleans:
         * 
         * [0] = playlist is bypassed
         * [1] = include the children of the playlist
         */
        boolean[] result =
        { false, false };

        /*
         * Get the preferences object instance.
         */
        Preferences prefs = Preferences.getInstance();

        /*
         * Walk the list of bypass playlist preferences.
         */
        List<BypassPreference> bypassPrefs = prefs.getBypassPrefs();
        for (BypassPreference bypassPref : bypassPrefs)
        {

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
