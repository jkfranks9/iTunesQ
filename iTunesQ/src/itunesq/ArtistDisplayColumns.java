package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.TableView;

/**
 * Class that represents the columns that are displayed for artists.
 * 
 * @author Jon
 *
 */
public class ArtistDisplayColumns
{

    // ---------------- Class variables -------------------------------------

    /**
     * Definition of all possible artist column names. The <code>enum</code>
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
         * artist name
         */
        ARTIST(StringConstants.TRACK_COLUMN_ARTIST, StringConstants.TRACK_COLUMN_ARTIST),

        /**
         * number of alternate names
         */
        NUM_ALTNAMES(StringConstants.ARTIST_COLUMN_NUM_ALTNAMES, "NumAltNames"),

        /**
         * number of tracks
         */
        NUM_TRACKS(StringConstants.ARTIST_COLUMN_TRACKS, "TotalTracks"),

        /**
         * total time of tracks
         */
        TOTAL_TIME(StringConstants.ARTIST_COLUMN_TIME, "TotalTime");

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
         * artist display columns
         */
        ARTIST_VIEW();

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
         * contains three elements, representing the column header, column 
         * name, and corresponding width.
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

    /*
     * Mapping of the column name to its relative width.
     */
    private static final Map<String, String> COLUMN_WIDTH_MAP;
    static
    {
        Map<String, String> result = new HashMap<String, String>();
        result.put(ColumnNames.ARTIST.getNameValue(), "3*");
        result.put(ColumnNames.NUM_ALTNAMES.getNameValue(), "1*");
        result.put(ColumnNames.NUM_TRACKS.getNameValue(), "1*");
        result.put(ColumnNames.TOTAL_TIME.getNameValue(), "1*");

        COLUMN_WIDTH_MAP = result;
    }

    /**
     * Class constructor.
     */
    public ArtistDisplayColumns()
    {
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Initializes the different column sets. This is called once during
     * initialization.
     */
    public static void initializeColumnSets()
    {
        buildArtistColumnSet();
    }

    /**
     * Creates a column set. This is called to create an artist listing for
     * display.
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

    // ---------------- Private methods -------------------------------------

    /*
     * Create the column data for an individual artist display column.
     */
    private static List<String> buildColumnData(String header, String name)
    {
        List<String> columnData = new ArrayList<String>();
        columnData.add(header);
        columnData.add(name);
        columnData.add(COLUMN_WIDTH_MAP.get(name));

        return columnData;
    }

    /*
     * Build the artist column set.
     */
    private static void buildArtistColumnSet()
    {
        List<List<String>> columnList = new ArrayList<List<String>>();

        columnList.add(buildColumnData(ColumnNames.ARTIST.getHeaderValue(),
                ColumnNames.ARTIST.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.NUM_ALTNAMES.getHeaderValue(),
                ColumnNames.NUM_ALTNAMES.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.NUM_TRACKS.getHeaderValue(),
                ColumnNames.NUM_TRACKS.getNameValue()));
        columnList.add(buildColumnData(ColumnNames.TOTAL_TIME.getHeaderValue(),
                ColumnNames.TOTAL_TIME.getNameValue()));

        ColumnSet.ARTIST_VIEW.buildColumnSet(columnList);
    }
}
