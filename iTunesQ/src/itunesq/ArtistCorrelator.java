package itunesq;

/**
 * Class that represents a correlator between the ArtistNames object (which
 * contains various forms of an artist name), and the corresponding Artist
 * object (which contains all other artist data). This class contains the key
 * for the map that houses all the Artist objects.
 * 
 * @author Jon
 *
 */
public class ArtistCorrelator
{

    // ---------------- Class variables -------------------------------------

    private String displayName = null;
    private String normalizedName = null;
    private int artistKey;

    /**
     * Class constructor.
     */
    public ArtistCorrelator()
    {
    }

    /**
     * Class constructor.
     * 
     * @param artistName artist display name
     */
    public ArtistCorrelator(String artistName)
    {
        this();
        this.displayName = artistName;
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the display name, which is the artist name from the XML 
     * file.
     * 
     * @return artist display name
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Sets the display name, which is the artist name from the XML 
     * file.
     * 
     * @param artistName artist display name
     */
    public void setDisplayName(String artistName)
    {
        this.displayName = artistName;
    }

    /**
     * Gets the normalized name, which is used internally.
     * 
     * @return normalized artist name
     */
    public String getNormalizedName()
    {
        return normalizedName;
    }

    /**
     * Sets the normalized name, which is used internally.
     * 
     * @param normalizedName normalized artist name
     */
    public void setNormalizedName(String normalizedName)
    {
        this.normalizedName = normalizedName;
    }

    /**
     * Gets the artist key, which is used to access Artist objects.
     * 
     * @return artist key
     */
    public int getArtistKey()
    {
        return artistKey;
    }

    /**
     * Sets the artist key, which is used to access Artist objects.
     * 
     * @param artistKey artist key
     */
    public void setArtistKey(int artistKey)
    {
        this.artistKey = artistKey;
    }
    
    //---------------- Public methods --------------------------------------

    /**
     * Compares a given correlator to this one, used for sorting. This method
     * sorts by the normalized artist name.
     * 
     * @param c2 correlator to be compared to this correlator
     * @return negative value, zero, or positive value to indicate less than,
     * equal to, or greater than, respectively
     */
    public int compareToNormalized(ArtistCorrelator c2)
    {
        return this.normalizedName.toLowerCase().compareTo(c2.normalizedName.toLowerCase());
    }

    /**
     * Compares a given correlator to this one, used for sorting. This method
     * sorts by the artist display name.
     * 
     * @param c2 correlator to be compared to this correlator
     * @return negative value, zero, or positive value to indicate less than,
     * equal to, or greater than, respectively
     */
    public int compareToDisplay(ArtistCorrelator c2)
    {
    	
    	/*
    	 * Ignore leading "The" (case insensitive).
    	 */
    	String thisName = this.displayName.replaceAll("^(?i)The ", "");
    	String thatName = c2.displayName.replaceAll("^(?i)The ", "");
    	
        return thisName.toLowerCase().compareTo(thatName.toLowerCase());
    }
}
