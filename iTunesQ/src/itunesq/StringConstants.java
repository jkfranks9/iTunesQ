package itunesq;

/**
 * Class that contains text visible to the user.
 * <p>
 * This does not include items such as:
 * <ul>
 * <li>log messages</li>
 * <li>exception text</li>
 * <li>strings found in the iTunes XML file</li>
 * <li>simple constants such as newline or "/"</li>
 * </ul>
 * 
 * @author Jon
 *
 */
public class StringConstants
{
    public static final String lineSeparator = System.lineSeparator();

    /*
     * Constants used in multiple places.
     */
    public static final String BYPASS_INCLUDE = "Include Children?";
    public static final String CANCEL = "Cancel";
    public static final String DONE = "Done";
    public static final String EDIT = "Edit";
    public static final String EXCLUDE_BYPASSED = "Exclude Bypassed Playlists?";
    public static final String EXCLUDE_BYPASSED_TIP = 
            "Check to exclude bypassed playlists from the list of results.";
    public static final String EXIT = "Exit";
    public static final String FILE = "File";
    public static final String OPEN = "Open";
    public static final String PLAYLIST_NAME = "Playlist Name";
    public static final String PLAYLIST_NUMBER = "Number of Playlists: ";
    public static final String PREFS_RESET = "Reset to Defaults";
    public static final String PREFERENCES = "Preferences";
    public static final String PROCEED = "Proceed";
    public static final String QUERY_PLAYLISTS = "Query Playlists";
    public static final String QUERY_TRACKS = "Query Tracks";
    public static final String SAVE = "Save";
    public static final String TRACK_NUMBER = "Number of Tracks: ";

    /*
     * ArtistDisplayColumns constants.
     */
    public static final String ARTIST_COLUMN_LOCAL_TIME = "Total Local Time";
    public static final String ARTIST_COLUMN_LOCAL_TRACKS = "Total Local Tracks";
    public static final String ARTIST_COLUMN_NUM_ALTNAMES = "Number Alternate Names";
    public static final String ARTIST_COLUMN_REMOTE_TIME = "Total Remote Time";
    public static final String ARTIST_COLUMN_REMOTE_TRACKS = "Total Remote Tracks";

    /*
     * ArtistsWindow constants.
     */
    public static final String ARTISTS_ALTSELECT_INSTRUCTIONS = 
            "Select the primary artist with which to combine the others.";
    public static final String ARTISTS_ALTSELECT_WARNING = 
            "Do you want to proceed? You have a lot of selections or dissimilar names.";
    public static final String ARTISTS_NUM_ARTISTS = "Number of Artists: ";
    public static final String ARTISTS_REMOVE_ALTNAME_BUTTON = "Remove Alternate Name";
    public static final String ARTISTS_REMOVE_ALTNAME_BUTTON_TIP = "Manually remove artist alternate name.";
    public static final String ARTISTS_REMOVE_INSTRUCTIONS = 
            "Select artists you want to remove as alternate names.";
    public static final String ARTISTS_REVIEW_OVERRIDES_AUTOMATIC = 
            ArtistAlternateNameOverride.OverrideType.AUTOMATIC.toString()
            + " overrides indicate automatically set alternate names that have been removed.";
    public static final String ARTISTS_REVIEW_OVERRIDES_BUTTON = "Review Artist Overrides";
    public static final String ARTISTS_REVIEW_OVERRIDES_BUTTON_TIP = 
            "Review artist alternate name overrides.";
    public static final String ARTISTS_REVIEW_OVERRIDES_MANUAL = 
            ArtistAlternateNameOverride.OverrideType.MANUAL.toString()
            + " overrides indicate manually set alternate names that have been added.";
    public static final String ARTISTS_SET_ALTNAME_BUTTON = "Set Alternate Name";
    public static final String ARTISTS_SET_ALTNAME_BUTTON_TIP = "Manually set artist alternate name.";
    public static final String ARTISTS_SET_INSTRUCTIONS = 
            "Select one or more artists to manually modify alternate artist names.";

