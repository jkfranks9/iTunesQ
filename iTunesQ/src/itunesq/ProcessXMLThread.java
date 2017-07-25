package itunesq;

import org.jdom2.JDOMException;

/**
 * Class that handles processing the iTunes XML file on a thread. 
 * Theoretically, this allows us to display the main window faster.
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
	 * Class constructor specifying the XML file name.
	 * 
	 * @param fileName name of the XML file to be processed
	 */
	public ProcessXMLThread (String fileName)
	{
		xmlFileName = fileName;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Returns a saved exception from the <code>run</code> method, if any.
	 * 
	 * @return saved exception, or <code>null</code>
	 */
	public static Exception getSavedException ()
	{
		return savedException;
	}
	
	/**
	 * Executes the <code>run</code> method on a thread. We simply call 
	 * <code>processXML</code> with the file name provided to the constructor,
	 * and save any exception that occurs.
	 */
	@Override
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
