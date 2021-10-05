package itunesq;

import java.io.IOException;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.TableView;

/**
 * Class that represents the columns that are displayed for various track
 * listings.
 * <p>
 * The following column set definitions exist:
 * <ul>
 * <li>Full - full list of tracks</li>
 * <li>Duplicates - list of duplicate tracks</li>
 * <li>Filtered - tracks that are displayed for a given query</li>
 * <li>Playlist - tracks that are displayed for a given playlist</li>
 * <li>Family - tracks that are displayed for a playlist family expansion</li>
 * </ul>
 * <p>
 * There are default values for each of these column sets, but most can be
 * changed with user preferences.
 * 
 * @author Jon
 *
 */
public class TrackDisplayColumns
{

    // ---------------- Class variables -------------------------------------

    /**
     * Definition of all possible track column names. The <code>enum</code>
     * parameters are the header and name of the associated column.
     */
    public enum ColumnNames
    {

        /*
         * NOTE: Columns have both a header value that is displayed, and a name value that is used
         * to reference column data. Blanks in the name value cause Pivot to crash. So we initialize
         * both values in the definitions. If the header name in StringConstants contains no blanks,
         * we use that value for both initializers. If it contains blanks, we use a hardcoded string
         * as the initializer.
         */

        /**
         * relative track number within a list
         */
        NUMBER(StringConstants.TRACK_COLUMN_NUMBER, StringConstants.TRACK_COLUMN_NUMBER),

        /**
         * track ID
         */
        ID(StringConstants.TRACK_COLUMN_ID, StringConstants.TRACK_COLUMN_ID),

        /**
         * track name
         */
        NAME(StringConstants.TRACK_COLUMN_NAME, StringConstants.TRACK_COLUMN_NAME),

        /**
         * artist name
         */
        ARTIST(StringConstants.TRACK_COLUMN_ARTIST, StringConstants.TRACK_COLUMN_ARTIST),

        /**
         * composer name
         */
        COMPOSER(StringConstants.TRACK_COLUMN_COMPOSER, StringConstants.TRACK_COLUMN_COMPOSER),

        /**
         * album name
         */
        ALBUM(StringConstants.TRACK_COLUMN_ALBUM, StringConstants.TRACK_COLUMN_ALBUM),

        /**
         * genre
         */
        GENRE(StringConstants.TRACK_COLUMN_GENRE, StringConstants.TRACK_COLUMN_GENRE),

        /**
         * kind of track
         */
        KIND(StringConstants.TRACK_COLUMN_KIND, StringConstants.TRACK_COLUMN_KIND),

        /**
         * size in bytes
         */
        SIZE(StringConstants.TRACK_COLUMN_SIZE, StringConstants.TRACK_COLUMN_SIZE),

        /**
         * duration
         */
        DURATION(StringConstants.TRACK_COLUMN_DURATION, StringConstants.TRACK_COLUMN_DURATION),

        /**
         * year of release
         */
        YEAR(StringConstants.TRACK_COLUMN_YEAR, StringConstants.TRACK_COLUMN_YEAR),

        /**
         * modified date
         */
        MODIFIED(StringConstants.TRACK_COLUMN_MODIFIED, StringConstants.TRACK_COLUMN_MODIFIED),

        /**
         * added date
         */
        ADDED(StringConstants.TRACK_COLUMN_ADDED, StringConstants.TRACK_COLUMN_ADDED),

        /**
         * bit rate
         */
        BITRATE(StringConstants.TRACK_COLUMN_BITRATE, "BitRate"),

        /**
         * sample rate
         */
        SAMPLERATE(StringConstants.TRACK_COLUMN_SAMPLERATE, "SampleRate"),

        /**
         * play count
         */
        PLAYCOUNT(StringConstants.TRACK_COLUMN_PLAYCOUNT, "PlayCount"),

        /**
         * released date
         */
        RELEASED(StringConstants.TRACK_COLUMN_RELEASED, StringConstants.TRACK_COLUMN_RELEASED),

        /**
         * rating from 0 to 5
         */
        RATING(StringConstants.TRACK_COLUMN_RATING, StringConstants.TRACK_COLUMN_RATING),

        /**
         * encoder
         */
        ENCODER(StringConstants.TRACK_COLUMN_ENCODER, StringConstants.TRACK_COLUMN_ENCODER),

        /**
         * number of playlists
         */
        NUMPLAYLISTS(StringConstants.TRACK_COLUMN_NUMPLAYLISTS, "NumPlaylists");

        private String headerValue;
        private String nameValue;

