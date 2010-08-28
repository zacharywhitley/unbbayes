/**
 * 
 */
package unbbayes.prs.prm;

import java.util.List;


/**
 * This is a utility to follow the path described by {@link IDependencyChain}.
 * Usually, a dependency chain is solved by quering databases.
 * @author Shou Matsumoto
 * @see DependencyChainSolver : this is a sample implementation using 
 * {@link unbbayes.controller.prm.IDatabaseController} to solve
 * database access
 * 
 */
public interface IDependencyChainSolver {
	
	/**
	 * TODO refactor it to return a Map<IDependencyChain, IAttributeValue> instead.
	 * Obtains the parents of a prmNode
	 * @param prmNode
	 * @return
	 */
	public List<IAttributeValue> solveParents(IAttributeValue prmNode);
	

	/**
	 * TODO refactor it to return a Map<IDependencyChain, IAttributeValue> instead.
	 * Obtains the children of a prmNode
	 * @param prmNode
	 * @return
	 */
	public List<IAttributeValue> solveChildren(IAttributeValue prmNode);
}
