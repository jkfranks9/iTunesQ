package itunesq;

/**
 * Class that represents a track resulting from a comparison of two or more
 * playlists. Such a comparison has three possible results:
 * <ol>
 * <li>A track exists in all compared playlists</li>
 * <li>A track exists in some, but not all, compared playlists</li>
 * <li>A track exists in only one playlist</li>
 * </ol>
 * <p>
 * The algorithm that determines these results first sorts tracks from all
 * compared playlists into the second and third cases above. It then looks in
 * the 'some' list for any tracks that really belong in the 'all' list. To do
 * this, it needs to know the number of playlists in which the track was found.
 * That is the reason for this class: to keep a playlist count for a given track
 * ID.
 * 
 * @author Jon
 *
 */
public class PlaylistComparisonTrack
{

    // ---------------- Class variables -------------------------------------

    private Integer trackID;
    private Integer playlistCount;

    /**
     * Class constructor.
     */
    public PlaylistComparisonTrack()
    {
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the track ID.
     * 
     * @return track ID
     */
    public Integer getTrackID()
    {
        return trackID;
    }

    /**
     * Sets the track ID.
     * 
     * @param trackID track ID
     */
    public void setTrackID(Integer trackID)
    {
        this.trackID = trackID;
    }

    /**
     * Gets the playlist count.
     * 
     * @return playlist count
     */
    public Integer getPlaylistCount()
    {
        return playlistCount;
    }

    /**
     * Sets the playlist count.
     * 
     * @param playlistCount playlist count
     */
    public void setPlaylistCount(Integer playlistCount)
    {
        this.playlistCount = playlistCount;
    }
}
