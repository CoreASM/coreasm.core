/*	
 * IntegerRangeElement.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2006 George Ma
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.number;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;

/** 
 *	Class for Number Range Elements;
 *   
 *  @author  George Ma
 *  
 */
public class NumberRangeElement extends Element implements Enumerable {
	
	private final double start;
    private final double end;
    private final double step;
    private final int hashCode;
    private List<Element> enumeration = null;
	
    private static final double DEFAULT_STEP = 1.0;
	
    /**
     * Creates a new NumberRangeElement
     * @param start - lower bound of range
     * @param end - upper bound of range
     */
    public NumberRangeElement(double start, double end) {
		this(start,end,DEFAULT_STEP);
	}
    
    /**
     * Creates a new NumberRangeElement
     * @param start - lower bound of range
     * @param end - upper bound of range
     * @param step - range increment size, must be greater than 0
     */
    public NumberRangeElement(double start, double end, double step) {
        if (step <= 0) {
            throw new IllegalArgumentException("Step size for a NumberRangeElement must be greater than 0.");
        }
        if (start > end) {
            throw new IllegalArgumentException("Start value must be less than end value for NumberRangeElement.");
        }
        this.start = start;
        this.end = end;
        this.step = step;
        this.hashCode = new Double(start + end + step).hashCode();
    }
	
    public String getBackground() {
    	return NumberRangeBackgroundElement.NUMBER_RANGE_BACKGROUND_NAME;
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return step == DEFAULT_STEP ? start +".."+ end : start +".."+ end+":"+step;
	}
		
    /* (non-Javadoc)
     * @see org.coreasm.engine.absstorage.Enumerable#enumerate()
     */
    public Collection<Element> enumerate() {
    	return getIndexedView();
    }

    /**
     * Compares this Element to the specified Element. 
     * The result is <code>true</code> if the argument 
     * is not null and is considered to be equal to this Element.
     * 
     * @param anElement the Element to compare with.
     * @return <code>true</code> if the Elements are equal; <code>false</code> otherwise.
     * @throws IllegalArgumentException if <code>anElement</code> is not an instance
     * of <code>Element</code>
     */
    public boolean equals(Object anElement) {
        
        boolean equals = false;

        // if both java objects are idential, no further checks are required
        if (super.equals(anElement))
            equals = true;
        // else both java objects are not identical, have to check that
        // both are number elements, and both have the same numerical value
        else
        {
            // both number elements
            if (anElement instanceof NumberRangeElement)
            {
                NumberRangeElement other = (NumberRangeElement)anElement;
                
                // if the current number and the other number equal each other
                // then objects are equal
                if ((start == other.start) && 
                    (end == other.end) &&
                    (step == other.step)) {
                    return true;
                }
            }
        }
        
        return equals;
    }
    
    public boolean contains(Element e) {
        if (NumberUtil.isInteger(e)) {
            NumberElement n = (NumberElement) e;
            
            return (n.value >= start) 
            		&& (n.value <= end) 
            		&& NumberUtil.isInteger(NumberElement.getInstance((n.value - start) / step));
        }
        
        return false;
    }

	@Override
	public int hashCode() {
		return hashCode;
	}

    /**
     * @return the end of this number range
     */
    public double getEnd() {
        return end;
    }

    /**
     * @return the start of this number range
     */
    public double getStart() {
        return start;
    }

    /**
     * @return the step interval of this number range
     */
    public double getStep() {
        return step;
    }

	public List<Element> getIndexedView() throws UnsupportedOperationException {
    	if (enumeration == null) {
	        enumeration = new ArrayList<Element>();
	        
	        for (double n = start; n <= end; n+=step) {
	            enumeration.add(NumberElement.getInstance(n));
	        }
	        
    	} 
    	return enumeration;
	}

	public boolean supportsIndexedView() {
		return true;
	}

	public int size() {
		return getIndexedView().size();
	}
}
