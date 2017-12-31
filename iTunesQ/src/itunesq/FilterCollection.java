package itunesq;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that represents a collection of filters.
 * <p>
 * Filters can be ANDed or ORed. In addition, subgroups of filters can exist.
 * For example: filter1 AND filter2 AND (filter3 OR filter4)
 * <p>
 * See the {@link Filter} class for more details on filter contents.
 */
public class FilterCollection
{

    //---------------- Class variables -------------------------------------
	
	/*
	 * List of filters.
	 */
	private List<Filter> filters;
	
	/*
	 * Result list of filtered tracks.
	 */
	private List<Track> filteredTracks;
	
	/*
	 * Filter error string. This is non-null only when one or more filters contains an error.
	 */
	private String filterError;

    //---------------- Private variables -----------------------------------
	
	private Logger logger = null;
	
	/**
	 * Class constructor.
	 */
	public FilterCollection ()
	{
    	
    	/*
    	 * Create a filter logger.
    	 */
    	String className = getClass().getSimpleName();
    	logger = (Logger) LoggerFactory.getLogger(className + "_Filter");
    	
    	/*
    	 * Get the logging object singleton.
    	 */
    	Logging logging = Logging.getInstance();
    	
    	/*
    	 * Register our logger.
    	 */
    	logging.registerLogger(Logging.Dimension.FILTER, logger);
    	
    	/*
    	 * Initialize variables.
    	 */
		filters = new ArrayList<Filter>();
		filterError = null;
		
		logger.trace("FilterCollection constructor: " + this.hashCode());
	}

    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the filter collection.
	 * 
	 * @return collection of all filters as a list
	 */
	public List<Filter> getFilters ()
	{
		return filters;
	}
	
	/**
	 * Adds a filter to the filter collection.
	 * 
	 * @param filter filter to be added
	 */
	public void addFilter (Filter filter)
	{
		filters.add(filter);
	}
	
	/**
	 * Gets the result list of displayable tracks.
	 * 
	 * @return list of filtered tracks as a list
	 */
	public List<Track> getFilteredTracks ()
	{
		return filteredTracks;
	}
	
	/**
	 * Gets the filter error string.
	 * 
	 * @return filter error string, or null
	 */
	public String getFilterErrorString ()
	{
		return filterError;
	}

    //---------------- Public methods --------------------------------------
	
