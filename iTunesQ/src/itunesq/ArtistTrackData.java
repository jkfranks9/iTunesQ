package itunesq;

/**
 * Class that represents accumulated track data for an artist.
 * 
 * @author Jon
 *
 */
public class ArtistTrackData
{

    // ---------------- Class variables -------------------------------------

    private Integer numLocalTracks;
    private Integer numRemoteTracks;
    private Integer totalLocalTime;
    private Integer totalRemoteTime;
    private RemoteArtistControl remoteArtistControl;

    /**
     * Control that allows us to keep track of the number of artists that
     * contain only remote tracks. Such artists should not be counted if remote
     * tracks are not being shown.
     */
    public enum RemoteArtistControl
    {

        /**
         * artist does not contain remote tracks
         */
        NO_REMOTE,

        /**
         * artist contains only remote tracks
         */
        REMOTE,

        /**
         * artist contains local and remote tracks
         */
        REMOTE_AND_LOCAL;
    }
    
    /**
     * Class constructor.
     */
    public ArtistTrackData ()
    {
        numLocalTracks = 0;
        numRemoteTracks = 0;
        totalLocalTime = 0;
        totalRemoteTime = 0;
        remoteArtistControl = RemoteArtistControl.NO_REMOTE;
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the number of local tracks for this artist.
     * 
     * @return number of local tracks
     */
    public Integer getNumLocalTracks()
    {
        return numLocalTracks;
    }

    /**
     * Sets the number of local tracks for this artist.
     * 
     * @param numLocalTracks number of local tracks
     */
    public void setNumLocalTracks(int numLocalTracks)
    {
        this.numLocalTracks = numLocalTracks;
    }

    /**
     * Gets the number of remote tracks for this artist.
     * 
     * @return number of remote tracks
     */
    public Integer getNumRemoteTracks()
    {
        return numRemoteTracks;
    }

    /**
     * Sets the number of remote tracks for this artist.
     * 
     * @param numRemoteTracks number of remote tracks
     */
    public void setNumRemoteTracks(int numRemoteTracks)
    {
        this.numRemoteTracks = numRemoteTracks;
    }

    /**
     * Gets the total time of local tracks for this artist.
     * 
     * @return total time of local tracks
     */
    public Integer getTotalLocalTime()
    {
        return totalLocalTime;
    }

    /**
     * Sets the total time of local tracks for this artist.
     * 
     * @param totalLocalTime total time of local tracks
     */
    public void setTotalLocalTime(int totalLocalTime)
    {
        this.totalLocalTime = totalLocalTime;
    }

    /**
     * Gets the total time of remote tracks for this artist.
     * 
     * @return total time of remote tracks
     */
    public Integer getTotalRemoteTime()
    {
        return totalRemoteTime;
    }

    /**
     * Sets the total time of remote tracks for this artist.
     * 
     * @param totalRemoteTime total time of remote tracks
     */
    public void setTotalRemoteTime(int totalRemoteTime)
    {
        this.totalRemoteTime = totalRemoteTime;
    }

    /**
     * Gets the remote artist control. This is used to remember that we've
     * accounted for this artist containing both local and remote tracks, and
     * have adjusted the count of remote-only artists accordingly.
     * 
     * @return remote artist control
     */
    public RemoteArtistControl getRemoteArtistControl()
    {
        return remoteArtistControl;
    }

    /**
     * Sets the remote artist control. This is used to remember that we've
     * accounted for this artist containing both local and remote tracks, and
     * have adjusted the count of remote-only artists accordingly.
     * 
     * @param remoteArtistControl remote artist control
     */
    public void setRemoteArtistControl(RemoteArtistControl remoteArtistControl)
    {
        this.remoteArtistControl = remoteArtistControl;
    }

    // ---------------- Public methods --------------------------------------
    
    /**
     * Increments the number of local tracks for this artist.
     * 
     * @param increment amount to be added to the number of local
     * tracks for this artist
     */
    public void incrementNumLocalTracks (int increment)
    {
        numLocalTracks += increment;
    }
    
    /**
     * Decrements the number of local tracks for this artist.
     * 
     * @param decrement amount to be subtracted from the number of local
     * tracks for this artist
     */
    public void decrementNumLocalTracks (int decrement)
    {
        numLocalTracks -= decrement;
    }
    
    /**
     * Increments the number of remote tracks for this artist.
     * 
     * @param increment amount to be added to the number of remote
     * tracks for this artist
     */
    public void incrementNumRemoteTracks (int increment)
    {
        numRemoteTracks += increment;
    }
    
    /**
     * Decrements the number of remote tracks for this artist.
     * 
     * @param decrement amount to be subtracted from the number of remote
     * tracks for this artist
     */
    public void decrementNumRemoteTracks (int decrement)
    {
        numRemoteTracks -= decrement;
    }
    
    /**
     * Increments the total time of local tracks for this artist.
     * 
     * @param increment time to be added to the total time of local 
     * tracks
     */
    public void incrementTotalLocalTime (int increment)
    {
        totalLocalTime += increment;
    }
    
    /**
     * Decrements the total time of local tracks for this artist.
     * 
     * @param decrement time to be subtracted from the total time of local 
     * tracks
     */
    public void decrementTotalLocalTime (int decrement)
    {
        totalLocalTime -= decrement;
    }

    /**
     * Increments the total time of remote tracks for this artist.
     * 
     * @param increment time to be added to the total time of remote tracks
     */
    public void incrementTotalRemoteTime (int increment)
    {
        totalRemoteTime += increment;
    }
    
    /**
     * Decrements the total time of remote tracks for this artist.
     * 
     * @param decrement time to be subtracted from the remote time of local 
     * tracks
     */
    public void decrementTotalRemoteTime (int decrement)
    {
        totalRemoteTime -= decrement;
    }
}
