package edu.gmu.ace.daggre;

import java.util.List;


/**
 * Classes implementing this interface represents
 * entries in the history of trades. 
 * 
 * <br/>
 * There are several frameworks that can
 * store records into database from beans (classes with only getters and setters)
 * like this.
 * @author Shou Matsumoto
 * @see DAGGREUnBBayesFacade
 */
public interface TradeHistory extends EditHistory {
	/**
	 * @return the networkID
	 */
	public int getNetworkID();
	/**
	 * @param networkID the networkID to set
	 */
	public void setNetworkID(int networkID);
	/**
	 * @return the userID
	 */
	public long getUserID();
	/**
	 * @param userID the userID to set
	 */
	public void setUserID(long userID);
	/**
	 * @return the questionID
	 */
	public long getQuestionID();
	/**
	 * @param questionID the questionID to set
	 */
	public void setQuestionID(long questionID);
	/**
	 * @return the oldValues
	 */
	public List<Float> getOldValues();
	/**
	 * @param oldValues the oldValues to set
	 */
	public void setOldValues(List<Float> oldValues);
	/**
	 * @return the newValues
	 */
	public List<Float> getNewValues();
	/**
	 * @param newValues the newValues to set
	 */
	public void setNewValues(List<Float> newValues);
	/**
	 * @return the assumptionIDs
	 */
	public List<Integer> getAssumptionIDs() ;
	/**
	 * @param assumptionIDs the assumptionIDs to set
	 */
	public void setAssumptionIDs(List<Integer> assumptionIDs);
	/**
	 * @return the assumedStates
	 */
	public List<Integer> getAssumedStates();
	/**
	 * @param assumedStates the assumedStates to set
	 */
	public void setAssumedStates(List<Integer> assumedStates);
	
	// TODO fill with more getters and setters
}
