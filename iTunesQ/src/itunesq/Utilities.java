package itunesq;

import java.io.IOException;
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
 * <p>
 * This is a final class consisting entirely of <code>static</code> methods.
 * 
 * @author Jon
 *
 */
public final class Utilities
{
	
    //---------------- Public variables ------------------------------------
	
	/**
	 * Java preferences key representing the save directory.
	 */
	public static final String JAVA_PREFS_KEY_SAVEDIR = "SAVE_DIRECTORY";
	
    //---------------- Private variables -----------------------------------

	private static Label fileLabel = null;
	private static Label numTracksLabel = null;
	private static Label numPlaylistsLabel = null;
	private static Label numArtistsLabel = null;
	
	/*
	 * Static string definitions.
	 */
	private static final String DATE_FORMAT = "EEE, MMM dd yyyy, HH:mm:ss";
	private static final String FORMATTED_DATE = "yyyy-MM-dd'T'HH:mm:ssX";
	private static final String MMSS_FORMAT = "%02d:%02d";
	private static final String HHMMSS_FORMAT = "%02d:%02d:%02d";
	private static final String UNKNOWN = StringConstants.UTILITY_UNKNOWN_DATE;
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Formats a date string from a date object.
	 * 
	 * @param date date object to be formatted
	 * @return formatted date string
	 */
	public static String formatDate (Date date)
	{
		if (date != null)
		{
			SimpleDateFormat dateFormatter = new SimpleDateFormat(new String(DATE_FORMAT));
			return dateFormatter.format(date);
		}
		else
		{
			return UNKNOWN;
		}
	}
	
	/**
	 * Parses a date string into a date object.
	 * 
	 * @param dateStr formatted date string
	 * @return date object
	 * @throws ParseException If an error occurs trying to parse the date 
	 * string.
	 */
	public static Date parseDate (String dateStr) 
			throws ParseException
	{
    	SimpleDateFormat dateParser = new SimpleDateFormat(new String(FORMATTED_DATE));
    	
		return dateParser.parse(dateStr);
	}
	
	/**
	 * Formats milliseconds into an [HH:]MM:SS string. Hours are optional 
	 * in the returned string. 
	 * 
	 * @param milliseconds milliseconds to be converted
	 * @return formatted string
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
		 * And finally get the number of hours from the minutes.
		 */
		long hours = TimeUnit.MINUTES.toHours(minutes);
		
		/*
		 * Subtract the hours (converted to minutes) from the the total minutes
		 * to get the remaining minutes.
		 */
		long remainMinutes = minutes - TimeUnit.HOURS.toMinutes(hours);
		
		/*
		 * Subtract the minutes (converted to seconds) from the the total seconds
		 * to get the remaining seconds.
		 */
		long remainSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
		
		/*
		 * Format the result based on whether or not we have any hours.
		 */
		if (hours > 0)
		{
			result = String.format(HHMMSS_FORMAT, hours, remainMinutes, remainSeconds);
		}
		else
		{
			result = String.format(MMSS_FORMAT, remainMinutes, remainSeconds);
		}
		
		return result;
	}
	
	/**
	 * Provides typing assistance for text input fields, when the list of 
	 * possible choices is reasonably small, for example artist or playlist 
	 * names.
	 * <p>
	 * This is expected to be called for every character entered into a text 
	 * field. As soon as enough characters are entered to uniquely identify a 
	 * name, the text field is populated with the name. A case-insensitive 
	 * comparator should be used on the list of names, so the user doesn't need
	 * to use correct case, but that's up to the caller. 
	 *  
	 * @param textInput text input object in which the user is typing
	 * @param names list of names to check against
	 * @param text text entered so far by the user
	 * @param operator filter operator (we only support IS and CONTAINS 
	 * operators)
	 * @return <code>true</code> if a match was found, otherwise 
	 * <code>false</code>
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
	 * Saves the file label for the main window.
	 * 
	 * @param label file label
	 */
	public static void saveFileLabel (Label label)
	{
		fileLabel = label;
	}

	/**
	 * Saves the number of tracks label for the main window.
	 * 
	 * @param label number of tracks label
	 */
	public static void saveNumTracksLabel (Label label)
	{
		numTracksLabel = label;
	}

	/**
	 * Saves the number of playlists label for the main window.
	 * 
	 * @param label number of playlists label
	 */
	public static void saveNumPlaylistsLabel (Label label)
	{
		numPlaylistsLabel = label;
	}

	/**
	 * Saves the number of artists label for the main window.
	 * 
	 * @param label number of artists label
	 */
	public static void saveNumArtistsLabel (Label label)
	{
		numArtistsLabel = label;
	}
	
	/**
	 * Processes the XML file, and updates the main window XML file 
	 * information.
	 * 
	 * @param xmlFileName name of the XML file to be processed
	 * @throws JDOMException If an error occurs trying to process the iTunes 
	 * XML file.
	 * @throws IOException If an error occurs trying to read the iTunes 
	 * XML file.
	 */
	public static void updateFromXMLFile (String xmlFileName) 
			throws JDOMException, IOException
	{

		/*
		 * Read and process the XML file.
		 */
		XMLHandler.processXML(xmlFileName);

		/*
		 * Update the main window information based on the XML file contents.
		 */
		updateMainWindowLabels(xmlFileName);
	}
	
	/**
	 * Updates the XML file information on the main window.
	 * <p>
	 * This is somewhat tricky. <code>MainWindow</code> calls several methods
	 * in this class to save the actual <code>Label</code> variables, for
	 * example <code>saveFileLabel</code>. It then calls this method to fill
	 * in the label values. Likewise, if the XML file is changed while running,
	 * <code>updateFromXMLFile</code> also calls this method. All this so I 
	 * don't have to repeat this code in two places.
	 * 
	 * @param xmlFileName iTunes XML file name
	 */
	public static void updateMainWindowLabels (String xmlFileName)
	{
		fileLabel.setText(xmlFileName + StringConstants.UTILITY_XMLFILE_DATE + 
				XMLHandler.getXMLFileTimestamp());
		numTracksLabel.setText(StringConstants.UTILITY_NUMTRACKS + 
				Integer.toString(XMLHandler.getNumberOfTracks()));
		numPlaylistsLabel.setText(StringConstants.UTILITY_NUMPLAYLISTS + 
				Integer.toString(XMLHandler.getNumberOfPlaylists()));
		numArtistsLabel.setText(StringConstants.UTILITY_NUMARTISTS + 
				Integer.toString(XMLHandler.getNumberOfArtists()));
	}
	
	/**
	 * Accesses the Java preference for a given key. This method also sets 
	 * the preference in case it was not currently set.
	 * 
	 * @param key key that represents the preference
	 * @return value for the specified key
	 */
	public static String accessJavaPreference (String key)
	{
		String result;
		
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
			defaultValue = Preferences.getDefaultSaveDirectory();
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
	 * Saves the Java preference for a given key.
	 * 
	 * @param key key that represents the preference
	 * @param value value for the specified key
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
