package itunesq;

/**
 * Class that represents a pair of playlists with a parent-child
 * relationship.
 * 
 * This is used to copy tracks from a child playlist to all parent
 * folder playlists, when those parents don't already contain such
 * tracks.
 * 
 * @author Jon
 *
 */
public class PlaylistPair
{

    // ---------------- Class variables -------------------------------------
	
	private Playlist child;
	private Playlist parent;

    /**
     * Class constructor.
     * 
     * @param child child playlist
     * @param parent parent playlist
     */	
	public PlaylistPair(Playlist child, Playlist parent)
	{
		this.child = child;
		this.parent = parent;
	}

    // ---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the child playlist.
	 * 
	 * @return child playlist
	 */
	public Playlist getChild()
	{
		return child;
	}
	
	/**
	 * Gets the parent playlist.
	 * 
	 * @return parent playlist
	 */
	public Playlist getParent()
	{
		return parent;
	}
}
