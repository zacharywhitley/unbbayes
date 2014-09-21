
package edu.gmu.ace.scicast;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * This class represents an entry in the history of
 * trades in the DAGGRE project.
 * @author Shou Matsumoto
 * @see MarkovEngineInterface
 */
public interface QuestionEvent extends Serializable {
	/**
	 * @return the date/time when this action request was originally created.
	 * This is likely to be the moment a user actually requested the trade.
	 */
	Date getWhenCreated();
	
	/**
	 * @return the date/time when this action was actually executed in the currently running system
	 * (more precisely, the moment it finished executing in the current running system).
	 */
	Date getWhenExecutedFirstTime();
	
	/**
	 * @return probability at this time of event.
	 * @see #getWhenExecutedFirstTime(). 
	 */
	List<Float> getOldValues();
	
	/**
	 * @return probability afer this time of event.
	 * @see #getWhenExecutedFirstTime(). 
	 */
	List<Float> getNewValues();
	
	/**
	 * @return the ID which can uniquely identify a trade 
	 * @see MarkovEngineInterface#addTrade(Long, Date, String, long, long, List, List, List, boolean)
	 */
	String getTradeId();
	
	/**
	 * @return the ID of the question related to this event.
	 */
	Long getQuestionId();
	
	/**
	 * @return if this question event represents a resolution of a question,
	 * this method the settled state.
	 */
	Integer getSettledState();
	
	/**
	 * @return true if this entry is a
	 * "corrective" trade. That is, a 
	 * virtual trade which will force the current probability
	 * of the system to become equal to {@link TradeSpecification#getOldProbabilities()}.
	 * This is useful for distinguishing whether this entry in the history was caused
	 * by an actual trade (or question settlement), or it was automatically
	 * performed by the engine for correcting the probabilities.
	 */
	Boolean isCorrectiveTrade();
	
	/**
	 * If this action is related to some user ID
	 * (e.g. the action is a trade performed by a
	 * given user), then this 
	 * method returns the user ID.
	 * @return the user id.
	 */
	Long getUserId();
}
