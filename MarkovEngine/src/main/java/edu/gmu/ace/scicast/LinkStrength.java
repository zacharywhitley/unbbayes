/**
 * 
 */
package edu.gmu.ace.scicast;

/**
 * @author Shou Matsumoto
 * @see MarkovEngineInterface#getLinkStrengthList(java.util.List, java.util.List)
 */
public interface LinkStrength {
	public Long getParent();
	
	public Long getChild();
	
	public float getLinkStrength();
}
