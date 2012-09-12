
package edu.gmu.ace.daggre;

import java.util.Date;
import java.util.List;

/**
 * This class represents an entry in the history of
 * trades in the DAGGRE project.
 * @author Shou Matsumoto
 * @see MarkovEngineInterface
 */
public interface QuestionEvent {
	/**
	 * @return the date/time when this action was created.
	 */
	Date getWhenCreated();
	
	/**
	 * @return the date/time when this action was actually executed
	 * (more precisely, the moment it finished executing).
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
}
