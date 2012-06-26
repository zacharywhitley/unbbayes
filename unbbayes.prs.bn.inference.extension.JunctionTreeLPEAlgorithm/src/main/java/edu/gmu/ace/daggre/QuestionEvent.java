
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
	 * @return probability at this time of event.
	 * @see #getDateTime(). 
	 */
	List<Float> getPercent();
	
	/**
	 * @return the ID which can uniquely identify a trade 
	 * @see MarkovEngineInterface#addTrade(long, Date, String, long, long, List, List, List, boolean)
	 */
	String getTradeId();
}
