package itunesq;

/**
 * Class that represents a single bypassed playlist preference. A bypassed
 * playlist is specified using the preferences dialog. Such a playlist is not
 * counted against the number of playlists associated with a given track, and
 * thus does not participate in the 'Playlist Count' subject of a
 * <code>Filter</code> object.
 * <p>
 * A bypassed playlist preference can also specify that all children playlists
 * are bypassed as well.
 * 
 * @author Jon
 *
 */
public class BypassPreference
{

    // ---------------- Class variables -------------------------------------

    private String playlistName;
    private boolean includeChildren;

    /**
     * Class constructor.
     */
    public BypassPreference()
    {
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the bypassed playlist name.
     * 
     * @return playlist name
     */
    public String getPlaylistName()
    {
        return playlistName;
    }

    /**
     * Sets the bypassed playlist name.
     * 
     * @param playlistName playlist name
     */
    public void setPlaylistName(String playlistName)
    {
        this.playlistName = playlistName;
    }

    /**
     * Gets the include children indicator.
     * 
     * @return include children indicator
     */
    public boolean getIncludeChildren()
    {
        return includeChildren;
    }

    /**
     * Sets the include children indicator.
     * 
     * @param includeChildren include children indicator
     */
    public void setIncludeChildren(boolean includeChildren)
    {
        this.includeChildren = includeChildren;
    }
}
