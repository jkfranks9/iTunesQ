package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;

/**
 * Class that represents a user override of an artist alternate name.
 * This can be an alternate name that was added by the user, or a removal of
 * an automatically generated alternate name.
 * 
 * @author Jon
 *
 */
public class ArtistAlternateNameOverride
{
    
    // ---------------- Class variables -------------------------------------
    
    private String primaryArtist;
    private List<String> alternateArtists;
    private OverrideType overrideType;
    
    /**
     * The type of artist override.
     */
    public enum OverrideType
    {
        
        /**
         * alternate artist name was added by the user
         */
        MANUAL,
        
        /**
         * automatically generated alternate artist name was removed
         */
        AUTOMATIC;
    }
    
    /**
     * Class constructor.
     */
    public ArtistAlternateNameOverride ()
    {
        alternateArtists = new ArrayList<String>();
    }
    
    /**
     * Class constructor.
     * 
     * @param primaryArtist primary artist name
     * @param overrideType override type
     */
    public ArtistAlternateNameOverride (String primaryArtist, OverrideType overrideType)
    {
        this();
        this.primaryArtist = primaryArtist;
        this.overrideType = overrideType;
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the primary artist.
     * 
     * @return primary artist
     */
    public String getPrimaryArtist()
    {
        return primaryArtist;
    }

    /**
     * Sets the primary artist.
     * 
     * @param primaryArtist primary artist
     */
    public void setPrimaryArtist(String primaryArtist)
    {
        this.primaryArtist = primaryArtist;
    }

    /**
     * Gets the list of alternate artists.
     * 
     * @return list of alternate artists
     */
    public List<String> getAlternateArtists()
    {
        return alternateArtists;
    }

    /**
     * Adds an artist to the list of alternate artists.
     * 
     * @param alternateArtist alternate artist to add
     */
    public void addAlternateArtist(String alternateArtist)
    {
        alternateArtists.add(alternateArtist);
    }

    /**
     * Removes an artist from the list of alternate artists.
     * 
     * @param alternateArtist alternate artist to remove
     */
    public void removeAlternateArtist(String alternateArtist)
    {
        alternateArtists.remove(alternateArtist);
    }
    
    /**
     * Gets the number of alternate artists.
     * 
     * @return number of alternate artists
     */
    public int getNumAlternateArtists ()
    {
        return alternateArtists.getLength();
    }

    /**
     * Gets the override type.
     * 
     * @return override type
     */
    public OverrideType getOverrideType()
    {
        return overrideType;
    }

    /**
     * Sets the override type.
     * 
     * @param overrideType override type
     */
    public void setOverrideType(OverrideType overrideType)
    {
        this.overrideType = overrideType;
    }
    
    //---------------- Public methods --------------------------------------

    /**
     * Compares a given override to this one, used for sorting. This method
     * sorts by the primary artist name.
     * 
     * @param o2 override to be compared to this override
     * @return negative value, zero, or positive value to indicate less than,
     * equal to, or greater than, respectively
     */    
    public int compareTo(ArtistAlternateNameOverride o2)
    {
        return this.primaryArtist.toLowerCase().compareTo(o2.primaryArtist.toLowerCase());
    }
}