    /*
     * FindDuplicatesDialog constants.
     */
    public static final String FIND_DUPLICATES_ALBUM = "Same Album";
    public static final String FIND_DUPLICATES_ARTIST = "Same Artist";
    public static final String FIND_DUPLICATES_DURATION = "Same Duration";
    public static final String FIND_DUPLICATES_EXACT = "Exact Match";
    public static final String FIND_DUPLICATES_EXCLUDE_LIVE = "Exclude Live";
    public static final String FIND_DUPLICATES_EXCLUDE_USER = "Exclude User";
    public static final String FIND_DUPLICATES_EXCLUDE_VIDEO = "Exclude Video";
    public static final String FIND_DUPLICATES_KIND = "Same Kind";
    public static final String FIND_DUPLICATES_NOT_ARTIST = "Different Artist";
    public static final String FIND_DUPLICATES_RATING = "Same Rating";
    public static final String FIND_DUPLICATES_SPEC = "Specify track search criteria";
    public static final String FIND_DUPLICATES_SPEC_TIP = "Specify '" + FIND_DUPLICATES_EXACT
            + "' to match songs exactly." + lineSeparator
            + "To find fuzzy duplicates, uncheck '"
            + FIND_DUPLICATES_EXACT + "' and specify only the track attributes you want to match.";
    public static final String FIND_DUPLICATES_YEAR = "Same Year";

    /*
     * FileSaveDialog constants.
     */
    public static final String FILESAVE_ENTER_FILE_NAME = "Enter file name";
    public static final String FILESAVE_HEADER = "iTunesQ Query Results" + lineSeparator;
    public static final String FILESAVE_NAME_TIP =
            "Specify a file name for saving the queried tracks.";
    public static final String FILESAVE_OPTIONS = "Options";
    public static final String FILESAVE_PLAYLISTS_LIMIT = "Include only compared playlists?";
    public static final String FILESAVE_PLAYLISTS_LIMIT_TIP =
            "Select to include only playlists that you specified to be compared in the saved output.";
    public static final String FILESAVE_PRINT_TIP = "Select to print the queried tracks.";
    public static final String FILESAVE_SAVE_TO_FILE = "Save to File";
    public static final String FILESAVE_SAVE_TO_PRINTER = "Save to Printer";
    public static final String FILESAVE_TRACKS_LIMIT = "Exclude bypassed playlists?";
    public static final String FILESAVE_TRACKS_LIMIT_TIP =
            "Select to exclude playlists that are bypassed from the saved output.";

    /*
     * Filter constants.
     */
    public static final String FILTER_LOGIC_ALL = "All";
    public static final String FILTER_LOGIC_ANY = "Any";
    public static final String FILTER_OPERATOR_CONTAINS = "contains";
    public static final String FILTER_OPERATOR_GREATER = "greater than or equal";
    public static final String FILTER_OPERATOR_IS = "is";
    public static final String FILTER_OPERATOR_IS_NOT = "is not";
    public static final String FILTER_OPERATOR_LESS = "less than or equal";
    public static final String FILTER_SUBJECT_ARTIST = "Artist";
    public static final String FILTER_SUBJECT_KIND = "Kind";
    public static final String FILTER_SUBJECT_NAME = "Name";
    public static final String FILTER_SUBJECT_PLAYLIST_COUNT = "Playlist Count";
    public static final String FILTER_SUBJECT_RATING = "Rating";
    public static final String FILTER_SUBJECT_YEAR = "Year";

    /*
     * FilterCollection constants.
     */
    public static final String FILTER_ERROR_BAD_OPERATOR = " operator not applicable to ";
    public static final String FILTER_ERROR_COMPLEX_LOGIC = "filter logic is too complex";

