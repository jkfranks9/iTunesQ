package itunesq;

import java.io.IOException;
import java.util.Iterator;

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
 * </ul>
 * <p>
 * There are default values of each of these column sets, but they can be 
 * changed with user preferences.
 * 
 * @author Jon
 *
 */
public class TrackDisplayColumns
{

    //---------------- Class variables -------------------------------------
	
	/**
	 * Definition of all possible track column names. The <code>enum</code> 
	 * value is the name of the associated column.
	 */
	public enum ColumnNames
	{
		
		/**
		 * relative track number within a list
		 */
		NUMBER(StringConstants.TRACK_COLUMN_NUMBER),
		
		/**
		 * track ID
		 */
		ID(StringConstants.TRACK_COLUMN_ID),
		
		/**
		 * track name
		 */
		NAME(StringConstants.TRACK_COLUMN_NAME),
		
		/**
		 * artist name
		 */
		ARTIST(StringConstants.TRACK_COLUMN_ARTIST),
		
		/**
		 * composer name
		 */
		COMPOSER(StringConstants.TRACK_COLUMN_COMPOSER),
		
		/**
		 * album name
		 */
		ALBUM(StringConstants.TRACK_COLUMN_ALBUM),
		
		/**
		 * genre
		 */
		GENRE(StringConstants.TRACK_COLUMN_GENRE),
		
		/**
		 * kind of track
		 */
		KIND(StringConstants.TRACK_COLUMN_KIND),
		
		/**
		 * size in bytes
		 */
		SIZE(StringConstants.TRACK_COLUMN_SIZE),
		
		/**
		 * duration
		 */
		DURATION(StringConstants.TRACK_COLUMN_DURATION),
		
		/**
		 * year of release
		 */
		YEAR(StringConstants.TRACK_COLUMN_YEAR),
		
		/**
		 * modified date
		 */
		MODIFIED(StringConstants.TRACK_COLUMN_MODIFIED),
		
		/**
		 * added date
		 */
		ADDED(StringConstants.TRACK_COLUMN_ADDED),
		
		/**
		 * bit rate
		 */
		BITRATE(StringConstants.TRACK_COLUMN_BITRATE),
		
		/**
		 * sample rate
		 */
		SAMPLERATE(StringConstants.TRACK_COLUMN_SAMPLERATE),
		
		/**
		 * play count
		 */
		PLAYCOUNT(StringConstants.TRACK_COLUMN_PLAYCOUNT),
		
		/**
		 * released date
		 */
		RELEASED(StringConstants.TRACK_COLUMN_RELEASED),
		
		/**
		 * rating from 0 to 5
		 */
		RATING(StringConstants.TRACK_COLUMN_RATING);
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private ColumnNames (String s)
		{
			displayValue = s;
		}
		
		/**
		 * Gets the display value.
		 * 
		 * @return <code>enum</code> display value
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}
		
		/**
		 * Reverse lookup the <code>enum</code> from the display value.
		 * 
		 * @param value display value to look up
		 * @return <code>enum</code> value
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
	 * The types of column sets, for example the set of tracks displayed for 
	 * a given playlist.
	 */
	public enum ColumnSet
	{
		
		/**
		 * full list of tracks
		 */
		FULL_VIEW(),
		
		/**
		 * full list of tracks
		 */
		DUPLICATES_VIEW(),
		
		/**
		 * tracks resulting from a query
		 */
		FILTERED_VIEW(),
		
		/**
		 * tracks shown for a given playlist
		 */
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
		 * Builds a column set. 
		 * 
		 * @param columnDef column set definitions. The inner list always 
		 * contains just two elements, representing the column name and 
		 * corresponding width.
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
		 * Gets the names list.
		 * 
		 * @return <code>enum</code> names list
		 */
		public List<String> getNamesList ()
		{
			return this.names;
		}
		
		/**
		 * Gets the widths list.
		 * 
		 * @return <code>enum</code> widths list
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
	
	/**
	 * Class constructor.
	 */
	public TrackDisplayColumns ()
	{
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Initializes the default column sets. This is called once during 
	 * initialization.
	 * @throws IOException If an error occurs trying to write the user 
	 * preferences.
	 */
	public static void initializeDefaults () 
			throws IOException
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
	public static List<List<String>> getFullColumnDefaults ()
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
		
		return columnList;
	}
	
	/**
	 * Gets the list of duplicates column set defaults.
	 * 
	 * @return list of duplicates column set defaults
	 */
	public static List<List<String>> getDuplicatesColumnDefaults ()
	{
		List<List<String>> columnList = new ArrayList<List<String>>();
		
		columnList.add(buildColumnData(ColumnNames.NAME.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ARTIST.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ALBUM.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.KIND.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.DURATION.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.YEAR.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.RATING.getDisplayValue()));
		
		return columnList;
	}
	
	/**
	 * Gets the list of filtered column set defaults.
	 * 
	 * @return list of filtered column set defaults
	 */
	public static List<List<String>> getFilteredColumnDefaults ()
	{
		List<List<String>> columnList = new ArrayList<List<String>>();

		columnList.add(buildColumnData(ColumnNames.NAME.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ARTIST.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ALBUM.getDisplayValue()));
		
		return columnList;
	}
	
	/**
	 * Gets the list of playlist column set defaults.
	 * 
	 * @return list of playlist column set defaults
	 */
	public static List<List<String>> getPlaylistColumnDefaults ()
	{
		List<List<String>> columnList = new ArrayList<List<String>>();

		columnList.add(buildColumnData(ColumnNames.NUMBER.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.NAME.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ARTIST.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.ALBUM.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.DURATION.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.YEAR.getDisplayValue()));
		columnList.add(buildColumnData(ColumnNames.RATING.getDisplayValue()));
		
		return columnList;
	}
	
	/**
	 * Creates a column set. This is called by the various methods that create 
	 * track listings for display.
	 * 
	 * @param type type of column set to create
	 * @param table table view for which the column set should be created
	 */
	public static void createColumnSet (ColumnSet type, TableView table)
	{
		if (table == null)
		{
			throw new IllegalArgumentException("table argument is null");
		}
		
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
	 * Creates the name + width data for an individual track display column.
	 * 
	 * @param name name of the column
	 * @return list of name + width data
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
	private static void buildDuplicatesColumnData (Preferences prefs)
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
	private static void buildFilteredColumnData (Preferences prefs)
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
	private static void buildPlaylistColumnData (Preferences prefs)
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
