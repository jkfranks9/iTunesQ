package itunesq;

/**
 * Class that represents an error while processing the iTunes XML file.
 * This is a runtime exception.
 * 
 * @author Jon
 *
 */
public class XMLProcessingException extends RuntimeException
{

    //---------------- Class variables -------------------------------------
	
	private int line;
	private int column;

    //---------------- Private variables -----------------------------------
	
	private static final long serialVersionUID = -2609150953149143887L;
	
	/**
	 * Constructor that specifies the line and column of the XML file, as well
	 * as the error message.
	 * 
	 * @param line line in the XML file
	 * @param column column in the line
	 * @param message error message
	 */
	public XMLProcessingException (int line, int column, String message)
	{
		super(message);
		this.line = line;
		this.column = column;
	}
	
	/**
	 * Gets the line in the XML file.
	 * 
	 * @return XML file line
	 */
	public int getLine ()
	{
		return line;
	}
	
	/**
	 * Gets the column in the line in the XML file.
	 * 
	 * @return XML file line column
	 */
	public int getColumn ()
	{
		return column;
	}
}