    /*
     * FiltersWindow constants.
     */
    public static final String FILTER_COMPLEX = "Complex";
    public static final String FILTER_COMPLEX_BUTTON_TIP = "Switch the type of filter from '"
            + FILTER_LOGIC_ALL + "' to '" + FILTER_LOGIC_ANY + "' or vice versa." + lineSeparator
            + "You can only include 2 such switches in the set of filters.";
    public static final String FILTER_LOGIC_TIP = "You can match '" + FILTER_LOGIC_ALL + "' or '"
            + FILTER_LOGIC_ANY + "' of the following filters.";
    public static final String FILTER_MINUS_BUTTON_TIP = "Remove this filter.";
    public static final String FILTER_OPERATOR_TIP = "What operator should be applied? "
            + "Note that not all operators apply to all subjects." + lineSeparator
            + "For example '"
            + FILTER_SUBJECT_ARTIST + "' '" + FILTER_OPERATOR_GREATER + "' does not make sense.";
    public static final String FILTER_PLUS_BUTTON_TIP = "Add a new filter of the same type ('"
            + FILTER_LOGIC_ALL + "' or '" + FILTER_LOGIC_ANY + "').";
    public static final String FILTER_SHOW_ME_BUTTON = "Show Me";
    public static final String FILTER_SHOW_ME_BUTTON_TIP =
            "Show the result of applying the above set of filters.";
    public static final String FILTER_SUBJECT_TIP = "What is the subject of this filter?";
    public static final String FILTER_TEXT_TIP =
            "Enter the value that applies to the subject of this filter.";

    /*
     * ListQueryType constants.
     */
    public static final String LIST_TYPE_PLAYLIST_FAMILY = "Playlist family expansion playlists";
    public static final String LIST_TYPE_TRACK_COMPARE = "Playlist comparison";
    public static final String LIST_TYPE_TRACK_DUPLICATES = "Duplicate tracks";
    public static final String LIST_TYPE_TRACK_FAMILY = "Playlist family expansion tracks";
    public static final String LIST_TYPE_TRACK_QUERY = "Query tracks";

    /*
     * MainWindow constants.
     */
    public static final String MAIN_QUERY_PLAYLISTS_TIP =
            "Perform operations such as comparing playlist contents.";
    public static final String MAIN_QUERY_TRACKS_TIP =
            "Use filters to select a group of tracks to display.";
    public static final String MAIN_VIEW_ARTISTS = "View Artists";
    public static final String MAIN_VIEW_ARTISTS_TIP = "Show all artists.";
    public static final String MAIN_VIEW_PLAYLISTS = "View Playlists";
    public static final String MAIN_VIEW_PLAYLISTS_TIP = "Show all playlists.";
    public static final String MAIN_VIEW_TRACKS = "View Tracks";
    public static final String MAIN_VIEW_TRACKS_TIP = "Show all tracks.";
    public static final String MAIN_XML_FILE_INFO = "Working XML File";
    public static final String MAIN_XML_FILE_STATS = "XML File Statistics";
    
    /*
     * PlaylistDisplayColumns constants.
     */
    public static final String PLAYLIST_COLUMN_BYPASSED = "Bypassed";
    public static final String PLAYLIST_COLUMN_NUM_TRACKS = "Number Tracks";
    public static final String PLAYLIST_COLUMN_PLAYLIST_NAMES = "Playlist Names";
    public static final String PLAYLIST_COLUMN_TRACK_NAMES = "Track Names";

    /*
     * PlaylistsWindow constants.
     */
    public static final String PLAYLIST_TOTAL_TIME = "Total Time: ";

    /*
     * PreferencesWindow constants ...
     */

