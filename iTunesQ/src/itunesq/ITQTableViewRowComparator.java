package itunesq;

import java.text.ParseException;
import java.util.Comparator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.TableView;

import ch.qos.logback.classic.Logger;

/**
 * Class that compares two table view rows during a sort operation. This gets
 * control when the user clicks on a column header in a table of tracks or
 * artists. Stolen without remorse from Pivot code, cleaned up to meet my
 * standards, and modified to handle columns that contain numerical data.
 * 
 * @author Jon
 *
 */
public class ITQTableViewRowComparator implements Comparator<Object>
{

    // ---------------- Private variables -----------------------------------

    private TableView tableView;
    private Logger logger;

    /*
     * Numerical data can either be a simple number ("11") or a time string
     * ("02:34").
     */
    private enum NumericalType
    {
        NUMBER, TIME;
    }

    private Map<String, NumericalType> numericalColumns = null;

    /**
     * Class constructor specifying the table view object with which we'll be
     * working.
     * 
     * @param tableView table view object containing the rows to be sorted
     * @param logger logger for logging messages
     */
    public ITQTableViewRowComparator(TableView tableView, Logger logger)
    {
        if (tableView == null)
        {
            throw new IllegalArgumentException();
        }

        this.tableView = tableView;
        this.logger = logger;

        /*
         * Initialize the list of column names that contain numerical data.
         */
        numericalColumns = new HashMap<String, NumericalType>();
        numericalColumns.put(TrackDisplayColumns.ColumnNames.NUMBER.getDisplayValue(), NumericalType.NUMBER);
        numericalColumns.put(TrackDisplayColumns.ColumnNames.DURATION.getDisplayValue(), NumericalType.TIME);
        numericalColumns.put(StringConstants.ARTIST_COLUMN_LOCAL_TRACKS_NAME, NumericalType.NUMBER);
        numericalColumns.put(StringConstants.ARTIST_COLUMN_LOCAL_TIME_NAME, NumericalType.TIME);
        numericalColumns.put(StringConstants.ARTIST_COLUMN_REMOTE_TRACKS_NAME, NumericalType.NUMBER);
        numericalColumns.put(StringConstants.ARTIST_COLUMN_REMOTE_TIME_NAME, NumericalType.TIME);
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
         * The sort dictionary contains the column that is being sorted, and
         * whether or not the sort is ascending or descending.
         */
        TableView.SortDictionary sort = tableView.getSort();

        /*
         * Continue if we have something to sort, which I assume should always
         * be the case unless something has gone awry.
         */
        int sortLen = sort.getLength();
        if (sortLen > 0)
        {
            result = 0;

            /*
             * Row objects are of type HashMap, which implements the Dictionary
             * interface.
             */
            Dictionary<String, ?> row1 = (Dictionary<String, ?>) o1;
            Dictionary<String, ?> row2 = (Dictionary<String, ?>) o2;

            /*
             * Loop through the sort objects. For the life of me I can't imagine
             * there being more than one, but I did steal this code, so I kept
             * this loop.
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
                 * The two objects being sorted should always be strings. For
                 * example, if the "Name" column was clicked, the two values
                 * should be item names.
                 */
                Object value1 = row1.get(columnName);
                Object value2 = row2.get(columnName);

                /*
                 * Do the comparison. We handle null values as being less than,
                 * unless of course if both values are null they are equal.
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
                 */
                else
                {

                    /*
                     * Check if the column is one containing numerical data.
                     */
                    boolean numerical = false;
                    boolean time = false;

                    for (String numericalColumnName : numericalColumns)
                    {

                        /*
                         * The column name matches. Check the type.
                         */
                        if (numericalColumnName.equals(columnName))
                        {
                            NumericalType numericalType = numericalColumns.get(numericalColumnName);

                            switch (numericalType)
                            {
                            case NUMBER:
                                numerical = true;
                                break;

                            case TIME:
                                time = true;
                                break;

                            default:
                                throw new InternalErrorException(true,
                                        "unknown numerical column type '" + numericalType + "'");
                            }
                        }
                    }

                    /*
                     * For a numerical column, convert the values to actual
                     * integers and compare them.
                     */
                    if (numerical == true)
                    {
                        int numValue1 = Integer.parseUnsignedInt((String) value1);
                        int numValue2 = Integer.parseUnsignedInt((String) value2);
                        result = numValue1 > numValue2 ? 1 : numValue1 < numValue2 ? -1 : 0;
                    }

                    /*
                     * For a time column, convert the values to milliseconds and
                     * compare them.
                     */
                    else if (time == true)
                    {
                        try
                        {
                            long timeValue1 = Utilities.parseTime((String) value1);
                            long timeValue2 = Utilities.parseTime((String) value2);
                            result = timeValue1 > timeValue2 ? 1 : timeValue1 < timeValue2 ? -1 : 0;
                        }
                        catch (ParseException e)
                        {
                            MainWindow.logException(logger, e);
                            throw new InternalErrorException(true, e.getMessage());
                        }
                    }

                    /*
                     * For all other columns, use the String Comparable
                     * interface compareTo method.
                     */
                    else
                    {
                        result = ((Comparable<Object>) value1).compareTo(value2);
                    }
                }

                /*
                 * Modify the result according to the sort direction. Ascending
                 * does nothing to the result, while descending reverses it.
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