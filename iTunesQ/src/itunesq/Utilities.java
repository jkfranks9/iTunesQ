package itunesq;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TextInput;

/**
 * Class that contains various useful utility methods.
 * <p>
 * This is a final class consisting entirely of static methods.
 * 
 * @author Jon
 *
 */
public final class Utilities
{

    // ---------------- Public variables ------------------------------------

    /**
     * Java preferences key representing the save directory.
     */
    public static final String JAVA_PREFS_KEY_SAVEDIR = "SAVE_DIRECTORY";

    // ---------------- Private variables -----------------------------------

    private static Label fileLabel = null;
    private static Label numAudioTracksLabel = null;
    private static Label numVideoTracksLabel = null;
    private static Label numPlaylistsLabel = null;
    private static Label numArtistsLabel = null;

    /*
     * Format or error string returned by formatDate().
     */
    private static final String DATE_FORMAT = "EEE, MMM dd yyyy, HH:mm:ss";
    private static final String UNKNOWN = StringConstants.UTILITY_UNKNOWN_DATE;
    
    /*
     * Format definitions for parseDate().
     */
    private static final String SIMPLE_DATE_AM = "AM";
    private static final String SIMPLE_DATE_PM = "PM";
    private static final String SIMPLE_DATE = "MM/dd/yyyy HH:mm a";
    private static final String FORMATTED_DATE_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssX";
    private static final String FORMATTED_DATE_MILLI = "yyyy-MM-dd'T'HH:mm:ss.";
    private static final String FORMATTED_DATE_UTC_ZONE_SUFFIX = "Z";
    private static final String FORMATTED_DATE_ISO8601_UTC_ZONE_SUFFIX = "X";
    private static final String FORMATTED_DATE_ISO8601_ZONE_SUFFIX = "XXX";
    
    /*
     * Format returned by getCurrentTimestamp().
     */
    private static final String FORMATTED_DATE_FILENAME = "yyyy-MM-dd-HH-mm-ss";
    
    /*
     * Format returned by convertMillisecondTime().
     */
    private static final String MMSS_FORMAT = "%02d:%02d";
    private static final String HHMMSS_FORMAT = "%02d:%02d:%02d";
    
    /*
     * Format definitions for parseTime().
     */
    private static final String CALENDAR_MMSS_FORMAT = "mm:ss";
    private static final String CALENDAR_HHMMSS_FORMAT = "HH:mm:ss";

    // ---------------- Public methods --------------------------------------

    /**
     * Formats a date string from a date object.
     * 
     * @param date date object to be formatted
     * @return formatted date string
     */
    public static String formatDate(Date date)
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
     * The date string can be any of the the following formats:
     * <ul>
     * <li>MM/dd/yyyy HH:mm {AM|PM}</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.mmmZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.mmm{+|-}HH</li>
     * </ul>
     * <p>
     * The number of millisecond digits ("mmm") can be variable.
     * 
     * @param dateStr formatted date string
     * @return date object
     * @throws ParseException If an error occurs trying to parse the date
     * string.
     */
    public static Date parseDate(String dateStr) 
            throws ParseException
    {
    	SimpleDateFormat dateParser = null;
    	
    	/*
    	 * Use the simple format if the string ends with AM or PM.
    	 */
    	if (dateStr.endsWith(SIMPLE_DATE_AM) || (dateStr.endsWith(SIMPLE_DATE_PM)))
    	{
            dateParser = new SimpleDateFormat(new String(SIMPLE_DATE));
    	}
    	
    	/*
    	 * The date does not contain milliseconds.
    	 */
    	else if (dateStr.indexOf('.') == -1)
    	{
			dateParser = new SimpleDateFormat(new String(FORMATTED_DATE_ISO8601));
    	}
    	
    	/*
    	 * The date contains milliseconds.
    	 */
    	else
    	{
    		
    		/*
    		 * Format the date according to the zone suffix used.
    		 */
    		String formattedDateMilli;
    		int startIndex = dateStr.lastIndexOf('.');
    		int endIndex;
    		int numMilliChars;
    		
    		if (dateStr.endsWith(FORMATTED_DATE_UTC_ZONE_SUFFIX))
    		{
        		
        		/*
        		 * Create the format string with the correct number of millisecond characters.
        		 */
        		endIndex = dateStr.lastIndexOf('Z');
        		numMilliChars = endIndex - startIndex - 1;
    			formattedDateMilli = FORMATTED_DATE_MILLI + 
    					String.join("", Collections.nCopies(numMilliChars, "S")) + 
    					FORMATTED_DATE_ISO8601_UTC_ZONE_SUFFIX;
    		}
    		else
    		{
        		
        		/*
        		 * Create the format string with the correct number of millisecond characters.
        		 */
        		endIndex = dateStr.lastIndexOf('-');
        		numMilliChars = endIndex - startIndex - 1;
        		
    			formattedDateMilli = FORMATTED_DATE_MILLI + 
    					String.join("", Collections.nCopies(numMilliChars, "S")) + 
    					FORMATTED_DATE_ISO8601_ZONE_SUFFIX;
    		}
            
    		dateParser = new SimpleDateFormat(new String(formattedDateMilli));
    	}

        return dateParser.parse(dateStr);
    }