    /*
     * ... first tab.
     */
    public static final String PREFS_BYPASS_BORDER =
            "Bypass Track Playlist Info For These Playlists";
    public static final String PREFS_BYPASS_INCLUDE_TIP =
            "Should counts also be bypassed for children playlists?";
    public static final String PREFS_BYPASS_MINUS_BUTTON =
            "Remove this playlist from the bypass counts list.";
    public static final String PREFS_BYPASS_PLUS_BUTTON =
            "Add a new playlist for which counts should be bypassed.";
    public static final String PREFS_BYPASS_TIP =
            "A list of playlists is accumulated for every track." + lineSeparator
            + "But you can indicate that certain playlists, and optionally their children, "
            + "should be bypassed." + lineSeparator
            + "Bypassed playlists are not counted when filtering tracks based on the playlist count.";
    public static final String PREFS_DUPLICATE_EXCLUSIONS_BORDER = "Duplicate tracks album keyword exclusions";
    public static final String PREFS_DUPLICATE_EXCLUSIONS_MINUS_BUTTON = "Remove this duplicate track exclusion.";
    public static final String PREFS_DUPLICATE_EXCLUSIONS_NAME = "Exclusion Name";
    public static final String PREFS_DUPLICATE_EXCLUSIONS_PLUS_BUTTON = "Add a new duplicate track exclusion.";
    public static final String PREFS_DUPLICATE_EXCLUSIONS_TIP = 
    		"Specify keywords for album names that you want to exclude when showing duplicate tracks." + lineSeparator
            + "For example, album names including 'anthology' probably contain live tracks or alternate versions "
            + "that you want to not be considered duplicates.";
    public static final String PREFS_IGNORED_BORDER = "Ignore The Following Playlists";
    public static final String PREFS_IGNORED_MINUS_BUTTON =
            "Remove this playlist from the ignored list.";
    public static final String PREFS_IGNORED_PLUS_BUTTON =
            "Add a new playlist to be completely ignored.";
    public static final String PREFS_IGNORED_TIP =
            "iTunes includes built-in playlists that might be considered clutter." + lineSeparator
            + "You can ignore them if you want, and also add your own "
            + "playlists to completely ignore." + lineSeparator
            + "The default ignored playlists are automatically populated, unless you've made changes.";
    public static final String PREFS_TAB1_BUTTON = "Playlist Management";
    public static final String PREFS_TAB1_RESET_TIP =
            "Reset the ignored playlists to the default value.";

    /*
     * ... second tab.
     */
    public static final String PREFS_COLUMN_BORDER = "Track Display Columns";
    public static final String PREFS_COLUMN_DUPLICATES = "Duplicate Tracks View:";
    public static final String PREFS_COLUMN_FILTERED = "Filtered List View:";
    public static final String PREFS_COLUMN_FULL = "Full Tracks View:";
    public static final String PREFS_COLUMN_PLAYLIST = "Playlist View:";
    public static final String PREFS_COLUMN_TIP =
            "Select the columns you want to be displayed for various types of track lists.";
    public static final String PREFS_SHOW_REMOTE_TRACKS = "Show remote tracks:";
    public static final String PREFS_SHOW_REMOTE_TRACKS_TIP =
            "iTunes can include tracks that are only available in the cloud, "
            + "not in your local database. Check here to include such tracks.";
    public static final String PREFS_TAB2_BUTTON = "Track Display";
    public static final String PREFS_TAB2_RESET_TIP =
            "Reset the track display choices to the default value.";

