package itunesq;

import java.awt.Font;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.ArrayStack;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Stack;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TreeView;

/**
 * Class that represents window skins. This is a singleton class.
 * <p>
 * Skins consist primarily of colors for elements of GUI widgets. Font is also
 * included, but currently is rather pointless since it's the same for every
 * skin.
 * <p>
 * A skin is a named entity that is a collection of HTML color codes that 
 * complement each other, for an amazingly pleasant use experience.
 * 
 * @author Jon
 *
 */
public class Skins
{

    //---------------- Singleton implementation ----------------------------
	
	/*
	 * Singleton class instance variable.
	 */
	private static Skins instance = null;
	
	/**
	 * Gets the singleton instance.
	 * 
	 * @return singleton class instance
	 */
	public static Skins getInstance ()
	{
		if (instance == null)
		{
			instance = new Skins();
		}
		
		return instance;
	}

    //---------------- Class variables -------------------------------------

	/*
	 * These are the names of the skins. Each skin is initialized with predefined values for
	 * the skin elements (as defined by the Element enum).
	 * 
	 * All colors are HTML color strings from the "Flat Design Color Chart" at:
	 * 
	 *     http://htmlcolorcodes.com/color-chart/
	 *     
	 *     c.2017
	 */
	
	/*
	 * Default skin.
	 */
	private static final String duskyGray = StringConstants.SKIN_NAME_DUSKY;
	private static final Map<Element, String> DUSKY_GRAY;	
	static
	{
		Map<Element, String> result = new HashMap<Element, String>();
		result.put(Element.BACKGROUND,    "#AEB6BF");
		result.put(Element.ALTBACKGROUND, "#EBEDEF");
		result.put(Element.BORDER,        "#212F3D");
		result.put(Element.BUTTON,        "#5D6D7E");
		result.put(Element.TEXT,          "#17202A");
		result.put(Element.ALTTEXT,       "#FDFEFE");
		result.put(Element.ACTIVE,        "#2980B9");
		result.put(Element.INACTIVE,      "#D4E6F1");
		result.put(Element.HEADER,        "#34495E");
		result.put(Element.FONT,          "SansSerif");
		
		DUSKY_GRAY = result;
	}
	
	/*
	 * Alternate skins selectable with a preference.
	 */
	private static final String pumpkinPatch = StringConstants.SKIN_NAME_PUMPKIN;
	private static final Map<Element, String> PUMPKIN_PATCH;	
	static
	{
		Map<Element, String> result = new HashMap<Element, String>();
		result.put(Element.BACKGROUND,    "#DC7633");
		result.put(Element.ALTBACKGROUND, "#FCF3CF");
		result.put(Element.BORDER,        "#873600");
		result.put(Element.BUTTON,        "#B7950B");
		result.put(Element.TEXT,          "#17202A");
		result.put(Element.ALTTEXT,       "#FDFEFE");
		result.put(Element.ACTIVE,        "#196F3D");
		result.put(Element.INACTIVE,      "#F9E79F");
		result.put(Element.HEADER,        "#784212");
		result.put(Element.FONT,          "SansSerif");
		
		PUMPKIN_PATCH = result;
	}
	
	private static final String seasideDaze = StringConstants.SKIN_NAME_SEASIDE;
	private static final Map<Element, String> SEASIDE_DAZE;	
	static
	{
		Map<Element, String> result = new HashMap<Element, String>();
		result.put(Element.BACKGROUND,    "#AED6F1");
		result.put(Element.ALTBACKGROUND, "#D6EAF8");
		result.put(Element.BORDER,        "#145A32");
		result.put(Element.BUTTON,        "#1A5276");
		result.put(Element.TEXT,          "#0B5345");
		result.put(Element.ALTTEXT,       "#B3B6B7");
		result.put(Element.ACTIVE,        "#117A65");
		result.put(Element.INACTIVE,      "#A2D9CE");
		result.put(Element.HEADER,        "#154360");
		result.put(Element.FONT,          "SansSerif");
		
		SEASIDE_DAZE = result;
	}
	
