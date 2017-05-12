package itunesq;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TextInput;
import org.jdom2.JDOMException;

/**
 * Class that contains various useful utility methods.
 * 
 * @author Jon
 *
 */
public class Utilities
{
	
    //---------------- Public variables ------------------------------------
	
	public static final String JAVA_PREFS_KEY_SAVEDIR = "SAVE_DIRECTORY";
	
    //---------------- Private variables -----------------------------------

	private static Label fileNameLabel = null;
	private static Label fileDateLabel = null;
	private static Label numTracksLabel = null;
	private static Label numPlaylistsLabel = null;
	
	/*
	 * Static string definitions.
	 */
	private static final String DATE_FORMAT = "EEE, MMM dd yyyy, HH:mm:ss";
	private static final String FORMATTED_DATE = "yyyy-MM-dd'T'HH:mm:ssX";
	private static final String HHMM_FORMAT = "%02d:%02d";
	private static final String UNKNOWN     = "--unknown--";
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Format a date string from a Date object.
	 * 
	 * @param date Date object to be formatted.
	 * @return Formatted date string.
	 */
	public static String formatDate (Date date)
	{
		if (date != null)
		{
			String dateFormat = new String(DATE_FORMAT);
			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
			return dateFormatter.format(date);
		}
		else
		{
			return UNKNOWN;
		}
	}
	
	/**
	 * Parse a date string into a Date object.
	 * 
	 * @param dateStr Formatted date string.
	 * @return Date object.
	 * @throws ParseException
	 */
	public static Date parseDate (String dateStr) 
			throws ParseException
	{
    	String dateFormat = new String(FORMATTED_DATE);
    	SimpleDateFormat dateParser = new SimpleDateFormat(dateFormat);
    	
		return dateParser.parse(dateStr);
	}
	
	/**
	 * Format milliseconds into an HH:MM string.
	 * 
	 * @param milliseconds Milliseconds to be converted.
	 * @return Formatted string.
	 */
	public static String convertMillisecondTime (int milliseconds)
	{
		String result;
		
		/*
		 * First convert the input value to seconds.
		 */
		long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
		
		/*
		 * Then get the number of minutes from the seconds.
		 */
		long minutes = TimeUnit.SECONDS.toMinutes(seconds);
		
		/*
		 * Finally, subtract the minutes (converted to seconds) from the the total seconds
		 * to get the remaining seconds.
		 */
		long remainSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
		
		result = String.format(HHMM_FORMAT, minutes, remainSeconds);
		
		return result;
	}
	
	/**
	 * Process the XML file, and update the main window XML file information.
	 * 
	 * @param xmlFileName Name of the XML file to be processed.
	 * @throws JDOMException
	 */
	public static void updateFromXMLFile (String xmlFileName) 
			throws JDOMException
	{

		/*
		 * Read and process the XML file.
		 */
		XMLHandler.processXML(xmlFileName);

		/*
		 * Update the main window information based on the XML file contents.
		 */
		fileNameLabel.setText(xmlFileName);
		fileDateLabel.setText(XMLHandler.getXMLFileTimestamp());
		numTracksLabel.setText(Integer.toString(XMLHandler.getNumberOfTracks()));
		numPlaylistsLabel.setText(Integer.toString(XMLHandler.getNumberOfPlaylists()));
	}
	