    /*
     * ... third tab.
     */
    public static final String PREFS_ARTIST_LOG_LEVEL = "Artist Log Level:";
    public static final String PREFS_ARTIST_LOG_LEVEL_TIP =
            "This is the log level for the artist management component.";
    public static final String PREFS_FILTER_LOG_LEVEL = "Filter Log Level:";
    public static final String PREFS_FILTER_LOG_LEVEL_TIP =
            "This is the log level for the filter management component.";
    public static final String PREFS_GLOBAL = "Apply To All?";
    public static final String PREFS_GLOBAL_LOG_LEVEL_TIP =
            "This is the global log level, if '" + PREFS_GLOBAL + "' is checked.";
    public static final String PREFS_LOG_HISTORY_BORDER = "Maximum Log History";
    public static final String PREFS_LOG_HISTORY_TIP =
            "Select the number of logs to keep in the log file history.";
    public static final String PREFS_LOG_LEVEL_BORDER = "Log Level";
    public static final String PREFS_LOG_LEVEL_TIP =
            "Log levels determine how much information is logged for debugging purposes." + lineSeparator
            + "These should only be changed if directed by support personnel.";
    public static final String PREFS_PLAYLIST_LOG_LEVEL = "Playlist Log Level:";
    public static final String PREFS_PLAYLIST_LOG_LEVEL_TIP =
            "This is the log level for the playlist management component.";
    public static final String PREFS_SAVE_DIR_BORDER = "Save Directory";
    public static final String PREFS_SAVE_DIR_TIP =
            "Select the directory keeping files such as user preferences and logs.";
    public static final String PREFS_SKIN_BORDER = "Skin Name";
    public static final String PREFS_SKIN_PREVIEW = "Preview";
    public static final String PREFS_SKIN_PREVIEW_BLACK = "Black";
    public static final String PREFS_SKIN_PREVIEW_CLOSE_BUTTON = "Close";
    public static final String PREFS_SKIN_PREVIEW_COLOR = "Color";
    public static final String PREFS_SKIN_PREVIEW_DETERMINED = "Determined";
    public static final String PREFS_SKIN_PREVIEW_FRIDAY = "Friday";
    public static final String PREFS_SKIN_PREVIEW_GRATEFUL = "Grateful";
    public static final String PREFS_SKIN_PREVIEW_GRAY = "Gray";
    public static final String PREFS_SKIN_PREVIEW_GREEN = "Green";
    public static final String PREFS_SKIN_PREVIEW_HAPPY = "Happy";
    public static final String PREFS_SKIN_PREVIEW_HATEFUL = "Hateful";
    public static final String PREFS_SKIN_PREVIEW_LIGHT_GREEN = "Light Green";
    public static final String PREFS_SKIN_PREVIEW_MONDAY = "Monday";
    public static final String PREFS_SKIN_PREVIEW_MOOD = "Mood";
    public static final String PREFS_SKIN_PREVIEW_OPTIMISTIC = "Optimistic";
    public static final String PREFS_SKIN_PREVIEW_RAINBOW = "Rainbow!";
    public static final String PREFS_SKIN_PREVIEW_RELAXED = "Relaxed";
    public static final String PREFS_SKIN_PREVIEW_SATURDAY = "Saturday";
    public static final String PREFS_SKIN_PREVIEW_SILVER = "Silver";
    public static final String PREFS_SKIN_PREVIEW_SOMBER = "Somber";
    public static final String PREFS_SKIN_PREVIEW_SUNDAY = "Sunday";
    public static final String PREFS_SKIN_PREVIEW_TEXT = "Try typing a weekday here -> ";
    public static final String PREFS_SKIN_PREVIEW_THURSDAY = "Thursday";
    public static final String PREFS_SKIN_PREVIEW_TUESDAY = "Tuesday";
    public static final String PREFS_SKIN_PREVIEW_WEDNESDAY = "Wednesday";
    public static final String PREFS_SKIN_PREVIEW_WEEKDAY = "Weekday";
    public static final String PREFS_SKIN_PREVIEW_YELLOW = "Yellow";
    public static final String PREFS_SKIN_TIP = "Select a skin name and click the '"
            + PREFS_SKIN_PREVIEW + "' button to see how it looks.";
    public static final String PREFS_TAB3_BUTTON = "General";
    public static final String PREFS_TAB3_RESET_TIP =
            "Reset all but the skin name to the default value.";
    public static final String PREFS_TRACK_LOG_LEVEL = "Track Log Level:";
    public static final String PREFS_TRACK_LOG_LEVEL_TIP =
            "This is the log level for the track management component.";
    public static final String PREFS_UI_LOG_LEVEL = "UI Log Level:";
    public static final String PREFS_UI_LOG_LEVEL_TIP =
            "This is the log level for the user interface component.";
    public static final String PREFS_XML_LOG_LEVEL = "XML Log Level:";
    public static final String PREFS_XML_LOG_LEVEL_TIP =
            "This is the log level for the XML management component.";

