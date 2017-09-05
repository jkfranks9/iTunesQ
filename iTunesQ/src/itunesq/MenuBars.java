package itunesq;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.List;
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
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.content.MenuItemData;
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
	 * Attributes of a display window.
	 * <p>
	 * When we display a list of tracks that result from some type of query, 
	 * the File {@literal ->} Save menu is enabled. This is handled by the 
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
	 * and then obtained by the <code>FileSaveDialog</code> class.
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
		QUERY_STRING,
		
		/**
		 * column names attribute
		 */
		COLUMN_NAMES
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
	
	/*
	 * Other BXML variables.
	 */
	@BXML private MenuBar menuBarHolder = null;
	@BXML private MenuBar.Item fileMenu = null;
	@BXML private Menu fileMenuItems = null;
	@BXML private Menu.Item fileMenuOpen = null;
	@BXML private Menu.Item fileMenuSave = null;
	@BXML private Menu.Item fileMenuExit= null;
	@BXML private MenuBar.Item editMenu = null;
	@BXML private Menu editMenuItems = null;
	@BXML private Menu.Item editMenuPreferences = null;

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
								try
								{
									userPrefs.writePreferences();
								}
								catch (IOException e)
								{
									logger.error("caught " + e.getClass().getSimpleName());
									e.printStackTrace();
								}

								logger.info("updating for new XML file '" + xmlFileName + "'");

								/*
								 * Update based on the new XML file.
								 */
								try
								{
									Utilities.updateFromXMLFile(xmlFileName);
								}							
								catch (JDOMException | IOException e)
								{
						    		logger.error("caught " + e.getClass().getSimpleName());
									Alert.alert(MessageType.ERROR, 
											StringConstants.ALERT_XML_FILE_ERROR + xmlFileName, MenuBars.this);
									e.printStackTrace();
								}

								/*
								 * Repaint the main window.
								 */
								MenuBars.this.repaint();
							}
							else
							{
								Alert.alert(MessageType.INFO, 
										StringConstants.ALERT_NO_FILE_SELECTED, MenuBars.this);
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
	 * Initializes the BXML variables and data for the menu bar that appears on 
	 * all primary windows.
	 * 
	 * @param windowSerializer BXML serializer used to read the window BXML 
	 * file
	 * @param components list of window components that is extended with menu 
	 * bar components
	 * @param showFileSave flag that indicates whether or not to show the File 
	 * {@literal ->} Save menu item
	 */
	public void initializeMenuBxmlVariables (BXMLSerializer windowSerializer, 
			List<Component> components, boolean showFileSave)
	{
		
		/*
		 * Initialize the menu bar BXML variables.
		 * 
		 * NOTE: Not all components that we deal with require skinning, as they are subcomponents of
		 * other components. For example, MenuBar includes MenuBar.Item. So we only need to add the
		 * top level components to the input component list.
		 */
        menuBarHolder = 
        		(MenuBar)windowSerializer.getNamespace().get("menuBarHolder");
		components.add(menuBarHolder);
        fileMenu = 
        		(MenuBar.Item)windowSerializer.getNamespace().get("fileMenu");
        fileMenuItems = 
        		(Menu)windowSerializer.getNamespace().get("fileMenuItems");
		components.add(fileMenuItems);
        fileMenuOpen = 
        		(Menu.Item)windowSerializer.getNamespace().get("fileMenuOpen");
    	fileMenuSave = 
    			(Menu.Item)windowSerializer.getNamespace().get("fileMenuSave");
        fileMenuExit = 
        		(Menu.Item)windowSerializer.getNamespace().get("fileMenuExit");
        editMenu = 
        		(MenuBar.Item)windowSerializer.getNamespace().get("editMenu");
        editMenuItems = 
        		(Menu)windowSerializer.getNamespace().get("editMenuItems");
		components.add(editMenuItems);
        editMenuPreferences = 
        		(Menu.Item)windowSerializer.getNamespace().get("editMenuPreferences");
		
		/*
		 * Initialize the 'File' menu text.
		 */
    	fileMenu.setButtonData(StringConstants.FILE);
		MenuItemData fileMenuOpenData = new MenuItemData(StringConstants.OPEN);
    	fileMenuOpen.setButtonData(fileMenuOpenData);
    	MenuItemData fileMenuSaveData = new MenuItemData(StringConstants.SAVE);
    	fileMenuSave.setButtonData(fileMenuSaveData);
		MenuItemData fileMenuExitData = new MenuItemData(StringConstants.EXIT);
    	fileMenuExit.setButtonData(fileMenuExitData);

    	/*
    	 * Disable the File -> Save menu item if requested.
    	 */
    	if (showFileSave == false)
    	{
    		fileMenuSave.getAction().setEnabled(false);
    		fileMenuSave.setEnabled(false);
    	}

    	/*
    	 * Initialize the 'Edit' menu text.
    	 */
    	editMenu.setButtonData(StringConstants.EDIT);
		MenuItemData editMenuPreferencesData = new MenuItemData(StringConstants.PREFERENCES);
    	editMenuPreferences.setButtonData(editMenuPreferencesData);
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