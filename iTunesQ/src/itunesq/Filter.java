package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;

/**
 * Class that represents a query filter.
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
	 * The logic of a filter, for example match all rules, or any rules.
	 */
	public enum Logic
	{
		AND("All"), OR("Any");
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private Logic (String s)
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
	 * The subject of a filter, for example artist name or year of release.
	 */
	public enum Subject
	{
		ARTIST("Artist"), KIND("Kind"), PLAYLIST_COUNT("Playlist Count"), 
		RATING("Rating"), YEAR("Year"), NAME("Name");
		
		private final String displayValue;
		
		/*
		 * Constructor.
		 */
		private Subject (String value)
		{
			displayValue = value;
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
	 * The operator of a filter.
	 */
	public enum Operator
	{
		IS("is"), LESS("less than or equal"), GREATER("greater than or equal"), 
		CONTAINS("contains"), IS_NOT("is not");
		
		private String displayValue;
		
		/*
		 * Constructor.
		 */
		private Operator (String s)
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

    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Get the filter logic.
	 * 
	 * @return The filter logic.
	 */
	public Logic getFilterLogic ()
	{
		return filterLogic;
	}
	
	/**
	 * Set the filter logic.
	 * 
	 * @param logic The filter logic.
	 */
	public void setFilterLogic (Logic logic)
	{
		filterLogic = logic;
	}
	
	/**
	 * Get the filter subject.
	 * 
	 * @return The filter subject.
	 */
	public Subject getFilterSubject ()
	{
		return filterSubject;
	}
	
	/**
	 * Set the filter subject.
	 * 
	 * @param subject The filter subject.
	 */
	public void setFilterSubject (Subject subject)
	{
		filterSubject = subject;
	}
	
	/**
	 * Get the filter operator.
	 * 
	 * @return The filter operator.
	 */
	public Operator getFilterOperator ()
	{
		return filterOperator;
	}
	
	/**
	 * Set the filter operator.
	 * 
	 * @param operator The filter operator.
	 */
	public void setFilterOperator (Operator operator)
	{
		filterOperator = operator;
	}
	
	/**
	 * Get the filter text.
	 * 
	 * @return The filter text.
	 */
	public String getFilterText ()
	{
		return filterText;
	}
	
	/**
	 * Set the filter text.
	 * 
	 * @param text The filter text.
	 */
	public void setFilterText (String text)
	{
		filterText = text;
	}

	/**
	 * Get the list of logic enum values.
	 * 
	 * @return The enum value list.
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
	 * Get the list of subject enum values.
	 * 
	 * @return The enum value list.
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
	 * Get the list of operator enum values.
	 * 
	 * @return The enum value list.
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
