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
	
	/*
	 * Constants used in multiple places.
	 */
	public static final String DONE            = "Done";
	public static final String EDIT            = "Edit";
	public static final String EXIT            = "Exit";
	public static final String FILE            = "File";
	public static final String OPEN            = "Open";
	public static final String PREFS_RESET     = "Reset to Defaults";
	public static final String PREFERENCES     = "Preferences";
	public static final String PLAYLIST_NAME   = "Playlist Name";
	public static final String QUERY_TRACKS    = "Query Tracks";
	public static final String QUERY_PLAYLISTS = "Query Playlists";
	public static final String SAVE            = "Save";
	public static final String TRACK_NUMBER    = "Number of Tracks: ";
	
	/*
	 * Alert constants.
	 */
	public static final String ALERT_FILTER_ERROR = 
			"Filter error: ";
	public static final String ALERT_FILTER_TOO_FEW_ROWS = 
			"You need at least one filter row.";
	public static final String ALERT_LOG_HISTORY_VALUE = 
			"You must specify a positive numeric value for the log history.";
	public static final String ALERT_NO_ALTERNATE_NAMES = 
			"There are no alternate names for the artist you selected.";
	public static final String ALERT_NO_FILE_SELECTED = 
			"You didn't select a file.";
	public static final String ALERT_NO_PRINTER = 
			"Could not locate a printer service.";
	public static final String ALERT_NO_TRACKS = 
			"There are no tracks matching the set of filters.";
	public static final String ALERT_NO_XML_FILE = 
			"No XML file has been saved. Use the File -> Open menu to select a file.";
	public static final String ALERT_NOTHING_SAVED = 
			"You didn't select a destination, so nothing was saved.";
	public static final String ALERT_PLAYLIST_INVALID_NAME_1 = 
			"Playlist name '";
	public static final String ALERT_PLAYLIST_INVALID_NAME_2 = 
			"' is not valid.";
	public static final String ALERT_PLAYLIST_PREFS_TOO_FEW_ROWS = 
			"You need at least one playlist preference row.";
	public static final String ALERT_PLAYLIST_TOO_FEW_ROWS = 
			"You need at least two rows for playlist comparison.";
	public static final String ALERT_XML_FILE_ERROR = 
			"Unable to read and process XML file: ";
	
	/*
	 * ArtistDisplayColumns constants.
	 */
	public static final String ARTIST_COLUMN_LOCAL_TRACKS_HEADER = 
			"Total Local Tracks";
	public static final String ARTIST_COLUMN_LOCAL_TRACKS_NAME = 
			"TotalLocalTracks";
	public static final String ARTIST_COLUMN_NUM_ALTNAMES_HEADER = 
			"Number Alternate Names";
	public static final String ARTIST_COLUMN_NUM_ALTNAMES_NAME = 
			"NumAltNames";
	public static final String ARTIST_COLUMN_REMOTE_TRACKS_HEADER = 
			"Total Remote Tracks";
	public static final String ARTIST_COLUMN_REMOTE_TRACKS_NAME = 
			"TotalRemoteTracks";
	public static final String ARTIST_COLUMN_LOCAL_TIME_HEADER = 
			"Total Local Time";
	public static final String ARTIST_COLUMN_LOCAL_TIME_NAME = 
			"TotalLocalTime";
	public static final String ARTIST_COLUMN_REMOTE_TIME_HEADER = 
			"Total Remote Time";
	public static final String ARTIST_COLUMN_REMOTE_TIME_NAME = 
			"TotalRemoteTime";
	
	/*
	 * ArtistsWindow constants.
	 */
	public static final String ARTISTS_NUM_ARTISTS = 
			"Number of Artists: ";

	/*
	 * FindDuplicatesDialog constants.
	 */
	public static final String FIND_DUPLICATES_ALBUM = 
			"Same Album";
	public static final String FIND_DUPLICATES_ARTIST = 
			"Same Artist";
	public static final String FIND_DUPLICATES_DURATION = 
			"Same Duration";
	public static final String FIND_DUPLICATES_EXACT = 
			"Exact Match";
	public static final String FIND_DUPLICATES_KIND = 
			"Same Kind";
	public static final String FIND_DUPLICATES_NOT_ARTIST = 
			"Different Artist";
	public static final String FIND_DUPLICATES_RATING = 
			"Same Rating";
	public static final String FIND_DUPLICATES_SHOW = 
			"Show Duplicate Tracks";
	public static final String FIND_DUPLICATES_SPEC = 
			"Specify track search criteria";
	public static final String FIND_DUPLICATES_SPEC_TIP = 
			"Specify '" + FIND_DUPLICATES_EXACT + "' to match songs exactly."
			+ "\nTo find fuzzy duplicates, uncheck '" + FIND_DUPLICATES_EXACT 
			+ "' and specify only the track attributes you want to match.";
	public static final String FIND_DUPLICATES_YEAR = 
			"Same Year";

	/*
	 * FileSaveDialog constants.
	 */
	public static final String FILESAVE_ENTER_FILE_NAME = 
			"Enter file name";
	public static final String FILESAVE_HEADER = 
			"iTunesQ Query Results\n";
	public static final String FILESAVE_NAME_TIP = 
			"Specify a file name for saving the queried tracks.";
	public static final String FILESAVE_OPTIONS = 
			"Options";
	public static final String FILESAVE_PLAYLISTS_LIMIT = 
			"Include only queried playlists?";
	public static final String FILESAVE_PLAYLISTS_LIMIT_TIP = 
			"Select to include only playlists that you specified to be compared in the saved output.";
	public static final String FILESAVE_PRINT_TIP = 
			"Select to print the queried tracks.";
	public static final String FILESAVE_SAVE_TO_FILE = 
			"Save to File";
	public static final String FILESAVE_SAVE_TO_PRINTER = 
			"Save to Printer";
	public static final String FILESAVE_TRACKS_LIMIT = 
			"Exclude bypassed playlists?";
	public static final String FILESAVE_TRACKS_LIMIT_TIP = 
			"Select to exclude playlists that are bypassed from the saved output.";
	
	/*
	 * Filter constants.
	 */
	public static final String FILTER_LOGIC_ALL              = "All";
	public static final String FILTER_LOGIC_ANY              = "Any";
	public static final String FILTER_OPERATOR_CONTAINS      = "contains";
	public static final String FILTER_OPERATOR_GREATER       = "greater than or equal";
	public static final String FILTER_OPERATOR_IS            = "is";
	public static final String FILTER_OPERATOR_IS_NOT        = "is not";
	public static final String FILTER_OPERATOR_LESS          = "less than or equal";
	public static final String FILTER_SUBJECT_ARTIST         = "Artist";
	public static final String FILTER_SUBJECT_KIND           = "Kind";
	public static final String FILTER_SUBJECT_NAME           = "Name";
	public static final String FILTER_SUBJECT_PLAYLIST_COUNT = "Playlist Count";
	public static final String FILTER_SUBJECT_RATING         = "Rating";
	public static final String FILTER_SUBJECT_YEAR           = "Year";
	
	/*
	 * FilterCollection constants.
	 */
	public static final String FILTER_ERROR_BAD_OPERATOR  = " operator not applicable to ";
	public static final String FILTER_ERROR_COMPLEX_LOGIC = "filter logic is too complex";
	
	/*
	 * FiltersWindow constants.
	 */
	public static final String FILTER_COMPLEX = 
			"Complex";
	public static final String FILTER_COMPLEX_BUTTON_TIP = 
			"Switch the type of filter from '" + FILTER_LOGIC_ALL + "' to '" 
			+ FILTER_LOGIC_ANY + "' or vice versa. "
	    	+ "\nYou can only include 2 such switches in the set of filters.";
	public static final String FILTER_LOGIC_TIP = 
			"You can match '" + FILTER_LOGIC_ALL + "' or '" + FILTER_LOGIC_ANY + "' of the "
			+ "following filters.";
	public static final String FILTER_MINUS_BUTTON_TIP = 
			"Remove this filter.";
	public static final String FILTER_OPERATOR_TIP = 
			"What operator should be applied? "
	        + "Note that not all operators apply to all subjects."
	        + "\nFor example '" + FILTER_SUBJECT_ARTIST + "' '" + FILTER_OPERATOR_GREATER 
	        + "' does not make sense.";
	public static final String FILTER_PLUS_BUTTON_TIP = 
			"Add a new filter of the same type ('" + FILTER_LOGIC_ALL + "' or '" 
			+ FILTER_LOGIC_ANY + "').";
	public static final String FILTER_SHOW_ME_BUTTON =
			"Show Me";
	public static final String FILTER_SHOW_ME_BUTTON_TIP =
			"Show the result of applying the above set of filters.";
	public static final String FILTER_SUBJECT_TIP = 
			"What is the subject of this filter?";
	public static final String FILTER_TEXT_TIP = 
			"Enter the value that applies to the subject of this filter.";
	
	/*
	 * MainWindow constants.
	 */
	public static final String MAIN_QUERY_PLAYLISTS_TIP = 
			"Perform operations such as comparing playlist contents.";
	public static final String MAIN_QUERY_TRACKS_TIP = 
			"Use filters to select a group of tracks to display.";
	public static final String MAIN_VIEW_ARTISTS    = 
			"View Artists";
	public static final String MAIN_VIEW_ARTISTS_TIP = 
			"Show all artists.";
	public static final String MAIN_VIEW_PLAYLISTS = 
			"View Playlists";
	public static final String MAIN_VIEW_PLAYLISTS_TIP = 
			"Show all playlists.";
	public static final String MAIN_VIEW_TRACKS    = 
			"View Tracks";
	public static final String MAIN_VIEW_TRACKS_TIP = 
			"Show all tracks.";
	public static final String MAIN_XML_FILE_INFO  = 
			"Working XML File";
	public static final String MAIN_XML_FILE_STATS = 
			"XML File Statistics";
	
	/*
	 * PlaylistsWindow constants.
	 */
	public static final String PLAYLIST_NUMBER     = "Number of Playlists: ";
	public static final String PLAYLIST_TOTAL_TIME = "Total Time: ";
	
	/*
	 * PreferencesWindow constants ...
	 */
	
	/*
	 * ... first tab.
	 */
	public static final String PREFS_BYPASS_BORDER = 
			"Bypass Track Playlist Info For These Playlists";
	public static final String PREFS_BYPASS_INCLUDE = 
			"Include Children?";
	public static final String PREFS_BYPASS_INCLUDE_TIP = 
			"Should counts also be bypassed for children playlists?";
	public static final String PREFS_BYPASS_MINUS_BUTTON = 
			"Remove this playlist from the bypass counts list.";
	public static final String PREFS_BYPASS_PLUS_BUTTON = 
			"Add a new playlist for which counts should be bypassed.";
	public static final String PREFS_BYPASS_TIP = 
			"A list of playlists is accumulated for every track."
	        + "\nBut you can indicate that certain playlists, and optionally their children, "
	        + "should be bypassed."
	        + "\nBypassed playlists are not counted when filtering tracks based on the playlist count.";
	public static final String PREFS_IGNORED_BORDER = 
			"Ignore The Following Playlists";
	public static final String PREFS_IGNORED_MINUS_BUTTON = 
			"Remove this playlist from the ignored list.";
	public static final String PREFS_IGNORED_PLUS_BUTTON = 
			"Add a new playlist to be completely ignored.";
	public static final String PREFS_IGNORED_TIP = 
			"iTunes includes built-in playlists that might be "
        	+ "considered clutter."
        	+ "\nYou can ignore them if you want, and also add your own "
        	+ "playlists to completely ignore."
        	+ "\nThe default ignored playlists are automatically selected.";
	public static final String PREFS_TAB1_BUTTON = 
			"Playlist Management";
	public static final String PREFS_TAB1_RESET_TIP = 
			"Reset the ignored playlists to the default value.";
	
	/*
	 * ... second tab.
	 */
	public static final String PREFS_COLUMN_BORDER =
			"Track Display Columns";
	public static final String PREFS_COLUMN_FULL =
			"Full Tracks View:";
	public static final String PREFS_COLUMN_DUPLICATES =
			"Duplicate Tracks View:";
	public static final String PREFS_COLUMN_FILTERED =
			"Filtered List View:";
	public static final String PREFS_COLUMN_PLAYLIST =
			"Playlist View:";
	public static final String PREFS_COLUMN_TIP = 
			"Select the columns you want to be displayed when "
        	+ "showing all tracks, filtered tracks, and the tracks shown for a "
        	+ "selected playlist.";
	public static final String PREFS_SHOW_REMOTE_TRACKS = 
			"Show remote tracks:";
	public static final String PREFS_SHOW_REMOTE_TRACKS_TIP = 
			"iTunes can include tracks that are only available in the cloud, "
			+ "not in your local database. Check here to include such tracks.";
	public static final String PREFS_TAB2_BUTTON = 
			"Track Display";
	public static final String PREFS_TAB2_RESET_TIP = 
			"Reset the track display columns to the default value.";
	
	/*
	 * ... third tab.
	 */
	public static final String PREFS_FILTER_LOG_LEVEL = 
			"Filter Log Level:";
	public static final String PREFS_FILTER_LOG_LEVEL_TIP = 
			"This is the log level for the filter management component.";
	public static final String PREFS_GLOBAL = 
			"Apply To All?";
	public static final String PREFS_GLOBAL_LOG_LEVEL_TIP = 
			"This is the global log level, if '" + PREFS_GLOBAL + "' is checked.";
	public static final String PREFS_LOG_HISTORY_BORDER = 
			"Maximum Log History (days)";
	public static final String PREFS_LOG_HISTORY_TIP = 
			"Select the number of days to keep log file history.";
	public static final String PREFS_LOG_LEVEL_BORDER = 
			"Log Level";
	public static final String PREFS_LOG_LEVEL_TIP = 
			"Log levels determine how much information is logged for debugging purposes."
        	+ "\nThese should only be changed if directed by support personnel.";
	public static final String PREFS_PLAYLIST_LOG_LEVEL = 
			"Playlist Log Level:";
	public static final String PREFS_PLAYLIST_LOG_LEVEL_TIP = 
			"This is the log level for the playlist management component.";
	public static final String PREFS_SAVE_DIR_BORDER = 
			"Save Directory";
	public static final String PREFS_SAVE_DIR_TIP = 
			"Select the directory keeping files such as "
        	+ "user preferences and log files.";
	public static final String PREFS_SKIN_BORDER = 
			"Skin Name";
	public static final String PREFS_SKIN_PREVIEW = 
			"Preview";
	public static final String PREFS_SKIN_PREVIEW_BLACK = 
			"Black";
	public static final String PREFS_SKIN_PREVIEW_CLOSE_BUTTON = 
			"Close";
	public static final String PREFS_SKIN_PREVIEW_COLOR = 
			"Color";
	public static final String PREFS_SKIN_PREVIEW_DETERMINED = 
			"Determined";
	public static final String PREFS_SKIN_PREVIEW_FRIDAY = 
			"Friday";
	public static final String PREFS_SKIN_PREVIEW_GRATEFUL = 
			"Grateful";
	public static final String PREFS_SKIN_PREVIEW_GRAY = 
			"Gray";
	public static final String PREFS_SKIN_PREVIEW_GREEN = 
			"Green";
	public static final String PREFS_SKIN_PREVIEW_HAPPY = 
			"Happy";
	public static final String PREFS_SKIN_PREVIEW_HATEFUL = 
			"Hateful";
	public static final String PREFS_SKIN_PREVIEW_LIGHT_GREEN = 
			"Light Green";
	public static final String PREFS_SKIN_PREVIEW_MONDAY = 
			"Monday";
	public static final String PREFS_SKIN_PREVIEW_MOOD = 
			"Mood";
	public static final String PREFS_SKIN_PREVIEW_OPTIMISTIC = 
			"Optimistic";
	public static final String PREFS_SKIN_PREVIEW_RAINBOW = 
			"Rainbow!";
	public static final String PREFS_SKIN_PREVIEW_RELAXED = 
			"Relaxed";
	public static final String PREFS_SKIN_PREVIEW_SATURDAY = 
			"Saturday";
	public static final String PREFS_SKIN_PREVIEW_SILVER = 
			"Silver";
	public static final String PREFS_SKIN_PREVIEW_SOMBER = 
			"Somber";
	public static final String PREFS_SKIN_PREVIEW_SUNDAY = 
			"Sunday";
	public static final String PREFS_SKIN_PREVIEW_TEXT = 
			"Try typing a weekday here -> ";
	public static final String PREFS_SKIN_PREVIEW_THURSDAY = 
			"Thursday";
	public static final String PREFS_SKIN_PREVIEW_TUESDAY = 
			"Tuesday";
	public static final String PREFS_SKIN_PREVIEW_WEDNESDAY = 
			"Wednesday";
	public static final String PREFS_SKIN_PREVIEW_WEEKDAY = 
			"Weekday";
	public static final String PREFS_SKIN_PREVIEW_YELLOW = 
			"Yellow";
	public static final String PREFS_SKIN_TIP = 
			"Select a skin name and click the '" + PREFS_SKIN_PREVIEW + "' "
        	+ "button to see how it looks.";
	public static final String PREFS_TAB3_BUTTON = 
			"General";
	public static final String PREFS_TAB3_RESET_TIP = 
			"Reset all but the skin name to the default value.";
	public static final String PREFS_TRACK_LOG_LEVEL = 
			"Track Log Level:";
	public static final String PREFS_TRACK_LOG_LEVEL_TIP = 
			"This is the log level for the track management component.";
	public static final String PREFS_UI_LOG_LEVEL = 
			"UI Log Level:";
	public static final String PREFS_UI_LOG_LEVEL_TIP = 
			"This is the log level for the user interface component.";
	public static final String PREFS_XML_LOG_LEVEL = 
			"XML Log Level:";
	public static final String PREFS_XML_LOG_LEVEL_TIP = 
			"This is the log level for the XML management component.";
	
	/*
	 * QueryPlaylistsWindow constants.
	 */
	public static final String QUERY_PLAYLIST_COMPARE_ALL  = 
			"(show all)";
	public static final String QUERY_PLAYLIST_COMPARE_BORDER  = 
			"Enter Two Or More Playlists To Compare";
	public static final String QUERY_PLAYLIST_COMPARE_ONE  = 
			"(show one)";
	public static final String QUERY_PLAYLIST_COMPARE_SOME = 
			"(show some)";
	public static final String QUERY_PLAYLIST_MINUS_BUTTON = 
			"Remove this playlist from the comparison list.";
	public static final String QUERY_PLAYLIST_PLUS_BUTTON  = 
			"Add a new playlist for comparison.";
	public static final String QUERY_PLAYLIST_SHOW_ALL_BUTTON  = 
			"Show All";
	public static final String QUERY_PLAYLIST_SHOW_ALL_BUTTON_TIP  = 
			"Show tracks belonging to all of the above playlists.";
	public static final String QUERY_PLAYLIST_SHOW_ONE_BUTTON  = 
			"Show One";
	public static final String QUERY_PLAYLIST_SHOW_ONE_BUTTON_TIP  = 
			"Show tracks belonging to only one of the above playlists.";
	public static final String QUERY_PLAYLIST_SHOW_SOME_BUTTON  = 
			"Show Some";
	public static final String QUERY_PLAYLIST_SHOW_SOME_BUTTON_TIP  = 
			"Show tracks belonging to some of the above playlists.";
	
	/*
	 * Skins constants.
	 */
	public static final String SKIN_NAME_DUSKY         = "Dusky Gray";
	public static final String SKIN_NAME_PUMPKIN       = "Pumpkin Patch";
	public static final String SKIN_NAME_SEASIDE       = "Seaside Daze";
	public static final String SKIN_WINDOW_ALTNAMES    = "Alternate Names";
	public static final String SKIN_WINDOW_ARTISTS     = "Artists";
	public static final String SKIN_WINDOW_FINDDUPS    = "Find Duplicates";
	public static final String SKIN_WINDOW_FILESAVE    = "File Save";
	public static final String SKIN_WINDOW_MAIN        = "iTunes Query Tool";
	public static final String SKIN_WINDOW_PLAYLISTS   = "Playlists";
	public static final String SKIN_WINDOW_PREFERENCES = "Preferences";
	public static final String SKIN_WINDOW_SKINPREVIEW = "Skin Preview";
	public static final String SKIN_WINDOW_TRACKINFO   = "Track Details";
	public static final String SKIN_WINDOW_TRACKS      = "Tracks";
	
	/*
	 * TrackDisplayColumns constants.
	 */
	public static final String TRACK_COLUMN_ADDED      = "Added";
	public static final String TRACK_COLUMN_ALBUM      = "Album";
	public static final String TRACK_COLUMN_ARTIST     = "Artist";
	public static final String TRACK_COLUMN_BITRATE    = "Bit Rate";
	public static final String TRACK_COLUMN_COMPOSER   = "Composer";
	public static final String TRACK_COLUMN_DURATION   = "Duration";
	public static final String TRACK_COLUMN_GENRE      = "Genre";
	public static final String TRACK_COLUMN_ID         = "ID";
	public static final String TRACK_COLUMN_KIND       = "Kind";
	public static final String TRACK_COLUMN_MODIFIED   = "Modified";
	public static final String TRACK_COLUMN_NAME       = "Name";
	public static final String TRACK_COLUMN_NUMBER     = "Number";
	public static final String TRACK_COLUMN_PLAYCOUNT  = "Play Count";
	public static final String TRACK_COLUMN_RATING     = "Rating";
	public static final String TRACK_COLUMN_RELEASED   = "Released";
	public static final String TRACK_COLUMN_REMOTE     = "Remote";
	public static final String TRACK_COLUMN_SAMPLERATE = "Sample Rate";
	public static final String TRACK_COLUMN_SIZE       = "Size";
	public static final String TRACK_COLUMN_YEAR       = "Year";
	
	/*
	 * TracksWindow constants.
	 */
	public static final String TRACK_PLAYLISTS_BYPASS_COLUMN = "Bypassed";
	public static final String TRACK_PLAYLISTS_NAME_COLUMN   = "Playlist Names";
	public static final String TRACK_QUERY_DUPLICATES        = "Duplicate tracks";
	public static final String TRACK_QUERY_PLAYLISTS         = "Query playlists";
	public static final String TRACK_QUERY_TRACKS            = "Query tracks";
	
	/*
	 * Utilities constants.
	 */
	public static final String UTILITY_NUMARTISTS   = "Number of artists: ";
	public static final String UTILITY_NUMPLAYLISTS = "Number of playlists: ";
	public static final String UTILITY_NUMTRACKS    = "Number of tracks: ";
	public static final String UTILITY_UNKNOWN_DATE = "--unknown--";
	public static final String UTILITY_XMLFILE_DATE = ", dated ";
}