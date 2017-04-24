package itunesq;

import org.jdom2.JDOMException;

/**
 * Class to process the iTunes XML file on a thread. This allows us to display the main window faster.
 * 
 * @author Jon
 *
 */
public class ProcessXMLThread implements Runnable
{
	
    //---------------- Private variables -----------------------------------
	
	private String xmlFileName;
	private static Exception savedException;

	/**
	 * Constructor.
	 * 
	 * @param fileName Name of the XML file to be processed.
	 */
	public ProcessXMLThread (String fileName)
	{
		xmlFileName = fileName;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Return a saved exception from the run() method, if any.
	 * 
	 * @return Saved exception, or null.
	 */
	public static Exception getSavedException ()
	{
		return savedException;
	}
	
	@Override
	/**
	 * Execute the run() method on a thread. We simply call processXML() with the file name provided
	 * to the constructor, and save any exception that occurs.
	 */
	public void run()
	{
		try
		{
			savedException = null;
			XMLHandler.processXML(xmlFileName);
		} 
		catch (JDOMException e)
		{
			savedException = e;
		}
	}
}
