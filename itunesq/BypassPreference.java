package itunesq;

import java.io.Serializable;

/**
 * Class representing a single bypass playlist preference.
 * 
 * @author Jon
 *
 */
public class BypassPreference implements Serializable
{

    //---------------- Class variables -------------------------------------
	
	private String playlistName;
	private boolean includeChildren;
	
    //---------------- Private variables -----------------------------------
	
	private static final long serialVersionUID = 153557071290842369L;
	
    //---------------- Getters and setters ---------------------------------

	/**
	 * Get the playlist name.
	 * 
	 * @return Playlist name.
	 */
	public String getPlaylistName()
	{
		return playlistName;
	}

	/**
	 * Set the playlist name.
	 * 
	 * @param playlistName Playlist name.
	 */
	public void setPlaylistName(String playlistName)
	{
		this.playlistName = playlistName;
	}

	/**
	 * Get the include children indicator.
	 * 
	 * @return Include children indicator.
	 */
	public boolean getIncludeChildren()
	{
		return includeChildren;
	}

	/**
	 * Set the include children indicator.
	 * 
	 * @param includeChildren Include children indicator.
	 */
	public void setIncludeChildren(boolean includeChildren)
	{
		this.includeChildren = includeChildren;
	}
}
