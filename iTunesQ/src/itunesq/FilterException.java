package itunesq;

/**
 * Filter exception class. Thrown for unsupported filter constructs, for example indecipherable
 * boolean logic in the collection of filters.
 *  
 * @author Jon
 *
 */
public class FilterException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	private String message = null;

	public FilterException()
	{
		super();
	}

	public FilterException(String message)
	{
		super(message);
		this.message = message;
	}

	public FilterException(Throwable cause)
	{
		super(cause);
	}

	@Override
	public String toString()
	{
		return message;
	}

	@Override
	public String getMessage()
	{
		return message;
	}
}
