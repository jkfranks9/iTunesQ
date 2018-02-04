package itunesq;

/**
 * Class that represents an internal error in the application. This is a 
 * runtime exception.
 * 
 * @author Jon
 *
 */
public class InternalErrorException extends RuntimeException
{

    //---------------- Class variables -------------------------------------
	
	private boolean fatal;

    //---------------- Private variables -----------------------------------

	private static final long serialVersionUID = -1902550162137484780L;
	
	/**
	 * Constructor that specifies an indicator of whether or not the error is 
	 * fatal, as well as the error message.
	 * 
	 * @param fatal true if the error should end the application
	 * @param message error message
	 */
	public InternalErrorException (boolean fatal, String message)
	{
		super(message);
		this.fatal = fatal;
	}

    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the fatal error indicator.
	 * 
	 * @return true if the application should end
	 */
	public boolean getFatal ()
	{
		return fatal;
	}
}
