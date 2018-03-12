package itunesq;

/**
 * Class that represents a single playlist info for a single track. This
 * consists of the playlist name and an indicator of whether or not the playlist
 * is bypassed.
 * 
 * @author Jon
 *
 */
public class TrackPlaylistInfo
{

    // ---------------- Class variables -------------------------------------

    private String playlistName;
    private boolean bypassed;

    /**
     * Class constructor.
     */
    public TrackPlaylistInfo()
    {
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the playlist name.
     * 
     * @return playlist name
     */
    public String getPlaylistName()
    {
        return playlistName;
    }

    /**
     * Sets the playlist name.
     * 
     * @param playlistName playlist name
     */
    public void setPlaylistName(String playlistName)
    {
        this.playlistName = playlistName;
    }

    /**
     * Gets the bypassed indicator.
     * 
     * @return bypassed indicator
     */
    public boolean getBypassed()
    {
        return bypassed;
    }

    /**
     * Sets the bypassed indicator.
     * 
     * @param bypassed bypassed indicator
     */
    public void setBypassed(boolean bypassed)
    {
        this.bypassed = bypassed;
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Compares a given playlist info to this one, used for sorting.
     * 
     * @param c2 playlist info to be compared to this playlist info
     * @return negative value, zero, or positive value to indicate less than,
     * equal to, or greater than, respectively
     */
    public int compareTo(TrackPlaylistInfo c2)
    {
        return this.playlistName.toLowerCase().compareTo(c2.playlistName.toLowerCase());
    }
}