	/**
	 * Provides typing assistance for text input fields, when the list of possible choices is
	 * reasonably small, for example artist or playlist names.
	 * <p>
	 * This is expected to be called for every character entered into a text field. As soon as enough
	 * characters are entered to uniquely identify a name, the text field is populated with the name.
	 * A case-insensitive comparator should be used on the list of names, so the user doesn't need
	 * to use correct case, but that's up to the caller. 
	 *  
	 * @param textInput Text input object in which the user is typing.
	 * @param names List of names to check against.
	 * @param text Text entered so far by the user.
	 * @param operator Filter operator (we only support IS and CONTAINS operators).
	 */
	public static boolean typingAssistant (TextInput textInput, ArrayList<String> names, 
			                            String text, Filter.Operator operator)
	{
		boolean result = false;
		boolean operatorIS;
		switch (operator)
		{
		case IS:
			operatorIS = true;
			break;
			
		case CONTAINS:
			operatorIS = false;
			break;
			
		default:
			return result;
		}
		/*
		 * A negative return from binarySearch() is the negative value of the index at which the
		 * name would be inserted, meaning it wasn't found. We use this as a trigger to keep looking.
		 */
		int insertionPoint = ArrayList.binarySearch(names, text, names.getComparator());	 
        if (insertionPoint < 0)
        {
            insertionPoint = -(insertionPoint + 1);
            int numNames = names.getLength();

            /*
             * No point in continuing to look if the insertion point is beyond the end of the 
             * names list.
             */
            if (insertionPoint < numNames)
            {
                text = text.toLowerCase();
                final String name = names.get(insertionPoint);

                /*
                 * We're getting closer if the name at the insertion point starts with the entered
                 * text.
                 */
                boolean isCandidate;
                if (operatorIS == true)
                {
                	isCandidate = name.toLowerCase().startsWith(text);
                }
                else
                {
                	isCandidate = name.toLowerCase().contains(text);
                }
                
                if (isCandidate == true)
                {
                    String nextName = (insertionPoint == numNames - 1) ? 
                    		null : names.get(insertionPoint + 1);

                    /*
                     * Paydirt if at least one more name exists, but doesn't start with or contain
                     * the entered text.
                     */
                    if (operatorIS == true)
                    {
                    	isCandidate = !name.toLowerCase().startsWith(text);
                    }
                    else
                    {
                    	isCandidate = !name.toLowerCase().contains(text);
                    }
                    
                    if (nextName == null || isCandidate == false)
                    {
                    	result = true;
                        textInput.setText(name);

                        int selectionStart = text.length();
                        int selectionLength = name.length() - selectionStart;
                        textInput.setSelection(selectionStart, selectionLength);
                    }
                }
            }
        }
        
        return result;
	}
	
	/**
	 * Set the file name label for the main window. This and the following methods are here because
	 * they are used at startup, and by the File - Open menu.
	 * 
	 * @param label The file name label.
	 */
	public static void setFileNameLabel (Label label)
	{
		fileNameLabel = label;
	}
	
	/**
	 * Set the file date label for the main window.
	 * 
	 * @param label The file date label.
	 */
	public static void setFileDateLabel (Label label)
	{
		fileDateLabel = label;
	}

	/**
	 * Set the number of tracks label for the main window.
	 * 
	 * @param label The number of tracks label.
	 */
	public static void setNumTracksLabel (Label label)
	{
		numTracksLabel = label;
	}

	/**
	 * Set the number of playlists label for the main window.
	 * 
	 * @param label The number of playlists label.
	 */
	public static void setNumPlaylistsLabel (Label label)
	{
		numPlaylistsLabel = label;
	}
	
	/**
	 * Access the Java preference for a given key. This method also sets the preference in case it
	 * was not currently set.
	 * 
	 * @param key Key that represents the preference.
	 * @return Value for the specified key.
	 */
	public static String accessJavaPreference (String key)
	{
		String result;
		
		/*
		 * Get the preferences object. We need this to get default values.
		 */
		Preferences userPrefs = Preferences.getInstance();
		
		/*
		 * Get the Java preferences node.
		 */
		java.util.prefs.Preferences javaPrefs;
		javaPrefs = java.util.prefs.Preferences.userRoot().node(Utilities.class.getName());
		
		/*
		 * Get the default value in case the Java preference doesn't exist.
		 * 
		 * NOTE: We don't expect an unknown key so we don't bother checking it.
		 */
		String defaultValue = null;
		switch (key)
		{
		case JAVA_PREFS_KEY_SAVEDIR:
			defaultValue = userPrefs.getDefaultSaveDirectory();
			break;
			
		default:
		}
		
		/*
		 * Get the Java preference value, or use the default.
		 */
		result = javaPrefs.get(key, defaultValue);
		
		/*
		 * We have no way of knowing if the Java preference was set before, so unconditionally set it.
		 */
		javaPrefs.put(key, result);
		
        return result;
	}
	
	/**
	 * Save the Java preference for a given key.
	 * 
	 * @param key Key that represents the preference.
	 * @param value Value for the specified key.
	 */
	public static void saveJavaPreference (String key, String value)
	{
		
		/*
		 * Get the Java preferences node.
		 */
		java.util.prefs.Preferences javaPrefs;
		javaPrefs = java.util.prefs.Preferences.userRoot().node(Utilities.class.getName());

		/*
		 * Save the java preference value.
		 */
		javaPrefs.put(key, value);
	}
}
