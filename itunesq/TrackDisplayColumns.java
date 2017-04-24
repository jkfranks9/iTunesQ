package itunesq;

import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.TableView;

/**
 * Class that represents the columns that are displayed for various track listings.
 * 
 * The following column definitions exist:
 * 
 * - Full:     The full list of tracks.
 * - Filtered: The tracks that are displayed for a given query.
 * - Playlist: The tracks that are displayed for a given playlist.
 * 
 * There are default values of each of these column sets, but they can be changed with user
 * preferences.
 * 
 * @author Jon
 *
 */
public class TrackDisplayColumns
{

    //---------------- Class variables -------------------------------------
	
	/**
	 * Definition of all possible column names.
	 */
	public enum ColumnNames
	{
		NUMBER("Number"), NAME("Name"), ARTIST("Artist"), ALBUM("Album"), KIND("Kind"),
		DURATION("Duration"), YEAR("Year"), ADDED("Added"), RATING("Rating");
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private ColumnNames (String s)
		{
			displayValue = s;
		}
		
		/**
		 * Get the display value.
		 * 
		 * @return The enum display value.
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}
		
		/**
		 * Reverse lookup the enum from the display value.
		 * 
		 * @param value The display value to look up.
		 * @return The enum.
		 */
		public static ColumnNames getEnum(String value)
		{
	        return lookup.get(value);
	    }
		
		/*
		 * Reverse lookup capability to get the enum based on its display value.
		 */
		private static final Map<String, ColumnNames> lookup = new HashMap<String, ColumnNames>();		
		static
		{
	        for (ColumnNames value : ColumnNames.values())
	        {
	            lookup.put(value.getDisplayValue(), value);
	        }
	    }
	}
	
	/**
	 * The types of column sets.
	 */
	public enum ColumnSet
	{
		FULL_VIEW(), 
		FILTERED_VIEW(),
		PLAYLIST_VIEW();
		
		private List<String> names;
		private List<String> widths;
		
		/*
		 * Constructor.
		 */
		private ColumnSet ()
		{
			names = new ArrayList<String>();
			widths = new ArrayList<String>();
		}
		
		/**
		 * Build a column set. 
		 * 
		 * @param columnDef List of column set definitions. The inner list always contains
		 * just 2 elements, representing the column name and corresponding width, so we use
		 * absolute indices to get these.
		 */
		public void buildColumnSet (List<List<String>> columnDef)
		{
			names.clear();
			widths.clear();
			
			Iterator<List<String>> columnDefIter = columnDef.iterator();
			while (columnDefIter.hasNext())
			{
				List<String> columnData = columnDefIter.next();
				this.names.add(columnData.get(0));
				this.widths.add(columnData.get(1));
			}
		}
		
		/**
		 * Get the names list.
		 * 
		 * @return The enum names list.
		 */
		public List<String> getNamesList ()
		{
			return this.names;
		}
		
		/**
		 * Get the widths list.
		 * 
		 * @return The enum widths list.
		 */
		public List<String> getWidthsList ()
		{
			return this.widths;
		}
	}
	
    //---------------- Private variables -----------------------------------
	
	private static boolean prefsAltered;
	
	/*
	 * Mapping of the column name to its relative width.
	 */
	private static final Map<String, String> COLUMN_WIDTH_MAP;	
	static
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put(ColumnNames.NUMBER.getDisplayValue(),   "1*");
		result.put(ColumnNames.NAME.getDisplayValue(),     "4*");
		result.put(ColumnNames.ARTIST.getDisplayValue(),   "2*");
		result.put(ColumnNames.ALBUM.getDisplayValue(),    "4*");
		result.put(ColumnNames.KIND.getDisplayValue(),     "2*");
		result.put(ColumnNames.DURATION.getDisplayValue(), "1*");
		result.put(ColumnNames.YEAR.getDisplayValue(),     "1*");
		result.put(ColumnNames.ADDED.getDisplayValue(),    "2*");
		result.put(ColumnNames.RATING.getDisplayValue(),   "1*");
		
		COLUMN_WIDTH_MAP = result;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Initialize the default columns. This is called once during initialization.
	 */
	public static void initializeDefaults ()
	{
		prefsAltered = false;
		
		Preferences prefs = Preferences.getInstance();
		
		/*
		 * Build the different types of column sets.
		 */
		buildFullColumnData(prefs);
		buildFilteredColumnData(prefs);
		buildPlaylistColumnData(prefs);
		
		/*
		 * If the preferences were altered, serialize them.
		 */
		if (prefsAltered == true)
		{
			prefs.writePreferences();
		}
	}
	
	/**
	 * Create a column set. This is called by the various methods that create track listings for
	 * display.
	 * 
	 * @param type The type of column set to create.
	 * @param table The TableView for which the column set should be created.
	 */
	public static void createColumnSet (ColumnSet type, TableView table)
	{
		
		TableView.ColumnSequence columns = table.getColumns();
		
		List<String> columnSetNames = type.getNamesList();
		List<String> columnSetWidths = type.getWidthsList();
		
		for (int i = 0; i < columnSetNames.getLength(); i++)
		{
			TableView.Column column = new TableView.Column();
			column.setName(columnSetNames.get(i));
			column.setHeaderData(columnSetNames.get(i));
			column.setWidth(columnSetWidths.get(i));
			columns.add(column);
		}
	}
	
	/**
	 * Create the name + width data for an individual track display column.
	 * 
	 * @param name Name of the column.
	 * @return List of name + width data.
	 */
	public static List<String> buildColumnData (String name)
	{
		List<String> columnData = new ArrayList<String>();
		columnData.add(name);
		columnData.add(COLUMN_WIDTH_MAP.get(name));
		
		return columnData;
	}
	
    //---------------- Private methods -------------------------------------
	
	/*
	 * Build the full column set.
	 */
	private static void buildFullColumnData (Preferences prefs)
	{
		List<List<String>> columnList = new ArrayList<List<String>>();
		
		columnList.add(buildColumnData(ColumnNames.NAME.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ARTIST.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ALBUM.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.KIND.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.DURATION.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.YEAR.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ADDED.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.RATING.getDisplayValue()));
		
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
	 * Build the filtered column set.
	 */
	private static void buildFilteredColumnData (Preferences prefs)
	{
		List<List<String>> columnList = new ArrayList<List<String>>();
		
		columnList.add(buildColumnData(ColumnNames.NAME.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ARTIST.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ALBUM.getDisplayValue()));
		
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
	private static void buildPlaylistColumnData (Preferences prefs)
	{
		List<List<String>> columnList = new ArrayList<List<String>>();

		columnList.add(buildColumnData(ColumnNames.NUMBER.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.NAME.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ARTIST.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ALBUM.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.DURATION.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.YEAR.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.RATING.getDisplayValue()));
		
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
