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
	 * Logic of a filter. For example match all rules, or any rules.
	 */
	public enum Logic
	{
		
		/**
		 * matches all of the following filters
		 */
		AND(StringConstants.FILTER_LOGIC_ALL),
		
		/**
		 * matches any of the following filters
		 */
		OR(StringConstants.FILTER_LOGIC_ANY);
		
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
		 * @return enum display value
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}
		
		/**
		 * Performs a reverse lookup of the <code>enum</code> from the display
		 * value.
		 * 
		 * @param value display value to look up
		 * @return enum value
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
	 * Subject of a filter. For example artist name or year of release.
	 */
	public enum Subject
	{
		
		/**
		 * artist name
		 */
		ARTIST(StringConstants.FILTER_SUBJECT_ARTIST),
		
		/**
		 * kind of track, for example AAC audio file or QuickTime movie file
		 */
		KIND(StringConstants.FILTER_SUBJECT_KIND),
		
		/**
		 * number of playlists that contain this track. Bypassed playlists
		 * are not counted.
		 */
		PLAYLIST_COUNT(StringConstants.FILTER_SUBJECT_PLAYLIST_COUNT),
		
		/**
		 * rating of this track, from 0 through 5
		 */
		RATING(StringConstants.FILTER_SUBJECT_RATING),
		
		/**
		 * year of release
		 */
		YEAR(StringConstants.FILTER_SUBJECT_YEAR),
		
		/**
		 * name of this track
		 */
		NAME(StringConstants.FILTER_SUBJECT_NAME);
		
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
		 * @return enum display value
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}
		
		/**
		 * Performs a reverse lookup of the <code>enum</code> from the display
		 * value.
		 * 
		 * @param value display value to look up
		 * @return enum value
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
	 * Operator of a filter.
	 */
	public enum Operator
	{
		
		/**
		 * equals the specified value
		 */
		IS(StringConstants.FILTER_OPERATOR_IS),
		
		/**
		 * less than or equal to the specified value
		 */
		LESS(StringConstants.FILTER_OPERATOR_LESS),
		
		/**
		 * greater than or equal to the specified value
		 */
		GREATER(StringConstants.FILTER_OPERATOR_GREATER),
		
		/**
		 * contains the specified value
		 */
		CONTAINS(StringConstants.FILTER_OPERATOR_CONTAINS),
		
		/**
		 * does not equal the specified value
		 */
		IS_NOT(StringConstants.FILTER_OPERATOR_IS_NOT);
		
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
		 * @return enum display value
		 */
		public String getDisplayValue ()
		{
			return displayValue;
		}
		
		/**
		 * Performs a reverse lookup of the <code>enum</code> from the display
		 * value.
		 * 
		 * @param value display value to look up
		 * @return enum value
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
	 * @return logic enum value list
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
	 * @return subject enum value list
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
	 * @return operator enum value list
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
