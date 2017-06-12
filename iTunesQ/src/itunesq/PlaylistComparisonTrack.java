package itunesq;

/**
 * Class representing a track resulting from a comparison of two or more playlists. Such a comparison
 * has three possible results:
 * 
 * 1) A track exists in all compared playlists.
 * 2) A track exists in some, but not all, compared playlists.
 * 3) A track exists in only one playlist.
 * 
 * The algorithm that determines these results first sorts tracks from all compared playlists into
 * the second and third cases above. It then looks in the 'some' list for any tracks that really belong
 * in the 'all' list. To do this, it needs to know the number of playlists in which the track was found.
 * That is the reason for this class: to keep a playlist count for a given track ID.
 * 
 * @author Jon
 *
 */
public class PlaylistComparisonTrack
{

    //---------------- Class variables -------------------------------------
	
	private Integer trackID;
	private Integer playlistCount;
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Get the track ID.
	 * 
	 * @return Track ID.
	 */
	public Integer getTrackID()
	{
		return trackID;
	}
	
	/**
	 * Set the track ID.
	 * 
	 * @param trackID Track ID.
	 */
	public void setTrackID(Integer trackID)
	{
		this.trackID = trackID;
	}
	
	/**
	 * Get the playlist count.
	 * 
	 * @return Playlist count.
	 */
	public Integer getPlaylistCount()
	{
		return playlistCount;
	}
	
	/**
	 * Set the playlist count.
	 * 
	 * @param playlistCount Playlist count.
	 */
	public void setPlaylistCount(Integer playlistCount)
	{
		this.playlistCount = playlistCount;
	}
}