	/**
	 * Filters the list of tracks based on the set of filters. The resulting
	 * list of tracks can be retrieved using the <code>getFilteredTracks</code>
	 * method.
	 * 
	 * @return <code>true</code> if the filters were executed without errors, and 
	 * <code>false</code> otherwise
	 */
	public boolean executeFilterList () 
	{
		boolean result = false;
		int startSubIndex = -1;
		int stopSubIndex = -1;
		int subRange = -1;
		
		logger.trace("executeFilterList: " + this.hashCode());
		
		/*
		 * Clear the error string before starting.
		 */
		filterError = null;
		
		/*
		 * Get the initial filter logic (AND = true, OR = false).
		 */
		boolean currentAnd = (filters.get(0).getFilterLogic() == Filter.Logic.AND);
		
		/*
		 * Run through the filter list to see if we have a subgroup. Because representing subgroups
		 * is quite difficult using Pivot, I've decided that a single subgroup is all I will support.
		 * 
		 * Subgroup detection is simply a matter of finding a change in the logic from AND to OR or
		 * vice versa. We only allow two such changes: one to enter the subgroup, and (optionally)
		 * one to exit.
		 */
		int filtersLen = filters.getLength();
		logger.debug("executing " + filtersLen + ((filtersLen == 1) ? " filter" : " filters"));
		
		for (int i = 0; i < filtersLen; i++)
		{
			Filter filter = filters.get(i);
			
			if (	filter.getFilterLogic() != null
				&&	(filter.getFilterLogic() == Filter.Logic.AND) != currentAnd)
			{
				if (startSubIndex == -1)
				{
					startSubIndex = i;
				}
				else if (stopSubIndex == -1)
				{
					stopSubIndex = i;
				}
				else
				{
					logger.warn("multiple subgroups detected, start index=" + startSubIndex +
							", stop index=" + stopSubIndex);
					filterError = StringConstants.FILTER_ERROR_COMPLEX_LOGIC;
					break;
				}
				currentAnd = !currentAnd;
			}
		}
		
		/*
		 * Continue if we didn't find an error in subgroup detection.
		 */
		if (filterError == null)
		{
		
			/*
			 * Did we find a subgroup?
			 */
			if (startSubIndex != -1)
			{
				logger.debug("subgroup detected, start index=" + startSubIndex +
						", stop index=" + stopSubIndex);

				/*
				 * Determine the range of filters that constitute the group.
				 */
				if (stopSubIndex != -1)
				{
					subRange = stopSubIndex - startSubIndex;
				}
				else
				{
					subRange = filters.getLength() - startSubIndex;
				}

				/*
				 * Since we only support a single subgroup, we can just reorder the filter list to
				 * place the subgroup at the end. This makes it easier to evaluate the list against a
				 * track.
				 */
				Sequence<Filter> filterSubgroup = filters.remove(startSubIndex, subRange);
				for (int i = 0; i < subRange; i++)
				{
					filters.add(filterSubgroup.get(i));
				}
			}

			/*
			 * Now evaluate the filter list against the list of all tracks.
			 */
			result = evaluateFilters();
		}

		/*
		 * Dump all filters to the log if trace level is enabled.
		 */
		if (logger.isTraceEnabled())
		{
			dumpAllFilters();
		}
		
		return result;
	}
	
	/**
	 * Returns the set of filters as a string. This is used  when saving or 
	 * printing the results of a query.
	 * 
	 * @return string representation of the set of filters 
	 */
	public String getFiltersAsString ()
	{
		logger.trace("getFiltersAsString: " + this.hashCode());
		
		StringBuilder result = new StringBuilder();
		
		/*
		 * Get the initial filter logic (AND = true, OR = false).
		 */
		Filter.Logic logic = filters.get(0).getFilterLogic();
		boolean currentAnd = (logic == Filter.Logic.AND);

		/*
		 * Walk through all filters.
		 */
		for (int index = 0; index < filters.getLength(); index++)
		{
			Filter filter = filters.get(index);

			/*
			 * Detect a logic switch and update the current logic if so.
			 */
			logic = filter.getFilterLogic();
			if (logic != null && (logic == Filter.Logic.AND) != currentAnd)
			{
				currentAnd = !currentAnd;
			}
			
			/*
			 * Get the subject and operator.
			 */
			Filter.Subject subject = filter.getFilterSubject();
			Filter.Operator operator = filter.getFilterOperator();
			
			/*
			 * Append the current logic for all but the first filter.
			 */
			if (index != 0)
			{
				result.append(" [" + ((currentAnd == true) ? 
						Filter.Logic.AND.getDisplayValue() + "] " : Filter.Logic.OR.getDisplayValue() + "] "));
			}
			
			/*
			 * Append the filter data.
			 */
			result.append(subject.getDisplayValue() + " ");
			result.append(operator.getDisplayValue() + " ");
			result.append(filter.getFilterText());
		}
		
		return result.toString();
	}

    //---------------- Private methods -------------------------------------
	
