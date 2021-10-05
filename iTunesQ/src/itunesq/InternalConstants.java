package itunesq;

/**
 * Class that contains internal constants that might need to be changed or
 * tweaked.
 * 
 * @author Jon
 *
 */
public class InternalConstants
{

    /**
     * Width and height of the main window activity indicator.
     */
    public static final int ACTIVITY_INDICATOR_SIZE = 50;
    
    /**
     * Characters that represent numeric scrolling for the alpha bar.
     */
    public static final char[] ALPHA_BAR_NUMERIC_CHARS = 
    	{
    	    
    	    /*
    	     * Numerals and all special characters on a standard keyboard are included.
    	     * These are the most rational ones.
    	     */
    		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '"', '\'', '#', '(', '.', '{', '[', '<', '*',
    		
    		/*
    		 * Somewhat irrational, but possible.
    		 */
    		'`', '~', '!', '@', '$', '%', '^', '&', ')', '-', '_', '=', '+', ']', '}', '\\', '|', ';', ':', ',', '>', '/', '?'
    	};
    
    /**
     * Padding of the alpha bar. 
     * 
     * This is hardcoded in the bxml, but is here to
     * highlight that we are dependent on the value.
     */
    public static final int ALPHA_BAR_PADDING = 15;

    /**
     * Width of the alpha bar.
     */
    public static final int ALPHA_BAR_WIDTH = 22 * 26;

    /**
     * Height of the scrollable area for the artist overrides dialogs.
     */
    public static final int ARTIST_OVERRIDES_SCROLLPANE_HEIGHT = 170;

    /**
     * Spacing of the labels for the info elements on the artists window.
     */
    public static final int ARTISTS_LABEL_SPACING = 50;

    /**
     * Default maximum log history.
     */
    public static final int DEFAULT_MAX_HISTORY = 30;

    /**
     * Width of the file save dialog.
     */
    public static final int FILE_SAVE_DIALOG_WIDTH = 500;

    /**
     * Width of the file name text input on the file save dialog.
     */
    public static final int FILE_SAVE_FILENAME_TEXT_SIZE = 64;

    /**
     * Separator for list items, for example lists of tracks. 
     * 
     * We pick something weird that should never occur in an actual list item. 
     * He says optimistically.
     */
    public static final String LIST_ITEM_SEPARATOR = "<@>";

    /**
     * Width of the column preferences labels.
     */
    public static final int PREFS_COLUMN_LABELS_WIDTH = 160;

    /**
     * Width of the log level labels on the preferences window.
     */
    public static final int PREFS_LOG_LEVEL_LABELS_WIDTH = 130;

    /**
     * Width of the log level spinner on the preferences window.
     */
    public static final int PREFS_LOG_LEVEL_SPINNER_WIDTH = 70;

    /**
     * Width of the skin spinner on the preferences window.
     */
    public static final int PREFS_SKIN_SPINNER_WIDTH = 120;

    /**
     * Height of the skin preview dialog.
     */
    public static final int SKIN_PREVIEW_DIALOG_HEIGHT = 280;
    
    /**
     * Width of the skin preview dialog.
     */
    public static final int SKIN_PREVIEW_DIALOG_WIDTH = 350;

    /**
     * Tooltip delay in milliseconds.
     */
    public static final int TOOLTIP_DELAY = 500;

    /**
     * Width of the track details label. This sets the separation between 
     * the track variables and their values.
     */
    public static final int TRACK_DETAILS_LABEL_WIDTH = 130;
}