    /**
     * Formats milliseconds into an [HH:]MM:SS string. Hours are optional in the
     * returned string.
     * 
     * @param milliseconds milliseconds to be converted
     * @return formatted string
     */
    public static String convertMillisecondTime(int milliseconds)
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
         * Subtract the minutes (converted to seconds) from the the total
         * seconds to get the remaining seconds.
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
     * Parses a time string into milliseconds. The time string is expected to be
     * of the form [HH:]MM:SS.
     * 
     * @param timeString time string to be parsed
     * @return time in milliseconds
     * @throws ParseException If an error occurs trying to parse the time
     * string.
     */
    public static long parseTime(String timeString) 
            throws ParseException
    {
        SimpleDateFormat format;
        Date date;

        /*
         * Determine if the input is in HH:MM:SS or MM:SS format.
         */
        if (timeString.indexOf(":") != timeString.lastIndexOf(":"))
        {
            format = new SimpleDateFormat(CALENDAR_HHMMSS_FORMAT);
        }
        else
        {
            format = new SimpleDateFormat(CALENDAR_MMSS_FORMAT);
        }

        /*
         * Parse and convert the string into a calendar object.
         */
        date = format.parse(timeString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        /*
         * Return the calendar time in milliseconds.
         */
        return calendar.getTimeInMillis();
    }

    /**
     * Gets the current timestamp.
     * 
     * @return current timestamp string
     */
    public static String getCurrentTimestamp()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(FORMATTED_DATE_FILENAME);
        return format.format(calendar.getTime());
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
    public static boolean typingAssistant(TextInput textInput, ArrayList<String> names, String text,
            Filter.Operator operator)
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
         * Don't do anything if the input list of names to search is null.
         */
        if (names != null)
        {

            /*
             * A negative return from binarySearch() is the negative value of the
             * index at which the name would be inserted, meaning it wasn't found.
             * We use this as a trigger to keep looking.
             */
            int insertionPoint = ArrayList.binarySearch(names, text, names.getComparator());
            if (insertionPoint < 0)
            {
                insertionPoint = -(insertionPoint + 1);
                int numNames = names.getLength();

                /*
                 * No point in continuing to look if the insertion point is beyond
                 * the end of the names list.
                 */
                if (insertionPoint < numNames)
                {
                    text = text.toLowerCase();
                    final String name = names.get(insertionPoint);

                    /*
                     * We're getting closer if the name at the insertion point
                     * starts with the entered text.
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
                        String nextName = (insertionPoint == numNames - 1) ? null : names.get(insertionPoint + 1);

                        /*
                         * Paydirt if at least one more name exists, but doesn't
                         * start with or contain the entered text.
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
        }

        return result;
    }

    /**
     * Saves the file label for the main window.
     * 
     * @param label file label
     */
    public static void saveFileLabel(Label label)
    {
        fileLabel = label;
    }

    /**
     * Saves the number of audio tracks label for the main window.
     * 
     * @param label number of audio tracks label
     */
    public static void saveNumAudioTracksLabel(Label label)
    {
        numAudioTracksLabel = label;
    }

    /**
     * Saves the number of video tracks label for the main window.
     * 
     * @param label number of video tracks label
     */
    public static void saveNumVideoTracksLabel(Label label)
    {
        numVideoTracksLabel = label;
    }

    /**
     * Saves the number of playlists label for the main window.
     * 
     * @param label number of playlists label
     */
    public static void saveNumPlaylistsLabel(Label label)
    {
        numPlaylistsLabel = label;
    }

    /**
     * Saves the number of artists label for the main window.
     * 
     * @param label number of artists label
     */
    public static void saveNumArtistsLabel(Label label)
    {
        numArtistsLabel = label;
    }

    /**
     * Updates the input file information on the main window.
     * <p>
     * This is somewhat tricky. <code>MainWindow</code> calls several methods in
     * this class to save the actual <code>Label</code> variables, for example
     * <code>saveFileLabel</code>. It then calls <code>updateFromInputFile</code>
     * which in turn calls this method to fill in the label values. Likewise, if 
     * the input file is changed while running, <code>updateFromInputFile</code>
     * is also called. All this so I don't have to repeat this code in two places.
     * 
     * @param inputFileName input file name
     */
    public static void updateMainWindowLabels(String inputFileName)
    {
    	String fileNameExt = FilenameUtils.getExtension(inputFileName);
    	
    	if (fileNameExt.equals(StringConstants.XML))
    	{
            fileLabel.setText(inputFileName + StringConstants.UTILITY_FILE_DATE 
                    + XMLHandler.getXMLFileTimestamp());
    	}
    	else if (fileNameExt.equals(StringConstants.JSON))
    	{
            fileLabel.setText(inputFileName + StringConstants.UTILITY_FILE_DATE 
                    + JSONHandler.getJSONFileTimestamp());
    	}
    	
        numAudioTracksLabel.setText(StringConstants.UTILITY_NUM_AUDIO_TRACKS 
                + Integer.toString(Database.getNumberOfAudioTracks()));
        numVideoTracksLabel.setText(StringConstants.UTILITY_NUM_VIDEO_TRACKS 
                + Integer.toString(Database.getNumberOfVideoTracks()));
        numPlaylistsLabel.setText(StringConstants.UTILITY_NUM_PLAYLISTS 
                + Integer.toString(Database.getNumberOfPlaylists()));
        numArtistsLabel.setText(StringConstants.UTILITY_NUM_ARTISTS 
                + Integer.toString(Database.getNumberOfArtists()));
    }

    /**
     * Accesses the Java preference for a given key. This method also sets the
     * preference in case it was not currently set.
     * 
     * @param key key that represents the preference
     * @return value for the specified key
     */
    public static String accessJavaPreference(String key)
    {
        String result;

        /*
         * Get the Java preferences node.
         */
        java.util.prefs.Preferences javaPrefs;
        javaPrefs = java.util.prefs.Preferences.userRoot().node(Utilities.class.getName());

        /*
         * Get the default value in case the Java preference doesn't exist.
         */
        String defaultValue = null;
        switch (key)
        {
        case JAVA_PREFS_KEY_SAVEDIR:
            defaultValue = Preferences.getDefaultSaveDirectory();
            break;

        default:
            throw new InternalErrorException(false, "unknown java preference key '" + key + "'");
        }

        /*
         * Get the Java preference value, or use the default.
         */
        result = javaPrefs.get(key, defaultValue);

        /*
         * We have no way of knowing if the Java preference was set before, so
         * unconditionally set it.
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
    public static void saveJavaPreference(String key, String value)
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