        /*
         * Constructor.
         */
        private ColumnNames(String header, String name)
        {
            headerValue = header;
            nameValue = name;
        }

        /**
         * Gets the header value.
         * 
         * @return enum header value
         */
        public String getHeaderValue()
        {
            return headerValue;
        }

        /**
         * Gets the name value.
         * 
         * @return enum name value
         */
        public String getNameValue()
        {
            return nameValue;
        }

        /**
         * Performs a reverse lookup of the <code>enum</code> from the name
         * value.
         * 
         * @param value name value to look up
         * @return enum value
         */
        public static ColumnNames getEnum(String value)
        {
            return lookup.get(value);
        }

        /*
         * Reverse lookup capability to get the enum based on its name value.
         */
        private static final Map<String, ColumnNames> lookup = new HashMap<String, ColumnNames>();
        static
        {
            for (ColumnNames value : ColumnNames.values())
            {
                lookup.put(value.getNameValue(), value);
            }
        }
    }

    /**
     * The types of column sets, for example the set of tracks displayed for a
     * given playlist.
     */
    public enum ColumnSet
    {

        /**
         * full list of tracks
         */
        FULL_VIEW(),

        /**
         * list of duplicate tracks
         */
        DUPLICATES_VIEW(),

        /**
         * tracks resulting from a query
         */
        FILTERED_VIEW(),

        /**
         * tracks shown for a given playlist
         */
        PLAYLIST_VIEW(),

        /**
         * tracks shown for a playlist family expansion
         */
        FAMILY_VIEW();

        private List<String> headers;
        private List<String> names;
        private List<String> widths;

        /*
         * Constructor.
         */
        private ColumnSet()
        {
            headers = new ArrayList<String>();
            names = new ArrayList<String>();
            widths = new ArrayList<String>();
        }

        /**
         * Builds a column set.
         * 
         * @param columnDef column set definitions. The inner list always
         * contains three elements, representing the column header, column name,
         * and corresponding width.
         */
        public void buildColumnSet(List<List<String>> columnDef)
        {
            headers.clear();
            names.clear();
            widths.clear();

            for (List<String> columnData : columnDef)
            {
                int index = 0;
                this.headers.add(columnData.get(index++));
                this.names.add(columnData.get(index++));
                this.widths.add(columnData.get(index++));
            }
        }

        /**
         * Gets the headers list.
         * 
         * @return enum headers list
         */
        public List<String> getHeadersList()
        {
            return this.headers;
        }

        /**
         * Gets the names list.
         * 
         * @return enum names list
         */
        public List<String> getNamesList()
        {
            return this.names;
        }

        /**
         * Gets the widths list.
         * 
         * @return enum widths list
         */
        public List<String> getWidthsList()
        {
            return this.widths;
        }
    }

    // ---------------- Private variables -----------------------------------

    private static boolean prefsAltered;

    /*
     * Mapping of the column name to its relative width.
     */
    private static final Map<String, String> COLUMN_WIDTH_MAP;
    static
    {
        Map<String, String> result = new HashMap<String, String>();
        result.put(ColumnNames.NUMBER.getNameValue(),       "1*");
        result.put(ColumnNames.NAME.getNameValue(),         "4*");
        result.put(ColumnNames.ARTIST.getNameValue(),       "2*");
        result.put(ColumnNames.ALBUM.getNameValue(),        "4*");
        result.put(ColumnNames.KIND.getNameValue(),         "2*");
        result.put(ColumnNames.DURATION.getNameValue(),     "1*");
        result.put(ColumnNames.YEAR.getNameValue(),         "1*");
        result.put(ColumnNames.ADDED.getNameValue(),        "2*");
        result.put(ColumnNames.RATING.getNameValue(),       "1*");
        result.put(ColumnNames.ENCODER.getNameValue(),      "2*");
        result.put(ColumnNames.NUMPLAYLISTS.getNameValue(), "1*");

        COLUMN_WIDTH_MAP = result;
    }

