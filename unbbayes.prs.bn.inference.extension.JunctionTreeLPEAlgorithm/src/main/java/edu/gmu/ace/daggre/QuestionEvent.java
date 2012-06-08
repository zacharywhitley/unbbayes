
package edu.gmu.ace.daggre;

import java.util.Date;

/**
 * This class represents an entry in the history of
 * trades in the DAGGRE project.
 * @author Shou Matsumoto
 * @see MarkovEngineInterface
 */
public interface QuestionEvent {
	/**
	 * @return the timestamp of this event.
	 */
	Date getDateTime();
	
	/**
	 * @return probability at this time of event.
	 * @see #getDateTime(). 
	 */
	Float getPercent();
	
	/**
	 * @return the ID which can uniquely identify a trade 
	 * @see MarkovEngineInterface#addTrade(long, Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, Boolean)
	 */
	Long getTradeId();
}
