package itunesq;

/**
 * Filter exception class. Thrown for unsupported filter constructs, for 
 * example indecipherable boolean logic in the collection of filters.
 *  
 * @author Jon
 *
 */
public class FilterException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	private String message = null;

	/**
	 * Class constructor.
	 */
	public FilterException()
	{
		super();
	}

	/**
	 * Class constructor specifying a descriptive message.
	 * 
	 * @param message message associated with the exception
	 */
	public FilterException(String message)
	{
		super(message);
		this.message = message;
	}

	/**
	 * Class constructor specifying a throwable cause.
	 * 
	 * @param cause cause associated with the exception
	 */
	public FilterException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Returns the exception message. This is a synonym for the 
	 * <code>getMessage</code> method.
	 */
	@Override
	public String toString()
	{
		return message;
	}

	/**
	 * Returns the exception message. This is a synonym for the 
	 * <code>toString</code> method.
	 */
	@Override
	public String getMessage()
	{
		return message;
	}
}
