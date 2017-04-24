package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;

/**
 * Class that represents a single playlist.
 * 
 * Interesting playlist data are defined for a playlist. There are three types of playlists:
 * 
 * 1) Top level folder that contains other folders or playlists. This is indicated by plIsFolder
 *    true but no plParentPersistentID.
 *    
 * 2) Intermediate folder that contains other folders or playlists. This is indicated by plIsFolder
 *    true and a plParentPersistentID.
 *    
 * 3) Non-folder playlist. This is indicated by plIsFolder false.
 * 
 * @author Jon
 *
 */
public class Playlist
{

    //---------------- Class variables -------------------------------------
	
	/**
	 * Default list of playlist names that should be filtered out.
	 */
	public static final List<String> DEFAULT_FILTERED_PLAYLISTS;
	static
	{
		List<String> result = new ArrayList<String>();
		result.add("Books");
		result.add("iTunes U");
		result.add("Library");
		result.add("Movies");
		result.add("Music");
		result.add("Podcasts");
		result.add("Purchased");
		result.add("Tones");
		result.add("TV Shows");
		
		DEFAULT_FILTERED_PLAYLISTS = result;
	};
	
	/*
	 * Playlist attributes.
	 */
	private String plName;
	private String plPersistentID;
	private String plParentPersistentID;
	private boolean plIsFolder;
	private List<Integer> plTracks;
	
	private boolean plFilteredOut;
	private boolean plSkipPlaylistInfo;
	
	/**
	 * Constructor.
	 */
	public Playlist ()
	{
		plIsFolder = false;
		plFilteredOut = false;
		plSkipPlaylistInfo = false;
	}

    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Get the playlist name.
	 * 
	 * @return Playlist name.
	 */
	public String getName ()
	{
		return plName;
	}
	
	/**
	 * Set the playlist name.
	 * 
	 * @param name Playlist name.
	 */
	public void setName (String name)
	{
		this.plName = name;
	}
	
	/**
	 * Get the playlist persistent ID.
	 * 
	 * @return Playlist persistent ID.
	 */
	public String getPersistentID ()
	{
		return plPersistentID;
	}
	
	/**
	 * Set the playlist persistent ID.
	 * 
	 * @param persistentID Playlist persistent ID.
	 */
	public void setPersistentID (String persistentID)
	{
		this.plPersistentID = persistentID;
	}
	
	/**
	 * Get the playlist parent persistent ID.
	 * 
	 * @return Playlist parent persistent ID.
	 */
	public String getParentPersistentID ()
	{
		return plParentPersistentID;
	}
	
	/**
	 * Set the playlist parent persistent ID.
	 * 
	 * @param parentPersistentID Playlist parent persistent ID.
	 */
	public void setParentPersistentID (String parentPersistentID)
	{
		this.plParentPersistentID = parentPersistentID;
	}
	
	/**
	 * Get the playlist folder indicator.
	 * 
	 * @return Playlist folder indicator.
	 */
	public boolean getIsFolder ()
	{
		return plIsFolder;
	}
	
	/**
	 * Set the playlist folder indicator.
	 * 
	 * @param isFolder Playlist folder indicator.
	 */
	public void setIsFolder (boolean isFolder)
	{
		this.plIsFolder = isFolder;
	}
	
	/**
	 * Get the playlist tracks.
	 * 
	 * @return Playlist tracks.
	 */
	public List<Integer> getTracks ()
	{
		return plTracks;
	}
	
	/**
	 * Set the playlist tracks.
	 * 
	 * @param tracks Playlist tracks.
	 */
	public void setTracks (List<Integer> tracks)
	{
		this.plTracks = tracks;
	}
	
	/**
	 * Get the playlist filtered out indicator.
	 * 
	 * @return Playlist filtered out indicator.
	 */
	public boolean getFilteredOut ()
	{
		return plFilteredOut;
	}
	
	/**
	 * Set the playlist filtered out indicator.
	 * 
	 * @param filtered Playlist filtered out indicator.
	 */
	public void setFilteredOut (boolean filtered)
	{
		this.plFilteredOut = filtered;
	}

	/**
	 * Get the skip playlist info indicator.
	 * 
	 * @return Skip playlist info indicator.
	 */
	public boolean getSkipPlaylistInfo()
	{
		return plSkipPlaylistInfo;
	}
	
	/**
	 * Set the skip playlist info indicator.
	 * 
	 * @param skipPlaylistInfo Skip playlist info indicator.
	 */
	public void setSkipPlaylistInfo(boolean skipPlaylistInfo)
	{
		this.plSkipPlaylistInfo = skipPlaylistInfo;
	}

    //---------------- Public methods --------------------------------------

	/**
	 * Comparison function to sort by the persistent ID.
	 * 
	 * @param key2 The persistent ID to compare to this persistent ID.
	 * @return -1, 0, or 1 to indicate less than, equal, or greater than.
	 */
	public int compareTo (String key2)
	{
		return this.plPersistentID.compareTo(key2);
	}
}
