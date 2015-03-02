/**
 * 
 */
package edu.gmu.ace.scicast;

import java.io.Serializable;

/**
 * This interface represents a java bean which holds 3 values:
 * 2 questions (to represent an arc that links two questions) and a numeric value
 * which represents the strength of such link.
 * @author Shou Matsumoto
 * @see MarkovEngineInterface#getLinkStrengthList(java.util.List, java.util.List)
 */
public interface LinkStrength extends Serializable {
	
	/**
	 * @return : one of the question linked by the arc. This is the question where the arc
	 * comes from.
	 */
	public Long getParent();
	
	/**
	 * @return : the other question in the link.  This is the question where the arc
	 * goes to.
	 */
	public Long getChild();
	
	/**
	 * @return : a numeric metric for the strength of the link in terms of probabilities.
	 */
	public float getLinkStrength();
}
