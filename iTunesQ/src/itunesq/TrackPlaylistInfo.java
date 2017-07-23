package itunesq;

/**
 * Class representing playlist info for a single track.
 * 
 * @author Jon
 *
 */
public class TrackPlaylistInfo
{

    //---------------- Class variables -------------------------------------
	
	private String playlistName;
	private boolean bypassed;
	
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
	 * Get the bypassed indicator.
	 * 
	 * @return Bypassed indicator.
	 */
	public boolean getBypassed()
	{
		return bypassed;
	}

	/**
	 * Set the bypassed indicator.
	 * 
	 * @param bypassed Bypassed indicator.
	 */
	public void setBypassed(boolean bypassed)
	{
		this.bypassed = bypassed;
	}
}