    /**
     * Class constructor.
     */
    public TrackDisplayColumns()
    {
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Initializes the default column sets. This is called once during
     * initialization.
     * 
     * @throws IOException If an error occurs trying to write the user
     * preferences.
     */
    public static void initializeDefaults() throws IOException
    {
        prefsAltered = false;

        Preferences prefs = Preferences.getInstance();

        /*
         * Build the different types of column sets.
         */
        buildFullColumnData(prefs);
        buildDuplicatesColumnData(prefs);
        buildFilteredColumnData(prefs);
        buildPlaylistColumnData(prefs);

        /*
         * Note that the family column set is built-in; there is no user preference for it. So we just
         * build it from the defaults.
         */
        ColumnSet.FAMILY_VIEW.buildColumnSet(getFamilyColumnDefaults());

        /*
         * If the preferences were altered, serialize them.
         */
        if (prefsAltered == true)
        {
            prefs.writePreferences();
        }
    }

    /**
     * Gets the list of full column set defaults.
     * 
     * @return list of full column set defaults
     */
    public static List<List<String>> getFullColumnDefaults()
    {
        List<List<String>> columnList = new ArrayList<List<String>>();

        columnList.add(buildColumnData(ColumnNames.NAME.getHeaderValue(), 
                ColumnNames.NAME.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ARTIST.getHeaderValue(), 
                ColumnNames.ARTIST.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ALBUM.getHeaderValue(), 
                ColumnNames.ALBUM.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.KIND.getHeaderValue(), 
                ColumnNames.KIND.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.DURATION.getHeaderValue(), 
                ColumnNames.DURATION.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.YEAR.getHeaderValue(), 
                ColumnNames.YEAR.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ADDED.getHeaderValue(), 
                ColumnNames.ADDED.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.RATING.getHeaderValue(), 
                ColumnNames.RATING.getNameValue()));

        return columnList;
    }

    /**
     * Gets the list of duplicates column set defaults.
     * 
     * @return list of duplicates column set defaults
     */
    public static List<List<String>> getDuplicatesColumnDefaults()
    {
        List<List<String>> columnList = new ArrayList<List<String>>();

        columnList.add(buildColumnData(ColumnNames.NAME.getHeaderValue(), 
                ColumnNames.NAME.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ARTIST.getHeaderValue(), 
                ColumnNames.ARTIST.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ALBUM.getHeaderValue(), 
                ColumnNames.ALBUM.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.KIND.getHeaderValue(), 
                ColumnNames.KIND.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.DURATION.getHeaderValue(), 
                ColumnNames.DURATION.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.YEAR.getHeaderValue(), 
                ColumnNames.YEAR.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.RATING.getHeaderValue(), 
                ColumnNames.RATING.getNameValue()));

        return columnList;
    }

    /**
     * Gets the list of filtered column set defaults.
     * 
     * @return list of filtered column set defaults
     */
    public static List<List<String>> getFilteredColumnDefaults()
    {
        List<List<String>> columnList = new ArrayList<List<String>>();

        columnList.add(buildColumnData(ColumnNames.NAME.getHeaderValue(), 
                ColumnNames.NAME.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ARTIST.getHeaderValue(), 
                ColumnNames.ARTIST.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ALBUM.getHeaderValue(), 
                ColumnNames.ALBUM.getNameValue()));

        return columnList;
    }

    /**
     * Gets the list of playlist column set defaults.
     * 
     * @return list of playlist column set defaults
     */
    public static List<List<String>> getPlaylistColumnDefaults()
    {
        List<List<String>> columnList = new ArrayList<List<String>>();

        columnList.add(buildColumnData(ColumnNames.NUMBER.getHeaderValue(), 
                ColumnNames.NUMBER.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.NAME.getHeaderValue(), 
                ColumnNames.NAME.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ARTIST.getHeaderValue(), 
                ColumnNames.ARTIST.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.ALBUM.getHeaderValue(), 
                ColumnNames.ALBUM.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.DURATION.getHeaderValue(), 
                ColumnNames.DURATION.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.YEAR.getHeaderValue(), 
                ColumnNames.YEAR.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.RATING.getHeaderValue(), 
                ColumnNames.RATING.getNameValue()));

        return columnList;
    }

    /**
     * Gets the list of family column set defaults.
     * 
     * @return list of family column set defaults
     */
    public static List<List<String>> getFamilyColumnDefaults()
    {
        List<List<String>> columnList = new ArrayList<List<String>>();

        columnList.add(buildColumnData(ColumnNames.NUMBER.getHeaderValue(), 
                ColumnNames.NUMBER.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.NAME.getHeaderValue(), 
                ColumnNames.NAME.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.NUMPLAYLISTS.getHeaderValue(),
                ColumnNames.NUMPLAYLISTS.getNameValue()));

        return columnList;
    }

    /**
     * Creates a column set. This is called by the various methods that create
     * track listings for display.
     * 
     * @param type type of column set to create
     * @param table table view for which the column set should be created
     */
    public static void createColumnSet(ColumnSet type, TableView table)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type argument is null");
        }

        if (table == null)
        {
            throw new IllegalArgumentException("table argument is null");
        }

