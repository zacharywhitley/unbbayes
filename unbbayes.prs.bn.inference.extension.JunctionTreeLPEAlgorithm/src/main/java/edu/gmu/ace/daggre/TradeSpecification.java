/**
 * 
 */
package edu.gmu.ace.daggre;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * This is a common interface for specifying a trade.
 * @author Shou Matsumoto
 * @see MarkovEngineInterface#previewBalancingTrades(long, long, java.util.List, java.util.List).
 *
 */
public interface TradeSpecification extends Serializable {
	
	/** The ID of the user making the trade */
	public Long getUserId();
	
	/** Question to be traded */
	public Long getQuestionId();
	
	/** IDs of the questions to be assumed */
	public List<Long> getAssumptionIds();
	
	/** States assumed in the questions in {@link #getAssumptionIds()} */
	public List<Integer> getAssumedStates();
	
	/** 
	 * Probability to be used in the trade.
	 * That is, this is the value to be inserted as newValue in 
	 * {@link MarkovEngineInterface#addTrade(long, Date, long, long, long, List, List, List, List, Boolean)} to balance the trade.
	 * For example, suppose T is the target question (i.e. the one returned from {@link #getQuestionId()}) 
	 * with states t1 and t2, and A1 and A2 are assumptions (i.e. {@link #getAssumptionIds()}) with possible states (a11, a12), and (a21 , a22) respectively.
	 * Also suppose that {@link #getAssumedStates()} is empty or null (i.e. the returned list will represent all cells in a conditional probability distribution).
	 * Then, the list is be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * <br/><br/>
	 * If assumedStates is set to {0, 0} (i.e. assumed A1 = a11 and A2 = a21), then:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * <br/><br/>
	 * If assumedStates is set to {1, null} (i.e. assumed A1 = a12 and no state assumed for A2), then:<br/>
	 * index 0 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a22)<br/>
	 */
	public List<Float> getProbabilities();
	
	/**
	 * Updates the value of {@link #getProbabilities()}
	 * @param probabilities
	 */
	public void setProbabilities(List<Float> probabilities);
	
	/**
	 * @return the probability before the trade.
	 */
	public List<Float> getOldProbabilities();
	
	/**
	 * @param oldProb :  the probability before the trade.
	 */
	public void setOldProbabilities(List<Float> oldProb);
	

}