	/*
	 * Evaluate the filters.
	 */
	private boolean evaluateFilters () 
	{
		boolean evaluation = true;
		boolean result;
		
		logger.trace("evaluateFilters: " + this.hashCode());

        /*
         * Create a list of tracks to be displayed. Make sure it's sorted by track name.
         */
        filteredTracks = new ArrayList<Track>();
        filteredTracks.setComparator(new Comparator<Track>() {
            @Override
            public int compare(Track t1, Track t2) {
                return t1.compareTo(t2);
            }
        });
        
        /*
         * Get the initial filter logic (AND = true, OR = false).
         */
		boolean initialAnd = (filters.get(0).getFilterLogic() == Filter.Logic.AND);
		logger.debug("initial filter logic is " + ((initialAnd == true) ? "AND" : "OR"));
		
		/*
		 * Walk through all tracks.
		 */
        List<Track> tracks = XMLHandler.getTracks();
        Iterator<Track> tracksIter = tracks.iterator();
        while (tracksIter.hasNext())
        {
        	Track track = tracksIter.next();
        	
    		boolean logicSwitch;
    		int index;
    		
    		/*
    		 * Reset to the initial filter logic.
    		 */
        	boolean currentAnd = initialAnd;
    		
    		/*
    		 * Process when the initial filter logic is AND.
    		 */
        	if (currentAnd == true)
        	{
        		logicSwitch = false;
        		
        		/*
        		 * Set the result to true, so we can exit out of the loop on the first false
        		 * result.
        		 */
        		result = true;
        		
        		/*
        		 * Loop through the filters until we run out, get a false result, or encounter
        		 * a subgroup.
        		 */
        		for (index = 0; index < filters.getLength() && result == true; index++)
        		{
        			Filter filter = filters.get(index);
        			
        			/*
        			 * A change in logic means we found a subgroup.
        			 */
        			Filter.Logic logic = filter.getFilterLogic();
        			if (logic != null && (logic == Filter.Logic.AND) != currentAnd)
        			{
        				logicSwitch = true;
        				currentAnd = !currentAnd;
        				break;
        			}
        			
        			/*
        			 * Check this track against the current filter.
        			 */
        			result = checkTrackAgainstFilter(track, filter);        			
        		}
        		
        		/*
        		 * If we detected a subgroup, handle that now.
        		 */
        		if (logicSwitch == true)
        		{
        			
            		/*
            		 * Set the result to false, so we can exit out of the loop on the first true
            		 * result.
            		 */
        			result = false;
        			
            		/*
            		 * Loop through the remaining filters until we run out or get a true result.
            		 */
            		for (; index < filters.getLength() && result == false; index++)
            		{
            			Filter filter = filters.get(index);
            			
            			/*
            			 * Check this track against the current filter.
            			 */
            			result = checkTrackAgainstFilter(track, filter);        			
            		}
        		}
        	}
        	
        	/*
        	 * Process when the initial filter logic is OR.
        	 */
        	else
        	{
        		logicSwitch = false;
        		
        		/*
        		 * Set the result to false.
        		 */
        		result = false;
        		
        		/*
        		 * Loop through the filters until we run out or encounter a subgroup. Once we get a
        		 * true result we can stop checking the track against the rest of the filters in the
        		 * OR group. But we have to check all filters, in case we have a subgroup.
        		 */
        		for (index = 0; index < filters.getLength(); index++)
        		{
        			Filter filter = filters.get(index);
        			
        			/*
        			 * A change in logic means we found a subgroup.
        			 */
        			Filter.Logic logic = filter.getFilterLogic();
        			if (logic != null && (logic == Filter.Logic.AND) != currentAnd)
        			{
        				logicSwitch = true;
        				currentAnd = !currentAnd;
        				break;
        			}
        			
        			/*
        			 * Check this track against the current filter if the result so far is false.
        			 */
        			if (result == false)
        			{
        				result = checkTrackAgainstFilter(track, filter);
        			}
        		}
        		
        		/*
        		 * If we detected a subgroup, and the result of checking the OR group is true, 
        		 * handle the subgroup now.
        		 */
        		if (logicSwitch == true && result == true)
        		{
        			
            		/*
            		 * Loop through the remaining filters until we run out or get a false result.
            		 */
            		for (; index < filters.getLength() && result == true; index++)
            		{
            			Filter filter = filters.get(index);
            			
            			/*
            			 * Check this track against the current filter.
            			 */
            			result = checkTrackAgainstFilter(track, filter);        			
            		}
        		}
        	}
        	
        	/*
        	 * Moment of truth: if we passed the filter list, add this track to be displayed.
        	 */
        	if (result == true)
        	{
        		logger.debug("track ID " + track.getID() + " matched all filters");
            	filteredTracks.add(track);
        	}
        	
        	/*
        	 * If the result is false, it could mean a simple mismatch, or it could mean one or
        	 * more filters has an error. If so, the error string is set and we should return
        	 * a false value.
        	 */
        	else if (filterError != null)
        	{
        		evaluation = false;
        	}
        }
        
        return evaluation;
	}
	
