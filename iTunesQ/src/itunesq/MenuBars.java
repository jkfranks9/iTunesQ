package itunesq;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
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
 * Class that represents the main window menu bar. The required functionality is not built in,
 * because we need a constructor that creates named actions.
 * 
 * @author Jon ... well, gleefully stolen from the Pivot tutorials :)
 *
 */
public class MenuBars extends Frame implements Bindable
{

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
	 * Constructor. This creates the named actions.
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
							 * Only one file can be selected, even though the method returns a sequence.
							 */
							Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();
							String xmlFileName = selectedFiles.get(0).getPath();
							
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
				});
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

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources)
	{
	}
}