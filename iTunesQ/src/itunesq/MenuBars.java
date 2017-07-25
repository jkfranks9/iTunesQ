package itunesq;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.jdom2.JDOMException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that represents the menu bar for non-dialog windows.
 * <p>
 * The required functionality is not built in to Pivot, because of the need to
 * create named actions that are driven from the menu items.
 * <p>Full disclosure: the template for this class was gleefully stolen from 
 * the Pivot tutorials :)
 * 
 * @author Jon
 *
 */
public class MenuBars extends Frame implements Bindable
{

    //---------------- Public variables ------------------------------------
	
	/**
	 * When we display a list of tracks that result from some type of query, 
	 * the File ... Save menu is enabled. This is handled by the 
	 * <code>FileSaveDialog</code> class. That class in turn needs to gather 
	 * the list of resulting tracks, and so needs the associated window 
	 * handler, which should always be of type <code>TracksWindow</code>. It 
	 * also needs other attributes, for example the string representation of 
	 * the query.
	 * <p>
	 * This <code>enum</code> defines values used to set and get these 
	 * attributes on the window represented by this class. 
	 * <p>
	 * The attributes are set by the <code>TracksWindow</code> class, 
	 * and then obtained by the <code>FileSaveDialog class</code>.
	 */
	public static enum WindowAttributes
	{
		/**
		 * window handler object attribute
		 */
		HANDLER,
		
		/**
		 * query type attribute
		 */
		QUERY_TYPE,
		
		/**
		 * query string representation attribute
		 */
		QUERY_STRING
	}

    //---------------- Private variables -----------------------------------

	private Logger logger = null;
	
	/*
	 * BXML variable for the file browser sheet, which lets the user select a file.
	 */
	@BXML private FileBrowserSheet fileBrowserSheet;
	
	/*
	 * BXML variable for the preferences sheet.
	 */
	@BXML private Sheet preferencesSheet;

	/**
	 * Class constructor. This creates named actions for the following menu
	 * items:
	 * <ul>
	 * <li>File {@literal ->} Open</li>
	 * <li>File {@literal ->} Save</li>
	 * <li>File {@literal ->} Exit</li>
	 * <li>Edit {@literal ->} Preferences</li>
	 * </ul>
	 */
	public MenuBars()
	{
    	
    	/*
    	 * Create a UI logger.
    	 */
    	String className = getClass().getSimpleName();
    	logger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Get the logging object singleton.
    	 */
    	Logging logging = Logging.getInstance();
    	
    	/*
    	 * Register our logger.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, logger);
		
    	logger.trace("MenuBars constructor: " + this.hashCode());

		/*
		 * Create the file open action. This opens a file browser sheet so a file can be selected.
		 * When that sheet is closed, the sheetClosed() method is called.
		 */
		Action.getNamedActions().put("fileOpen", new Action()
		{
			@Override
			public void perform(Component source)
			{
				logger.info("executing file open action");
				
				fileBrowserSheet.open(MenuBars.this, new SheetCloseListener()
				{
					@Override
					public void sheetClosed(Sheet sheet)
					{
						logger.info("file browser sheet closed");

						/*
						 * Make sure a file was selected.
						 */
						if (sheet.getResult())
						{

							/*
							 * Get the selected file name.
							 */
							File selectedFile = fileBrowserSheet.getSelectedFile();
							
							if (selectedFile != null)
							{
								String xmlFileName = selectedFile.getPath();

								/*
								 * Save the selected XML file, then write the preferences.
								 */
								Preferences userPrefs = Preferences.getInstance();
								userPrefs.setXMLFileName(xmlFileName);
								userPrefs.writePreferences();

								logger.info("updating for new XML file '" + xmlFileName + "'");

								/*
								 * Update based on the new XML file.
								 */
								try
								{
									Utilities.updateFromXMLFile(xmlFileName);
								}							
								catch (JDOMException e)
								{
									logger.error("caught JDOMException");
									Alert.alert(MessageType.ERROR, 
											"Unable to read and process XML file: " + 
													xmlFileName, MenuBars.this);
									e.printStackTrace();
								}

								/*
								 * Repaint the main window.
								 */
								MenuBars.this.repaint();
							}
							else
							{
								Alert.alert(MessageType.INFO, "You didn't select anything.", MenuBars.this);
							}
						}
					}
				});
			}
		});

		/*
		 * Create the file save action.
		 */
		Action.getNamedActions().put("fileSave", new Action()
		{
			@Override
			public void perform(Component source)
			{
				logger.info("executing file save action");
				
            	Display display = source.getDisplay();
        		FileSaveDialog fileSaveDialogHandler = new FileSaveDialog(MenuBars.this);
        		
        		try
				{
        			fileSaveDialogHandler.displayFileSaveDialog(display);
				} 
        		catch (IOException | SerializationException e)
				{
            		logger.error("caught " + e.getClass().getSimpleName());
					e.printStackTrace();
				}
			}
		});

		/*
		 * Create the exit action.
		 */
		Action.getNamedActions().put("exit", new Action()
		{
			@Override
			public void perform(Component source)
			{
				logger.info("executing file exit action");
				DesktopApplicationContext.exit(false);
			}
		});

		/*
		 * Create the preferences action.
		 */
		Action.getNamedActions().put("preferences", new Action()
		{
			@Override
			public void perform(Component source)
			{
				logger.info("executing edit preferences action");
				
				Display display = source.getDisplay();
        		PreferencesWindow prefsWindowHandler = 
        				new PreferencesWindow(preferencesSheet, MenuBars.this);
        		
        		try
				{
					prefsWindowHandler.displayPreferences(display);
				} 
        		catch (IOException | SerializationException e)
				{
            		logger.error("caught " + e.getClass().getSimpleName());
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initializes the class after it has been completely processed and bound
	 * by the serializer. This method is required to be overriden for the
	 * <code>Bindable</code> interface, but does nothing.
	 * 
	 * @param namespace serializer's namespace
	 * @param location location of the BXML source
	 * @param resources resources used to localize the deserialized content,
	 * or null
	 */
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
	}
}