package itunesq;

/**
 * Class that represents the type of query for a list of query results. The 
 * results might be a list of tracks or playlists. This class is primarily 
 * used for the File {@literal ->} Save action.
 * 
 * @author Jon
 *
 */
public class ListQueryType
{

    // ---------------- Public variables ------------------------------------

    /**
     * The type of query associated with the list of tracks.
     */
    public enum Type
    {

        /**
         * no query
         */
        NONE(""),

        /**
         * list of tracks from a tracks (filter) query
         */
        TRACK_QUERY(StringConstants.LIST_TYPE_TRACK_QUERY),

        /**
         * list of duplicate tracks
         */
        TRACK_DUPLICATES(StringConstants.LIST_TYPE_TRACK_DUPLICATES),

        /**
         * list of tracks from a playlist comparison
         */
        TRACK_COMPARE(StringConstants.LIST_TYPE_TRACK_COMPARE),

        /**
         * list of tracks from a playlist family expansion
         */
        TRACK_FAMILY(StringConstants.LIST_TYPE_TRACK_FAMILY),

        /**
         * list of playlists from a playlist family expansion
         */
        PLAYLIST_FAMILY(StringConstants.LIST_TYPE_PLAYLIST_FAMILY);

        private String displayValue;

        /*
         * Constructor.
         */
        private Type(String s)
        {
            displayValue = s;
        }

        /**
         * Gets the display value.
         * 
         * @return enum display value
         */
        public String getDisplayValue()
        {
            return displayValue;
        }
    }
}
