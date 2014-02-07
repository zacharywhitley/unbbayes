/**
 * 
 */
package unbbayes.prs.bn.cpt;

import unbbayes.prs.bn.IProbabilityFunction;

/**
 * Classes implementing this interface shall be able to load and adapt {@link IProbabilityFunction}
 * to itself. Usually, instances of {@link unbbayes.prs.bn.IRandomVariable}
 * are candidates for this interface, because they will be able to copy CPT of other nodes to their own CPTs.
 * This interface does not extend  {@link unbbayes.prs.bn.IRandomVariable}, because the instance
 * that adapts the function may not be the node itself (separation of concerns).
 * @author Shou Matsumoto
 *
 */
public interface IProbabilityFunctionAdapter {

	/**
	 * Basically copies the content of a probability function to itself.
	 * If the argument is a function of nodes that are not specified
	 * in current scope, these nodes shall be ignored.
	 * For example, if the function specifies P(X|Y,Z), but the node using this method
	 * does not depend on Z, this method shall convert P(X|Y,Z) to P(X|Y) by using
	 * some mechanism, like marginalization.
	 * @param probabilityFunction : usually a CPT or a utility function
	 */
	void loadProbabilityFunction( IProbabilityFunction probabilityFunction);

}