    /*
     * QueryPlaylistsWindow constants.
     */
    public static final String QUERY_PLAYLIST_BYPASS_INCLUDE_TIP =
            "Should child playlists also be included or excluded?";
    public static final String QUERY_PLAYLIST_COMPARE_ALL = "(show all)";
    public static final String QUERY_PLAYLIST_COMPARE_BORDER =
            "Enter Two Or More Playlists To Compare";
    public static final String QUERY_PLAYLIST_COMPARE_ONE = "(show one)";
    public static final String QUERY_PLAYLIST_COMPARE_SOME = "(show some)";
    public static final String QUERY_PLAYLIST_COMPARE_RECURSIVE = "(show recursive)";
    public static final String QUERY_PLAYLIST_FAMILY =
            "Enter Playlist To Expand With Family Playlists";
    public static final String QUERY_PLAYLIST_FAMILY_PLAYLISTS = "Expand Family To Playlists";
    public static final String QUERY_PLAYLIST_FAMILY_PLAYLISTS_TIP =
            "Expand the entered playlist into a list of playlists.";
    public static final String QUERY_PLAYLIST_FAMILY_TRACKS = "Expand Family To Tracks";
    public static final String QUERY_PLAYLIST_FAMILY_TRACKS_TIP =
            "Expand the entered playlist into a list of tracks.";
    public static final String QUERY_PLAYLIST_MINUS_BUTTON =
            "Remove this playlist from the comparison list.";
    public static final String QUERY_PLAYLIST_RECURSIVE_COMPARE_ALL = "Compare All Playlists?";
    public static final String QUERY_PLAYLIST_RECURSIVE_COMPARE_ALL_TIP = 
            "Check to include all playlists except those listed.";
    public static final String QUERY_PLAYLIST_RECURSIVE_COMPARE_BORDER =
            "Enter Two Or More Playlists To Recursively Compare (These Are Exclusions If All Is Checked)";
    public static final String QUERY_PLAYLIST_PLUS_BUTTON = "Add a new playlist for comparison.";
    public static final String QUERY_PLAYLIST_SHOW_ALL_BUTTON = "Show All";
    public static final String QUERY_PLAYLIST_SHOW_ALL_BUTTON_TIP =
            "Show tracks belonging to all of the above playlists.";
    public static final String QUERY_PLAYLIST_SHOW_BUTTON = "Show";
    public static final String QUERY_PLAYLIST_SHOW_BUTTON_TIP =
            "Recursively compare all of the above playlists.";
    public static final String QUERY_PLAYLIST_SHOW_ONE_BUTTON = "Show One";
    public static final String QUERY_PLAYLIST_SHOW_ONE_BUTTON_TIP =
            "Show tracks belonging to only one of the above playlists.";
    public static final String QUERY_PLAYLIST_SHOW_SOME_BUTTON = "Show Some";
    public static final String QUERY_PLAYLIST_SHOW_SOME_BUTTON_TIP =
            "Show tracks belonging to some of the above playlists.";

    /*
     * Skins constants.
     */
    public static final String SKIN_NAME_DUSKY = "Dusky Gray";
    public static final String SKIN_NAME_PUMPKIN = "Pumpkin Patch";
    public static final String SKIN_NAME_SEASIDE = "Seaside Daze";
    public static final String SKIN_WINDOW_ALT_NAMES = "Alternate Names";
    public static final String SKIN_WINDOW_ARTIST_OVERRIDES = "Artist Overrides";
    public static final String SKIN_WINDOW_ARTISTS = "Artists";
    public static final String SKIN_WINDOW_FAMILY_PLAYLISTS = "Family Expansion Playlists";
    public static final String SKIN_WINDOW_FILE_SAVE = "File Save";
    public static final String SKIN_WINDOW_FIND_DUPLICATES = "Find Duplicates";
    public static final String SKIN_WINDOW_MAIN = "iTunes Query Tool";
    public static final String SKIN_WINDOW_PLAYLISTS = "Playlists";
    public static final String SKIN_WINDOW_PREFERENCES = "Preferences";
    public static final String SKIN_WINDOW_REMOVE_ALT_NAME_SELECTION = "Remove Alternate Name Selection";
    public static final String SKIN_WINDOW_SET_ALT_NAME_SELECTION = "Set Alternate Name Selection";
    public static final String SKIN_WINDOW_SKIN_PREVIEW = "Skin Preview";
    public static final String SKIN_WINDOW_TRACK_INFO = "Track Details";
    public static final String SKIN_WINDOW_TRACKS = "Tracks";

