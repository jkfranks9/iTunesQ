package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;

/**
 * Class that represents a single playlist.
 * <p>
 * Note that there are three types of playlists:
 * <ol>
 * <li>Top level folder that contains other folders or playlists. This is 
 * indicated by <code>plIsFolder</code> equal to <code>true</code> but no 
 * <code>plParentPersistentID</code> value.</li>
 * <li>Intermediate folder that contains other folders or playlists. This is 
 * indicated by <code>plIsFolder</code> equal to <code>true</code> along with
 * a <code>plParentPersistentID</code> value.</li>
 * <li>Non-folder playlist. This is indicated by <code>plIsFolder</code> 
 * equal to <code>false</code>.
 * </ol>
 * 
 * @author Jon
 *
 */
public class Playlist
{

    //---------------- Class variables -------------------------------------
	
	/**
	 * Default list of playlist names that should be ignored.
	 */
	public static final List<String> DEFAULT_IGNORED_PLAYLISTS;
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
		
		DEFAULT_IGNORED_PLAYLISTS = result;
	};
	
	/*
	 * Playlist attributes.
	 */
	private String plName;
	private String plPersistentID;
	private String plParentPersistentID;
	private boolean plIsFolder;
	private List<Integer> plTracks;
	
	private boolean plIgnored;
	private boolean plBypassed;
	
	/**
	 * Class constructor.
	 */
	public Playlist ()
	{
		plIsFolder = false;
		plIgnored = false;
		plBypassed = false;
	}

    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the playlist name.
	 * 
	 * @return playlist name
	 */
	public String getName ()
	{
		return plName;
	}
	
	/**
	 * Sets the playlist name.
	 * 
	 * @param name playlist name
	 */
	public void setName (String name)
	{
		this.plName = name;
	}
	
	/**
	 * Gets the playlist persistent ID.
	 * 
	 * @return playlist persistent ID
	 */
	public String getPersistentID ()
	{
		return plPersistentID;
	}
	
	/**
	 * Sets the playlist persistent ID.
	 * 
	 * @param persistentID playlist persistent ID
	 */
	public void setPersistentID (String persistentID)
	{
		this.plPersistentID = persistentID;
	}
	
	/**
	 * Gets the playlist parent persistent ID.
	 * 
	 * @return playlist parent persistent ID
	 */
	public String getParentPersistentID ()
	{
		return plParentPersistentID;
	}
	
	/**
	 * Sets the playlist parent persistent ID.
	 * 
	 * @param parentPersistentID playlist parent persistent ID
	 */
	public void setParentPersistentID (String parentPersistentID)
	{
		this.plParentPersistentID = parentPersistentID;
	}
	
	/**
	 * Gets the playlist folder indicator.
	 * 
	 * @return playlist folder indicator
	 */
	public boolean getIsFolder ()
	{
		return plIsFolder;
	}
	
	/**
	 * Sets the playlist folder indicator.
	 * 
	 * @param isFolder playlist folder indicator
	 */
	public void setIsFolder (boolean isFolder)
	{
		this.plIsFolder = isFolder;
	}
	
	/**
	 * Gets the playlist tracks.
	 * 
	 * @return playlist tracks
	 */
	public List<Integer> getTracks ()
	{
		return plTracks;
	}
	
	/**
	 * Sets the playlist tracks.
	 * 
	 * @param tracks playlist tracks
	 */
	public void setTracks (List<Integer> tracks)
	{
		this.plTracks = tracks;
	}
	
	/**
	 * Gets the playlist ignored indicator.
	 * 
	 * @return playlist ignored indicator
	 */
	public boolean getIgnored ()
	{
		return plIgnored;
	}
	
	/**
	 * Sets the playlist ignored indicator.
	 * 
	 * @param ignored playlist ignored indicator
	 */
	public void setIgnored (boolean ignored)
	{
		this.plIgnored = ignored;
	}

	/**
	 * Gets the playlist bypassed indicator.
	 * 
	 * @return playlist bypassed indicator
	 */
	public boolean getBypassed()
	{
		return plBypassed;
	}
	
	/**
	 * Sets the playlist bypassed indicator.
	 * 
	 * @param bypassed playlist bypassed indicator
	 */
	public void setBypassed(boolean bypassed)
	{
		this.plBypassed = bypassed;
	}

    //---------------- Public methods --------------------------------------

	/**
	 * Compares a given persistent ID to this persistent ID.
	 * 
	 * @param key2 persistent ID to compare to this persistent ID
	 * @return negative value, zero, or positive value to indicate less than, 
	 * equal to, or greater than, respectively
	 */
	public int compareTo (String key2)
	{
		return this.plPersistentID.compareTo(key2);
	}
}
