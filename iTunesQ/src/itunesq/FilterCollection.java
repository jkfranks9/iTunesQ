package itunesq;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class representing a collection of filters.
 * 
 * Filters can be ANDed or ORed. In addition, subgroups of filters can exist. For example:
 * 
 *     ARTIST CONTAINS "Oyster"
 * AND YEAR   GREATER  "1975"
 * AND (
 *           KIND CONTAINS "MPEG"
 *       OR  KIND CONTAINS "AAC"
 *     )
 */
public class FilterCollection
{

    //---------------- Class variables -------------------------------------
	
	/*
	 * A filter collection is a List of Object.
	 */
	private List<Filter> filters;
	
	/*
	 * Result list of filtered tracks.
	 */
	private List<Track> filteredTracks;

    //---------------- Private variables -----------------------------------
	
	private Logger logger = null;
	
	/**
	 * Constructor.
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
		
		logger.trace("FilterCollection constructor: " + this.hashCode());
	}

    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Get the filter collection.
	 * 
	 * @return The filter collection.
	 */
	public List<Filter> getFilters ()
	{
		return filters;
	}
	
	/**
	 * Add a filter to the filter collection.
	 * 
	 * @param filter The filter to be added.
	 */
	public void addFilter (Filter filter)
	{
		filters.add(filter);
	}
	
	/**
	 * Get the result list of displayable tracks.
	 * 
	 * @return Result list of tracks in setTableData() format.
	 */
	public List<Track> getFilteredTracks ()
	{
		return filteredTracks;
	}

    //---------------- Public methods --------------------------------------
	
	/**
	 * Filter the list of tracks based on the list of filters.
	 * 
	 * @throws FilterException 
	 */
	public void executeFilterList () 
			throws FilterException
	{
		int startSubIndex = -1;
		int stopSubIndex = -1;
		int subRange = -1;
		
		logger.trace("executeFilterList: " + this.hashCode());
		
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
					throw new FilterException("filter logic is too complex");
				}
				currentAnd = !currentAnd;
			}
		}
		
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
		 * Dump all filters to the log if trace level is enabled.
		 */
		if (logger.isTraceEnabled())
		{
			dumpAllFilters();
		}
		
		/*
		 * Now evaluate the filter list against the list of all tracks.
		 */
		evaluateFilters();
	}

    //---------------- Private methods -------------------------------------
	
	/*
	 * Evaluate the filters.
	 */
	private void evaluateFilters () 
			throws FilterException
	{
		boolean result;
		
		logger.trace("evaluateFilters: " + this.hashCode());

        /*
         * Create a list suitable for the setTableData() method. This holds the tracks to be
         * displayed. Make sure it's sorted by track name.
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
		boolean currentAnd = (filters.get(0).getFilterLogic() == Filter.Logic.AND);
		logger.debug("initial filter logic is " + ((currentAnd == true) ? "AND" : "OR"));
		
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
        			if ((filter.getFilterLogic() == Filter.Logic.AND) != currentAnd)
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
        		 * Set the result to false, so we can exit out of the loop on the first true
        		 * result.
        		 */
        		result = false;
        		
        		/*
        		 * Loop through the filters until we run out, get a true result, or encounter
        		 * a subgroup.
        		 */
        		for (index = 0; index < filters.getLength() && result == false; index++)
        		{
        			Filter filter = filters.get(index);
        			
        			/*
        			 * A change in logic means we found a subgroup.
        			 */
        			if ((filter.getFilterLogic() == Filter.Logic.AND) != currentAnd)
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
            		 * Set the result to true, so we can exit out of the loop on the first false
            		 * result.
            		 */
        			result = true;
        			
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
            	filteredTracks.add(track);
        	}
        }
	}
	
	/*
	 * Check a given track against a single filter.
	 */
	private boolean checkTrackAgainstFilter (Track track, Filter filter) 
			throws FilterException
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
				throw new FilterException(
						"'" + filter.getFilterOperator().getDisplayValue() + 
						"' operator not applicable to " + 
						filter.getFilterSubject().getDisplayValue());
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
				throw new FilterException(
						"'" + filter.getFilterOperator().getDisplayValue() + 
						"' operator not applicable to " + 
						filter.getFilterSubject().getDisplayValue());
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
				throw new FilterException(
						"'" + filter.getFilterOperator().getDisplayValue() + 
						"' operator not applicable to " + 
						filter.getFilterSubject().getDisplayValue());
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
				throw new FilterException(
						"'" + filter.getFilterOperator().getDisplayValue() + 
						"' operator not applicable to " + 
						filter.getFilterSubject().getDisplayValue());
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
				throw new FilterException(
						"'" + filter.getFilterOperator().getDisplayValue() + 
						"' operator not applicable to " + 
						filter.getFilterSubject().getDisplayValue());
			}
			
			if (result == true)
			{
				logger.debug("Year '" + filter.getFilterOperator().getDisplayValue() 
						+ "' match: " + year);
			}
			
			break;
			
		case NAME:
			String name = track.getKind();
			if (name == null)
			{
				break;
			}
			
			/*
			 * For a String value, we only support the IS and CONTAINS operators.
			 */
			switch (filter.getFilterOperator())
			{
			case IS:
				if (name.equals(filterText))
				{
					result = true;
				}
				break;
				
			case CONTAINS:
				if (name.contains(filterText))
				{
					result = true;
				}
				break;
				
			default:
				throw new FilterException(
						"'" + filter.getFilterOperator().getDisplayValue() + 
						"' operator not applicable to " + 
						filter.getFilterSubject().getDisplayValue());
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
