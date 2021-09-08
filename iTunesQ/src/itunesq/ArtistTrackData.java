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

    private Integer numTracks;
    private Integer totalTime;
    
    /**
     * Class constructor.
     */
    public ArtistTrackData ()
    {
        numTracks = 0;
        totalTime = 0;
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the number of tracks for this artist.
     * 
     * @return number of tracks
     */
    public Integer getNumTracks()
    {
        return numTracks;
    }

    /**
     * Sets the number of tracks for this artist.
     * 
     * @param numTracks number of tracks
     */
    public void setNumTracks(int numTracks)
    {
        this.numTracks = numTracks;
    }

    /**
     * Gets the total time of tracks for this artist.
     * 
     * @return total time of tracks
     */
    public Integer getTotalTime()
    {
        return totalTime;
    }

    /**
     * Sets the total time of tracks for this artist.
     * 
     * @param totalTime total time of tracks
     */
    public void setTotalTime(int totalTime)
    {
        this.totalTime = totalTime;
    }

    // ---------------- Public methods --------------------------------------
    
    /**
     * Increments the number of tracks for this artist.
     * 
     * @param increment amount to be added to the number of 
     * tracks for this artist
     */
    public void incrementNumTracks (int increment)
    {
        numTracks += increment;
    }
    
    /**
     * Decrements the number of tracks for this artist.
     * 
     * @param decrement amount to be subtracted from the number of 
     * tracks for this artist
     */
    public void decrementNumTracks (int decrement)
    {
        numTracks -= decrement;
    }
    
    /**
     * Increments the total time of tracks for this artist.
     * 
     * @param increment time to be added to the total time of  
     * tracks
     */
    public void incrementTotalTime (int increment)
    {
        totalTime += increment;
    }
    
    /**
     * Decrements the total time of tracks for this artist.
     * 
     * @param decrement time to be subtracted from the total time of  
     * tracks
     */
    public void decrementTotalTime (int decrement)
    {
        totalTime -= decrement;
    }
}
