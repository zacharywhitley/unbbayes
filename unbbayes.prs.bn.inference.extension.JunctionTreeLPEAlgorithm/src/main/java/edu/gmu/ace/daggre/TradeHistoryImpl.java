
package edu.gmu.ace.daggre;

import java.util.List;

/**
 * Default implementation of {@link TradeHistory}.
 * @author Shou Matsumoto
 */
public class TradeHistoryImpl implements TradeHistory {

	private int networkID; 
	private long userID; 
	private long questionID; 
	private List<Float> oldValues; 
	private List<Float> newValues; 
	private List<Integer> assumptionIDs; 
	private List<Integer> assumedStates; 
	/**
	 * 
	 */
	public TradeHistoryImpl() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the networkID
	 */
	public int getNetworkID() {
		return networkID;
	}
	/**
	 * @param networkID the networkID to set
	 */
	public void setNetworkID(int networkID) {
		this.networkID = networkID;
	}
	/**
	 * @return the userID
	 */
	public long getUserID() {
		return userID;
	}
	/**
	 * @param userID the userID to set
	 */
	public void setUserID(long userID) {
		this.userID = userID;
	}
	/**
	 * @return the questionID
	 */
	public long getQuestionID() {
		return questionID;
	}
	/**
	 * @param questionID the questionID to set
	 */
	public void setQuestionID(long questionID) {
		this.questionID = questionID;
	}
	/**
	 * @return the oldValues
	 */
	public List<Float> getOldValues() {
		return oldValues;
	}
	/**
	 * @param oldValues the oldValues to set
	 */
	public void setOldValues(List<Float> oldValues) {
		this.oldValues = oldValues;
	}
	/**
	 * @return the newValues
	 */
	public List<Float> getNewValues() {
		return newValues;
	}
	/**
	 * @param newValues the newValues to set
	 */
	public void setNewValues(List<Float> newValues) {
		this.newValues = newValues;
	}
	/**
	 * @return the assumptionIDs
	 */
	public List<Integer> getAssumptionIDs() {
		return assumptionIDs;
	}
	/**
	 * @param assumptionIDs the assumptionIDs to set
	 */
	public void setAssumptionIDs(List<Integer> assumptionIDs) {
		this.assumptionIDs = assumptionIDs;
	}
	/**
	 * @return the assumedStates
	 */
	public List<Integer> getAssumedStates() {
		return assumedStates;
	}
	/**
	 * @param assumedStates the assumedStates to set
	 */
	public void setAssumedStates(List<Integer> assumedStates) {
		this.assumedStates = assumedStates;
	}

}