	/**
	 * Default skin name.
	 */
	public static final String defaultSkin = duskyGray;
	
	/*
	 * Name of the current skin.
	 */
	private String currentSkin;
	
	/**
	 * The type of skin element, for example a border around a portion of the 
	 * window.
	 */
	public enum Element
	{
		
		/*
		 * IMPORTANT: The constructor values must match the corresponding bitmask numbers in
		 * the bitmasks for each component.
		 */
		
		/**
		 * the background of an element
		 */
		BACKGROUND(1),
		
		/**
		 * alternate background of an element, for example to differentiate
		 * alternating rows of a table
		 */
		ALTBACKGROUND(2),
		
		/**
		 * the border around an element or a collection of elements
		 */
		BORDER(4),
		
		/**
		 * a button element
		 */
		BUTTON(8),
		
		/**
		 * the text of an element
		 */
		TEXT(16),
		
		/**
		 * alternate for the text of an element, for example a light color
		 * for a dark background
		 */
		ALTTEXT(32),
		
		/**
		 * an active (currently selected) element
		 */
		ACTIVE(64),
		
		/**
		 * an inactive (previously selected) element
		 */
		INACTIVE(128),
		
		/**
		 * a header element, for example a table header
		 */
		HEADER(256),
		
		/**
		 * the font of an element
		 */
		FONT(512);
		
		private int maskValue;
		private String elementValue;
		
		/*
		 * Constructor.
		 */
		private Element (int value)
		{
			maskValue = value;
		}
		
		/**
		 * Gets the mask value.
		 * 
		 * @return mask value
		 */
		public int getMaskValue ()
		{
			return maskValue;
		}
		
		/**
		 * Gets the element value.
		 * 
		 * @return element value
		 */
		public String getElementValue ()
		{
			return elementValue;
		}
		
		/**
		 * Sets the element value.
		 * 
		 * @param value element value
		 */
		public void setElementValue (String value)
		{
			elementValue = value;
		}
	}
	
	/**
	 * The specific window, for example the window showing the list of tracks.
	 * The <code>enum</code> value is the title of the associated window.
	 */
	public enum Window
	{
		
		/**
		 * main window
		 */
		MAIN(StringConstants.SKIN_WINDOW_MAIN),
		
		/**
		 * tracks window
		 */
		TRACKS(StringConstants.SKIN_WINDOW_TRACKS),
		
		/**
		 * playlists window
		 */
		PLAYLISTS(StringConstants.SKIN_WINDOW_PLAYLISTS),
		
		/**
		 * artists window
		 */
		ARTISTS(StringConstants.SKIN_WINDOW_ARTISTS),
		
		/**
		 * filters (query tracks) window
		 */
		FILTERS(StringConstants.QUERY_TRACKS),
		
		/**
		 * query playlists window
		 */
		QUERYPLAYLISTS(StringConstants.QUERY_PLAYLISTS),
		
		/**
		 * preferences window
		 */
		PREFERENCES(StringConstants.SKIN_WINDOW_PREFERENCES),
		
		/**
		 * skin preview window
		 */
		SKINPREVIEW(StringConstants.SKIN_WINDOW_SKINPREVIEW),
		
		/**
		 * window that displays track details
		 */
		TRACKINFO(StringConstants.SKIN_WINDOW_TRACKINFO),
		
		/**
		 * file save window
		 */
		FILESAVE(StringConstants.SKIN_WINDOW_FILESAVE),
		
		/**
		 * find duplicates window
		 */
		FINDDUPLICATES(StringConstants.SKIN_WINDOW_FINDDUPS);
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private Window (String s)
		{
			displayValue = s;
		}
		
		/**
		 * Gets the display value.
		 * 
		 * @return enum display value
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}
		
		/**
		 * Performs a reverse lookup of the <code>enum</code> from the display
		 * value.
		 * 
		 * @param value display value to look up
		 * @return enum value
		 */
		public static Window getEnum(String value)
		{
	        return lookup.get(value);
	    }
		
