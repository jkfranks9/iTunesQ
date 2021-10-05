package itunesq;

/**
 * Class that represents library codec statistics.
 * 
 * @author Jon
 *
 */
public class CodecStats
{

    // ---------------- Class variables -------------------------------------
	
	private Integer count;
	private Track.TrackType type;

    /**
     * Class constructor.
     */
    public CodecStats()
    {
    }

    // ---------------- Getters and setters ---------------------------------
    
    /**
     * Gets the codec count.
     * 
     * @return codec count
     */
    public Integer getCount()
    {
    	return count;
    }
    
    /**
     * Sets the codec count.
     * 
     * @param count codec count
     */
    public void setCount(Integer count)
    {
    	this.count = count;
    }
    
    /**
     * Gets the codec type.
     * 
     * @return codec type
     */
    public Track.TrackType getType()
    {
    	return type;
    }
    
    /**
     * Sets the codec type.
     * 
     * @param type codec type
     */
    public void setType(Track.TrackType type)
    {
    	this.type = type;
    }

    // ---------------- Public methods --------------------------------------
    
    /**
     * Increments the codec count.
     */
    public void incrementCount()
    {
    	count++;
    }
}