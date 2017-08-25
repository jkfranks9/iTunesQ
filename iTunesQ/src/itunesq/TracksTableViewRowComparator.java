package itunesq;

import java.util.Comparator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.TableView;

/**
 * Class that compares two table view rows during a sort operation. This gets 
 * control when the user clicks on a column header in a table of tracks. 
 * Stolen without remorse from Pivot code, cleaned up to meet my standards, 
 * and modified to handle the "Number" column.
 * 
 * @author Jon
 *
 */
public class TracksTableViewRowComparator implements Comparator<Object>
{
	
    //---------------- Private variables -----------------------------------
	
    private TableView tableView;

    /**
     * Class constructor specifying the table view object with which we'll be working.
     * 
     * @param tableView table view object containing the list of tracks being sorted
     */
    public TracksTableViewRowComparator(TableView tableView)
    {
        if (tableView == null)
        {
            throw new IllegalArgumentException();
        }

        this.tableView = tableView;
    }
    
    /**
     * Compares two rows in the table view.
     * 
     * @param o1 first row being compared
     * @param o2 second row being compared
     * @return negative integer, zero, or positive integer if the first object
     * is less than, equal to, or greater than the second object
     */
    @Override
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2)
    {
        int result;

        /*
         * The sort dictionary contains the column that is being sorted, and whether or not the sort
         * is ascending or descending.
         */
        TableView.SortDictionary sort = tableView.getSort();

        /*
         * Continue if we have something to sort, which I assume should always be the case unless something
         * has gone awry.
         */
        int sortLen = sort.getLength();
        if (sortLen > 0)
        {
            result = 0;
            
            /*
             * Track row objects are of type HashMap, which implements the Dictionary interface.
             */
            Dictionary<String, ?> row1 = (Dictionary<String, ?>)o1;
            Dictionary<String, ?> row2 = (Dictionary<String, ?>)o2;

            /*
             * Loop through the sort objects. For the life of me I can't imagine there being more
             * than one, but I did steal this code, so I kept this loop.
             */
            for (int i = 0; i < sortLen && result == 0; i++)
            {
            	
            	/*
            	 * Get the sort information.
            	 */
                Dictionary.Pair<String, SortDirection> pair = sort.get(i);
                String columnName = pair.key;
                SortDirection sortDirection = sort.get(columnName);

                /*
                 * The two objects being sorted should always be strings. For example, if the "Name"
                 * column was clicked, the two values should be track names.
                 */
                Object value1 = row1.get(columnName);
                Object value2 = row2.get(columnName);

                /*
                 * Do the comparison. We handle null values as being less than, unless of course
                 * if both values are null they are equal.
                 */
                if (value1 == null && value2 == null)
                {
                    result = 0;
                }
                else if (value1 == null)
                {
                    result = -1;
                }
                else if (value2 == null)
                {
                    result = 1;
                }
                
                /*
                 * Both values exist, and we know they are strings.
                 * if both values exist, then we use the Comparable interface compareTo method.
                 */
                else
                {
                	
                	/*
                	 * For the "Number" column, convert the values to actual integers and compare them.
                	 */
                	if (columnName.equals(TrackDisplayColumns.ColumnNames.NUMBER.getDisplayValue()))
                	{
                		int numValue1 = Integer.parseUnsignedInt((String) value1);
                		int numValue2 = Integer.parseUnsignedInt((String) value2);
                		result = numValue1 > numValue2 ? 1 : numValue1 < numValue2 ? -1 : 0;
                	}
                	
                	/*
                	 * For all other columns, use the String comparable interface compareTo method.
                	 */
                	else
                	{
                		result = ((Comparable<Object>)value1).compareTo(value2);
                	}
                }

                /*
                 * Modify the result according to the sort direction. Ascending does nothing to the
                 * result, while descending reverses it.
                 */
                result *= (sortDirection == SortDirection.ASCENDING ? 1 : -1);
            }
        }
        
        /*
         * For some reason we have nothing to sort, so return equal.
         */
        else
        {
            result = 0;
        }

        return result;
    }
}