package itunesq;

/**
 * Class that represents an error while processing the JSON file. This is
 * a runtime exception.
 * 
 * @author Jon
 *
 */
public class JSONProcessingException extends RuntimeException
{

    // ---------------- Private variables -----------------------------------
	
	private static final long serialVersionUID = 5077806745427586837L;

	/**
     * Class constructor.
     * 
     * @param message error message
     */
    public JSONProcessingException(String message)
    {
        super(message);
    }
}