    /*
     * TrackDisplayColumns constants.
     */
    public static final String TRACK_COLUMN_ADDED = "Added";
    public static final String TRACK_COLUMN_ALBUM = "Album";
    public static final String TRACK_COLUMN_ARTIST = "Artist";
    public static final String TRACK_COLUMN_BITRATE = "Bit Rate";
    public static final String TRACK_COLUMN_COMPOSER = "Composer";
    public static final String TRACK_COLUMN_DURATION = "Duration";
    public static final String TRACK_COLUMN_GENRE = "Genre";
    public static final String TRACK_COLUMN_ID = "ID";
    public static final String TRACK_COLUMN_KIND = "Kind";
    public static final String TRACK_COLUMN_MODIFIED = "Modified";
    public static final String TRACK_COLUMN_NAME = "Name";
    public static final String TRACK_COLUMN_NUMBER = "Number";
    public static final String TRACK_COLUMN_NUMPLAYLISTS = "Number Playlists";
    public static final String TRACK_COLUMN_PLAYCOUNT = "Play Count";
    public static final String TRACK_COLUMN_RATING = "Rating";
    public static final String TRACK_COLUMN_RELEASED = "Released";
    public static final String TRACK_COLUMN_REMOTE = "Remote";
    public static final String TRACK_COLUMN_SAMPLERATE = "Sample Rate";
    public static final String TRACK_COLUMN_SIZE = "Size";
    public static final String TRACK_COLUMN_YEAR = "Year";

    /*
     * TracksWindow constants.
     */
    public static final String TRACK_SHOW_DUPLICATES = "Show Duplicate Tracks";
    public static final String TRACK_SHOW_DUPLICATES_TIP = 
            "Show duplicate tracks using exact or fuzzy matching.";

    /*
     * Utilities constants.
     */
    public static final String UTILITY_NUMARTISTS = "Number of artists: ";
    public static final String UTILITY_NUMPLAYLISTS = "Number of playlists: ";
    public static final String UTILITY_NUMTRACKS = "Number of tracks: ";
    public static final String UTILITY_UNKNOWN_DATE = "--unknown--";
    public static final String UTILITY_XMLFILE_DATE = ", dated ";

    /*
     * Alert constants. These are last so they can reference other elements such as buttons.
     */
    public static final String ALERT_CANT_REMOVE_ALTERNATE =
            "One or more alternate names you selected to be removed could not be processed, "
            + "because the names are too similar and would result in chaos.";
    public static final String ALERT_DUPLICATE_TRACK_EXCLUSIONS_TOO_FEW_ROWS =
            "You need at least one duplicate track exclusion row.";
    public static final String ALERT_FATAL_ERROR =
            "A fatal error has occurred. The application will now close.";
    public static final String ALERT_FILE_IS_DIRECTORY = "The file you specified is a directory.";
    public static final String ALERT_FILE_NOT_WRITABLE = "The file you specified is not writable.";
    public static final String ALERT_FILTER_ERROR = "Filter error: ";
    public static final String ALERT_FILTER_TOO_FEW_ROWS = "You need at least one filter row.";
    public static final String ALERT_LOG_HISTORY_VALUE =
            "You must specify a positive numeric value for the log history.";
    public static final String ALERT_NO_ALTERNATE_NAMES =
            "There are no alternate names for the artist you selected.";
    public static final String ALERT_NO_FAMILY_PLAYLIST = 
            "You did not enter a family playlist for expansion";
    public static final String ALERT_NO_FILE_SELECTED = "You didn't select a file.";
    public static final String ALERT_NO_PRINTER = "Could not locate a printer service.";
    public static final String ALERT_NO_TRACKS = "There are no tracks matching the set of filters.";
    public static final String ALERT_NO_XML_FILE =
            "No XML file has been saved. Use the File -> Open menu to select a file.";
    public static final String ALERT_NON_FATAL_ERROR =
            "An internal error has occurred, but it is not severe enough to close the application.";
    public static final String ALERT_NOTHING_SAVED =
            "You didn't select a destination, so nothing was saved.";
    public static final String ALERT_PLAYLIST_INVALID_NAME_1 = "Playlist name '";
    public static final String ALERT_PLAYLIST_INVALID_NAME_2 = "' is not valid.";
    public static final String ALERT_PLAYLIST_PREFS_TOO_FEW_ROWS =
            "You need at least one playlist preference row.";
    public static final String ALERT_PLAYLIST_TOO_FEW_ROWS =
            "You need at least two rows for playlist comparison.";
    public static final String ALERT_PRINT_FAILED = "There was an error trying to print.";
    public static final String ALERT_XML_FILE_ERROR = "Unable to read and process XML file: ";
}