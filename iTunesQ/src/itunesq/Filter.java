package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;

/**
 * Class that represents a track query filter. Filters are the mechanism that 
 * allow users to query the set of tracks. A filter contains the following
 * parts:
 * <ol>
 * <li>Logic - matches any or all of a set of filters</li>
 * <li>Subject - the 'what' of a filter, for example artist or year of 
 * release</li>
 * <li>Operator - for example is or contains</li>
 * <li>Text - the value associated with the subject</li>
 * </ol>
 * <p>
 * When grouping filters, the logic is specified for the first filter, and
 * applies to all following filters until a different logic value is
 * specified. Only two such logic changes are allowed, one to specify a new
 * group of filters with a different logic value, and an optional one to
 * revert back to the original logic. For example:
 * <pre>
 *   <b>Logic Subject Operator              Text</b>
 *   All   Year    greater than or equal 2001
 *         Rating  is                    5
 *   Any   Artist  is                    Ego Likeness
 *         Artist  is                    The Birthday Massacre
 * </pre>
 * This can be read as "show all tracks with a release year greater than or 
 * equal to 2001, with a rating of 5, by either of the bands Ego Likeness
 * or The Birthday Massacre".
 * 
 * @author Jon
 *
 */
public class Filter
{

    //---------------- Class variables -------------------------------------
	
	/*
	 * A filter consists of a logic element, a subject, an operator, and text. For example:
	 * 
	 * AND YEAR GREATER 1983
	 */
	private Logic filterLogic;
	private Subject filterSubject;
	private Operator filterOperator;
	private String filterText;
	
	/**
	 * logic of a filter. For example match all rules, or any rules.
	 */
	public enum Logic
	{
		
		/**
		 * matches all of the following filters
		 */
		AND("All"),
		
		/**
		 * matches any of the following filters
		 */
		OR("Any");
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private Logic (String s)
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
		public static Logic getEnum(String value)
		{
	        return lookup.get(value);
	    }
		
		/*
		 * Reverse lookup capability to get the enum based on its display value.
		 */
		private static final Map<String, Logic> lookup = new HashMap<String, Logic>();		
		static
		{
	        for (Logic value : Logic.values())
	        {
	            lookup.put(value.getDisplayValue(), value);
	        }
	    }
	}
	
	/**
	 * subject of a filter. For example artist name or year of release.
	 */
	public enum Subject
	{
		
		/**
		 * artist name
		 */
		ARTIST("Artist"),
		
		/**
		 * kind of track, for example AAC audio file or QuickTime movie file
		 */
		KIND("Kind"),
		
		/**
		 * number of playlists that contain this track. Bypassed playlists
		 * are not counted.
		 */
		PLAYLIST_COUNT("Playlist Count"),
		
		/**
		 * rating of this track, from 0 through 5
		 */
		RATING("Rating"),
		
		/**
		 * year of release
		 */
		YEAR("Year"),
		
		/**
		 * name of this track
		 */
		NAME("Name");
		
		private final String displayValue;
		
		/*
		 * Constructor.
		 */
		private Subject (String value)
		{
			displayValue = value;
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
		public static Subject getEnum(String value)
		{
	        return lookup.get(value);
	    }
		
		/*
		 * Reverse lookup capability to get the enum based on its display value.
		 */
		private static final Map<String, Subject> lookup = new HashMap<String, Subject>();		
		static
		{
	        for (Subject value : Subject.values())
	        {
	            lookup.put(value.getDisplayValue(), value);
	        }
	    }
	}

	/**
	 * operator of a filter
	 */
	public enum Operator
	{
		
		/**
		 * equals the specified value
		 */
		IS("is"),
		
		/**
		 * less than or equal to the specified value
		 */
		LESS("less than or equal"),
		
		/**
		 * greater than or equal to the specified value
		 */
		GREATER("greater than or equal"),
		
		/**
		 * contains the specified value
		 */
		CONTAINS("contains"),
		
		/**
		 * does not equal the specified value
		 */
		IS_NOT("is not");
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private Operator (String s)
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
		public static Operator getEnum(String value)
		{
	        return lookup.get(value);
	    }
		
		/*
		 * Reverse lookup capability to get the enum based on its display value.
		 */
		private static final Map<String, Operator> lookup = new HashMap<String, Operator>();		
		static
		{
	        for (Operator value : Operator.values())
	        {
	            lookup.put(value.getDisplayValue(), value);
	        }
	    }
	}
	
	/**
	 * Class constructor.
	 */
	public Filter ()
	{
	}

    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the filter logic.
	 * 
	 * @return filter logic value
	 */
	public Logic getFilterLogic ()
	{
		return filterLogic;
	}
	
	/**
	 * Sets the filter logic.
	 * 
	 * @param logic filter logic value
	 */
	public void setFilterLogic (Logic logic)
	{
		filterLogic = logic;
	}
	
	/**
	 * Gets the filter subject.
	 * 
	 * @return filter subject value
	 */
	public Subject getFilterSubject ()
	{
		return filterSubject;
	}
	
	/**
	 * Sets the filter subject.
	 * 
	 * @param subject filter subject value
	 */
	public void setFilterSubject (Subject subject)
	{
		filterSubject = subject;
	}
	
	/**
	 * Gets the filter operator.
	 * 
	 * @return filter operator value
	 */
	public Operator getFilterOperator ()
	{
		return filterOperator;
	}
	
	/**
	 * Sets the filter operator.
	 * 
	 * @param operator filter operator value
	 */
	public void setFilterOperator (Operator operator)
	{
		filterOperator = operator;
	}
	
	/**
	 * Gets the filter text.
	 * 
	 * @return filter text value
	 */
	public String getFilterText ()
	{
		return filterText;
	}
	
	/**
	 * Sets the filter text.
	 * 
	 * @param text filter text value
	 */
	public void setFilterText (String text)
	{
		filterText = text;
	}

	/**
	 * Gets the list of logic <code>enum</code> values.
	 * 
	 * @return logic <code>enum</code> value list
	 */
	public static List<String> getLogicLabels ()
	{
		List<String> logicLabels = new ArrayList<String>();
		
        for (Logic s : Logic.values())
        {
        	logicLabels.add(s.getDisplayValue());
        }
        
		return logicLabels;
	}
	
	/**
	 * Gets the list of subject <code>enum</code> values.
	 * 
	 * @return subject <code>enum</code> value list
	 */
	public static List<String> getSubjectLabels ()
	{
		List<String> subjectLabels = new ArrayList<String>();
	      
        for (Subject s : Subject.values())
        {
        	subjectLabels.add(s.getDisplayValue());
        }
        
		return subjectLabels;
	}

	/**
	 * Gets the list of operator <code>enum</code> values.
	 * 
	 * @return operator <code>enum</code> value list
	 */
	public static List<String> getOperatorLabels ()
	{
		List<String> operatorLabels = new ArrayList<String>();

        for (Operator s : Operator.values())
        {
        	operatorLabels.add(s.getDisplayValue());
        }
        
		return operatorLabels;
	}
}
