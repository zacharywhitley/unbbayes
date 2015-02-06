/**
 * 
 */
package edu.gmu.ace.scicast;

import java.io.Serializable;

/**
 * Classes implementing this interface represents a tuple with complexity factors/metrics
 * before/after adding links, and the specification of the link used to obtain such complexity factor.
 * This is used in {@link MarkovEngineInterface#getLinkComplexitySuggestion(Long, Long, int, boolean)}
 * and {@link MarkovEngineInterface#getLinkComplexitySuggestions(java.util.List, java.util.List, int, boolean, boolean)}
 * @author Shou Matsumoto
 * @see MarkovEngineImpl
 * @see LinkSugestionImpl
 */
public interface LinkSuggestion extends Serializable {

	/**
	 * @return : the metric (complexity factor) before including the link.
	 */
	public int getPriorComplexity();
	
	/**
	 * @return the metric (complexity factor) after including the link.
	 */
	public int getPosteriorComplexity();
	
	/**
	 * @return : a link is a directed arc from parent question to child question. This is the Id of the parent question.
	 */
	public Long getSuggestedParentId();
	
	/**
	 * @return : a link is a directed arc from parent question to child question. This is the Id of the child question.
	 */
	public Long getSuggestedChildId();
	
}
