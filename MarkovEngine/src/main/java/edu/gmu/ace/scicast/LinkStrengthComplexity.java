package edu.gmu.ace.scicast;

/**
 * This is an extension of {@link LinkStrength} which also includes 
 * the link complexity factor.
 * @author Shou Matsumoto
 * @see MarkovEngineInterface#getComplexityFactor(java.util.Map)
 */
public interface LinkStrengthComplexity extends LinkStrength {

	/**
	 * @return the complexity factor you would get by removing this link.
	 * @see MarkovEngineInterface#getComplexityFactor(java.util.Map)
	 */
	public float getComplexityFactor();
	
}
