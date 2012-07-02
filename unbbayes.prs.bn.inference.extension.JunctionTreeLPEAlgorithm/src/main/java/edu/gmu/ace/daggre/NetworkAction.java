package edu.gmu.ace.daggre;

import java.util.List;


/**
 * This interface represents any action performed between
 * {@link MarkovEngineImpl#startNetworkActions()}
 * and 
 * {@link MarkovEngineImpl#commitNetworkActions(long)}.
 * Objects of classes implementing this interface basically
 * implements a 
 * <a href="http://en.wikipedia.org/wiki/Command_pattern">Command design pattern<a>
 * 
 * @author Shou Matsumoto
 *
 */
public interface NetworkAction extends QuestionEvent {
	/**
	 * Runs the associated command.
	 */
	void execute();
	
	/**
	 * Undo the action performed at {@link #execute()}.
	 * This is an optional operation, so not all
	 * implementations of this interface will
	 * implement this method.
	 * @throws UnsupportedOperationException if {@link #execute()} cannot
	 * be reverted.
	 */
	void revert() throws UnsupportedOperationException;
	
	
	/**
	 * Actions which changes the structure of a network
	 * may require the network and/or the assets to be rebuilt.
	 * @return true if this action causes changes in network structure.
	 */
	boolean isStructureChangeAction();
	
	/**
	 * @return the identifier of the transaction containing this action.
	 */
	Long getTransactionKey();
	
	/**
	 * If this action is related to some user ID
	 * (e.g. the action is a trade performed by a
	 * given user), then this 
	 * method returns the user ID.
	 * @return the user id.
	 */
	Long getUserId();
	
	/**
	 * @return the identifier of the question associated with this network action.
	 * If this action is not associated with any ID, then this method shall
	 * return null.
	 */
	Long getQuestionId();
	
	/**
	 * @return : list of question identifiers considered
	 * as assumptions of this network action.
	 * If assumptions does not apply for this network action,
	 * this method shall return null.
	 * @see #getAssumedStates()
	 */
	List<Long> getAssumptionIds(); 
	
	/**
	 * @return states of questions returned by {@link #getAssumptionIds()}.
	 * Objects in this list and in {@link #getAssumptionIds()}
	 * are related by their indexes (e.g. {@link List#indexOf(Object)}). 
	 * @see #getAssumptionIds()
	 */
	List<Integer> getAssumedStates();
}