	/*
	 * Check a given track against a single filter.
	 */
	private boolean checkTrackAgainstFilter (Track track, Filter filter) 
	{
		boolean result = false;
		String filterText = filter.getFilterText();
		
		/*
		 * Handle all the different subject cases.
		 */
		switch (filter.getFilterSubject())
		{
		case ARTIST:
			String artist = track.getArtist();
			if (artist == null)
			{
				break;
			}
			
			/*
			 * For a String value, we only support the IS and CONTAINS operators. Technically,
			 * we could support IS_NOT, but that seems stupid.
			 */
			switch (filter.getFilterOperator())
			{
			case IS:
				if (artist.equals(filterText))
				{
					result = true;
				}
				break;
				
			case CONTAINS:
				if (artist.contains(filterText))
				{
					result = true;
				}
				break;
				
			default:
				filterError = 
						"'" + filter.getFilterOperator().getDisplayValue() + "'" +
						StringConstants.FILTER_ERROR_BAD_OPERATOR + 
						"'" + filter.getFilterSubject().getDisplayValue() + "'";
			}
			
			if (result == true)
			{
				logger.debug("Artist '" + filter.getFilterOperator().getDisplayValue() 
						+ "' match: " + artist);
			}
			
			break;

		case KIND:
			String kind = track.getKind();
			if (kind == null)
			{
				break;
			}
			
			/*
			 * For a String value, we only support the IS and CONTAINS operators.
			 */
			switch (filter.getFilterOperator())
			{
			case IS:
				if (kind.equals(filterText))
				{
					result = true;
				}
				break;
				
			case CONTAINS:
				if (kind.contains(filterText))
				{
					result = true;
				}
				break;
				
			default:
				filterError =  
						"'" + filter.getFilterOperator().getDisplayValue() + "'" +
						StringConstants.FILTER_ERROR_BAD_OPERATOR + 
						"'" + filter.getFilterSubject().getDisplayValue() + "'";
			}
			
			if (result == true)
			{
				logger.debug("Kind '" + filter.getFilterOperator().getDisplayValue() 
						+ "' match: " + kind);
			}
			
			break;
			
		case PLAYLIST_COUNT:
			int playlistCount = track.getTrkPlaylistCount();
			
			/*
			 * For an Integer value, we support all but the CONTAINS operator.
			 */
			switch (filter.getFilterOperator())
			{
			case IS:
				if (playlistCount == Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case IS_NOT:
				if (playlistCount != Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case GREATER:
				if (playlistCount >= Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case LESS:
				if (playlistCount <= Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			default:
				filterError =  
						"'" + filter.getFilterOperator().getDisplayValue() + "'" +
						StringConstants.FILTER_ERROR_BAD_OPERATOR + 
						"'" + filter.getFilterSubject().getDisplayValue() + "'";
			}
			
			if (result == true)
			{
				logger.debug("Playlist count '" + filter.getFilterOperator().getDisplayValue() 
						+ "' match: " + playlistCount);
			}
			
			break;
			
		case RATING:
			int rating = track.getCorrectedRating();
			
			/*
			 * For an Integer value, we support all but the CONTAINS operator.
			 */
			switch (filter.getFilterOperator())
			{
			case IS:
				if (rating == Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case IS_NOT:
				if (rating != Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case GREATER:
				if (rating >= Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case LESS:
				if (rating <= Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			default:
				filterError =  
						"'" + filter.getFilterOperator().getDisplayValue() + "'" +
						StringConstants.FILTER_ERROR_BAD_OPERATOR + 
						"'" + filter.getFilterSubject().getDisplayValue() + "'";
			}
			
			if (result == true)
			{
				logger.debug("Rating '" + filter.getFilterOperator().getDisplayValue() 
						+ "' match: " + rating);
			}
			
			break;
			
		case YEAR:
			int year = track.getYear();
			
			/*
			 * For an Integer value, we support all but the CONTAINS operator.
			 */
			switch (filter.getFilterOperator())
			{
			case IS:
				if (year == Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case IS_NOT:
				if (year != Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case GREATER:
				if (year >= Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			case LESS:
				if (year <= Integer.valueOf(filterText))
				{
					result = true;
				}
				break;
				
			default:
				filterError =  
						"'" + filter.getFilterOperator().getDisplayValue() + "'" +
						StringConstants.FILTER_ERROR_BAD_OPERATOR + 
						"'" + filter.getFilterSubject().getDisplayValue() + "'";
			}
			
			if (result == true)
			{
				logger.debug("Year '" + filter.getFilterOperator().getDisplayValue() 
						+ "' match: " + year);
			}
			
			break;
			
		case NAME:
			String name = track.getName();
			if (name == null)
			{
				break;
			}
			
			/*
			 * For a String value, we only support the IS and CONTAINS operators. Since this is the
			 * track name, do a case-insensitive comparison.
			 */
			switch (filter.getFilterOperator())
			{
			case IS:
				if (name.equalsIgnoreCase(filterText))
				{
					result = true;
				}
				break;
				
			case CONTAINS:
				if (name.toLowerCase().contains(filterText.toLowerCase()))
				{
					result = true;
				}
				break;
				
			default:
				filterError =  
						"'" + filter.getFilterOperator().getDisplayValue() + "'" +
						StringConstants.FILTER_ERROR_BAD_OPERATOR + 
						"'" + filter.getFilterSubject().getDisplayValue() + "'";
			}
			
			if (result == true)
			{
				logger.debug("Name '" + filter.getFilterOperator().getDisplayValue() 
						+ "' match: " + name);
			}
			
			break;
			
		default:
		}
		
		return result;
	}
	
	/*
	 * Dump all filters to the log. This only executes if the trace log level is enabled.
	 */
	private void dumpAllFilters ()
	{
		
		/*
		 * Get the maximum length of the various enum values, so we can control the width of the logged
		 * fields (for prettiness).
		 */
		int logicMax = 0;
		for (Filter.Logic value : Filter.Logic.values())
		{
			int len = value.getDisplayValue().length();
			if (len  > logicMax)
			{
				logicMax = len;
			}
		}
		
		int subjectMax = 0;
		for (Filter.Subject value : Filter.Subject.values())
		{
			int len = value.getDisplayValue().length();
			if (len  > subjectMax)
			{
				subjectMax = len;
			}
		}
		
		int operatorMax = 0;
		for (Filter.Operator value : Filter.Operator.values())
		{
			int len = value.getDisplayValue().length();
			if (len  > operatorMax)
			{
				operatorMax = len;
			}
		}
		
		/*
		 * Create the format string using the above determined width values.
		 */
		String formatStr = String.format("filter  %%2d: %%%1$ds %%%2$ds %%%3$ds %%s", 
				logicMax, subjectMax, operatorMax);
		
		/*
		 * Walk and log all filters.
		 */
		for (int index = 0; index < filters.getLength(); index++)
		{
			Filter filter = filters.get(index);
			
			Filter.Logic logic = filter.getFilterLogic();
			Filter.Subject subject = filter.getFilterSubject();
			Filter.Operator operator = filter.getFilterOperator();
			
			String logStr = String.format(formatStr, index, 
					((logic == null) ? "" : logic.getDisplayValue()), subject.getDisplayValue(),
					operator.getDisplayValue(), filter.getFilterText());
			
			logger.trace(logStr);
		}
	}
}
