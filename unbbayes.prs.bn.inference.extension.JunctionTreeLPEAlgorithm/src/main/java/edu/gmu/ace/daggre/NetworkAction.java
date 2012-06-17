package edu.gmu.ace.daggre;

import java.util.Date;

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
public interface NetworkAction {
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
	 * @return the date/time when this action was created.
	 */
	Date getWhenCreated();
	
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
}
