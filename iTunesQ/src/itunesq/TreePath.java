package itunesq;

import org.apache.pivot.collections.ArrayList;

import ch.qos.logback.classic.Logger;

/**
 * Class that represents a path to an element in the tree of artist alternate
 * name manual overrides.
 * <p>
 * Pivot already includes a Path class for this purpose, but I'm using the 
 * path as the key in a HashMap, which requires an <code>equals</code>
 * method, which Pivot's class does not have.
 * 
 * @author Jon
 *
 */
public class TreePath
{

    //---------------- Class variables -------------------------------------
    
    private ArrayList<Integer> elements;
    private Logger logger = null;
    
    /**
     * Class constructor.
     * 
     * @param logger logger to use
     * @param elements array of integer elements
     */
    public TreePath (Logger logger, Integer... elements)
    {
        this.logger = logger;
        this.elements = new ArrayList<Integer>(elements);
    }

    //---------------- Getters and setters ---------------------------------
    
    /**
     * Gets the element value for a given index.
     * 
     * @param index index of the element value to return
     * @return element value for the given index
     */
    public Integer get(int index)
    {
        return elements.get(index);
    }

    /**
     * Gets the length of the elements.
     * 
     * @return length of the elements
     */
    public int getLength()
    {
        return elements.getLength();
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Determines if an instance of TreePath is equal to this one.
     * 
     * @param obj class instance to compare to this one
     * @return true if the input instance is equal to this one, otherwise false
     */
    @Override
    public boolean equals (Object obj)
    {
        boolean result = false;
        
        if (obj instanceof TreePath)
        {
            TreePath path = (TreePath) obj;
            if (this.getLength() == path.getLength())
            {
                int i;
                for (i = 0; i < elements.getLength(); i++)
                {
                    if (elements.get(i) != path.get(i))
                    {
                        break;
                    }
                }
                if (i >= elements.getLength())
                {
                    result = true;
                }
            }
        }
        
        return result;
    }

    /**
     * Generates a hash code value.
     * 
     * @return hash code value
     */
    @Override
    public int hashCode ()
    {
        int result = 17;
        int code = (elements.getLength() + 1) * result;        
        double multiplier = 2;
        StringBuilder elementStr = new StringBuilder();
        
        elementStr.append("[");
        if (elements != null)
        {
            for (int i = 0; i < elements.getLength(); i++)
            {
                double exponent = i * 10 + 1;
                code += (elements.get(i).intValue() + 1) * Math.pow(multiplier, exponent);
                if (i > 0)
                {
                    elementStr.append(",");
                }
                elementStr.append(elements.get(i));
            }
        }
        elementStr.append("]");
        
        result = 31 * result + code;
        logger.info("hash code " + result + " for: " + elementStr.toString());
        
        return result;
    }
}