        TableView.ColumnSequence columns = table.getColumns();

        List<String> columnSetHeaders = type.getHeadersList();
        List<String> columnSetNames = type.getNamesList();
        List<String> columnSetWidths = type.getWidthsList();

        for (int i = 0; i < columnSetNames.getLength(); i++)
        {
            TableView.Column column = new TableView.Column();
            column.setHeaderData(columnSetHeaders.get(i));
            column.setName(columnSetNames.get(i));
            column.setWidth(columnSetWidths.get(i));
            columns.add(column);
        }
    }

    /**
     * Creates the column data for an individual track display column.
     * 
     * @param header column header
     * @param name name of the column
     * @return list of column data
     */
    public static List<String> buildColumnData(String header, String name)
    {
        List<String> columnData = new ArrayList<String>();
        columnData.add(header);
        columnData.add(name);
        columnData.add(COLUMN_WIDTH_MAP.get(name));

        return columnData;
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Build the full column set.
     */
    private static void buildFullColumnData(Preferences prefs)
    {
        List<List<String>> columnList = getFullColumnDefaults();

        /*
         * Note that we only need to modify column set preferences if the preferences for that set
         * don't exist.
         * 
         * If the preferences do exist, then we just need to build the column set from the 
         * existing preferences.
         */
        List<List<String>> prefsFullColumnSet = prefs.getTrackColumnsFullView();
        if (prefsFullColumnSet == null || prefsFullColumnSet.getLength() == 0)
        {
            ColumnSet.FULL_VIEW.buildColumnSet(columnList);
            prefs.setTrackColumnsFullView(columnList);
            prefsAltered = true;
        }
        else
        {
            ColumnSet.FULL_VIEW.buildColumnSet(prefsFullColumnSet);
        }
    }

    /*
     * Build the duplicates column set.
     */
    private static void buildDuplicatesColumnData(Preferences prefs)
    {
        List<List<String>> columnList = getDuplicatesColumnDefaults();

        /*
         * Note that we only need to modify column set preferences if the preferences for that set
         * don't exist.
         * 
         * If the preferences do exist, then we just need to build the column set from the 
         * existing preferences.
         */
        List<List<String>> prefsDuplicatesColumnSet = prefs.getTrackColumnsDuplicatesView();
        if (prefsDuplicatesColumnSet == null || prefsDuplicatesColumnSet.getLength() == 0)
        {
            ColumnSet.DUPLICATES_VIEW.buildColumnSet(columnList);
            prefs.setTrackColumnsDuplicatesView(columnList);
            prefsAltered = true;
        }
        else
        {
            ColumnSet.DUPLICATES_VIEW.buildColumnSet(prefsDuplicatesColumnSet);
        }
    }

    /*
     * Build the filtered column set.
     */
    private static void buildFilteredColumnData(Preferences prefs)
    {
        List<List<String>> columnList = getFilteredColumnDefaults();

        /*
         * Note that we only need to modify column set preferences if the preferences for that set
         * don't exist.
         * 
         * If the preferences do exist, then we just need to build the column set from the 
         * existing preferences.
         */
        List<List<String>> prefsFilteredColumnSet = prefs.getTrackColumnsFilteredView();
        if (prefsFilteredColumnSet == null || prefsFilteredColumnSet.getLength() == 0)
        {
            ColumnSet.FILTERED_VIEW.buildColumnSet(columnList);
            prefs.setTrackColumnsFilteredView(columnList);
            prefsAltered = true;
        }
        else
        {
            ColumnSet.FILTERED_VIEW.buildColumnSet(prefsFilteredColumnSet);
        }
    }

    /*
     * Build the playlist column set.
     */
    private static void buildPlaylistColumnData(Preferences prefs)
    {
        List<List<String>> columnList = getPlaylistColumnDefaults();

        /*
         * Note that we only need to modify column set preferences if the preferences for that set
         * don't exist.
         * 
         * If the preferences do exist, then we just need to build the column set from the 
         * existing preferences.
         */
        List<List<String>> prefsPlaylistColumnSet = prefs.getTrackColumnsPlaylistView();
        if (prefsPlaylistColumnSet == null || prefsPlaylistColumnSet.getLength() == 0)
        {
            ColumnSet.PLAYLIST_VIEW.buildColumnSet(columnList);
            prefs.setTrackColumnsPlaylistView(columnList);
            prefsAltered = true;
        }
        else
        {
            ColumnSet.PLAYLIST_VIEW.buildColumnSet(prefsPlaylistColumnSet);
        }
    }
}
