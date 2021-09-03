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

    /*
     * Width and height of the main window activity indicator.
     */
    public static final int ACTIVITY_INDICATOR_SIZE = 50;

    /*
     * Spacing of the labels for the info elements on the artists window.
     */
    public static final int ARTISTS_LABEL_SPACING = 50;

    /*
     * Default maximum log history.
     */
    public static final int DEFAULT_MAX_HISTORY = 30;

    /*
     * Width of the file save dialog.
     */
    public static final int FILE_SAVE_DIALOG_WIDTH = 500;

    /*
     * Width of the file name text input on the file save dialog.
     */
    public static final int FILE_SAVE_FILENAME_TEXT_SIZE = 64;

    /*
     * Separator for list items, for example lists of tracks. We pick something weird that
     * should never occur in an actual list item. He says optimistically.
     */
    public static final String LIST_ITEM_SEPARATOR = "<@>";

    /*
     * Height of the scrollable area for the artist overrides dialogs.
     */
    public static final int ARTIST_OVERRIDES_SCROLLPANE_HEIGHT = 170;

    /*
     * Width of the column preferences labels.
     */
    public static final int PREFS_COLUMN_LABELS_WIDTH = 160;

    /*
     * Width of the log level labels on the preferences window.
     */
    public static final int PREFS_LOG_LEVEL_LABELS_WIDTH = 130;

    /*
     * Width of the log level spinner on the preferences window.
     */
    public static final int PREFS_LOG_LEVEL_SPINNER_WIDTH = 70;

    /*
     * Width of the skin spinner on the preferences window.
     */
    public static final int PREFS_SKIN_SPINNER_WIDTH = 120;

    /*
     * Width and height of the skin preview dialog.
     */
    public static final int SKIN_PREVIEW_DIALOG_HEIGHT = 280;
    public static final int SKIN_PREVIEW_DIALOG_WIDTH = 350;

    /*
     * Tooltip delay in milliseconds.
     */
    public static final int TOOLTIP_DELAY = 500;

    /*
     * Width of the track details label. This sets the separation between the track variables and
     * their values.
     */
    public static final int TRACK_DETAILS_LABEL_WIDTH = 130;
}
