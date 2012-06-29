package edu.gmu.ace.daggre;


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
}