		/*
		 * Reverse lookup capability to get the enum based on its display value.
		 */
		private static final Map<String, Window> lookup = new HashMap<String, Window>();		
		static
		{
	        for (Window value : Window.values())
	        {
	            lookup.put(value.getDisplayValue(), value);
	        }
	    }
	}
	
    //---------------- Private variables -----------------------------------
	
	/*
	 * Components can have multiple skin items, for example text font, text color, and background
	 * color. These masks map the type of component to the skin items, so window managers can 
	 * register the correct set of skin items without ridiculous brute force code.
	 * 
	 * The mask values are decimal numbers that represent bitmask values.
	 * 
	 *   1 = background color
	 *   2 = alternate background color (for example tree view alternate rows)
	 *   4 = border color
	 *   8 = button color
	 *  16 = text color
	 *  32 = alternate text color (for example button text color)
	 *  64 = active color (for example a table view row that is selected)
	 * 128 = inactive color (for example when the cursor hovers over a table row)
	 * 256 = header color (for example a table view header)
	 * 512 = font
	 * 
	 * IMPORTANT: The mask number values must match the corresponding values in the constructor
	 * for each element type.
	 */
	private static final int borderMask          =         4 +     16 +                       512;
	private static final int boxPaneMask         = 1;
	private static final int checkboxMask        =                 16 +                       512;
	private static final int fileBrowserMask     = 1;
	private static final int fillPaneMask        = 1;
	private static final int labelMask           =                 16 +                       512;
	private static final int menuBarMask         = 1 +             16 +      64 +             512;
	private static final int menuMask            = 1 +             16 +      64 +             512;
	private static final int pushButtonMask      =         4 + 8 +      32 +                  512;
	private static final int scrollPaneMask      = 1;
	private static final int separatorMask       =                 16 +                       512;
	private static final int spinnerMask         = 1 +     4 +     16 +                       512;
	private static final int tablePaneMask       = 1;
	private static final int tabPaneMask         =     2 + 4 +     16 +           128 +       512;
	private static final int tableViewHeaderMask = 1 +     4 +          32 +            256 + 512;
	private static final int tableViewMask       = 1 + 2 + 4 +     16 +      64 + 128 +       512;
	private static final int textInputMask       =     2 + 4 +     16 +      64 +             512;
	private static final int treeViewMask        = 1 +             16 +      64 + 128 +       512;
	
	/*
	 * The window registry is a map of the Window enum to a map of the Element enum to a list
	 * of components.
	 */
	private Map<Window, Map<Element, List<Component>>> windowRegistry;
	
	/*
	 * The skin registry is a map of the user-friendly skin name to its skin definition.
	 */
	private Map<String, Map<Element, String>> skinRegistry;
	
	/*
	 * The window stack is used when multiple windows are displayed on top of each other. If the skin is
	 * changed when there are multiple stacked windows, then they all need to be re-skinned. Window 
	 * handlers that call the skinMe() method are responsible to maintain this stack. The preferences
	 * window handler calls reskinWindowStack() if the skin is changed.
	 */
	private Stack<Window> windowStack;
	
