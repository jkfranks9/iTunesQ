package itunesq;

import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.TableView;

/**
 * Class that represents the columns that are displayed for artists.
 * <p>
 * The following column set definitions exist:
 * <ul>
 * <li>Local - display when remote tracks are not being shown</li>
 * <li>Remote - display when remote tracks are being shown</li>
 * </ul>
 * 
 * @author Jon
 *
 */
public class ArtistDisplayColumns
{

    //---------------- Class variables -------------------------------------
	
	/**
	 * Definition of all possible artist column names. The <code>enum</code> 
	 * parameters are the header and name of the associated column.
	 */
	public enum ColumnNames
	{
		
		/**
		 * artist name
		 */
		ARTIST(StringConstants.TRACK_COLUMN_ARTIST,
				StringConstants.TRACK_COLUMN_ARTIST),
		
		NUM_ALTNAMES(StringConstants.ARTIST_COLUMN_NUM_ALTNAMES_HEADER,
				StringConstants.ARTIST_COLUMN_NUM_ALTNAMES_NAME),
		
		/**
		 * number of local tracks
		 */
		LOCAL_NUM_TRACKS(StringConstants.ARTIST_COLUMN_LOCAL_TRACKS_HEADER,
				StringConstants.ARTIST_COLUMN_LOCAL_TRACKS_NAME),
		
		/**
		 * total time of local tracks
		 */
		LOCAL_TOTAL_TIME(StringConstants.ARTIST_COLUMN_LOCAL_TIME_HEADER,
				StringConstants.ARTIST_COLUMN_LOCAL_TIME_NAME),
		
		/**
		 * number of remote tracks
		 */
		REMOTE_NUM_TRACKS(StringConstants.ARTIST_COLUMN_REMOTE_TRACKS_HEADER,
				StringConstants.ARTIST_COLUMN_REMOTE_TRACKS_NAME),
		
		/**
		 * total time of remote tracks
		 */
		REMOTE_TOTAL_TIME(StringConstants.ARTIST_COLUMN_REMOTE_TIME_HEADER,
				StringConstants.ARTIST_COLUMN_REMOTE_TIME_NAME);
		
		private String headerValue;
		private String nameValue;
		
		/*
		 * Constructor.
		 */
		private ColumnNames (String header, String name)
		{
			headerValue = header;
			nameValue = name;
		}
		
		/**
		 * Gets the header value.
		 * 
		 * @return enum header value
		 */
		public String getHeaderValue ()
		{
			return headerValue;
		}
		
		/**
		 * Gets the name value.
		 * 
		 * @return enum name value
		 */
		public String getNameValue ()
		{
			return nameValue;
		}
	}
	
	/**
	 * The types of column sets, for example the set of tracks displayed for 
	 * a given playlist.
	 */
	public enum ColumnSet
	{
		
		/**
		 * display when remote tracks are not being shown
		 */
		LOCAL_VIEW(),
		
		/**
		 * display when remote tracks are being shown
		 */
		REMOTE_VIEW();
		
		private List<String> headers;
		private List<String> names;
		private List<String> widths;
		
		/*
		 * Constructor.
		 */
		private ColumnSet ()
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
		public void buildColumnSet (List<List<String>> columnDef)
		{
			headers.clear();
			names.clear();
			widths.clear();
			
			Iterator<List<String>> columnDefIter = columnDef.iterator();
			while (columnDefIter.hasNext())
			{
				int index = 0;
				List<String> columnData = columnDefIter.next();
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
		public List<String> getHeadersList ()
		{
			return this.headers;
		}
		
		/**
		 * Gets the names list.
		 * 
		 * @return enum names list
		 */
		public List<String> getNamesList ()
		{
			return this.names;
		}
		
		/**
		 * Gets the widths list.
		 * 
		 * @return enum widths list
		 */
		public List<String> getWidthsList ()
		{
			return this.widths;
		}
	}
	
    //---------------- Private variables -----------------------------------
	
	/*
	 * Mapping of the column name to its relative width.
	 */
	private static final Map<String, String> COLUMN_WIDTH_MAP;	
	static
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put(ColumnNames.ARTIST.getNameValue(),            "3*");
		result.put(ColumnNames.NUM_ALTNAMES.getNameValue(),      "1*");
		result.put(ColumnNames.LOCAL_NUM_TRACKS.getNameValue(),  "1*");
		result.put(ColumnNames.LOCAL_TOTAL_TIME.getNameValue(),  "1*");
		result.put(ColumnNames.REMOTE_NUM_TRACKS.getNameValue(), "1*");
		result.put(ColumnNames.REMOTE_TOTAL_TIME.getNameValue(), "1*");
		
		COLUMN_WIDTH_MAP = result;
	}
	
	/**
	 * Class constructor.
	 */
	public ArtistDisplayColumns ()
	{
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Initializes the different column sets. This is called once during 
	 * initialization.
	 */
	public static void initializeColumnSets ()
	{
		
		/*
		 * Build the different types of column sets.
		 */
		buildLocalColumnSet();
		buildRemoteColumnSet();
	}
	
	/**
	 * Creates a column set. This is called to create an artist listing for 
	 * display.
	 * 
	 * @param type type of column set to create
	 * @param table table view for which the column set should be created
	 */
	public static void createColumnSet (ColumnSet type, TableView table)
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
	 * Creates the column data for an individual artist display column.
	 * 
	 * @param header column header
	 * @param name name of the column
	 * @return list of name + width data
	 */
	public static List<String> buildColumnData (String header, String name)
	{
		List<String> columnData = new ArrayList<String>();
		columnData.add(header);
		columnData.add(name);
		columnData.add(COLUMN_WIDTH_MAP.get(name));
		
		return columnData;
	}
	
    //---------------- Private methods -------------------------------------
	
	/*
	 * Build the local column set.
	 */
	private static void buildLocalColumnSet ()
	{
		List<List<String>> columnList = new ArrayList<List<String>>();
		
		columnList.add(buildColumnData(ColumnNames.ARTIST.getHeaderValue(),
				ColumnNames.ARTIST.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.NUM_ALTNAMES.getHeaderValue(),
				ColumnNames.NUM_ALTNAMES.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.LOCAL_NUM_TRACKS.getHeaderValue(),
				ColumnNames.LOCAL_NUM_TRACKS.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.LOCAL_TOTAL_TIME.getHeaderValue(),
				ColumnNames.LOCAL_TOTAL_TIME.getNameValue()));
		
		ColumnSet.LOCAL_VIEW.buildColumnSet(columnList);
	}
	
	/*
	 * Build the remote column set.
	 */
	private static void buildRemoteColumnSet ()
	{
		List<List<String>> columnList = new ArrayList<List<String>>();
		
		columnList.add(buildColumnData(ColumnNames.ARTIST.getHeaderValue(),
				ColumnNames.ARTIST.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.NUM_ALTNAMES.getHeaderValue(),
				ColumnNames.NUM_ALTNAMES.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.LOCAL_NUM_TRACKS.getHeaderValue(),
				ColumnNames.LOCAL_NUM_TRACKS.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.LOCAL_TOTAL_TIME.getHeaderValue(),
				ColumnNames.LOCAL_TOTAL_TIME.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.REMOTE_NUM_TRACKS.getHeaderValue(),
				ColumnNames.REMOTE_NUM_TRACKS.getNameValue()));
		columnList.add(buildColumnData(ColumnNames.REMOTE_TOTAL_TIME.getHeaderValue(),
				ColumnNames.REMOTE_TOTAL_TIME.getNameValue()));
		
		ColumnSet.REMOTE_VIEW.buildColumnSet(columnList);
	}
}
