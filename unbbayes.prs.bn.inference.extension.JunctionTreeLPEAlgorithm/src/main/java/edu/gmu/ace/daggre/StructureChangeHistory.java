package edu.gmu.ace.daggre;

/**
 * Classes implementing this interface represents
 * entries in the history of edits
 * in the bayesian network structure or asset table structure
 * ({@link StructureChangeHistory}). 
 * <br/>
 * There are several frameworks that can
 * store records into database from beans (classes with only getters and setters)
 * like this.
 * @author Shou Matsumoto
 * @see DAGGREUnBBayesFacade
 *
 */
public interface StructureChangeHistory extends EditHistory {
	// TODO fill with getters and setters
}