	/*
	 * Constructor. Making it private prevents instantiation by any other class.
	 */
	private Skins ()
	{
		
		/*
		 * Initialize the window registry.
		 */
		windowRegistry = new HashMap<Window, Map<Element, List<Component>>>();
		
		/*
		 * Initialize the skin registry.
		 */
		skinRegistry = new HashMap<String, Map<Element, String>>();
		skinRegistry.put(duskyGray, DUSKY_GRAY);
		skinRegistry.put(pumpkinPatch, PUMPKIN_PATCH);
		skinRegistry.put(seasideDaze, SEASIDE_DAZE);
		
		/*
		 * Initialize the window stack.
		 */
		windowStack = new ArrayStack<Window>();
		
        /*
         * Get the preferences object instance.
         */
        Preferences prefs = Preferences.getInstance();
        
        /*
         * If the skin preference exists, use it. Otherwise use the default skin.
         */
        String skinName;
        if ((skinName = prefs.getSkinName()) == null)
        {
        	skinName = defaultSkin;
        }

		initializeSkinElements(skinName);
	}
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the current skin name.
	 * 
	 * @return current skin name
	 */
	public String getCurrentSkinName ()
	{
		return currentSkin;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Gets the list of skin names.
	 * 
	 * @return list of skin names
	 */
	public Sequence<String> getSkinNames ()
	{
		Sequence<String> skinNames = new ArrayList<String>();
		Iterator<String> skinRegistryIter = skinRegistry.iterator();
		while (skinRegistryIter.hasNext())
		{
			String skin = skinRegistryIter.next();
			skinNames.add(skin);
		}
		return skinNames;
	}
	
	/**
	 * Initializes the skin element values from a named skin.
	 * 
	 * @param skinName named skin
	 */
	public void initializeSkinElements (String skinName)
	{
		
		/*
		 * Set the current skin name. This is the only way to set this field; it does not have its
		 * own setter method.
		 */
		currentSkin = skinName;
		
		/*
		 * Get the skin set from the registry and set all the element values.
		 */
		Map<Element, String> skinSet = skinRegistry.get(skinName);
		for (Element element : Element.values())
		{
			element.setElementValue(skinSet.get(element));
		}
	}
	
	/**
	 * Registers the elements for a specific window that require skinning.
	 * 
	 * @param window window for which elements are being registered
	 * @param elements map of the element type to a list of components
	 */
	public void registerWindowElements (Window window, Map<Element, List<Component>> elements)
	{
    	if (window == null)
    	{
    		throw new IllegalArgumentException("window argument is null");
    	}
    	
    	if (elements == null)
    	{
    		throw new IllegalArgumentException("elements argument is null");
    	}
    	
		windowRegistry.put(window, elements);
	}
	
	/**
	 * 
	 * Registers additional dynamic elements for a specific window that 
	 * require skinning. This is used when some of the content of a window
	 * is dynamically built in the code, and is thus not derived from the
	 * BXML file.
	 * 
	 * @param window window for which elements are being registered
	 * @param elements map of the element type to a dynamic list of components
	 */
	public void registerDynamicWindowElements (Window window, Map<Element, List<Component>> elements)
	{
    	if (window == null)
    	{
    		throw new IllegalArgumentException("window argument is null");
    	}
    	
    	if (elements == null)
    	{
    		throw new IllegalArgumentException("elements argument is null");
    	}
    	
		Map<Element, List<Component>> windowElements = windowRegistry.remove(window);
		for (Element element : Element.values())
		{
			List<Component> windowElementComponents = windowElements.remove(element);
			List<Component> inputComponents = elements.remove(element);
			Iterator<Component> inputComponentsIter = inputComponents.iterator();
			while (inputComponentsIter.hasNext())
			{
				Component inputComponent = inputComponentsIter.next();
				windowElementComponents.add(inputComponent);
			}
			windowElements.put(element, windowElementComponents);
		}
		windowRegistry.put(window, windowElements);
	}
	
	/**
	 * Pushes a window onto the window stack. The window stack keeps the 
	 * current set of stacked windows, so they can all be re-skinned if the
	 * user changes the skin name from the top window of a stack.
	 * 
	 * @param window window to be pushed
	 */
	public void pushSkinnedWindow (Window window)
	{
    	if (window == null)
    	{
    		throw new IllegalArgumentException("window argument is null");
    	}
    	
		windowStack.push(window);
	}
	
	/**
	 * Pops a window off of the window stack and throws it away. We don't need
	 * to do anything with the window, because its only purpose is to let
	 * <code>reskinWindowStack</code> know which windows to re-skin. Once that
	 * has happened there is nothing else to do.
	 */
	public void popSkinnedWindow ()
	{
		windowStack.pop();
	}
	
	/**
	 * Re-skins all the windows on the window stack. This is called when the 
	 * skin is changed via preferences.
	 */
	public void reskinWindowStack ()
	{
		Iterator<Window> windowStackIter = windowStack.iterator();
		while (windowStackIter.hasNext())
		{
			Window window = windowStackIter.next();
			skinMe(window);
		}
	}
	
	/**
	 * Applies the skin values to the elements for a specific window.
	 * 
	 * @param window window for which skin elements should be applied
	 */
	public void skinMe (Window window)
	{
    	if (window == null)
    	{
    		throw new IllegalArgumentException("window argument is null");
    	}
		
		/*
		 * Get the elements map for the input window.
		 */
		Map<Element, List<Component>> elements = windowRegistry.get(window);
		
		/*
		 * Walk through all element types.
		 */
		for (Element element : Element.values())
		{
			
			/*
			 * Get the list of components for this element type.
			 */
			List<Component> components = elements.get(element);
			
			/*
			 * Iterate if no components exist.
			 */
			if (components == null)
			{
				continue;
			}
			
			/*
			 * Walk through all components.
			 */
			Iterator<Component> componentsIter = components.iterator();
			while (componentsIter.hasNext())
			{
				Component component = componentsIter.next();
				
				/*
				 * Apply the skin to this component.
				 */
				applySkinToComponent(element, component);
			}
		}
	}
	
	/**
	 * Maps each component to all of the skin elements that apply, for a list
	 * of window components.
	 * 
	 * @param components list of window components
	 * @return map of element to applicable components
	 */
	public Map<Element, List<Component>> mapComponentsToSkinElements (List<Component> components)
	{
    	if (components == null)
    	{
    		throw new IllegalArgumentException("components argument is null");
    	}
    	
		Map<Element, List<Component>> windowElements = new HashMap<Element, List<Component>>();
		
		/*
		 * Walk through all elements.
		 */
		for (Element element : Element.values())
		{
			List<Component> elementComponents = new ArrayList<Component>();
			
			/*
			 * Walk through the input component list.
			 */
			Iterator<Component> componentsIter = components.iterator();
			while (componentsIter.hasNext())
			{
				Component component = componentsIter.next();
				
				/*
				 * Add this component for this element if it's used.
				 */
				if (isElementUsedForComponent(element, component))
				{
					elementComponents.add(component);
				}
			}
			
			windowElements.put(element, elementComponents);
		}
		
		return windowElements;
	}
	
    //---------------- Private methods -------------------------------------
	
	/*
	 * Determine if a given element type is used for a given component. For example, is the
	 * border element used for a button?
	 */
	private boolean isElementUsedForComponent (Element element, Component component)
	{
		boolean result = false;
		
		/*
		 * Brute force checking of the various components against the associated component mask.
		 */
		if (component instanceof Border)
		{
			if ((element.getMaskValue() & borderMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof BoxPane)
		{
			if ((element.getMaskValue() & boxPaneMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof Checkbox)
		{
			if ((element.getMaskValue() & checkboxMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof FileBrowser)
		{
			if ((element.getMaskValue() & fileBrowserMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof FillPane)
		{
			if ((element.getMaskValue() & fillPaneMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof Label)
		{
			if ((element.getMaskValue() & labelMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof MenuBar)
		{
			if ((element.getMaskValue() & menuBarMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof Menu)
		{
			if ((element.getMaskValue() & menuMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof PushButton)
		{
			if ((element.getMaskValue() & pushButtonMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof ScrollPane)
		{
			if ((element.getMaskValue() & scrollPaneMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof Separator)
		{
			if ((element.getMaskValue() & separatorMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof Spinner)
		{
			if ((element.getMaskValue() & spinnerMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof TablePane)
		{
			if ((element.getMaskValue() & tablePaneMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof TableViewHeader)
		{
			if ((element.getMaskValue() & tableViewHeaderMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof TableView)
		{
			if ((element.getMaskValue() & tableViewMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof TabPane)
		{
			if ((element.getMaskValue() & tabPaneMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof TextInput)
		{
			if ((element.getMaskValue() & textInputMask) != 0)
			{
				result = true;
			}
		}
		else if (component instanceof TreeView)
		{
			if ((element.getMaskValue() & treeViewMask) != 0)
			{
				result = true;
			}
		}
		
		return result;
	}
	
	/*
	 * Apply the skin to a single component, for a given type of element.
	 */
	private void applySkinToComponent (Element element, Component component)
	{
		Map<String, String> componentSkinValue = new HashMap<String, String>();
		
		switch (element)
		{
		
		/*
		 * Background is used for many components.
		 */			
		case BACKGROUND:
			componentSkinValue.put("backgroundColor", element.getElementValue());
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Alternate background is used for alternate rows in a TableView and for TextInput.
		 * The name of the style varies.
		 */
		case ALTBACKGROUND:
			if (component instanceof TableView)
			{
				componentSkinValue.put("alternateRowBackgroundColor", element.getElementValue());
			}
			else if (component instanceof TabPane)
			{
				componentSkinValue.put("activeTabColor", element.getElementValue());
			}
			else
			{
				componentSkinValue.put("backgroundColor", element.getElementValue());
			}
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Border is used for many components. The name of the style varies.
		 */
		case BORDER:
			if (component instanceof Border)
			{
				componentSkinValue.put("color", element.getElementValue());
			}
			else if (component instanceof TableView)
			{
				componentSkinValue.put("verticalGridColor", element.getElementValue());
			}
			else
			{
				componentSkinValue.put("borderColor", element.getElementValue());
				if (component instanceof TabPane)
				{
					componentSkinValue.put("inactiveBorderColor", element.getElementValue());
				}
			}
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Button is actually the background color.
		 */
		case BUTTON:
			componentSkinValue.put("backgroundColor", element.getElementValue());
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Text is used for many components. The name of the style varies.
		 */
		case TEXT:
			if (component instanceof Border)
			{
				componentSkinValue.put("titleColor", element.getElementValue());
			}
			else if (component instanceof TabPane)
			{
				componentSkinValue.put("buttonColor", element.getElementValue());
			}
			else if (component instanceof Separator)
			{
				componentSkinValue.put("headingColor", element.getElementValue());
			}
			else
			{
				componentSkinValue.put("color", element.getElementValue());
			}
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Alternate text is an alternate text color for usually dark backgrounds, such as buttons and 
		 * table headers.
		 */
		case ALTTEXT:
			componentSkinValue.put("color", element.getElementValue());
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Active is used for many components. The name of the style varies. 
		 */
		case ACTIVE:
			if (component instanceof MenuBar || component instanceof Menu)
			{
				componentSkinValue.put("activeBackgroundColor", element.getElementValue());
			}
			else
			{
				componentSkinValue.put("selectionBackgroundColor", element.getElementValue());
			}
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Inactive is used for TabPane, TableView and TreeView components.
		 */
		case INACTIVE:
			if (component instanceof TabPane)
			{
				componentSkinValue.put("inactiveTabColor", element.getElementValue());
			}
			else
			{
				componentSkinValue.put("inactiveSelectionBackgroundColor", element.getElementValue());
				componentSkinValue.put("highlightBackgroundColor", element.getElementValue());
			}
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Header is only used for TableView components.
		 */
		case HEADER:
			componentSkinValue.put("backgroundColor", element.getElementValue());
			component.setStyles(componentSkinValue);
			break;
			
		/*
		 * Font is used for many components. We only specify the font name here; font attributes 
		 * such as size, bold, etc are specified in the BXML or elsewhere in the code.
		 */
		case FONT:
			Component.StyleDictionary styles = component.getStyles();
			Font currFont;
			String fontStyleName;
			
			if (component instanceof TabPane)
			{
				fontStyleName = "buttonFont";
			}
			else
			{
				fontStyleName = "font";
			}
			currFont = (Font) styles.get(fontStyleName);
			
			/*
			 * If a font exists, update it. Otherwise create a new font style.
			 */
			if (currFont != null)
			{
				Font newFont = new Font(element.getElementValue(), currFont.getStyle(), currFont.getSize());
				styles.put(fontStyleName, newFont);
			}
			else
			{
				componentSkinValue.put(fontStyleName, element.getElementValue());
				component.setStyles(componentSkinValue);
			}
			break;
			
		default :
		}
	}
}
